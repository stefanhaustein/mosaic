package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive


// Can't be an abstract class becuase PortHolder needs to be a sub-interface
interface Node {
    val value: Any?
    /** Used to track when the value was changed last. */
    val valueTag: Long
    val outputs: MutableSet<Node>
    val inputs: MutableSet<Node>
    val owner: Namespace
    /**
     * Re-calculates the value bases on inputs.
     * Input port values will be refreshed from the port value here.
     */
    fun recalculateValue(tag: Long): Boolean
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