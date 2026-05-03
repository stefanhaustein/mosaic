package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import org.kobjects.mosaic.model.ExpressionNode
import org.kobjects.mosaic.model.ModificationToken

class OutputPortNode(
    override val owner: Integration,
    override val name: String,
    override val descriptor: OutputPortDescriptor,
    rawFormula: String,
    override val displayName: String? = null,
    override val category: String? = null
) : ExpressionNode(owner),  PortNode {
    var instance: OutputPortInstance? = null
    var error: Exception? = null

    override var jsonConfiguration = JsonObject(emptyMap())
    override var deleted = false


    init {
        require(!name.contains(".")) { "Port name '$name' must not contain '.'" }
        this.rawFormula = rawFormula
    }

    override fun configureInternal(config: Map<String, Any?>, token: ModificationToken) {
        instance = descriptor.createFn(config)
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