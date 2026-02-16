package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.pluginapi.Namespace

interface Node {
    val value: Any?
    /** Used to track when the value was changed last. */
    val valueTag: Long
    val outputs: MutableSet<Node>
    val inputs: MutableSet<Node>
    val owner: Namespace?
    /**
     * Re-calculates the value bases on inputs.
     * Input port values will be refreshed from the port value here.
     */
    fun recalculateValue(tag: Long): Boolean
    fun detach()

    fun qualifiedId(): String

    fun serializeDependencies(sb: StringBuilder) {
        if (inputs.isNotEmpty()) {
            sb.append(""", "inputs":[${inputs.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
        if (outputs.isNotEmpty()) {
            sb.append(""", "outputs":[${outputs.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
    }

}