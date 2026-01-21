package org.kobjects.mosaic.plugins.homeassistant

import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec
import org.kobjects.mosaic.pluginapi.ModelInterface
import org.kobjects.mosaic.pluginapi.Plugin

class HomeAssistantPlugin(val model: ModelInterface) : Plugin {
    override val operationSpecs = listOf<AbstractArtifactSpec>(
        HomeAssistantIntegration.spec(model)
    )
}