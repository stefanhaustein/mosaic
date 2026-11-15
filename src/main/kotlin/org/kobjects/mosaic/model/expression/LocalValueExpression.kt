package org.kobjects.mosaic.model.expression

object LocalValueExpression : Expression() {

    override fun eval(context: EvaluationContext): Any? = context.localValue

    override val children: Collection<Expression>
        get() = emptyList()
}
