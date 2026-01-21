package org.kobjects.mosaic.pluginapi

interface InputPortInstance {
    val value: Any

    fun detach()

}