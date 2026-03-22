package org.kobjects.mosaic.model.function

import org.kobjects.mosaic.model.expression.EvaluationContext

interface FunctionInstance {
    fun apply(context: EvaluationContext, params: Map<String, Any?>): Any?
}