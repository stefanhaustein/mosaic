package org.kobjects.mosaic.model.expression;

import org.kobjects.mosaic.model.*
import org.kobjects.mosaic.model.integration.PortNode

class PortExpression(
    val owner: Node,
    val port: PortNode

) : Expression() {

    init {
        owner.inputs.add(port)
        port.outputs.add(owner)
    }

    override fun eval(context: EvaluationContext): Any? {
        try {
            return owner.owner.model.values[port]?.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()


}
