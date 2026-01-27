package org.kobjects.mosaic.pluginapi

class OutputPortSpec(
    namespace: Namespace?,
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (configuration: Map<String, Any?>) -> OutputPortInstance,
) : AbstractFactorySpec(
    namespace,
    category,
    OperationKind.OUTPUT_PORT,
    name,
    null,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
)