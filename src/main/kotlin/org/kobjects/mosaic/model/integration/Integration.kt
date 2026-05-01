package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractPortDescriptor
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
    abstract val portFactories: Map<String, AbstractPortDescriptor>

    abstract fun detach(token: ModificationToken)

    protected abstract fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken)

    // Base implementation

    fun configure(json: JsonObject, token: ModificationToken) {
        jsonConfiguration = json
        tag = token.tag
        if (deleted) {
            detach(token)
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
        val existing = nodes[portName]
        val deleted = jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull == true

        if (existing != null) {
            // TODO: Make sure this is a dedicated request instead.
            if (existing.specification.modifiers.contains(AbstractArtifactSpec.Modifier.UNINSTANTIABLE) ||
                !deleted && !jsonSpec.containsKey("kind") && !jsonSpec.containsKey("configuration")) {
                (existing as? OutputPortNode)?.setFormula(jsonSpec["source"]?.jsonPrimitive?.content ?: "", token)
                return
            }

            if (existing.specification.name == jsonSpec["kind"]?.jsonPrimitive?.content || deleted) {
                existing.configure(jsonSpec, token)
                return
            }

            // Will be overwritten...
            existing.detach()
        }

        if (jsonSpec["deleted"]?.jsonPrimitive?.booleanOrNull == true) {
            return
        }

        val kind = jsonSpec["kind"]!!.jsonPrimitive.content
        val descriptor = portFactories[kind] ?: throw IllegalArgumentException("'$kind' not found in integration $this.")
        val displayName =  jsonSpec["displayName"]?.jsonPrimitive?.contentOrNull

        val port = when (descriptor) {
            is InputPortDescriptor -> InputPortNode(this, portName, descriptor, displayName)
            is OutputPortDescriptor -> OutputPortNode(
                this,
                portName,
                    descriptor,
                displayName = displayName,
                rawFormula = jsonSpec["source"]?.jsonPrimitive?.contentOrNull ?: jsonSpec["expression"]?.jsonPrimitive?.contentOrNull ?: "")
            else -> throw IllegalArgumentException("Operation specification $descriptor does not specify a port.")
        }
        nodes[portName] = port
        port.configure(jsonSpec, token)

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