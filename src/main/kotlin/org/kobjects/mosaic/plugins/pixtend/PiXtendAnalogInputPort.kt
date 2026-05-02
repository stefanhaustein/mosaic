package org.kobjects.mosaic.plugins.pixtend

import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.InputPortListener
import org.kobjects.mosaic.model.integration.InputPortDescriptor

class PiXtendAnalogInputPort(
    integration: PiXtendIntegration,
    val index: Int,
    listener: InputPortListener
) : PiXtendInputPortInstance(integration, listener) {

    override val value: Any
        get() = integration.driver?.getAnalogIn(index) ?: Unit

    override fun detach() {
        integration.inputPorts.remove(this)
    }

    companion object {
        fun spec(integration: PiXtendIntegration): InputPortDescriptor = InputPortDescriptor(
            integration,
            "ain",
            Type.REAL,
            "PiXtend analog input.",
            listOf(ParameterSpec("index", Type.INT, 0))
        ) { config, listener ->
            PiXtendAnalogInputPort(integration, config["index"] as Int, listener).apply {
                integration.inputPorts.add(
                    this
                )
            }
        }
    }
}