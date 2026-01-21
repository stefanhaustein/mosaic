package org.kobjects.mosaic.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.mosaic.pluginapi.*

class DigitalInputPort(
    val host: InputPortListener,
    val plugin: Pi4jPlugin,
    val address: Int
) : InputPortInstance, DigitalStateChangeListener {

    val digitalInput: DigitalInput = plugin.pi4j!!.create(
        DigitalInputConfig.newBuilder(plugin.pi4j).address(address).build())

    override var value: Boolean = digitalInput.isHigh


    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
            value = event?.state()?.isHigh ?: false
        plugin.model.setPortValue(host, value)
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
            category = "GPIO",
            "din",
            Type.BOOL,
            "Configures the given pin address for digital input and reports a high value as TRUE and a low value as FALSE.",
            listOf(ParameterSpec("address", Type.INT, null, setOf(ParameterSpec.Modifier.CONSTANT))),
            createFn = { config, listener -> DigitalInputPort(listener, plugin, config["address"] as Int) },
        )
    }
}