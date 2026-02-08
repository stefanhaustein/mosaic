package org.kobjects.mosaic.pluginapi

import org.kobjects.mosaic.json.ToJson
import org.kobjects.mosaic.json.quote

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
) : ToJson {

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

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${fqName.quote()},"category":${category.quote()},"kind":"$kind",""")
        if (type != null) {
            sb.append(""""type":${type.toJson()},""")
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
            param.toJson(sb)
        }
        sb.append("]")
        if (modifiers.isNotEmpty()) {
            sb.append(""","modifiers":[""")
            sb.append(modifiers.joinToString(",") { it.name.quote() })
            sb.append("]")
        }
        sb.append("}")
    }


    enum class Modifier {
         NO_SIMULATION, DELETED, SINGLETON, SETTABLE, UNINSTANTIABLE
    }
}

