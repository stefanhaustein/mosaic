package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.json.LegacyToJson
import org.kobjects.tomson.ToJson
import org.kobjects.tomson.toJson

abstract class AbstractArtifactSpec(
    val namespace: Namespace?,
    val category: String,
    val kind: OperationKind,
    val type: Type?,
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val modifiers: Set<Modifier>,
    val tag: Long,
    val displayName: String?,
) : LegacyToJson, ToJson {

    val fqName
        get() = if (namespace != null) namespace.name + "." + name else name


    fun convertConfiguration(rawConfig: Map<String, Any?>): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for (paramSpec in parameters) {
            val paramName = paramSpec.name
            val rawValue = rawConfig[paramName]
            if (rawValue == null || rawValue == Unit) {
                require (paramSpec.modifiers.contains(ParameterSpec.Modifier.OPTIONAL)) {
                    "Missing mandatory configuration parameter: $paramName for $fqName"
                }
            } else if (paramSpec.modifiers.contains(ParameterSpec.Modifier.REFERENCE)) {
                throw RuntimeException("References NYI (config param $paramName for $fqName")
            } else {
                result[paramName] = paramSpec.type.valueFromJson(rawValue)
            }
        }
        return result
    }

    override fun toJson() = buildJsonObject {
        put("name", JsonPrimitive(fqName))
        put("category", JsonPrimitive(category))
        put("kind", JsonPrimitive(kind.name))
        if (type != null) {
            put ("type", type.toJson())
        }
        if (displayName != null) {
            put("displayName", JsonPrimitive(displayName))
        }
        put("description", JsonPrimitive(description))
        put("params", parameters.toJson())
        put("modifiers", modifiers.toJson())
    }

    override fun legacyToJson(sb: StringBuilder) {
        sb.append(toJson().toString())
        /*
        sb.append("""{"name":${fqName.quote()},"category":${category.quote()},"kind":"$kind",""")
        if (type != null) {
            sb.append(""""type":${type.legacyToJson()},""")
        }
        if (displayName != null) {
            sb.append(""""displayName":${displayName.quote()},""")
        }
        sb.append(""""description":${description.quote()},"params":[""")
        var first = true
        for (param in parameters) {
            if (first) {
                first = false
            } else {
                sb.append(",")
            }
            param.legacyToJson(sb)
        }
        sb.append("]")
        if (modifiers.isNotEmpty()) {
            sb.append(""","modifiers":[""")
            sb.append(modifiers.joinToString(",") { it.name.quote() })
            sb.append("]")
        }
        sb.append("}")*/
    }


    enum class Modifier {
         NO_SIMULATION, DELETED, SINGLETON, SETTABLE, UNINSTANTIABLE
    }
}

