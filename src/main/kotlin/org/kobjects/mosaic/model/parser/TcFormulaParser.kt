package org.kobjects.mosaic.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.mosaic.model.CellRangeReference
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.expression.*
import org.kobjects.mosaic.pluginapi.FunctionSpec


fun createBinaryOperatorNode(name: String, leftOperand: Expression, rightOperand: Expression): Expression =
    when (name) {
        "and", "AND", "And" -> LogicalOperator(LogicalOperator.LogicalOperator.AND, leftOperand, rightOperand)
        "or", "OR", "Or" -> LogicalOperator(LogicalOperator.LogicalOperator.OR, leftOperand, rightOperand)
        else -> BinaryOperator(name, leftOperand, rightOperand)
    }


object TcFormulaParser : PrattParser<TcScanner, ParsingContext, Expression>(
    { scanner, context -> TcFormulaParser.parsePrimary(scanner, context) },
    { _, _, name, operand -> UnaryOperator(name, operand) },
    { _, _, name, leftOperand, rightOperand -> createBinaryOperatorNode(name, leftOperand, rightOperand) },
    Operator.Prefix(10, "not", "NOT", "Not"),
    Operator.Infix(9, "."),
    Operator.Prefix(8, "-"),
    Operator.Suffix(7, "%"),
    Operator.Infix(6, "^"),
    Operator.Infix(5, "*", "/"),
    Operator.Infix(4, "+", "-"),
    Operator.Infix(3, "&"),
    Operator.Infix(2, "=", "<>", "<=", "=>", "<", ">"),
    Operator.Infix(1, "and", "AND", "And"),
    Operator.Infix(0, "or", "OR", "Or")
) {

    fun parsePrimary(scanner: TcScanner, context: ParsingContext): Expression {
        var result = when (scanner.current.type) {
            TcTokenType.NUMBER -> if (scanner.current.text.contains(".")
                || scanner.current.text.contains("e")
                || scanner.current.text.contains("E")
            ) Literal(scanner.consume().text.toDouble()) else Literal(scanner.consume().text.toInt())

            TcTokenType.STRING -> {
                val text = scanner.consume().text
                Literal(text.substring(1, text.length - 1))
            }

            TcTokenType.CELL_IDENTIFIER -> {
                val name = scanner.consume().text
                val cellRange = CellRangeReference.parse(name, context.cell.sheet)

                if (name.contains(":")) {
                    CellRangeExpression(context.cell, cellRange)
                } else {
                    CellExpression(context.cell, cellRange.iterator().next())
                }
            }

            TcTokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                val parameterList = if (scanner.tryConsume("(")) parseParameterList(scanner, context) else emptyMap()
                when (name.lowercase()) {
                    "true" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        Literal(true)
                    }

                    "false" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        Literal(false)
                    }

                    else -> {
                        val lowercase = name.lowercase()
                        val integration = Model.integrations.integrationMap[lowercase]
                        if (integration != null) {
                            scanner.consume(".")
                            val qName = lowercase + "." + scanner.consume(TcTokenType.IDENTIFIER).text.lowercase()
                            val spec = integration.operationSpecs.find { it.name == qName } ?: throw IllegalArgumentException("'$qName' not found in integration '$lowercase'")
                            PluginOperationCall.create(context.cell, spec as FunctionSpec, parameterList)
                        } else {
                            val functionSpec = Model.functions[lowercase] as FunctionSpec?
                            if (functionSpec != null) {
                                PluginOperationCall.create(context.cell, functionSpec, parameterList)
                            } else {
                                val port = Model.ports[lowercase]
                                require(port != null) {
                                    "Unresolved identifier $lowercase"
                                }
                                PortExpression(context.cell, port)
                            }
                        }
                    }
                }
            }

            TcTokenType.SYMBOL -> {
                if (scanner.tryConsume("(")) {
                    val result = parsePrimary(scanner, context)
                    scanner.consume(")")
                    result
                } else {
                    throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
                }
            }

            else -> {
                throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
            }
        }

        while (scanner.tryConsume(".")) {
            result = FieldExpression(result, scanner.consume().text)
        }
        return result

    }

    fun parseParameterList(scanner: TcScanner, context: ParsingContext): Map<String, Expression> {
        if (scanner.tryConsume(")")) {
            return emptyMap()
        }
        val result = mutableMapOf<String, Expression>()
        var index = 0
        do {
            var name = (index++).toString()
            if (scanner.current.type == TcTokenType.IDENTIFIER &&
                scanner.lookAhead(1).text == "=") {
                name = scanner.consume().text
                scanner.consume("=")
            }
            val expression = TcFormulaParser.parseExpression(scanner, context)
            result[name] = expression
        } while (scanner.tryConsume(","))
        scanner.consume(")")
        return result.toMap()
    }


    fun parseExpression(value: String, context: ParsingContext): Expression {
        val scanner = TcScanner(value)
        val result = parseExpression(scanner, context)
        require(scanner.eof)
        return result
    }
}