package org.kobjects.mosaic.plugins.gpio

import com.pi4j.context.Context
import org.kobjects.mosaic.model.AbstractDescriptor
import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationDescriptor

class GpioIntegration(
    val model: ModelInterface,
) : Integration("GPIO", "gpio") {
    val pi4j: Context? = Model.pi4J

    override val portDescriptors = listOf<AbstractPortDescriptor>(
        DigitalInputPort.descriptor(this),
        PwmInput.descriptor(this),
        DigitalOutputPort.descriptor(this),
        TextLcd.descriptor(this),
    ).associateBy { it.name }

    override fun detach(token: ModificationToken) {
    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {
    }

    companion object {
        fun spec(model: ModelInterface) = IntegrationDescriptor(
            "",
            "GPIO",
            "GPIO integration",
            emptyList(),
            setOf(AbstractDescriptor.Modifier.SINGLETON)

        ) { _ -> GpioIntegration(model)
        }
    }

}