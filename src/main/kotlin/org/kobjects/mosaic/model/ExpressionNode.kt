package org.kobjects.mosaic.model

import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.expression.Expression
import org.kobjects.mosaic.model.expression.Literal
import org.kobjects.mosaic.model.parser.ParsingContext
import org.kobjects.mosaic.model.parser.TcFormulaParser

abstract class ExpressionNode(
    override val owner: Namespace
) : Node {
    var rawFormula = ""

    var expression: Expression = Literal(Unit)
    override var value: Any? = null

    override var valueTag = 0L
    override var tag = 0L

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()


    override fun detach() {
        clearDependsOn()
    }

    fun clearDependsOn() {
        for (dep in inputs) {
            dep.outputs.remove(this)
        }
        inputs.clear()
    }


    open fun reparse() {
        clearDependsOn()
        expression.detachAll()
        expression = TcFormulaParser.parseNodeExpression(rawFormula, ParsingContext(this))
    }

    open fun notifyValueChanged(newValue: Any?) {}


    override fun recalculateValue(tag: Long): Boolean {
        var newValue: Any?
        try {
            newValue = expression.eval(EvaluationContext(tag))
        } catch (e: Exception) {
            e.printStackTrace()
            newValue = e
        }
        return if (newValue == value) false else {
            value = newValue
            valueTag = tag
            notifyValueChanged(newValue)
            true
        }
    }
}