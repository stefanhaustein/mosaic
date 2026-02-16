package org.kobjects.mosaic.model.expression;

import org.kobjects.mosaic.model.Cell
import org.kobjects.mosaic.model.CellRangeReference
import org.kobjects.mosaic.model.CellRangeValues
import org.kobjects.mosaic.model.ExpressionNode

class CellRangeExpression(
    owner: ExpressionNode,
    val target: CellRangeReference
) : Expression() {

    init {
        // All dependencies are removed on re-parse. This can't be "balanced" as
        // an expression might reference the same cells multiple times
        for (t in target.iterator()) {
            owner.inputs.add(t)
            t.outputs.add(owner)
        }
    }

    override fun eval(context: EvaluationContext): Any {
        try {
            return CellRangeValues(target)
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()

}
