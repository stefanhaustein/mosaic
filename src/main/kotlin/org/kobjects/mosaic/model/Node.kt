package org.kobjects.mosaic.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.sheet.Cell.Companion.TIME_FORMAT_SECONDS


// Can't be an abstract class because PortHolder needs to be a sub-interface
interface Node : Comparable<Node> {
    val outputs: MutableSet<Node>
    val inputs: MutableSet<Node>
    val owner: Namespace
    var tag: Long
    var topologicalOrder: Int

    override fun compareTo(other: Node): Int = topologicalOrder.compareTo(other.topologicalOrder)

    /**
     * Re-calculates the value bases on inputs. Returns true if the value has changed.
     *
     * This is only relevant for "inner" nodes and will never return true for input ports; input port / external
     * changes are communicated by other means.
     */
    fun calculateValue(context: EvaluationContext): NodeValue?

    fun detach()

    fun qualifiedId(): String


    fun serializeDependencies(builder: JsonObjectBuilder) {
        if (inputs.isNotEmpty()) {
            builder.put("inputs", JsonArray(inputs.map { JsonPrimitive(it.qualifiedId()) }  ))
        }
        if (outputs.isNotEmpty()) {
            builder.put("outputs", JsonArray(outputs.map { JsonPrimitive(it.qualifiedId()) }  ))
        }
    }
}