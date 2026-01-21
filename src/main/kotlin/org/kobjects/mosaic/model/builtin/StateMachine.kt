package org.kobjects.mosaic.model.builtin

import org.kobjects.mosaic.model.Cell
import org.kobjects.mosaic.model.CellRangeReference
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.expression.CellRangeExpression
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.pluginapi.ValueChangeListener
import org.kobjects.mosaic.pluginapi.StatefulFunctionInstance
import java.util.*

class StateMachine(
    val cellRange: CellRangeReference
) : StatefulFunctionInstance {
    private var currentState = ""
    private val highlighted = mutableSetOf<CellRangeReference>()

    var currentRow: Int = 0

    val timer = Timer()
    var timedTransition: TimedTransition? = null
    var host: ValueChangeListener? = null

    val rowCount: Int = cellRange.toRow - cellRange.fromRow + 1

    override fun attach(host: ValueChangeListener) {
        this.host = host
    }

    override fun detach() {
        host = null
    }

    fun getValue(col: Int, row: Int): Any? {
        return cellRange.sheet.getOrCreateCell(Cell.id(
            (if (col < 0) cellRange.toColumn + 1 else cellRange.fromColumn) + col, cellRange.fromRow + row)).value
    }


    fun setCurrentState(tag: Long, state: String) {
        if (state == currentState) {
            return
        }
        for (highlight in highlighted) {
            cellRange.sheet.setHighlight(tag, highlight, false)
        }
        for (i in 0 until rowCount) {
            if (getValue(0, i) == state) {
                val toHighlight = CellRangeReference(cellRange.sheet, cellRange.fromColumn, cellRange.fromRow + i, cellRange.toColumn, cellRange.fromRow + i)
                highlighted.add(toHighlight)
                cellRange.sheet.setHighlight(tag, toHighlight, true)
            }
        }
        currentState = state
    }

    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        println("**** state machine apply called; currentState: $currentState")

        if (currentState == "") {
            setCurrentState(context.tag, getValue(0, 0).toString())
        }

        val initialRow = currentRow
        do {
            if (getValue(0, currentRow).toString() == currentState) {
                val gate = getValue(-2, currentRow)
                if (gate == true) {
                    timedTransition?.cancel()
                    timedTransition = null
                    setCurrentState(context.tag, getValue(-1, currentRow).toString())

                    // Trigger a re-calculation as there might be a followup state transition
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            Model.notifyValueChanged(host)
                        }
                    }, 1)
                    break
                } else if (gate is Number) {
                    val delay = gate.toLong()
                    val targetTime = System.currentTimeMillis() + delay
                    if (targetTime < timedTransition?.targetTime ?: Long.MAX_VALUE) {
                        timedTransition?.cancel()
                        val newTransition = TimedTransition(
                            targetTime, getValue(-1, currentRow).toString(), currentRow)
                        timedTransition = newTransition
                        timer.schedule(newTransition, delay)
                    }
                }
            }

            currentRow = (currentRow + 1) % rowCount
        } while (initialRow != currentRow)

        println("**** state machine apply result: $currentState")

        return currentState
    }


    inner class TimedTransition(
        val targetTime: Long,
        val targetState: String,
        val targetRow: Int,
    ) : TimerTask() {
        override fun run() {
            Model.requestSynchronizedWithToken {
                timedTransition = null
                setCurrentState(it.tag, targetState)
                currentRow = targetRow
                host?.notifyValueChanged(it)
            }
        }
    }



    companion object {
        fun create(configuration: Map<String, Any?>) = StateMachine(
            (configuration["transitions"] as CellRangeExpression).target)
    }

}