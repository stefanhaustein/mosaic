package org.kobjects.mosaic.model.builtin

import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.pluginapi.FunctionInstance

object PiFunction : FunctionInstance {
    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        return Math.PI
    }
}