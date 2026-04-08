package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Node
import org.kobjects.tomson.toJson

open class InputPortHolder(
    override val owner: Integration,
    override val name: String,
    override val specification: InputPortSpec,
    override val configuration: Map<String, Any?>,
    override val displayName: String? = null,
    override val category: String? = null,
    override val tag: Long

) : PortHolder, Node, InputPortListener {

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()

    var instance: InputPortInstance? = null

    override var valueTag  = 0L
    override var value: Any? = null

    var portValue: Any? = null

    init {
        require(!name.contains(".")) { "Port name '$name' must not contain '.'" }
    }


    override fun attach(token: ModificationToken) {
        detach()
        try {
            instance = specification.createFn(configuration, this)
        } catch (e: Exception) {
            portValue = e
            e.printStackTrace()
        }
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

            token.addRefresh(this)
        }


    override fun recalculateValue(tag: Long): Boolean {
        if (valueTag == tag) {
            return false
        }
        val newValue = portValue
        if (value == newValue) {
            return false
        }
        valueTag = tag
        value = newValue
        return true
    }



    override fun toString() = name

}