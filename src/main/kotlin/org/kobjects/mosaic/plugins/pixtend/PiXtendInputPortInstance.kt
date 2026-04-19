package org.kobjects.mosaic.plugins.pixtend

import org.kobjects.mosaic.model.integration.InputPortInstance
import org.kobjects.mosaic.model.integration.InputPortListener
import org.kobjects.mosaic.model.ModificationToken

abstract class PiXtendInputPortInstance(
    val integration: PiXtendIntegration,
    listener: InputPortListener) : InputPortInstance(listener) {

    abstract val value: Any
    var lastValue: Any? = null

    fun syncState(token: ModificationToken) {
        val newValue = value
        if (newValue != lastValue) {
            listener.portValueChanged(newValue, token)
            lastValue = newValue
        }
    }

}