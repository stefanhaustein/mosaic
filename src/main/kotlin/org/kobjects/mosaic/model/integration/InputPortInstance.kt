package org.kobjects.mosaic.model.integration

abstract class InputPortInstance(
    val listener: InputPortListener
) {
    open val initialValue: Any? = null

    open fun detach() {}
}