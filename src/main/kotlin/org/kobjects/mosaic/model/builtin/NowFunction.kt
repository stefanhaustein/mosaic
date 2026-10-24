package org.kobjects.mosaic.model.builtin

import kotlinx.datetime.Clock
import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.function.StatefulFunctionInstance
import java.util.Timer
import java.util.TimerTask

class NowFunction(
    val owner: Node,
    val updateInterval: Double,
) : StatefulFunctionInstance {
    val timer = Timer()
    val timerId = timerCounter++
    var task: TimerTask? = null

    init {
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            task = object : TimerTask() {
                override fun run() {
                    owner.owner.model.enqueueUpdateAction { owner }
                }
            }
            timer.schedule(task, period, period)
        }
        println("Started timer $timerId")
    }


    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        return Clock.System.now()
    }


    override fun detach() {
        println("Cancelling timer $timerId!")
        //timerTask?.cancel()
        task?.cancel()
        task = null

        //timer.purge()
    }


    companion object {
        var timerCounter = 0

        fun create(owner: Node, configuration: Map<String, Any?>): NowFunction {
            return NowFunction(owner, (configuration["interval"] as Number).toDouble())
        }

    }
}