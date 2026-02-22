package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec.Modifier
import java.io.Writer


class Ports : Iterable<PortHolder> {

    override fun iterator(): Iterator<PortHolder> = Model.integrations.flatMap { it.nodes.values }.iterator()

    operator fun get(key: String): PortHolder? {
        val cut: Int = key.indexOf('.')
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
    fun definePort(fqName: String, jsonSpec: Map<String, Any?>, token: ModificationToken) {
        token.symbolsChanged = true

        // Always delete what's there.
        val previousName = jsonSpec["previousName"]?.toString() ?: fqName
        try {
            deletePort(previousName, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (jsonSpec["deleted"] as Boolean? != true) {
            if (!jsonSpec.containsKey("kind") && !jsonSpec.containsKey("configuration")) {
                val port = this[fqName]
                if (port is OutputPortHolder) {
                    port.rawFormula = jsonSpec["source"]?.toString() ?: ""
                    port.reparse()
                    port.tag = token.tag
                }
            } else {
                val kind = jsonSpec["kind"].toString()

                val parts = kind.split(".")
                val integrationName = parts[0]
                val loclName = parts[1]
                val integration = Model.integrations[integrationName] ?: throw IllegalArgumentException("Integration '$integrationName' not found.")
                val specification = integration.operationSpecs.find { it.fqName == kind } ?: throw IllegalArgumentException("'$loclName' not found in integration $integration.")

                this[fqName]?.detach()

                val config = specification.convertConfiguration(
                    jsonSpec["configuration"] as? Map<String, Any> ?: emptyMap()
                )

                val port = when (specification) {
                    is InputPortSpec -> InputPortHolder(integration, fqName, specification, config, tag = token.tag)
                    is OutputPortSpec -> OutputPortHolder(integration, fqName, specification, config, jsonSpec["source"] as String? ?: jsonSpec["expression"] as String, tag = token.tag)
                    else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
                }
                integration.nodes[loclName] = port
                port.attach(token)

            }
        }
    }

    fun serialize(writer: Writer, forClient: Boolean, tag: Long) {
        val definitions = StringBuilder()
        val values = StringBuilder()
        for (port in this) {
            if (port.name.contains(".")) {
                println()
            }
            if (port.tag > tag && (forClient || port is OutputPortHolder || !port.specification.modifiers.contains(Modifier.UNINSTANTIABLE))) {
                definitions.append(port.name).append(": ")
                port.toJson(definitions, forClient)
                definitions.append('\n')
            }
            if (port.valueTag > tag) {
                values.append("${port.name}: ${port.value.toJson()}\n")
            }
        }

        if (definitions.isNotEmpty()) {
            writer.write("[ports]\n\n$definitions\n")
        }
        if (forClient && values.isNotEmpty()) {
            writer.write("[portValues]\n\n$values\n")
        }
    }
}