package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec.Modifier

class OutputPortHolder(
    override val name: String,
    override val specification: OutputPortSpec,
    val configuration: Map<String, Any?>,
    var rawFormula: String,
    override val displayName: String? = null,
    override val category: String? = null,
    override var tag: Long
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
        val newValue = if(cellRange == null) null else if (singleCell) cellRange!!.iterator().next().value else CellRangeValues(cellRange!!)
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
        val newCellRange = if (rawReference.isBlank()) null else CellRangeReference.parse(rawReference)
        cellRange = newCellRange

        clearDependsOn()

        if (newCellRange != null) {
            for (cell in newCellRange) {
                inputs.add(cell)
                cell.outputs.add(this)
            }
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


            try {
                instance = specification.createFn(configuration)
            } catch (exception: Exception) {
                error = exception
                exception.printStackTrace()
            }

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
        sb.append("""{"name":${name.quote()}""")
        if (forClient || !specification.modifiers.contains(Modifier.UNINSTANTIABLE)) {
            sb.append(""", "kind":${specification.fqName.quote()}""")
            if (category != null) {
                sb.append(""", "category": ${category?.quote()}""")
            }
            if (displayName != null) {
                sb.append(""", "displayName": ${displayName?.quote()}""")
            }
            sb.append(""", "configuration": """)
            configuration.toJson(sb)
        }
        if (forClient) {
            serializeDependencies(sb)
        }
        sb.append(""", "source":${rawFormula.quote()}}""")

    }


    override fun toString() = name
}