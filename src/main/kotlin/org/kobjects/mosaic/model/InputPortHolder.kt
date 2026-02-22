package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*

open class InputPortHolder(
    override val owner: Namespace,
    override val name: String,
    override val specification: InputPortSpec,
    val configuration: Map<String, Any?>,
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


    override fun toJson(sb: StringBuilder, forClient: Boolean) {
        sb.append("""{"name":${name.quote()}, "kind":${specification.fqName.quote()}, "type":""")
        specification.type.toJson(sb)
        if (category != null) {
            sb.append(""", "category": ${category?.quote()}""")
        }
        if (displayName != null) {
            sb.append(""", "displayName": ${displayName?.quote()}""")
        }
        sb.append(""", "configuration": """)
        configuration.toJson(sb)
        if (forClient) {
            serializeDependencies(sb)
        }
        sb.append("}")
    }



    override fun toString() = name

}