package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec.Modifier
import java.io.Writer


class Ports : Iterable<PortHolder> {

    private val portMap = mutableMapOf<String, PortHolder>()

    override fun iterator(): Iterator<PortHolder> = (portMap.values + Model.integrations.flatMap { it.nodes.values }).iterator()

    operator fun get(key: String): PortHolder? {
        val cut: Int = key.indexOf('.')
        if (cut == -1) return portMap[key]
        val integration = Model.integrations[key.substring(0, cut)]
        return integration?.nodes?.get(key.substring(cut + 1))
    }

    val keys
        get() = portMap.keys

    fun deletePort(name: String, token: ModificationToken) {
        val port = portMap[name]
        if (port != null) {
            token.symbolsChanged = true
            port.detach()
            portMap[name] = InputPortHolder(
                port.owner, name, InputPortSpec(
                    null,
                    "GPIO",
                    // The operation name; used to identify tombstone ports on the client
                    "TOMBSTONE",
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
    fun definePort(name: String, jsonSpec: Map<String, Any?>, token: ModificationToken) {
        token.symbolsChanged = true

        // Always delete what's there.
        val previousName = jsonSpec["previousName"]?.toString() ?: name
        try {
            deletePort(previousName, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (jsonSpec["deleted"] as Boolean? != true) {
            if (!jsonSpec.containsKey("kind") && !jsonSpec.containsKey("configuration")) {
                val port = this[name]
                if (port is OutputPortHolder) {
                    port.rawFormula = jsonSpec["source"]?.toString() ?: ""
                    port.reparse()
                    port.tag = token.tag
                }
            } else {
                val kind = jsonSpec["kind"].toString()

                val parts = kind.split(".")
                val integration = Model.integrations[parts[0]] ?: throw IllegalArgumentException("Integration '${parts[0]}' not found.")
                val specification = integration.operationSpecs.find { it.fqName == kind } ?: throw IllegalArgumentException("'${parts[1]}' not found in integration $integration.")

                portMap[name]?.detach()

                val config = specification.convertConfiguration(
                    jsonSpec["configuration"] as? Map<String, Any> ?: emptyMap()
                )

                val port = when (specification) {
                    is InputPortSpec -> InputPortHolder(integration, name, specification, config, tag = token.tag)
                    is OutputPortSpec -> OutputPortHolder(integration, name, specification, config, jsonSpec["source"] as String? ?: jsonSpec["expression"] as String, tag = token.tag)
                    else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
                }
                portMap[name] = port
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