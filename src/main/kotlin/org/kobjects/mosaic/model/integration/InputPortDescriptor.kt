package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.OperationKind
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type

class InputPortDescriptor(
    namespace: Namespace?,
    name: String,
    type: Type,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    val createFn: (configuration: Map<String, Any?>, listener: InputPortListener) -> InputPortInstance,
) : AbstractPortDescriptor(
    namespace,
    OperationKind.INPUT_PORT,
    name,
    type,
    description,
    parameters,
    modifiers,
) {
    companion object {
        fun createUninstantiable(
            namespace: Namespace?,
            name: String,
            type: Type,
            description: String,
        ) = InputPortDescriptor(
            namespace,
            name,
            type,
            description,
            parameters = emptyList(),
            modifiers = setOf(Modifier.UNINSTANTIABLE),
            createFn = { configuration, _ -> throw UnsupportedOperationException("$name is Uninstantiable (configuration: $configuration)") })
    }
}