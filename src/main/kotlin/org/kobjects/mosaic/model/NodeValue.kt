package org.kobjects.mosaic.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.sheet.Cell.Companion.TIME_FORMAT_SECONDS

class NodeValue(
    val tag: Long,
    val value: Any?,
    val imageConditionValues: List<Any?> = emptyList()
) {


    fun contentEquals(other: NodeValue?): Boolean {
        return other != null && value == other.value && imageConditionValues == other.imageConditionValues
    }

    fun serialize(): JsonObject = buildJsonObject {
        serialize(this)
    }

    fun serialize(builder: JsonObjectBuilder) {
        val value = this@NodeValue.value
        when (value) {
            null,
            is Unit -> {
                builder.put("value", JsonNull)
            }
            is Exception -> {
                builder.put("type", JsonPrimitive("err"))
                builder.put ("msg", JsonPrimitive(value::class.simpleName.toString() + value.message))
            }
            is Instant -> {
                val localDateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
                builder.put("type", JsonPrimitive("instant"))
                builder.put("rendered", JsonPrimitive(localDateTime.time.format(TIME_FORMAT_SECONDS)))
            }
            is Number -> builder.put("value", JsonPrimitive(value))
            is String -> builder.put("value", JsonPrimitive(value))
            is Boolean -> builder.put("value", JsonPrimitive(value))
            else -> {
                builder.put("type", JsonPrimitive("err"))
                builder.put ("msg", JsonPrimitive("Unrecognized value type: '${value.javaClass}' for $value"))
            }
        }
        if (imageConditionValues.isNotEmpty()) {
            builder.put("images", buildJsonArray {
                for (value in imageConditionValues) {
                    add(Values.toJson(value))
                }
            })
        }
    }
}