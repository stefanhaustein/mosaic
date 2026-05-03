package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractDescriptor
import org.kobjects.mosaic.model.OperationKind
import org.kobjects.mosaic.model.ParameterSpec

class IntegrationDescriptor(
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (name: String) -> Integration,
) : AbstractDescriptor(
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