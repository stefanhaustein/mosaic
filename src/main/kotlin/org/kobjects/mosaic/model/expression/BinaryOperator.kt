package org.kobjects.mosaic.model.expression

import org.kobjects.mosaic.model.Values

class BinaryOperator(
    val name: String,
    val leftOperand: Expression,
    val rightOperand: Expression
): Expression() {

    override fun eval(context: EvaluationContext): Any {
        val l = leftOperand.eval(context) ?: 0.0
        val r = rightOperand.eval(context) ?: 0.0

        if (name == "&") {
            return "$l$r"
        }

        if ((l is Int || l is Boolean) && (r is Int || r is Boolean)) {
            val li = Values.toInt(l)
            val ri = Values.toInt(r)
            when (name) {
                "." -> return (li shr ri) and 1
                "+" -> return li + ri
                "-" -> return li - ri
                "*" -> return li * ri
                "//" -> return li / ri
                "=" -> return li == ri
                "<>" -> return li != ri
                "<=" -> return li <= ri
                ">=" -> return li >= ri
                "<" -> return li < ri
                ">" -> return li > ri
            }
        }

        return when (name) {
            "=" -> l == r
            "<>" -> l != r
            else -> {
                val ld = Values.toDouble(l)
                val rd = Values.toDouble(r)

                return when (name) {
                    "+" -> ld + rd
                    "-" -> ld - rd
                    "*" -> ld * rd
                    "/" -> ld / rd
                    "^" -> Math.pow(ld, rd)
                    "<=" -> return ld <= rd
                    ">=" -> return ld >= rd
                    "<" -> return ld < rd
                    ">" -> return ld > rd
                    else -> throw UnsupportedOperationException("$name for Double")
                }
            }
        }


    }

    override val children: Collection<Expression>
        get() = listOf(leftOperand, rightOperand)
}