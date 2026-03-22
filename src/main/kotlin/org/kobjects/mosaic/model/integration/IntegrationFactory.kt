package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractFactorySpec
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
    val createFn: (kind: String, name: String, tag: Long, initialConfiguration: Map<String, Any?>) -> Integration,
) : AbstractFactorySpec(
    null,
    category,
    OperationKind.INTEGRATION,
    name,
    null,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
)