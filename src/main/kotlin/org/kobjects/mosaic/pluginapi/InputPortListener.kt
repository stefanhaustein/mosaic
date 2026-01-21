package org.kobjects.mosaic.pluginapi

interface InputPortListener {
    fun portValueChanged(token: ModificationToken, newValue: Any?)
}