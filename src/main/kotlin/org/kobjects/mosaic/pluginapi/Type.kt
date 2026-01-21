package org.kobjects.mosaic.pluginapi

import org.kobjects.mosaic.json.ToJson
import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import kotlin.enums.EnumEntries

/** This looks odd because it used to be an enum */
interface Type : ToJson {
    object INT: Type {
        override fun toString() = "Int"
        override fun valueFromString(s: String) = s.toInt()
        override fun valueFromJson(value: Any) = (value as Number).toInt()
    }
    object REAL: Type {
        override fun toString() = "Real"
        override fun valueFromString(s: String) = s.toDouble()
        override fun valueFromJson(value: Any) = (value as Number).toDouble()
    }
    object BOOL: Type {
        override fun toString() = "Bool"
        override fun valueFromString(s: String) = s.toBoolean()
        override fun valueFromJson(value: Any) = (value as Boolean)
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

    override fun toJson(sb: StringBuilder) {
        sb.append(toString().quote())
    }

    fun valueFromString(s: String): Any =
        throw UnsupportedOperationException("Can't parse '$this' yet.")

    fun valueFromJson(value: Any): Any =
        if (value is String) valueFromString(value)
        else throw UnsupportedOperationException("Can't parse '$this' from JSON yet.")

    class ENUM<T : Enum<T>>(val entries: EnumEntries<T>) : Type {

        override fun toJson(sb: StringBuilder) {
            entries.map {it.name}.toJson(sb)
        }

        override fun valueFromString(s: String) =
            entries.first { it.name.lowercase() == s.lowercase() }

    }

    class Struct(val fields: List<Field>) : Type {
        override fun toJson(sb: StringBuilder) = fields.toJson(sb)
    }

    class Field(val name: String, val type: Type) : ToJson {
        override fun toJson(sb: StringBuilder) {
            sb.append("""{"name": ${name.quote()}, "type":""")
            type.toJson(sb)
            sb.append("}")
        }
    }

}