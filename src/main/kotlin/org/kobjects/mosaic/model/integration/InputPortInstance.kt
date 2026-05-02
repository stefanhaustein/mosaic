package org.kobjects.mosaic.model.integration

abstract class InputPortInstance(
    val listener: InputPortListener
) {
    open fun detach() {}
}