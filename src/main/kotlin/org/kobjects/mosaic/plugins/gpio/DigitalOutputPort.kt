package org.kobjects.mosaic.plugins.gpio


import com.pi4j.io.gpio.digital.*
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.OutputPortInstance
import org.kobjects.mosaic.model.integration.OutputPortDescriptor

class DigitalOutputPort(
    plugin: GpioIntegration,
    address: Int
) : OutputPortInstance {

    val digitalOutput = plugin.pi4j!!.create(DigitalOutputConfig.newBuilder(plugin.pi4j).bcm(address).build())

    override fun setValue(value: Any?) {
        val value = when(val raw = value) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw;")
        }
        digitalOutput.setState(value)
    }

    override fun detach() {
        digitalOutput.close()
    }


    companion object {
        fun descriptor(plugin: GpioIntegration) = OutputPortDescriptor(
            null,
            "Digital Output",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(ParameterSpec("address", Type.INT, 1)),
            nameTemplate = "dout_{address}",
        ) { DigitalOutputPort(plugin, it["address"] as Int) }
    }
}