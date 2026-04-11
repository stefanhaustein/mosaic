package org.kobjects.mosaic.server

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.html.dom.serialize
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.tomson.toJson
import org.kobjects.mosaic.model.sheet.CellRangeReference
import org.kobjects.mosaic.model.Model
import org.kobjects.tomson.TomsonOutput
import org.kobjects.tomson.TomsonParser
import java.io.File
import java.io.StringWriter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module() {
    routing {
        post("/clear/{range}") {
            val rawRange = call.parameters["range"]!!
            val range = CellRangeReference.parse(rawRange)
            Model.applySynchronizedWithToken {
                range.clear(it)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/clearAll") {
            Model.applySynchronizedWithToken {
                Model.clearAll(it)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/update/{cell}") {
            val cell = call.parameters["cell"]!!
            val text = call.receiveText()
            val json = Json.parseToJsonElement(text).jsonObject
            Model.applySynchronizedWithToken { token ->
                Model.getOrCreate(cell).setJson(json, token)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/runMode") {
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val value = Json.parseToJsonElement(jsonText)
            Model.applySynchronizedWithToken { token ->
                Model.setRunMode(value.jsonPrimitive.boolean, token)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/paste/{target}") {
            val rawTargetRange = call.parameters["target"]!!
            val targetRange = CellRangeReference.parse(rawTargetRange)
            val tomsonText = call.receiveText()
            val tomson = TomsonParser.parse(tomsonText)

            println("/paste/$targetRange: $tomsonText")

            Model.applySynchronizedWithToken { token ->
                targetRange.sheet.paste(token, targetRange, tomson)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/ports/{integrationName}/{portName}") {
            val integrationName = call.parameters["integrationName"]!!
            val portName = call.parameters["portName"]!!
            val jsonText = call.receiveText()
            println("/ports/$integrationName/$portName: $jsonText")
            val jsonSpec = Json.parseToJsonElement(jsonText).jsonObject
            val integration = Model.integrations[integrationName] ?: throw IllegalArgumentException("Integration '$integrationName' not found")
            Model.applySynchronizedWithToken { token ->
                integration.definePort(portName, jsonSpec, token)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/integrations/{name}") {
            val name = call.parameters["name"]!!
            val jsonText = call.receiveText()
            println("/integrations/$name: $jsonText")
            val jsonSpec = Json.parseToJsonElement(jsonText).jsonObject
            Model.applySynchronizedWithToken { token ->
                Model.integrations.configureIntegration(name, jsonSpec, token)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/sheet") {
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val jsonSpec = Json.parseToJsonElement(jsonText).jsonObject
            val name = jsonSpec["name"] as String?
            Model.applySynchronizedWithToken { token ->
                Model.updateSheet(name, jsonSpec, token)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/upload") {
            val fileItem = call.receiveMultipart().readPart() as PartData.FileItem
            val data = fileItem.provider().toByteArray().toString(Charsets.UTF_8)
            Model.applySynchronizedWithToken {
                Model.clearAll(it)
                Model.loadData(data, it)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/loadExample") {
            val name = call.receiveText()
            val exampleFile = File("src/main/resources/examples", name + ".tc")
            val data = exampleFile.readText()
            Model.applySynchronizedWithToken {
                Model.clearAll(it)
                Model.loadData(data, it)
            }
        }

        get("/data") {
            val rawTag = call.request.queryParameters["tag"]?.toLong()
            val forClient = rawTag != null
            val tag = rawTag ?: -1

            if (tag >= Model.modificationTag) {
                suspendCoroutine<Unit> { continuation ->
                    Model.applySynchronized {
                        Model.addUpdateListener(permanent = false, onChangeOnly = true) { tag, anyChange ->
                            continuation.resume(Unit)
                        }
                    }
                }
            }
            val result = Model.applySynchronized {
                val writer = StringWriter()
                val tomson = TomsonOutput(writer)
                if (forClient) {
                    tomson.appendValue("tag", JsonPrimitive(Model.modificationTag))
                }
                Model.serialize(tomson, forClient, tag)
                writer.close()
                writer.toString()
            }
            call.respondText(result, ContentType.Text.Plain, HttpStatusCode.OK,)
        }
        get("/rest/{path...}") {
            val path = call.parameters.getAll("path")!!.joinToString("/")
            //val json = Model.restValues[path]?.toJson()
            call.respondText("null", ContentType.Application.Json, HttpStatusCode.OK)
        }
        get("img/{name...}") {
            val path = call.parameters.getAll("name")!!.joinToString("/")
            println("Svg requested: $path; available: ${Model.svgs.map}")
            val svg = Model.svgs.map["img/$path"]
            println("Found: $svg")

            val parameterMap = call.request.queryParameters.entries().map { Pair(it.key, it.value.first()) }.toMap()
            val parameterized = svg!!.parameterized(parameterMap)

            call.respondText(parameterized.documentElement.serialize(), ContentType.Image.SVG)
        }

        /* get("/") {
             call.respondText("Hello World!")
         }*/
        // Static plugin. Try to access `/static/index.html`
        staticFiles("/", File("src/main/resources/static"))
        //staticResources("/", "static")
    }
}
