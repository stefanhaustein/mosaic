package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.legacyToJson
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec.Modifier
import org.kobjects.tomson.toJson

class OutputPortHolder(
    override val owner: Integration,
    override val name: String,
    override val specification: OutputPortSpec,
    val configuration: Map<String, Any?>,
    rawFormula: String,
    override val displayName: String? = null,
    override val category: String? = null,
    override var tag: Long
) : ExpressionNode(owner),  PortHolder {
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


    override fun legacyToJson(sb: StringBuilder, forClient: Boolean) {
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
            configuration.legacyToJson(sb)
        }
        if (forClient) {
            serializeDependencies(sb)
        }
        sb.append(""", "source":${rawFormula.quote()}}""")

    }


    override fun toJson(forClient: Boolean) = buildJsonObject {
        put("kind", JsonPrimitive(specification.name))
        val type = specification.type
        if (type != null) {
            put("type", type.toJson())
        }
        if (category != null) {
            put("category", JsonPrimitive(category))
        }
        if (displayName != null) {
            put("displayName", JsonPrimitive(displayName))
        }
        put("configuration", configuration.toJson())
        // if (forClient) {
        //  serializeDependencies
        // }
        put("source", JsonPrimitive(rawFormula))
    }



    override fun notifyValueChanged(newValue: Any?) {
        instance?.setValue(newValue)
    }

    override fun toString() = name
}