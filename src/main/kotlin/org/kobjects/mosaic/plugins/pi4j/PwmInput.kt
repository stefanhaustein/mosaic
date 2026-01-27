package org.kobjects.mosaic.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.mosaic.pluginapi.*

class PwmInput(
    val host: InputPortListener,
    val plugin: Pi4jPlugin,
    val address: Int
) : InputPortInstance, DigitalStateChangeListener {

    val digitalInput: DigitalInput = plugin.pi4j!!.create(DigitalInputConfig.newBuilder(plugin.pi4j).address(address).build())
    var t0: Long = 0
    override var value: Double = 0.0

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        when (event!!.state().isHigh()) {
            true -> {
                t0 = System.currentTimeMillis()
            }
            false -> {
                val newValue = (System.currentTimeMillis() - t0) / 1000.0
                if (newValue != value && t0 != 0L) {
                    value = newValue
                    plugin.model.setPortValue(host, newValue)
                }
            }
        }
    }

    override fun detach() {
        digitalInput.removeListener(this)
        try {
            plugin.pi4j!!.shutdown(digitalInput.getId())
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    companion object {
        fun spec(plugin: Pi4jPlugin) = InputPortSpec(
            null,
            category = "GPIO",
            "pwmin",
            Type.REAL,
            "Configures the given pin address for input and reports the pulse width in seconds.",
            listOf(ParameterSpec("address", Type.INT, null, setOf(ParameterSpec.Modifier.CONSTANT))),
            createFn = { config, host ->
                PwmInput(host, plugin, config["address"] as Int)
            },
        )
    }

}