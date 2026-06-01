package org.kobjects.mosaic.plugins.display

import com.pi4j.drivers.display.graphics.Graphics
import com.pi4j.drivers.display.graphics.GraphicsDisplay
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.OutputPortDescriptor
import org.kobjects.mosaic.model.integration.OutputPortNode


abstract class AbstractDisplayIntegration(
    integrationName: String, name: String
) : Integration(integrationName, name){
    var display: GraphicsDisplay? = null

    override val portDescriptors = listOf(
        OutputPortDescriptor.Companion.createUninstantiable(
            this,
            "TextDisplay",
            "",
        )).associateBy { it.name }


    fun configureDisplay(display: GraphicsDisplay) {
        this.display = display
        val portNode = OutputPortNode(
            this,
            "value",
            portDescriptors.values.first(),
            "",
            instance = TextDisplayPort(this))
        nodes["value"] = portNode
    }

}