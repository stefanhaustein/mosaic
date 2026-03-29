package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.json.LegacyToJson
import org.kobjects.tomson.ToJson
import org.kobjects.tomson.genericToJson
import org.kobjects.tomson.toJson

data class ParameterSpec(
    val name: String,
    val type: Type,
    val defaultValue: Any?,
    val modifiers: Set<Modifier> = emptySet()
) : LegacyToJson, ToJson {

    override fun toJson() = buildJsonObject {
        put ("name", JsonPrimitive(name))
        put("type", type.toJson())
        if (defaultValue != null) {
            put("default", genericToJson(defaultValue))
        }
        if (modifiers.isNotEmpty()) {
            put ("modifiers", modifiers.toJson())
        }
    }

    enum class Modifier {
        CONSTANT, OPTIONAL, REFERENCE
    }
}