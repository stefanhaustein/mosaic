package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import org.kobjects.mosaic.model.ExpressionNode
import org.kobjects.mosaic.model.ModificationToken

class OutputPortNode(
    override val owner: Integration,
    override val name: String,
    override val specification: OutputPortSpec,
    override val configuration: Map<String, Any?>,
    rawFormula: String,
    override val displayName: String? = null,
    override val category: String? = null,
    override var tag: Long
) : ExpressionNode(owner),  PortNode {
    var instance: OutputPortInstance? = null
    var error: Exception? = null

    override var value: Any? = null
    override var valueTag: Long = tag

    init {
        require(!name.contains(".")) { "Port name '$name' must not contain '.'" }
        this.rawFormula = rawFormula
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

    override fun needsSaving() = super.needsSaving() || rawFormula.isNotEmpty()

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


    override fun serialize(builder: JsonObjectBuilder, forClient: Boolean) {
        super.serialize(builder, forClient)
        builder.put("source", JsonPrimitive(rawFormula))
    }



    override fun notifyValueChanged(newValue: Any?) {
        instance?.setValue(newValue)
    }

    override fun toString() = name
}