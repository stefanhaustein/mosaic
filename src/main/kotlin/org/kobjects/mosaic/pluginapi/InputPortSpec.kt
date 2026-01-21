package org.kobjects.mosaic.pluginapi

class InputPortSpec(
    category: String,
    name: String,
    type: Type,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (configuration: Map<String, Any?>, listener: InputPortListener) -> InputPortInstance,
) : AbstractFactorySpec(
    category,
    OperationKind.INPUT_PORT,
    name,
    type,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
)