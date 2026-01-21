package org.kobjects.mosaic.svg.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.mosaic.model.expression.*

object ExpressionParser : PrattParser<ExpressionScanner, Map<String, Any>, Any>(
    { scanner, context -> ExpressionParser.parsePrimary(scanner, context) },
    { scanner, context, name, operand ->
        if (name == "?") processTernary(scanner, context, operand)
        else evalUnary(name, operand) },
    { _, _, name, leftOperand, rightOperand ->  evalBinary(name, leftOperand, rightOperand) },
    Operator.Prefix(3, "-"),
    Operator.Infix(2, "*", "/"),
    Operator.Infix(1, "+", "-"),
    Operator.Suffix(0, "?")
) {


    fun parsePrimary(scanner: ExpressionScanner, context: Map<String, Any>): Any =
        when (scanner.current.type) {
            ExpressionTokenType.NUMBER -> Literal(scanner.consume().text.toDouble())
            ExpressionTokenType.STRING -> {
                val text = scanner.consume().text
                text.substring(1, text.length - 1)
            }
            ExpressionTokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                context[name] ?: when (name) {
                    "true" -> true
                    "false" -> false
                    else -> throw UnsupportedOperationException(name)
                }
            }
            else -> {
                throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
            }

    }



    fun evaluateExpression(value: String, context: Map<String, Any>): Any {
        val scanner = ExpressionScanner(value)
        val result = parseExpression(scanner, context)
        require(scanner.eof) { "Leftover tokens: $scanner" }
        return result
    }
}

fun evalBinary(name: String, leftOperand: Any, rightOperand: Any): Expression {
    when (name) {
        else -> throw UnsupportedOperationException("Binary operator $name")
    }
}

fun processTernary(scanner: ExpressionScanner, context: Map<String, Any>, condition: Any): Any {
    val trueValue = ExpressionParser.parseExpression(scanner, context, 0)
    scanner.consume(":")
    val falseValue = ExpressionParser.parseExpression(scanner, context, 0)
    return if (condition as Boolean) trueValue else falseValue
}

fun evalUnary(name: String, operand: Any): Any = {
    when (name) {
        "-" -> -(operand as Number).toDouble()
        "!" -> !(operand as Boolean)
        else -> throw UnsupportedOperationException("Unary operator $name")
    }
}