package org.kobjects.mosaic.model.expression

class LogicalOperator(
    val kind: LogicalOperator,
    val leftOperand: Expression,
    val rightOperand: Expression
): Expression() {


    override fun eval(context: EvaluationContext): Any {
        val left = leftOperand.evalBoolean(context)
        return when (kind) {
            LogicalOperator.AND -> left && rightOperand.evalBoolean(context)
            LogicalOperator.OR -> left || rightOperand.evalBoolean(context)
        }
    }

    override val children: Collection<Expression>
        get() = listOf(leftOperand, rightOperand)


    enum class LogicalOperator {
        AND, OR
    }

}