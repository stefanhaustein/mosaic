package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*
import java.io.Writer


class Ports : Iterable<PortHolder> {

    private val portMap = mutableMapOf<String, PortHolder>()

    override fun iterator(): Iterator<PortHolder> = portMap.values.iterator()

    operator fun get(key: String): PortHolder? = portMap[key]

    val keys
        get() = portMap.keys

    fun deletePort(name: String, token: ModificationToken) {
        val port = portMap[name]
        if (port != null) {
            token.symbolsChanged = true
            port.detach()
            portMap[name] = InputPortHolder(
                name, InputPortSpec(
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
                }, emptyMap(), token.tag
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
            val kind = jsonSpec["kind"].toString()

            val specification = if (kind.contains(".")) {
                val parts = kind.split(".")
                val integration = Model.integrations[parts[0]] ?: throw IllegalArgumentException("Integration '${parts[0]}' not found.")
                integration.operationSpecs.find { it.name == kind } ?: throw IllegalArgumentException("'${parts[1]}' not found in integration $integration.")
            } else {
                Model.factories[kind] ?: throw IllegalArgumentException("Unrecognized port type '$kind'")
            }

            portMap[name]?.detach()

            val config = specification.convertConfiguration(
                jsonSpec["configuration"] as? Map<String, Any> ?: emptyMap()
            )

            val port = when (specification) {
                is InputPortSpec -> InputPortHolder(name, specification, config, token.tag)
                is OutputPortSpec -> OutputPortHolder(name, specification, config, jsonSpec["source"] as String? ?: jsonSpec["expression"] as String, token.tag)
                is PropertySpec -> OutputPortHolder(name, specification.getOutputPortSpec(), config, jsonSpec["source"] as String? ?: jsonSpec["expression"] as String, token.tag)
                else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
            }
            portMap[name] = port
            port.attach(token)
        }
    }

    fun serialize(writer: Writer, forClient: Boolean, tag: Long) {
        val definitions = StringBuilder()
        val values = StringBuilder()
        val simulationValues = StringBuilder()
        for (port in this) {
            if (port.tag > tag) {
                definitions.append(port.name).append(": ")
                port.toJson(definitions, forClient)
                definitions.append('\n')
            }
            if (port.valueTag > tag) {
                values.append("${port.name}: ${port.value.toJson()}\n")
            }
            if (port is InputPortHolder && port.simulationValueTag > tag) {
                simulationValues.append("${port.name}: ${port.simulationValue.toJson()}\n")
            }
        }

        if (definitions.isNotEmpty()) {
            writer.write("[ports]\n\n$definitions\n")
        }
        if (forClient && values.isNotEmpty()) {
            writer.write("[portValues]\n\n$values\n")
        }

        if (simulationValues.isNotEmpty()) {
            writer.write("[simulationValues]\n\n$simulationValues\n")
        }
    }
}