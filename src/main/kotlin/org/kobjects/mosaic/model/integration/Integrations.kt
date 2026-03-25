package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.Model.integrations
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.tomson.TomsonOutput
import java.io.Writer

class Integrations : Iterable<Integration> {

    val integrationMap = mutableMapOf<String, Integration>()

    operator fun get(id: String): Integration? = integrationMap[id]

    fun deleteIntegration(name: String, token: ModificationToken) {
        val integration = integrationMap[name]
        if (integration != null) {
            integration.detach()
            integrationMap[name] = Integration.Tombstone(integration, token.tag)
        }
        token.symbolsChanged = true
    }

    fun configureIntegration(name: String, jsonSpec: JsonObject, token: ModificationToken) {
        if (jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull == true) {
            deleteIntegration(name, token)
            return
        }

        val type = jsonSpec["type"]!!.jsonPrimitive.content
        val specification = Model.factories[type] ?: throw IllegalArgumentException("$type is not a integration factory")
        val config = specification.convertConfiguration(jsonSpec["configuration"]?.jsonObject)
        var integration = integrationMap[name]

        if (integration != null) {
            integration.reconfigure(config)
        } else {
            integration = specification.createFn(type, name, token.tag, config)
            integrationMap[name] = integration
            token.symbolsChanged = true
        }
    }


    fun serialize(writer: Writer, forClient: Boolean, tag: Long) {
        val tomson = TomsonOutput(writer)
        for (integration in integrations) {

                integration.serialize(tomson, forClient, tag)

        }
    }


    override fun iterator() = integrationMap.values.iterator()
}