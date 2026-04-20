package org.kobjects.mosaic.plugins.rpi

import com.pi4j.Pi4J
import com.pi4j.context.Context
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractPortFactorySpec
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationFactory
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

    override val portFactories = listOf<AbstractPortFactorySpec>(
        DigitalInputPort.spec(this),
        PwmInput.spec(this),
        DigitalOutputPort.spec(this),
        TextLcd.spec(this),
        Bmp280Port.spec(this),
        Scd4xPort.spec(this),
    //    PiXtendIntegration.spec(this),
    )

    override fun close() {

    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {

    }

    companion object {
        fun spec(model: ModelInterface) = IntegrationFactory(
            "",
            "rpi",
            "Raspberry Pi GPIO integration",
            emptyList(),
            setOf(AbstractArtifactSpec.Modifier.SINGLETON)

        ) { _ -> RpiIntegration(model)
        }
    }

}