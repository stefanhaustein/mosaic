package org.kobjects.mosaic.pluginapi

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.PortHolder


abstract class IntegrationInstance(
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

    open fun notifySimulationModeChanged(token: ModificationToken) {

    }

    fun portsToJson(sb: StringBuilder, forClient: Boolean) {

    }


    fun toJson(sb: StringBuilder, forClient: Boolean) {
        sb.append("""{"name":${name.quote()}, "type":${kind.quote()}, "configuration": """)
        configuration.toJson(sb)
        if (forClient) {
            sb.append(""", "operations": [""")
            var first = true
            for (operationSpec in operationSpecs) {
                if (first) {
                    first = false
                } else {
                    sb.append(", ")
                }
                operationSpec.toJson(sb)
            }
            sb.append("]")
        }
        sb.append("}")
    }

    abstract fun reconfigure(configuration: Map<String, Any?>)

    class Tombstone(
        deletedInstance: IntegrationInstance,
        tag: Long
    ) : IntegrationInstance(
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