package org.kobjects.mosaic.model.builtin

import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.function.StatefulFunctionInstance
import java.util.Timer
import java.util.TimerTask

class TimedPulse(
    val owner: Node,
    val delay: Double,
) : StatefulFunctionInstance {

    val timer = Timer()
    var task: TimerTask? = null
    var outputState: Boolean = false
    var armed: Boolean = true


    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        val inputState = params["input"] as Boolean
        if (inputState) {
            if (!armed) {
                armed = true
            } else {
                armed = false
                if (!outputState) {
                    outputState = true
                    task = object : TimerTask() {
                        override fun run() {
                            owner.owner.model.enqueueUpdateAction { _ ->
                                outputState = false
                                owner
                            }
                        }
                    }
                    timer.schedule(task, (delay * 1000.0).toLong())
                }
            }
        }
        return outputState
    }


    override fun detach() {
        task?.cancel()
        task = null
    }


    companion object {
        fun create(owner: Node, configuration: Map<String, Any?>): TimedPulse {
            return TimedPulse(owner, (configuration["delay"] as Number).toDouble())
        }
    }
}