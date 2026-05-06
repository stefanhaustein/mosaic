package org.kobjects.mosaic.plugins.gpio

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.InputPortInstance
import org.kobjects.mosaic.model.integration.InputPortListener
import org.kobjects.mosaic.model.integration.InputPortDescriptor

class PwmInput(
    listener: InputPortListener,
    plugin: GpioIntegration,
    address: Int
) : InputPortInstance(listener), DigitalStateChangeListener {

    val digitalInput: DigitalInput = plugin.pi4j!!.create(DigitalInputConfig.newBuilder(plugin.pi4j).bcm(address).build())
    var t0: Long = 0
    var value: Double = 0.0

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        when (event!!.state().isHigh()) {
            true -> {
                t0 = System.currentTimeMillis()
            }
            false -> {
                val newValue = (System.currentTimeMillis() - t0) / 1000.0
                if (newValue != value && t0 != 0L) {
                    listener.portValueChanged(newValue)
                }
            }
        }
    }

    override fun detach() {
        digitalInput.removeListener(this)
        digitalInput.close()
    }

    companion object {
        fun descriptor(plugin: GpioIntegration) = InputPortDescriptor(
            null,
            "PWM Input",
            Type.REAL,
            "Configures the given bcm pin address for input and reports the pulse width in seconds.",
            listOf(ParameterSpec("address", Type.INT, 1)),
            nameTemplate = "pwmin_{address}"
        ) { config, host ->
            PwmInput(host, plugin, config["address"] as Int)
        }
    }

}