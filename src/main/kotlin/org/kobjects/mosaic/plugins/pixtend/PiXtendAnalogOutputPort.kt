package org.kobjects.mosaic.plugins.pixtend

import io.ktor.http.parameters
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.OutputPortInstance
import org.kobjects.mosaic.model.integration.OutputPortDescriptor

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
        fun spec(integration: PiXtendIntegration) = OutputPortDescriptor(
            integration,
            "aout",
            //     Type.REAL,
            "PiXtend analog output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
        ) {
            PiXtendAnalogOutputPort(integration, it["index"] as Int)
        }
    }
}