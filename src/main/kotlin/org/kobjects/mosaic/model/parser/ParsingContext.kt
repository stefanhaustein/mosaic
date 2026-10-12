package org.kobjects.mosaic.model.parser

import org.kobjects.mosaic.model.ExpressionNode
import org.kobjects.mosaic.model.Model


class ParsingContext(
    val cell: ExpressionNode
) {
    val model: Model
    get() = cell.owner.model
}