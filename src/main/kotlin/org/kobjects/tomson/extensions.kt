package org.kobjects.tomson

import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

fun <K, V> Map<K, V>.toJson() =
    buildJsonObject { toJson(this) }

fun <K, V> Map<K, V>.toJson(builder: JsonObjectBuilder) {
    for ((key, value) in this@toJson) {
        builder.put(key.toString(), genericToJson(value))
    }
}

fun <V> Iterable<V>.toJson(builder: JsonArrayBuilder) {
    for (value in this) {
        builder.add(genericToJson(value))
    }
}

fun <V> Iterable<V>.toJson() = buildJsonArray {
    toJson(this)
}

fun genericToJson(value: Any?): JsonElement =
    when (value) {
        null -> JsonNull
        is ToJson -> value.toJson()
        is Enum<*> -> JsonPrimitive(value.name)
        is String -> JsonPrimitive(value)
        is Int -> JsonPrimitive(value)
        is Long -> JsonPrimitive(value)
        is Float -> JsonPrimitive(value)
        is Double -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is JsonElement -> value
        is Map<*, *> -> value.toJson()
        is Iterable<*> -> value.toJson()
        else -> throw IllegalArgumentException("Unsupported type ${value.javaClass}")
    }


