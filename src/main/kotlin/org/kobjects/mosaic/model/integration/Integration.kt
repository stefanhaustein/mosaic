package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractFactorySpec
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.Type
import org.kobjects.tomson.TomsonOutput
import org.kobjects.tomson.toJson


abstract class Integration(
    // The name of the IntegrationSpec
    val kind: String,
    // The name of this instance.
    name: String,
    val tag: Long,
) : Namespace(name) {
    val nodes = mutableMapOf<String, PortHolder>()

    abstract val operationSpecs: List<AbstractArtifactSpec>

    abstract val configuration: Map<String, Any?>

    abstract fun detach()

    fun serialize(out: TomsonOutput, forClient: Boolean, tag: Long) {
        if (this.tag > tag && (forClient || (this !is Tombstone && this !is Root))) {
            out.appendSection("integration.$name", configToJson())
            if (forClient) {
                out.appendSection("integration.$name.factories", factoriesToJson())
            }
        }
        out.appendSection("integration.$name.ports", portsToJson(forClient, tag))
    }

    fun configToJson(): JsonObject =
        buildJsonObject {
            put("type", JsonPrimitive(kind))
            put("configuration", configuration.toJson())
        }


    // The name is separate because it's typically the key of the spec map
    fun definePort(portName: String, jsonSpec: JsonObject, token: ModificationToken) {

        if (nodes[portName]?.specification?.modifiers?.contains(AbstractArtifactSpec.Modifier.UNINSTANTIABLE) ?: false ||
            jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull != true && !jsonSpec.containsKey("kind") && !jsonSpec.containsKey("configuration")) {
            val port = nodes[portName]
            if (port is OutputPortHolder) {
                port.setFormula(jsonSpec["source"]?.jsonPrimitive?.content ?: "", token)
            }
            return
        }

        token.symbolsChanged = true

        // Always delete what's there.
        val previousName = jsonSpec["previousName"]?.jsonPrimitive?.contentOrNull ?: portName
        try {
            deletePort(previousName, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (jsonSpec["deleted"] as Boolean? != true) {

            val kind = jsonSpec["kind"]!!.jsonPrimitive.content


            val specification = operationSpecs.find { it.name == kind } ?: throw IllegalArgumentException("'$kind' not found in integration $this.")

            nodes[portName]?.detach()

            val resolvedConfiguration = specification.convertConfiguration(jsonSpec["configuration"]?.jsonObject)

            val port = when (specification) {
                is InputPortSpec -> InputPortHolder(this, portName, specification, resolvedConfiguration, tag = token.tag)
                is OutputPortSpec -> OutputPortHolder(
                    this,
                    portName,
                    specification,
                    resolvedConfiguration,
                    displayName = jsonSpec["displayName"]?.jsonPrimitive?.contentOrNull,
                    rawFormula = jsonSpec["source"]?.jsonPrimitive?.contentOrNull ?: jsonSpec["expression"]?.jsonPrimitive?.contentOrNull ?: "",
                    tag = token.tag)
                else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
            }
            nodes[portName] = port
            port.attach(token)
        }
    }


    fun deletePort(name: String, token: ModificationToken) {
        val port = nodes[name]
        if (port != null) {
            val cut = name.indexOf('.')
            val localName = name.substring(cut + 1)
            token.symbolsChanged = true
            port.detach()
            nodes[localName] = InputPortHolder(
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



    fun factoriesToJson(): JsonObject = buildJsonObject {
        for (operationSpec in operationSpecs) {
            put(operationSpec.name,operationSpec.toJson())
        }
    }

    fun portsToJson(forClient: Boolean, tag: Long): JsonObject = buildJsonObject {
        for (port in nodes.values) {
            if (forClient || port.needsSaving()) {
                if (!forClient || port.tag > tag || port.valueTag > tag) {
                    put(port.name, port.toJson(forClient))
                }
            }
        }
    }

    abstract fun reconfigure(configuration: Map<String, Any?>)

    class Tombstone(
        deletedInstance: Integration,
        tag: Long
    ) : Integration(
        "TOMBSTONE",
        deletedInstance.name,
        tag
    ) {
        override val configuration = emptyMap<String, Any>()
        override fun reconfigure(configuration: Map<String, Any?>) {}
        override val operationSpecs = emptyList<AbstractFactorySpec>()

        override fun detach() {}
    }

}