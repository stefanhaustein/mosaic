package org.kobjects.mosaic.model

import org.kobjects.mosaic.model.Model.integrations
import org.kobjects.mosaic.pluginapi.Integration
import org.kobjects.mosaic.pluginapi.IntegrationFactory
import org.kobjects.mosaic.pluginapi.ModificationToken
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
        val specification = Model.factories[type] as IntegrationFactory
        val config = specification.convertConfiguration(jsonSpec["configuration"] as Map<String, Any?>)
        var integration = integrationMap[name]

        if (integration != null) {
            integration.reconfigure(config)
        } else {
            integration = specification.createFn(type, name, token.tag, config)
            integrationMap[name] = integration
            //for (operation in integration.operationSpecs) {
            //    Model.factories.add(operation)
            //}
            token.symbolsChanged = true
        }
    }


    fun serialize(writer: Writer, forClient: Boolean, tag: Long) {
        val sb = StringBuilder()
        for (integration in integrations) {
            if (integration.tag > tag && (forClient || (integration !is Integration.Tombstone && integration !is Root))) {
                sb.append(integration.name).append(": ")
                integration.toJson(sb, forClient)
                sb.append('\n')
            }
        }
        if (sb.isNotEmpty()) {
            writer.write("[integrations]\n\n")
            writer.write(sb.toString())
        }
    }


    override fun iterator() = integrationMap.values.iterator()
}