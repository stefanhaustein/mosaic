package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractPortFactorySpec
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.OperationKind
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type

class InputPortSpec(
    namespace: Namespace?,
    category: String,
    name: String,
    type: Type,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (configuration: Map<String, Any?>, listener: InputPortListener) -> InputPortInstance,
) : AbstractPortFactorySpec(
    namespace,
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