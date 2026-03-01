package org.kobjects.mosaic.pluginapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.json.LegacyToJson
import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.legacyToJson
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

    override fun legacyToJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":""")
        type.legacyToJson(sb)
        if (defaultValue != null) {
            sb.append(""", "default":""")
            defaultValue.legacyToJson(sb)
        }
        if (modifiers.isNotEmpty()) {
            sb.append(""", "modifiers":${modifiers.map { it.name }.legacyToJson()}""")
        }
        sb.append("}")
    }

    enum class Modifier {
        CONSTANT, OPTIONAL, REFERENCE
    }
}