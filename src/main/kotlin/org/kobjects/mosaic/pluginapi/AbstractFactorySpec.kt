package org.kobjects.mosaic.pluginapi

abstract class AbstractFactorySpec(
    category: String,
    kind: OperationKind,
    name: String,
    type: Type?,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
    tag: Long,
    displayName: String?,
) : AbstractArtifactSpec(
    category,
    kind,
    type,
    name,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
)