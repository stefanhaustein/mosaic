package org.kobjects.mosaic.plugins.gpio

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.InputPortInstance
import org.kobjects.mosaic.model.integration.InputPortListener
import org.kobjects.mosaic.model.integration.InputPortDescriptor

class DigitalInputPort(
    listener: InputPortListener,
    gpio: GpioIntegration,
    address: Int
) : InputPortInstance(listener), DigitalStateChangeListener {

    val digitalInput: DigitalInput = gpio.pi4j!!.create(
        DigitalInputConfig.newBuilder(gpio.pi4j).bcm(address).build())

    init {
        digitalInput.addListener(this)
    }

    override val initialValue: Boolean = digitalInput.isHigh

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        listener.portValueChanged(event?.state()?.isHigh ?: false)
    }

    override fun detach() {
        digitalInput.removeListener(this)
        digitalInput.close()
    }

    companion object {
        fun descriptor(plugin: GpioIntegration) = InputPortDescriptor(
            namespace = plugin,
            name = "Digital Input",
            type = Type.BOOL,
            description = "Configures the given pin bcm address for digital input and reports a high value as TRUE and a low value as FALSE.",
            parameters = listOf(ParameterSpec("address", Type.INT, 1)),
            modifiers = setOf(),
            nameTemplate = "din_{address}"
        ) { config, listener -> DigitalInputPort(listener, plugin, config["address"] as Int) }
    }
}