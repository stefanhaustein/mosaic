package org.kobjects.mosaic.model.expression

import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.Node


class EvaluationContext(
    val model: Model,
    val tag: Long,
    val newInputPortValues: Map<Node, Any?>,
    val localValue: Any? = null
) {
    fun withLocalValue(localValue: Any?) =
        EvaluationContext(model, tag, newInputPortValues, localValue)
}
