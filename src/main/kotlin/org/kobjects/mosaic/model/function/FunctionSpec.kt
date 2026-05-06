package org.kobjects.mosaic.model.function

import org.kobjects.mosaic.model.AbstractDescriptor
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.DescriptorKind
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type

open class FunctionSpec(
    namespace: Namespace?,
    category: String,
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    kind: DescriptorKind = DescriptorKind.FUNCTION,
    val createFn: (configuration: Map<String, Any?>) -> FunctionInstance,
) : AbstractDescriptor(
    namespace,
    category,
    kind,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
    nameTemplate = null,
)