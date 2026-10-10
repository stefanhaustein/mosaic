package org.kobjects.mosaic.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.kobjects.mosaic.model.expression.Expression.Companion.ZERO_TIME
import kotlin.time.DurationUnit

object Values {

    fun parseNumber(s: String): Number {
        val d = s.toDouble()
        return if (s.contains(".") || s.contains("e") || s.contains("E") || d != d.toInt().toDouble()) d else d.toInt()
    }


    fun toDouble(value: Any?) = when (value) {
        null, Unit -> 0.0
        is Double -> value
        is Number -> value.toDouble()
        is Instant -> (value - ZERO_TIME.toInstant(TimeZone.currentSystemDefault())).toDouble(DurationUnit.DAYS)
        else -> throw IllegalArgumentException("Not a number: ${value::class.qualifiedName}: '$value'")
    }

    fun toInt(value: Any?) = when (value) {
        null, Unit -> 0
        is Int -> value.toInt()
        is Number -> value.toInt()
        else -> throw IllegalArgumentException("Not convertible to int: ${value::class.qualifiedName}: '$value'")
    }

    fun toBoolean(value: Any?) = when (value) {
        null, Unit -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        else -> throw IllegalArgumentException("Not convertible to boolean: ${value::class.qualifiedName}: '$value'")
    }

    fun toJson(value: Any?) = when (value) {
        null, Unit -> JsonNull
        is Double -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value.toDouble())
        is Instant -> JsonPrimitive(value.toString())
        is String -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        else -> throw IllegalArgumentException("Not convertible to json: ${value::class.qualifiedName}: '$value'")
    }

}