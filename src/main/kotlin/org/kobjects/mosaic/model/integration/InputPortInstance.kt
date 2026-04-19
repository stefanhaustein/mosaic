package org.kobjects.mosaic.model.integration

abstract class InputPortInstance(
    val listener: InputPortListener
) {
    abstract fun detach()
}