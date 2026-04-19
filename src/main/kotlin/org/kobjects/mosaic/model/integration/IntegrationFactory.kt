package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.OperationKind
import org.kobjects.mosaic.model.ParameterSpec

class IntegrationFactory(
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (kind: String, name: String, tag: Long) -> Integration,
) : AbstractArtifactSpec(
    null,
    category,
    OperationKind.INTEGRATION,
    null,
    name,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
)