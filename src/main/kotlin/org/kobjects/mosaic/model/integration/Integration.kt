package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractFactorySpec
import org.kobjects.mosaic.model.Namespace
import org.kobjects.tomson.TomsonOutput
import org.kobjects.tomson.toJson


abstract class Integration(
    // The name of the IntegrationSpec
    val kind: String,
    // The name of this instance.
    override val name: String,
    val tag: Long,
) : Namespace {
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
        out.appendSection("integration.$name.ports", portsToJson(forClient))
    }

    fun configToJson(): JsonObject =
        buildJsonObject {
            put("type", JsonPrimitive(kind))
            put("configuration", configuration.toJson())
        }

    fun factoriesToJson(): JsonObject = buildJsonObject {
        for (operationSpec in operationSpecs) {
            put(operationSpec.name,operationSpec.toJson())
        }
    }

    fun portsToJson(forClient: Boolean): JsonObject = buildJsonObject {
        for (port in nodes.values) {
            if (forClient || !port.specification.modifiers.contains(AbstractArtifactSpec.Modifier.UNINSTANTIABLE)) {
                put(port.name, port.toJson(forClient))
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