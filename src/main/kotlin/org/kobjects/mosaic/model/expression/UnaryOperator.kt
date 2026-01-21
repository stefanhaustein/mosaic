package org.kobjects.mosaic.model.expression

class UnaryOperator(val name: String, val operand: Expression) : Expression() {

    override fun eval(context: EvaluationContext): Any? {
        val value = operand.eval(context)
        return when (value) {
            is Double -> when (name) {
                    "-" -> -value
                    else -> throw UnsupportedOperationException("$name not supported for Number operants.")
                }
            is Int -> when (name) {
                    "-" -> -value
                    else -> throw UnsupportedOperationException("$name not supported for Int operands.")
                }
            is Boolean -> when (name) {
                "not" -> !value
                else -> throw UnsupportedOperationException("$name not supported for Boolean operands.")
            }
            null -> null
            else -> throw UnsupportedOperationException("Unary operantions (here, '$name') are not supported for type ${value::class}")
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()
}