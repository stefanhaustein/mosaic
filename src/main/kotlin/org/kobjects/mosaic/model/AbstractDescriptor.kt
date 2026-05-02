package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.tomson.ToJson
import org.kobjects.tomson.toJson

abstract class AbstractDescriptor(
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


    fun convertConfiguration(rawConfig: JsonObject?): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for (paramSpec in parameters) {
            val paramName = paramSpec.name
            val rawValue = rawConfig?.get(paramName)
            if (rawValue == null) {
                require (paramSpec.modifiers.contains(ParameterSpec.Modifier.OPTIONAL)) {
                    "Missing mandatory configuration parameter: $paramName for $fqName in $rawConfig"
                }
            } else if (paramSpec.modifiers.contains(ParameterSpec.Modifier.REFERENCE)) {
                throw RuntimeException("References NYI (config param $paramName for $fqName")
            } else {
                result[paramName] = paramSpec.type.valueFromJson(rawValue)
            }
        }
        return result.toMap()
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

    enum class Modifier {
         NO_SIMULATION, DELETED, SINGLETON, SETTABLE, UNINSTANTIABLE
    }
}

