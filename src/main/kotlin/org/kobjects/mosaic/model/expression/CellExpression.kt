package org.kobjects.mosaic.model.expression;

import org.kobjects.mosaic.model.Cell

class CellExpression(
    owner: Cell,
    val target: Cell
) : Expression() {

    init {
        owner.inputs.add(target)
        target.outputs.add(owner)
    }

    override fun eval(context: EvaluationContext): Any? {
        try {
            return target.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()

}
