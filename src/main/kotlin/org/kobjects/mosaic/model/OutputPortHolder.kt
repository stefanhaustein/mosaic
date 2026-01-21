package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*

class OutputPortHolder(
    override val name: String,
    val specification: OutputPortSpec,
    val configuration: Map<String, Any?>,
    val rawFormula: String,
    override val tag: Long
) : /*ExpressionNode(),*/Node,  PortHolder {
    var instance: OutputPortInstance? = null
    var error: Exception? = null
    var cellRange: CellRangeReference? = null
    var singleCell = false

    override var value: Any? = null
    override var valueTag: Long = tag

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()


    override fun recalculateValue(tag: Long): Boolean {
        val newValue = if (singleCell) cellRange!!.iterator().next().value else CellRangeValues(cellRange!!)
        valueTag = tag
        if (newValue == this.value) {
            return false
        }
        this.value = newValue
        if (instance != null) {
            try {
                instance?.setValue(value)
                error = null
            } catch (e: Exception) {
                e.printStackTrace()
                error = e
            }
        }
        return true
    }


    fun reparse() {
        singleCell = !rawFormula.contains(":")
        val rawReference = if (rawFormula.startsWith("=")) rawFormula.substring(1) else rawFormula
        cellRange = CellRangeReference.parse(rawReference)

        clearDependsOn()
        for (cell in cellRange!!) {
            inputs.add(cell)
            cell.outputs.add(this)
        }
    }

    fun clearDependsOn() {
        for (dep in inputs) {
            dep.outputs.remove(this)
        }
        inputs.clear()
    }

    override fun attach(token: ModificationToken) {
        detach()

        reparse()

        if (!Model.simulationMode || specification.modifiers.contains(AbstractArtifactSpec.Modifier.NO_SIMULATION)) {
            try {
                instance = specification.createFn(configuration)
            } catch (exception: Exception) {
                error = exception
                exception.printStackTrace()
            }
        }
    }

    override fun notifySimulationModeChanged(token: ModificationToken) {
        detach()
        attach(token)
    }

    override fun detach() {

        if (instance != null) {
            try {
                instance?.detach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            instance = null
        }
    }


    override fun toJson(sb: StringBuilder, forClient: Boolean) {
        sb.append("""{"name":${name.quote()}, "kind":${specification.name.quote()}, "configuration": """)
        configuration.toJson(sb)
        if (forClient) {
            serializeDependencies(sb)
        }
        sb.append(""", "source":${rawFormula.quote()}}""")

    }


    override fun toString() = name
}