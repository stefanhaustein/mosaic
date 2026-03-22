package org.kobjects.mosaic.model.integration

interface InputPortInstance {
    val value: Any

    fun detach()

}