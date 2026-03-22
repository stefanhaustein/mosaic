package org.kobjects.mosaic.model.integration

interface OutputPortInstance {
    fun setValue(value: Any?)

    fun detach()
}