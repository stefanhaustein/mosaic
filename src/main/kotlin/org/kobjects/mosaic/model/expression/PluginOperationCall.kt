package org.kobjects.mosaic.model.expression

import org.kobjects.mosaic.model.Cell
import org.kobjects.mosaic.pluginapi.ModificationToken
import org.kobjects.mosaic.pluginapi.*

class PluginOperationCall(
    val owner: Cell,
    operationSpec: FunctionSpec,
    val configuration: Map<String, Any?>,
    val parameters: Map<String, Pair<Expression, Type>>

): Expression(), ValueChangeListener {

    override val children: Collection<Expression>
        get() = parameters.values.map { it.first }

    val operation = operationSpec.createFn(configuration) as FunctionInstance

    override fun attach() {
        if (operation is StatefulFunctionInstance) {
            operation.attach(this)
        }
    }

    override fun detach() {
        if (operation is StatefulFunctionInstance) {
            operation.detach()
        }
    }

    override fun notifyValueChanged(token: ModificationToken) {
        token.addRefresh(owner)
    }

    override fun eval(context: EvaluationContext): Any? {
        return operation.apply(context, parameters.mapValues {
            val expr = it.value.first
            when (it.value.second) {
                Type.INT -> expr.evalInt(context)
                Type.REAL -> expr.evalDouble(context)
                Type.BOOL -> expr.evalBoolean(context)
                Type.STRING -> expr.eval(context).toString()
                else -> expr.eval(context)
            }
        })
    }


    companion object {
        fun create(expressionHolder: Cell, operationSpec: FunctionSpec, parameters: Map<String, Expression>): PluginOperationCall {
            val mappedConfig = mutableMapOf<String, Any?>()
            val mappedParameters = mutableMapOf<String, Pair<Expression, Type>>()
            for ((index, specParam) in operationSpec.parameters.withIndex()) {
                val actualParameter = parameters[specParam.name] ?: parameters["$index"]
                if (actualParameter != null) {
                    if (specParam.modifiers.contains(ParameterSpec.Modifier.CONSTANT)) {
                        require(actualParameter is Literal) { "Literal expression required for configuration parameter ${specParam.name}" }
                        mappedConfig[specParam.name] = actualParameter.value
                    } else if (specParam.modifiers.contains(ParameterSpec.Modifier.REFERENCE)) {
                        mappedConfig[specParam.name] = actualParameter
                    } else {
                        mappedParameters[specParam.name] = actualParameter to specParam.type
                    }
                } else if (!specParam.modifiers.contains(ParameterSpec.Modifier.OPTIONAL)) {
                    throw IllegalStateException("Parameter '${specParam.name}' not found in $parameters")
                }

            }
            return PluginOperationCall(expressionHolder, operationSpec, mappedConfig, mappedParameters)
        }
    }

    override fun toString() = "$operation configuration: $configuration parameters: $parameters cell: $owner"

}