package org.kobjects.mosaic.model

import org.kobjects.mosaic.model.Model.integrations
import org.kobjects.mosaic.pluginapi.Integration
import org.kobjects.mosaic.pluginapi.IntegrationFactory
import org.kobjects.mosaic.pluginapi.ModificationToken
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

    fun configureIntegration(name: String, jsonSpec: Map<String, Any?>, token: ModificationToken) {
        if (jsonSpec["deleted"] == true) {
            deleteIntegration(name, token)
            return
        }

        val type = jsonSpec["type"].toString()
        val specification = Model.factories[type] ?: throw IllegalArgumentException("$type is not a integration factory")
        val config = specification.convertConfiguration(jsonSpec["configuration"] as Map<String, Any?>)
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
            if (integration.tag > tag && (forClient || (integration !is Integration.Tombstone && integration !is Root))) {
                integration.serialize(tomson, forClient)
            }
        }
    }


    override fun iterator() = integrationMap.values.iterator()
}