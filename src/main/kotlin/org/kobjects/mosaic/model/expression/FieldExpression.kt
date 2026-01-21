package org.kobjects.mosaic.model.expression

class FieldExpression(
    val base: Expression,
    val name: String) : Expression() {

    override fun eval(context: EvaluationContext): Any? {
        return (base.eval(context) as? Map<String, Any?>)?.get(name)
    }

    override val children: Collection<Expression>
        get() = listOf(base)
}