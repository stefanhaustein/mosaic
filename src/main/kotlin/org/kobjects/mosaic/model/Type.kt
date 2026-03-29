package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.tomson.ToJson
import org.kobjects.tomson.toJson
import kotlin.enums.EnumEntries

/** This looks odd because it used to be an enum */
interface Type : ToJson {
    object INT: Type {
        override fun toString() = "Int"
        override fun valueFromString(s: String) = s.toInt()
        override fun valueFromJson(value: JsonElement) = value.jsonPrimitive.int
    }
    object REAL: Type {
        override fun toString() = "Real"
        override fun valueFromString(s: String) = s.toDouble()
        override fun valueFromJson(value: JsonElement) = value.jsonPrimitive.double
    }
    object BOOL: Type {
        override fun toString() = "Bool"
        override fun valueFromString(s: String) = s.toBoolean()
        override fun valueFromJson(value: JsonElement) = value.jsonPrimitive.boolean
    }
    object STRING: Type {
        override fun toString() = "String"
        override fun valueFromString(s: String) = s
    }

    object DATE: Type {
        override fun toString() = "Date"
    }

    object VOID: Type {
        override fun toString() = "Void"
    }

    object RANGE: Type {
        override fun toString() = "Range"
    }

    override fun toJson(): JsonElement = JsonPrimitive(toString())

    fun valueFromString(s: String): Any =
        throw UnsupportedOperationException("Can't parse '$this' yet.")

    fun valueFromJson(value: JsonElement): Any =
        if (value is JsonPrimitive) valueFromString(value.jsonPrimitive.content)
        else throw UnsupportedOperationException("Can't parse '$this' from JSON yet.")

    class ENUM<T : Enum<T>>(val entries: EnumEntries<T>) : Type {

        override fun toJson() = JsonArray(entries.toJson())

        override fun valueFromString(s: String) =
            entries.first { it.name.lowercase() == s.lowercase() }

    }

    class Struct(val fields: List<Field>) : Type {
        override fun toJson() = JsonArray(fields.map { it.toJson() })
    }

    class Field(val name: String, val type: Type) : ToJson {
        override fun toJson() = buildJsonObject {
            put("name", JsonPrimitive(name))
            put("type", type.toJson())
        }
    }

}