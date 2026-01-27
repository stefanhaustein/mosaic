package org.kobjects.mosaic.pluginapi

class PropertySpec (
    namespace: Namespace?,
    category: String,
    type: Type,
    name: String,
    description: String,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val setterCreateFn: (() -> OutputPortInstance)? = null,
    getterCreateFn: () -> FunctionInstance,
) : FunctionSpec(
    namespace,
    category,
    type,
    name,
    description,
    emptyList(),
    if (setterCreateFn == null) modifiers else modifiers + listOf(Modifier.SETTABLE),
    tag,
    displayName,
    kind = OperationKind.PROPERTY,
    createFn = {
        getterCreateFn()
    }
) {
    fun getOutputPortSpec() = OutputPortSpec(
        namespace,
        category,
        name = name,
        description = description,
        parameters = emptyList(),
        modifiers = setOf(AbstractArtifactSpec.Modifier.NO_SIMULATION),
        tag = tag,
        displayName = displayName,
        createFn = {
            setterCreateFn?.invoke() ?: throw IllegalStateException("Setter create fn missing")
        },
    )
}