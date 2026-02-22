package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.Integration
import org.kobjects.mosaic.pluginapi.OutputPortInstance
import org.kobjects.mosaic.pluginapi.OutputPortSpec

class Root : Integration(
    "Root",
    "root",
    0
) {
    override val operationSpecs = listOf(
        OutputPortSpec(
            this,
            category = "",
            name = "NamedCell",
            description = "A named cell or range of cells",
            parameters = emptyList(),
            modifiers = emptySet(),
            tag = 0,
            createFn = { _ ->
                object : OutputPortInstance {
                    override fun setValue(value: Any?) {}
                    override fun detach() {}
                }
            }
        )
    )

    override val configuration = emptyMap<String, Any?>()

    override fun detach() {

    }

    override fun reconfigure(configuration: Map<String, Any?>) {

    }
}