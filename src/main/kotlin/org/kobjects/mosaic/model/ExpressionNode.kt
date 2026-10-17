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

    override fun calculateValue(context: EvaluationContext) = NodeValue(context.tag,expression.eval(context))
}