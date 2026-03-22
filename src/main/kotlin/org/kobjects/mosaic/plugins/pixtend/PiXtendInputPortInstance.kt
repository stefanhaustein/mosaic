package org.kobjects.mosaic.plugins.pixtend

import org.kobjects.mosaic.model.integration.InputPortInstance
import org.kobjects.mosaic.model.integration.InputPortListener
import org.kobjects.mosaic.model.ModificationToken

abstract class PiXtendInputPortInstance(
    val integration: PiXtendIntegration,
    val listener: InputPortListener) : InputPortInstance {

    var lastValue: Any? = null

    fun syncState(token: ModificationToken) {
        val newValue = value
        if (newValue != lastValue) {
            listener.portValueChanged(token, newValue)
            lastValue = newValue
        }
    }

}