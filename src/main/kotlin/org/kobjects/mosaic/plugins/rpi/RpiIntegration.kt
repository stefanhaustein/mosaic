package org.kobjects.mosaic.plugins.rpi

import com.pi4j.Pi4J
import com.pi4j.context.Context
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.plugins.rpi.devices.Bmp280Port
import org.kobjects.mosaic.plugins.rpi.devices.Scd4xPort

class RpiIntegration(
    val model: ModelInterface,
    tag: Long
) : Integration("rpi", "rpi", tag) {
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

    override val operationSpecs = listOf<AbstractArtifactSpec>(
        DigitalInputPort.spec(this),
        PwmInput.spec(this),
        DigitalOutputPort.spec(this),
        TextLcd.spec(this),
        Bmp280Port.spec(this),
        Scd4xPort.spec(this),
    //    PiXtendIntegration.spec(this),
    )
    override val configuration: Map<String, Any?>
        get() = emptyMap()

    override fun detach() {

    }

    override fun reconfigure(configuration: Map<String, Any?>) {

    }

    companion object {
        fun spec(model: ModelInterface) = IntegrationFactory(
            "",
            "rpi",
            "Raspberry Pi GPIO integration",
            emptyList(),
            setOf(AbstractArtifactSpec.Modifier.SINGLETON)
            
        ) { _, _, tag, _ ->
            RpiIntegration(model, tag)
        }
    }

}