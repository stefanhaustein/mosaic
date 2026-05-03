package org.kobjects.mosaic.plugins.rpi

import com.pi4j.Pi4J
import com.pi4j.context.Context
import org.kobjects.mosaic.model.AbstractDescriptor
import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationDescriptor
import org.kobjects.mosaic.plugins.rpi.devices.Bmp280Port
import org.kobjects.mosaic.plugins.rpi.devices.Scd4xPort

class RpiIntegration(
    val model: ModelInterface,
) : Integration("rpi", "rpi") {
    var pi4j: Context? = null
    var error: Throwable? = null

    init {
        reInit()
    }


    fun reInit() {

        try {
            pi4j?.shutdown()
            pi4j = Pi4J.newAutoContext()
        } catch (e: Throwable) {
            pi4j = null
            error = e
        }
    }

    override val portDescriptors = listOf<AbstractPortDescriptor>(
        DigitalInputPort.spec(this),
        PwmInput.spec(this),
        DigitalOutputPort.spec(this),
        TextLcd.spec(this),
        Bmp280Port.spec(this),
        Scd4xPort.spec(this),
    //    PiXtendIntegration.spec(this),
    ).associateBy { it.name }

    override fun detach(token: ModificationToken) {

    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {

    }

    companion object {
        fun spec(model: ModelInterface) = IntegrationDescriptor(
            "",
            "rpi",
            "Raspberry Pi GPIO integration",
            emptyList(),
            setOf(AbstractDescriptor.Modifier.SINGLETON)

        ) { _ -> RpiIntegration(model)
        }
    }

}