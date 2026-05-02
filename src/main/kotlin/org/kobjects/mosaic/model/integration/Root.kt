package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken

class Root : Integration(
    "Root",
    "root",
) {
    override val portFactories = listOf(
        OutputPortDescriptor(
            this,
            name = "NamedCell",
            description = "A named cell or range of cells",
            parameters = emptyList()
        ) { _ ->
            object : OutputPortInstance {
                override fun setValue(value: Any?) {}
                override fun detach() {}
            }
        }
    ).associateBy { it.name }

    override fun detach(token: ModificationToken) {

    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {

    }

    companion object {
        fun spec(model: ModelInterface) = IntegrationFactory(
            "", "Root", "", emptyList()) {
            _ -> throw UnsupportedOperationException()

        }
    }
}