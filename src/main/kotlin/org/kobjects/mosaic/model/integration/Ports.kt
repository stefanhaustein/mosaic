package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Type


class Ports : Iterable<PortHolder> {

    override fun iterator(): Iterator<PortHolder> = Model.integrations.flatMap { it.nodes.values }.iterator()

    operator fun get(key: String): PortHolder? {
        val cut: Int = key.indexOf('.')
        if (cut == -1) {
            return null
        }
        val integration = Model.integrations[key.substring(0, cut)]
        return integration?.nodes?.get(key.substring(cut + 1))
    }

    fun deletePort(name: String, token: ModificationToken) {
        val port = this[name]
        if (port != null) {
            val cut = name.indexOf('.')
            val localName = name.substring(cut + 1)
            token.symbolsChanged = true
            port.detach()
            port.owner.nodes[localName] = InputPortHolder(
                port.owner, localName, InputPortSpec(
                    port.owner,
                    "TOMBSTONE",
                    localName,
                    Type.VOID,
                    "",
                    emptyList(),
                    emptySet(),
                    token.tag
                ) { _, _ ->
                    object : InputPortInstance {
                        override val value = Unit
                        override fun detach() {}
                    }
                }, emptyMap(), tag = token.tag
            )
        }
    }

    // The name is separate because it's typically the key of the spec map
    fun definePort(integrationName: String, portName: String, jsonSpec: JsonObject, token: ModificationToken) {
        token.symbolsChanged = true
        val fqName = "$integrationName.$portName"

        if (jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull != true && !jsonSpec.containsKey("kind") && !jsonSpec.containsKey("configuration")) {
            val port = this[fqName]
            if (port is OutputPortHolder) {
                port.rawFormula = jsonSpec["source"]?.jsonPrimitive?.content ?: ""
                port.reparse()
                port.tag = token.tag
            }
            return
        }

        // Always delete what's there.
        val previousName = jsonSpec["previousName"]?.jsonPrimitive?.contentOrNull ?: fqName
        try {
            deletePort(previousName, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (jsonSpec["deleted"] as Boolean? != true) {

                val kind = jsonSpec["kind"]!!.jsonPrimitive.content

                val integration = Model.integrations[integrationName] ?: throw IllegalArgumentException("Integration '$integrationName' not found.")
                val specification = integration.operationSpecs.find { it.name == kind } ?: throw IllegalArgumentException("'$kind' not found in integration $integration.")

                this[fqName]?.detach()

                val resolvedConfiguration = specification.convertConfiguration(jsonSpec["configuration"]?.jsonObject)

                val port = when (specification) {
                    is InputPortSpec -> InputPortHolder(integration, portName, specification, resolvedConfiguration, tag = token.tag)
                    is OutputPortSpec -> OutputPortHolder(
                        integration,
                        portName,
                        specification,
                        resolvedConfiguration,
                        displayName = jsonSpec["displayName"]?.jsonPrimitive?.contentOrNull,
                        rawFormula = jsonSpec["source"]?.jsonPrimitive?.contentOrNull ?: jsonSpec["expression"]?.jsonPrimitive?.contentOrNull ?: "",
                        tag = token.tag)
                    else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
                }
                integration.nodes[portName] = port
                port.attach(token)
        }
    }

}