package org.kobjects.mosaic.plugins.display

import org.kobjects.mosaic.model.integration.OutputPortDescriptor
import org.kobjects.mosaic.model.integration.OutputPortInstance

class TextDisplayPort(val display: AbstractDisplayIntegration) : OutputPortInstance {
    override fun setValue(value: Any?) {
        val display = display.display ?: return
        val graphics = display.graphics
        graphics.setColor(0x0ff000000.toInt())
        graphics.fillRect(0, 0, display.width, display.height)
        graphics.setColor(0x00ffffffff.toInt())
        graphics.renderText(0, 8, value.toString())
    }

    companion object {
        fun descriptor(namespace: AbstractDisplayIntegration) = OutputPortDescriptor.createUninstantiable(
            namespace,
            "TextDisplay",
            "Renders a cell or a range of cells"
        )
    }
}