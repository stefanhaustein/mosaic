package org.kobjects.mosaic.model.integration


import kotlinx.serialization.json.JsonObject
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.NodeValue
import org.kobjects.mosaic.model.expression.EvaluationContext

open class InputPortNode(
    override val owner: Integration,
    override val name: String,
    override val descriptor: InputPortDescriptor,
    override val displayName: String? = null,
    override val category: String? = null,

) : PortNode, Node, InputPortListener {

    override var topologicalOrder: Int = -1

    override var jsonConfiguration = JsonObject(emptyMap())
    override var tag: Long = 0
    override var deleted = false

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()

    var instance: InputPortInstance? = null


    init {
        require(!name.contains(".")) { "Port name '$name' must not contain '.'" }
    }

    override fun configureInternal(config: Map<String, Any?>, token: ModificationToken) {
        instance = descriptor.createFn(config, this)
    }

    override fun detach() {
        // This doesn't really need to do anything about dependencies -- dependencies will be updated in their reset
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

    // Implements the corresponding value change listener methods.

    override fun portValueChanged(newValue: Any?) {
        owner.model.setInputPortValue(this, newValue)
    }


    override fun calculateValue(context: EvaluationContext) =
        if (context.newInputPortValues.containsKey(this))
            NodeValue(context.tag, context.newInputPortValues[this]) else null


    override fun toString() = name
}