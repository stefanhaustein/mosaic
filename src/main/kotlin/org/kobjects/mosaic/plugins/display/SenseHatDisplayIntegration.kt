package org.kobjects.mosaic.plugins.display

import com.pi4j.drivers.hat.raspberry.SenseHat
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.integration.IntegrationDescriptor

class SenseHatDisplayIntegration(
    private val model: ModelInterface,
    name: String
) : AbstractDisplayIntegration(INTEGRATION_NAME, name) {
    var senseHat: SenseHat? = null

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {
        val hat = SenseHat(Model.pi4J)
        senseHat = hat
        configureDisplay(hat.display)
    }

    override fun detach(token: ModificationToken) {
        senseHat?.display?.close()
        senseHat = null
    }

    companion object {
        const val INTEGRATION_NAME = "SenseHatDisplay"

        fun descriptor(model: ModelInterface) = IntegrationDescriptor(
            category = "IoT",
            name = INTEGRATION_NAME,
            description = "Sense Hat 8x8 RGB Matrix",
            parameters = emptyList(),
            modifiers = emptySet(),
        ) { name ->
            SenseHatDisplayIntegration(model, name)
        }
    }
}