package org.kobjects.mosaic.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.sheet.Cell.Companion.TIME_FORMAT_SECONDS


// Can't be an abstract class because PortHolder needs to be a sub-interface
interface Node {
    val value: Any?
    /** Used to track when the value was changed last. */
    var valueTag: Long
    val outputs: MutableSet<Node>
    val inputs: MutableSet<Node>
    val owner: Namespace
    var tag: Long

    /**
     * Re-calculates the value bases on inputs.
     * Input port values will be refreshed from the port value here.
     */
    fun recalculateValue(tag: Long): Boolean
    fun detach()

    fun qualifiedId(): String


    fun serializeValue(): JsonElement {
        val value = this.value
        return when (value) {
            null,
            is Unit -> JsonNull
            is Exception -> buildJsonObject {
                put("type", JsonPrimitive("err"))
                put ("msg", JsonPrimitive(value::class.simpleName.toString() + value.message))
            }
            is Instant -> buildJsonObject {
                val localDateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
                put("type", JsonPrimitive("instant"))
                put("rendered", JsonPrimitive(localDateTime.time.format(TIME_FORMAT_SECONDS)))
            }
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            else -> buildJsonObject {
                put("type", JsonPrimitive("err"))
                put ("msg", JsonPrimitive("Unrecognized value type: '${value.javaClass}' for $value"))
            }
        }
    }




    fun serializeDependencies(builder: JsonObjectBuilder) {
        if (inputs.isNotEmpty()) {
            builder.put("inputs", JsonArray(inputs.map { JsonPrimitive(it.qualifiedId()) }  ))
        }
        if (outputs.isNotEmpty()) {
            builder.put("outputs", JsonArray(outputs.map { JsonPrimitive(it.qualifiedId()) }  ))
        }
    }
}