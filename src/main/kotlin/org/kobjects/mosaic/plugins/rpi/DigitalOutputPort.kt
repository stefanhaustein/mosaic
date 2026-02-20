package org.kobjects.mosaic.plugins.rpi


import com.pi4j.io.gpio.digital.*
import org.kobjects.mosaic.pluginapi.*

class DigitalOutputPort(
    val plugin: RpiIntegration,
    val address: Int
)  : OutputPortInstance {

    val digitalOutput= plugin.pi4j!!.create(DigitalOutputConfig.newBuilder(plugin.pi4j).address(address).build())



    override fun setValue(value: Any?) {
        val value = when(val raw = value) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw;")
        }
        digitalOutput.setState(value)
    }

    override fun detach() {
        try {
            plugin.pi4j!!.shutdown(digitalOutput.getId())
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }


    companion object {
        fun spec(plugin: RpiIntegration) = OutputPortSpec(
            null,
            "GPIO",
            "dout",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(ParameterSpec("address", Type.INT, null, setOf(ParameterSpec.Modifier.CONSTANT))),
        ) { DigitalOutputPort(plugin, it["address"] as Int) }
    }
}