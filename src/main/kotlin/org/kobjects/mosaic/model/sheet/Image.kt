package org.kobjects.mosaic.model.sheet

import org.kobjects.mosaic.model.expression.Expression
import org.kobjects.mosaic.model.expression.Literal
import org.kobjects.mosaic.model.parser.ParsingContext
import org.kobjects.mosaic.model.parser.TcFormulaParser

class Image(
    val cell: Cell,
    val rawCondition: String? = null,
    val image: String? = null,
    val rotation: Int? = null,
    val color: String? = null,
    val background: String? = null,
) {
    var parsedCondition: Expression = Literal(Unit)

    fun reparse(parsingContext: ParsingContext) {
        parsedCondition.detachAll()
        parsedCondition = TcFormulaParser.parseNodeExpression(
            if (rawCondition.isNullOrBlank()) "true" else rawCondition,
            parsingContext)
    }
}