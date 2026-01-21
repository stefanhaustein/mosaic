package org.kobjects.mosaic.model.builtin

import kotlinx.datetime.Clock
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.pluginapi.ValueChangeListener
import org.kobjects.mosaic.pluginapi.StatefulFunctionInstance
import java.util.Timer
import java.util.TimerTask

class NowFunction(
    val updateInterval: Double,
) : StatefulFunctionInstance {
    val timer = Timer()
    val timerId = timerCounter++
    var task: TimerTask? = null
    var host: ValueChangeListener? = null

    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        return Clock.System.now()
    }


    override fun attach(host: ValueChangeListener) {
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            task = object : TimerTask() {
                override fun run() {
                    Model.notifyValueChanged(host)
                }
            }
            timer.schedule(task, period, period)
        }
        println("Started timer $timerId")
    }


    override fun detach() {
        println("Cancelling timer $timerId!")
        //timerTask?.cancel()
        task?.cancel()
        task = null
        host = null
        //timer.purge()
    }


    companion object {
        var timerCounter = 0

        fun create(configuration: Map<String, Any?>): NowFunction {
            return NowFunction((configuration["interval"] as Number).toDouble())
        }

    }
}