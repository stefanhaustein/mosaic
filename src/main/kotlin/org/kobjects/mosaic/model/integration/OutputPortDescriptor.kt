package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.DescriptorKind
import org.kobjects.mosaic.model.ParameterSpec

class OutputPortDescriptor(
    namespace: Namespace?,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    nameTemplate: String? = null,
    val createFn: (configuration: Map<String, Any?>) -> OutputPortInstance,
) : AbstractPortDescriptor(
    namespace = namespace,
    kind = DescriptorKind.OUTPUT_PORT,
    name = name,
    type =null,
    description = description,
    parameters = parameters,
    modifiers = modifiers,
    nameTemplate = nameTemplate,
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