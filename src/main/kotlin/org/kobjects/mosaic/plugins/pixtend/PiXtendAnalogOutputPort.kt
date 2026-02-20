package org.kobjects.mosaic.plugins.pixtend

import org.kobjects.mosaic.pluginapi.*

class PiXtendAnalogOutputPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {

    init {
        integration.driver?.setAnalogOutEnabled(index, true)
    }

    override fun setValue(value: Any?) {
        integration.driver?.setAnalogOut(index, value as Double)
    }

    override fun detach() {
        integration.driver?.setAnalogOutEnabled(index, false)
    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            integration,
            "PiXtend",
            "aout",
       //     Type.REAL,
            "PiXtend analog output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendAnalogOutputPort(integration, it["index"] as Int)
        }
    }
}