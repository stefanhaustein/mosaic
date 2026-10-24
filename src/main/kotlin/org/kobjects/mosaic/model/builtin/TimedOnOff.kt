package org.kobjects.mosaic.model.builtin

import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.function.StatefulFunctionInstance
import java.util.Timer
import java.util.TimerTask

class TimedOnOff(
    val owner: Node,
    val delayedState: Boolean,
    val delay: Double,

) : StatefulFunctionInstance {

    val timer = Timer()
    var task: TimerTask? = null
    var outputState: Boolean = false


    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        val inputState = params["input"] as Boolean
        if (inputState != delayedState) {
            task?.cancel()
            outputState = inputState
        } else if (outputState != delayedState) {
            task?.cancel()
            task = object : TimerTask() {
                override fun run() {
                    owner.owner.model.enqueueUpdateAction {
                        outputState = delayedState
                        owner
                    }
                    task = null
                }
            }
            timer.schedule(task, (delay * 1000.0).toLong())
        }
        return outputState
    }


    override fun detach() {
        task?.cancel()
        task = null
    }


    companion object {
        fun createTon(node: Node, configuration: Map<String, Any?>): TimedOnOff {
            return TimedOnOff(node, true, (configuration["delay"] as Number).toDouble())
        }

        fun createToff(node: Node, configuration: Map<String, Any?>): TimedOnOff {
            return TimedOnOff(node,false, (configuration["delay"] as Number).toDouble())
        }
    }
}