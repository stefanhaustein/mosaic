package org.kobjects.mosaic.plugins.pixtend

import org.kobjects.mosaic.pluginapi.*

class PiXtendDigitalOutputPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {


    override fun setValue(value: Any?) {
        integration.driver?.setDigitalOut(index, value as Boolean)
    }

    override fun detach() {

    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            integration,
            "PiXtend",
            "dout",
       //     Type.REAL,
            "PiXtend digital output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendDigitalOutputPort(integration, it["index"] as Int)
        }
    }
}