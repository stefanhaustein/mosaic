package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.OperationKind
import org.kobjects.mosaic.model.ParameterSpec

class OutputPortDescriptor(
    namespace: Namespace?,
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (configuration: Map<String, Any?>) -> OutputPortInstance,
) : AbstractPortDescriptor(
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