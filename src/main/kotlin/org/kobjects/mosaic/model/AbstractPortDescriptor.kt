package org.kobjects.mosaic.model

abstract class AbstractPortDescriptor(
    namespace: Namespace?,
    kind: DescriptorKind,
    name: String,
    type: Type?,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
    nameTemplate: String?,
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
    nameTemplate = nameTemplate
)