package org.kobjects.mosaic.plugins.pixtend

import com.pi4j.drivers.plc.pixtend.PiXtendDriver
import org.kobjects.mosaic.pluginapi.*

class PiXtendGpioDigitalOutputPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {

    init {
        integration.driver?.setGpioMode(index, PiXtendDriver.GpioMode.DIGITAL_OUTPUT)
    }

    override fun setValue(value: Any?) {
        integration.driver?.setGpioOut(index, value as Boolean)
    }

    override fun detach() {

    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            integration,
            "PiXtend",
            "gpio_dout",
       //     Type.REAL,
            "PiXtend GPIO configured as digital output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendGpioDigitalOutputPort(integration, it["index"] as Int)
        }
    }
}