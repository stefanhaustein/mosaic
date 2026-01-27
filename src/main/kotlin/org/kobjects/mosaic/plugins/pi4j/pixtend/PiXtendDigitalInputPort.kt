package org.kobjects.mosaic.plugins.pi4j.pixtend

import org.kobjects.mosaic.pluginapi.*

class PiXtendDigitalInputPort(
    integration: PiXtendIntegration,
    val index: Int,
    listener: InputPortListener
) : PiXtendInputPortInstance(integration, listener) {

    override val value: Any
        get() = integration.driver?.getDigitalIn(index) ?: Unit

    override fun detach() {
        integration.inputPorts.remove(this)
    }

    companion object {
        fun spec(integration: PiXtendIntegration): InputPortSpec = InputPortSpec(
            null,
            "PiXtend",
            "pixt.din",
            Type.BOOL,
            "PiXtend digital input.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) { config, listener ->
            PiXtendDigitalInputPort(integration, config["index"] as Int, listener).apply { integration.inputPorts.add(this) }
        }
    }
}