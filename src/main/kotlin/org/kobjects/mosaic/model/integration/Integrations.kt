package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.Model.integrations
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.tomson.TomsonOutput

class Integrations : Iterable<Integration> {

    val integrationMap = mutableMapOf<String, Integration>()

    operator fun get(id: String): Integration? = integrationMap[id]

    fun deleteIntegration(name: String, token: ModificationToken) {
        val integration = integrationMap[name]
        if (integration != null) {
            integration.close()
            integrationMap[name] = Integration.Tombstone(integration, token.tag)
        }
        token.symbolsChanged = true
    }

    fun configureIntegration(name: String, jsonSpec: JsonObject, token: ModificationToken) {
        if (jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull == true) {
            deleteIntegration(name, token)
            return
        }

        val kind = jsonSpec["kind"]?.jsonPrimitive?.content ?: throw IllegalArgumentException("'kind' missing in $jsonSpec")
        val specification = Model.integrationFactories[kind] ?: throw IllegalArgumentException("$kind is not a integration factory in $jsonSpec")

        var integration = integrationMap[name]

        //val config = jsonSpec["configuration"]?.jsonObject!!
        if (integration == null) {
            integration = specification.createFn(kind, name, token.tag)
            integrationMap[name] = integration
            token.symbolsChanged = true
        }
        integration.configure(jsonSpec)
    }


    fun serialize(tomson: TomsonOutput, forClient: Boolean, tag: Long) {
        for (integration in integrations) {
                integration.serialize(tomson, forClient, tag)

        }
    }


    override fun iterator() = integrationMap.values.iterator()
}