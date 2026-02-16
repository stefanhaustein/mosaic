package org.kobjects.mosaic.model

import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.expression.Expression
import org.kobjects.mosaic.model.expression.Literal
import org.kobjects.mosaic.model.parser.ParsingContext
import org.kobjects.mosaic.model.parser.TcFormulaParser
import org.kobjects.mosaic.pluginapi.ModificationToken

abstract class ExpressionNode : Node {


    var rawFormula = ""

    var expression: Expression = Literal(Unit)
    override var value: Any? = null

    override var valueTag = 0L
    var formulaTag = 0L

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

    fun setFormula(value: String, modificationToken: ModificationToken) {
        if (value != rawFormula) {
            rawFormula = value
            reparse()
            formulaTag = modificationToken.tag
            modificationToken.formulaChanged = true
            modificationToken.addRefresh(this)
        }
    }


    fun reparse() {
        clearDependsOn()
        expression.detachAll()
        expression = if (rawFormula.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = TcFormulaParser.parseExpression(rawFormula.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                e.printStackTrace()
                Literal(e)
            }
        } else {
            when (rawFormula.lowercase()) {
                "true" -> Literal(true)
                "false" -> Literal(false)
                else -> {
                    try {
                        Literal(Values.parseNumber(rawFormula))
                    } catch (e: Exception) {
                        Literal(rawFormula)
                    }
                }
            }
        }
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