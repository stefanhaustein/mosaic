package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec.Modifier

class OutputPortHolder(
    owner: Namespace?,
    override val name: String,
    override val specification: OutputPortSpec,
    val configuration: Map<String, Any?>,
    override val displayName: String? = null,
    override val category: String? = null,
    override var tag: Long
) : ExpressionNode(owner),  PortHolder {
    var instance: OutputPortInstance? = null
    var error: Exception? = null

    override var value: Any? = null
    override var valueTag: Long = tag



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

    override fun notifyValueChanged(newValue: Any?) {
        instance?.setValue(newValue)
    }

    override fun toString() = name
}