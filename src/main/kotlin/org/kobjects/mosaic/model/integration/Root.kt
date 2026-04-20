package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken

class Root : Integration(
    "Root",
    "root",
    0
) {
    override val portFactories = listOf(
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

    override fun close() {

    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {

    }

    companion object {
        fun spec(model: ModelInterface) = IntegrationFactory(
            "", "Root", "", emptyList()) {
            _, _, _ -> throw UnsupportedOperationException()

        }
    }
}