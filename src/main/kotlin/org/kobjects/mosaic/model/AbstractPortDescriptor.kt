package org.kobjects.mosaic.model

abstract class AbstractPortDescriptor(
    namespace: Namespace?,
    kind: OperationKind,
    name: String,
    type: Type?,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
) : AbstractDescriptor(
    namespace,
    category = "",
    kind,
    type,
    name,
    description,
    parameters,
    modifiers,
    tag = 0,
    displayName = null,
)