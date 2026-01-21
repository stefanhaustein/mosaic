package org.kobjects.mosaic.json

import kotlinx.serialization.json.*

object JsonParser {

    fun parse(serialized: String): Any? {
        try {
            val json = Json.parseToJsonElement(serialized)
            return convertElement(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing JSON from '$serialized'", e)
        }
    }

    fun parseObject(serialized: String): Map<String, Any?> {
        try {
            val json = Json.parseToJsonElement(serialized)
            return convertObject(json.jsonObject)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing JSON from '$serialized'", e)
        }
    }

    fun convertObject(jsonObject: JsonObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for ((key, value) in jsonObject.entries) {
            map[key] = convertElement(value)
        }
        return map.toMap()
    }

    fun convertArray(jsonArray: JsonArray): List<Any?> {
        val result = mutableListOf<Any?>()
        for (jsonElement in jsonArray) {
            result.add(convertElement(jsonElement))
        }
        return result.toList()
    }

    fun convertElement(jsonElement: JsonElement): Any? {
        return when (jsonElement) {
            is JsonObject -> convertObject(jsonElement)
            is JsonArray -> convertArray(jsonElement)
            is JsonPrimitive -> if (jsonElement.isString) jsonElement.content else when (jsonElement.content) {
                "true" -> true
                "false" -> false
                "null" -> null
                else -> {
                    val d = jsonElement.content.toDouble()
                    if (d == d.toLong().toDouble()) d.toLong() else d
                }
            }
            else -> throw IllegalArgumentException("Unsupported json element type '${jsonElement::class}' for '$jsonElement'")
        }
    }

}