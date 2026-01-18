package org.kobjects.tablecraft.plugins.homeassistant.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.associateBy

class HomeAssistantClient(
    private val host: String,
    private val port: Int,
    private val token: String
) {
    val messageId = AtomicInteger(1)

    // Encapsulated HttpClient configuration
    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    val areas = runBlocking { fetchAreas() }.associateBy { it.id }
    val devices = runBlocking { fetchDevices() }.associateBy { it.id }
    val entities: Map<String, HAEntity>

    init {
        entities = runBlocking {
            println("*** fetching entity states ******")
            val entityStates = fetchEntityStates().associateBy { it.id }
            println("*** fetching entities ******")
            val rawEntities = fetchJson("config/entity_registry/list")
            buildMap<String, HAEntity> {
                for (rawEntity in rawEntities.jsonArray) {
                    val id = rawEntity.jsonObject["entity_id"]?.jsonPrimitive?.contentOrNull
                    if (id != null) {
                        val state = entityStates[id]
                        if (state != null) {
                            put(id, HAEntity(this@HomeAssistantClient, rawEntity.jsonObject, state))
                        }
                    }
                }
            }
        }

        println("*** subscribing ******")
        Thread {
            runBlocking {
                subscribeEntityStates {
                    for ((id, update) in it.jsonObject.entries) {
                        val entity = entities[id]
                        if (entity != null) {
                            entity.state = entity.state.update(update.jsonObject)
                        }
                    }
                }
            }
        }.start()
    }

    private suspend fun fetchJson(commandType: String, keepListening: Boolean = false, callback: (JsonElement) -> Unit) {
        val cmd = buildJsonObject {
            put("id", messageId.getAndIncrement())
            put("type", commandType)
        }
        sendJson(json.encodeToString(cmd), keepListening, callback = callback)
    }

    suspend fun sendJson(command: String, keepListening: Boolean = false, callback: ((JsonElement) -> Unit)? = null) {
        client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/api/websocket") {

            for (frame in incoming) {
                if (frame !is Frame.Text) continue

                val response = Json.parseToJsonElement(frame.readText()).jsonObject

                when (response.get("type")?.jsonPrimitive?.content) {
                    "auth_required" -> sendAuth()
                    "auth_ok" -> {
                        send(Frame.Text(command))
                        if (callback == null) {
                            close()
                        }
                    }
                    "event" -> {
                        val result = response.get("event")!!
                        //println("fetched result: $result")
                        if (callback != null) {
                            callback(result)
                        }
                    }
                    "result" -> {
                        if (response.get("success")?.jsonPrimitive?.booleanOrNull == true) {
                            val result = response.get("result")!!
                            //println("fetched result: $result")
                            if (callback != null) {
                                callback(result)
                                if (!keepListening) {
                                    close()
                                }

                            }
                        } else {
                            close()
                            throw RuntimeException("HA Command Failed: $command; response: $response")
                        }
                    }
                    "auth_invalid" -> {
                        close()
                        throw RuntimeException("Authentication failed")
                    }
                }
            }
        }
    }



    /**
     * Generic helper to fetch lists (Devices or Entities)
     */
    private suspend fun fetchJson(commandType: String): JsonElement = coroutineScope {
        val deferred = CompletableDeferred<JsonElement>()
        fetchJson(commandType) {
            deferred.complete(it)
        }
        deferred.await()
    }

    suspend fun fetchAreas() = fetchJson("config/area_registry/list").jsonArray.map { HAArea(this, it.jsonObject) }

    suspend fun fetchDevices() = fetchJson("config/device_registry/list").jsonArray.map { HADevice(this, it.jsonObject) }

    suspend fun fetchEntityStates() = fetchJson("get_states").jsonArray.map { HAEntityState(it.jsonObject) }

    suspend fun subscribeEntityStates(callback: (JsonObject) -> Unit) =
        fetchJson("subscribe_entities", keepListening = true) {
            if (it is JsonObject) {
                val c = it["c"]?.jsonObject
                if (c != null) {
                    callback(c)
                    return@fetchJson
                }
            }
            println("Entity update doesn't contain 'c' field: ${it.toString().take(1000)}")
        }

    private suspend fun DefaultClientWebSocketSession.sendAuth() {
        val authPayload = """{"type": "auth", "access_token": "$token"}"""
        send(Frame.Text(authPayload))
    }

    /**
     * Clean up the internal HTTP client
     */
    fun close() {
        client.close()
    }
}