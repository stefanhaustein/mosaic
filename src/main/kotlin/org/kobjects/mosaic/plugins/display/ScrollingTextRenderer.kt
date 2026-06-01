package org.kobjects.mosaic.plugins.display

import com.pi4j.drivers.display.BitmapFont
import com.pi4j.drivers.display.graphics.GraphicsDisplay
import java.util.Timer
import java.util.TimerTask

class ScrollingTextRenderer(
    val display: GraphicsDisplay,
    val x: Int,
    val y: Int,
    val width: Int,
    val backgroundColor: Int,
    val foregroundColor: Int,
    val font: BitmapFont,
    val text: String,
) {
    val graphics = display.getGraphics()
    val timer = Timer()
    var offset = 0;
    var closed = false

    init {
        render()
    }

    fun render() {
        //        graphics.setClip(x, y, width, font.cellHeight)
        graphics.setColor(backgroundColor)
        graphics.fillRect(x, y, width, font.cellHeight)
        graphics.setColor(foregroundColor)
        val textWidth = graphics.renderText(x + offset, y + font.cellHeight, text)

        if (!closed && textWidth > width) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (--offset < -textWidth - width) {
                        offset = width
                    }
                    render()
                }
            }, 50)
        }
    }

    fun close() {
        closed = true
        timer.cancel()
        timer.purge()
    }


}