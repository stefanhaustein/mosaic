package org.kobjects.mosaic.plugins.pi4j.pixtend

import org.kobjects.mosaic.pluginapi.*

class PiXtendRelayPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {


    override fun setValue(value: Any?) {
        integration.driver?.setRelay(index, value as Boolean)
    }

    override fun detach() {

    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            "PiXtend",
            "pixt.relay",
       //     Type.REAL,
            "PiXtend relay.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendDigitalOutputPort(integration, it["index"] as Int)
        }
    }
}