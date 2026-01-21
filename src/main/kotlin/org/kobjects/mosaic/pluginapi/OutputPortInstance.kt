package org.kobjects.mosaic.pluginapi

interface OutputPortInstance {
    fun setValue(value: Any?)

    fun detach()
}