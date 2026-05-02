package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.OperationKind
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type

class OutputPortDescriptor(
    namespace: Namespace?,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    val createFn: (configuration: Map<String, Any?>) -> OutputPortInstance,
) : AbstractPortDescriptor(
    namespace,
    OperationKind.OUTPUT_PORT,
    name,
    null,
    description,
    parameters,
    modifiers,
) {
    companion object {
        fun createUninstantiable(
            namespace: Namespace?,
            name: String,
            description: String,
        ) = OutputPortDescriptor(
            namespace,
            name,
            description,
            parameters = emptyList(),
            modifiers = setOf(Modifier.UNINSTANTIABLE),
            createFn = { configuration -> throw UnsupportedOperationException("$name is Uninstantiable (configuration: $configuration)") })
    }
}