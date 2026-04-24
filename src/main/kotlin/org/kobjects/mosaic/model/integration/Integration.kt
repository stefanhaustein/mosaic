package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractPortFactorySpec
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.Type
import org.kobjects.tomson.TomsonOutput


abstract class Integration(
    // The name of the factory
    kind: String,
    // The name of this instance
    name: String,
) : Namespace(name) {
    // Values

    val factory = Model.integrationFactories[kind]!!

    // Variables

    var tag = 0L
    var jsonConfiguration: JsonObject = JsonObject(mapOf("kind" to JsonPrimitive(kind)))
    val nodes = mutableMapOf<String, PortNode>()
    val deleted: Boolean
        get() = jsonConfiguration["deleted"]?.jsonPrimitive?.booleanOrNull == true

    // Abstract stuff

    // A ctor param would be tricky as these should be tied to this instance.
    abstract val portFactories: Map<String, AbstractPortFactorySpec>

    abstract fun close()

    protected abstract fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken)

    // Base implementation

    fun configure(json: JsonObject, token: ModificationToken) {
        jsonConfiguration = json
        tag = token.tag
        if (deleted) {
            close()
            token.symbolsChanged = true
        } else {
            try {
                val config = factory.convertConfiguration(json)
                configureInternal(config, token)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun serialize(out: TomsonOutput, forClient: Boolean, tag: Long) {
        if (this.tag > tag && (!deleted || forClient)) {
            out.appendSection("integration.$name", jsonConfiguration)
            if (forClient && !deleted) {
                out.appendSection("integration.$name.factories", factoriesToJson())
            }
        }
        out.appendSection("integration.$name.ports", portsToJson(forClient, tag))
    }


    // The name is separate because it's typically the key of the spec map
    fun definePort(portName: String, jsonSpec: JsonObject, token: ModificationToken) {

        if (nodes[portName]?.specification?.modifiers?.contains(AbstractArtifactSpec.Modifier.UNINSTANTIABLE) ?: false ||
            jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull != true && !jsonSpec.containsKey("kind") && !jsonSpec.containsKey("configuration")) {
            val port = nodes[portName]
            if (port is OutputPortNode) {
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


            val specification = portFactories[kind] ?: throw IllegalArgumentException("'$kind' not found in integration $this.")

            nodes[portName]?.detach()

            val resolvedConfiguration = specification.convertConfiguration(jsonSpec["configuration"]?.jsonObject)

            val port = when (specification) {
                is InputPortSpec -> InputPortNode(this, portName, specification, resolvedConfiguration, tag = token.tag)
                is OutputPortSpec -> OutputPortNode(
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
            nodes[localName] = InputPortNode(
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
                    object : InputPortInstance(object : InputPortListener {
                        override fun portValueChanged(newValue: Any?) {}
                        override fun portValueChanged(newValue: Any?, token: ModificationToken) {}
                    }) {
                        override fun detach() {}
                    }
                }, emptyMap(), tag = token.tag
            )
        }
    }



    fun factoriesToJson(): JsonObject = buildJsonObject {
        for (operationSpec in portFactories.values) {
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
}