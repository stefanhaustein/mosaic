package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*

class InputPortHolder(
    override val name: String,
    val specification: InputPortSpec,
    val configuration: Map<String, Any?>,
    override val tag: Long

) : PortHolder, Node, InputPortListener {

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()

    var instance: InputPortInstance? = null

    override var valueTag  = 0L
    override var value: Any? = null

    var portValue: Any? = null
    var simulationValue: Any? = null

    // We track the simulation value separately, as it sent to the client separately.
    var simulationValueTag = 0L


    override fun attach(token: ModificationToken) {
        detach()

        if (!Model.simulationMode) {
            try {
                instance = specification.createFn(configuration, this)
            } catch (e: Exception) {
                portValue = e
                e.printStackTrace()
            }
        }
    }

    override fun notifySimulationModeChanged(token: ModificationToken) {
        detach()
        attach(token)
        token.addRefresh(this)
    }

    override fun detach() {
        // This doesn't really need to do anything about dependencies -- dependencies will be updatend in their reset
        // methods.
        if (instance != null) {
            try {
                instance?.detach()
            } catch (e: Exception) {
                e.printStackTrace()
                instance = null
            }
        }
    }

    // Implements the corresponding value change listener method.
    override fun portValueChanged(token: ModificationToken, newValue: Any?) {
        portValue = newValue
        if (!Model.simulationMode) {
            token.addRefresh(this)
        }
    }


    override fun recalculateValue(tag: Long): Boolean {
        if (valueTag == tag) {
            return false
        }
        val newValue = if (Model.simulationMode) simulationValue else portValue
        if (value == newValue) {
            return false
        }
        valueTag = tag
        value = newValue
        return true
    }

    fun simulationValueChanged(token: ModificationToken, newValue: Any?) {
        simulationValue = newValue
        simulationValueTag = token.tag
        if (Model.simulationMode) {
            token.addRefresh(this)
        }
    }

    override fun toJson(sb: StringBuilder, forClient: Boolean) {
        sb.append("""{"name":${name.quote()}, "kind":${specification.name.quote()}, "type":""")
        specification.type.toJson(sb)
        sb.append(""", "configuration": """)
        configuration.toJson(sb)
        if (forClient) {
            serializeDependencies(sb)
        }
        sb.append("}")
    }



    override fun toString() = name

}