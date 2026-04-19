package org.kobjects.mosaic.model.integration


import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Node

open class InputPortNode(
    override val owner: Integration,
    override val name: String,
    override val specification: InputPortSpec,
    override val configuration: Map<String, Any?>,
    override val displayName: String? = null,
    override val category: String? = null,
    override val tag: Long

) : PortNode, Node, InputPortListener {

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

    // Implements the corresponding value change listener methods.

    override fun portValueChanged(newValue: Any?) {
        Model.requestSynchronizedWithToken {
            portValueChanged(newValue, it)
        }
    }

    override fun portValueChanged(newValue: Any?, token: ModificationToken) {
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