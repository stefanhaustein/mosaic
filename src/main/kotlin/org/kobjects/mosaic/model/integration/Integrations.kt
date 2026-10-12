package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.tomson.TomsonOutput

class Integrations(val model: Model) : Iterable<Integration> {

    val integrationMap = mutableMapOf<String, Integration>()

    operator fun get(id: String): Integration? = integrationMap[id]

    fun configureIntegration(name: String, jsonSpec: JsonObject, token: ModificationToken) {
        val kind = jsonSpec["kind"]?.jsonPrimitive?.content ?: throw IllegalArgumentException("'kind' missing in $jsonSpec")
        val specification = model.integrationFactories[kind] ?: throw IllegalArgumentException("$kind is not a integration factory in $jsonSpec")

        var integration = integrationMap[name]

        //val config = jsonSpec["configuration"]?.jsonObject!!
        if (integration == null) {
            if (jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull == true) {
                return
            }
            integration = specification.createFn(name)
            integrationMap[name] = integration
            token.symbolsChanged = true
        }
        integration.configure(jsonSpec, token)
    }


    fun serialize(tomson: TomsonOutput, forClient: Boolean, tag: Long) {
        for (integration in model.integrations) {
                integration.serialize(tomson, forClient, tag)

        }
    }


    override fun iterator() = integrationMap.values.iterator()
}