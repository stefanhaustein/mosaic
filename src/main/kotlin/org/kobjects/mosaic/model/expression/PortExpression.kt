package org.kobjects.mosaic.model.expression;

import org.kobjects.mosaic.model.*

class PortExpression(
    owner: Cell,
    val port: PortHolder

) : Expression() {

    init {
        owner.inputs.add(port)
        port.outputs.add(owner)
    }

    override fun eval(context: EvaluationContext): Any? {
        try {
            return port.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()


}
