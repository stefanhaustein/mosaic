package org.kobjects.mosaic.plugins.display

import com.pi4j.drivers.display.BitmapFont
import org.kobjects.mosaic.model.integration.OutputPortDescriptor
import org.kobjects.mosaic.model.integration.OutputPortInstance

class TextDisplayPort(val display: AbstractDisplayIntegration) : OutputPortInstance {

    var renderer: ScrollingTextRenderer? = null
    var detached = false

    override fun setValue(value: Any?) {
        val display = display.display ?: return

        if (renderer != null) {
            renderer?.close()
        }

        if (!detached) {
            renderer = ScrollingTextRenderer(
                display, 0, 0, 8, 0xff000000.toInt(), 0xffffffff.toInt(),
                BitmapFont.get5x8Font(), value?.toString() ?: "(null)"
            )
        }
    }

    override fun detach() {
        detached = true
        renderer?.close()
    }

    companion object {
        fun descriptor(namespace: AbstractDisplayIntegration) = OutputPortDescriptor.createUninstantiable(
            namespace,
            "TextDisplay",
            "Renders a cell or a range of cells"
        )
    }
}