package org.kobjects.mosaic.plugins.pi4j


import com.pi4j.io.i2c.I2C
import com.pi4j.drivers.display.character.hd44780.Hd44780Driver
import org.kobjects.mosaic.pluginapi.*
import kotlin.math.min

class TextLcd(
    val plugin: Pi4jPlugin,
    bus: Int,
    address: Int,
    val width: Int,
    val height: Int,
    val format: List<Int?>?,
)  : OutputPortInstance {

    val i2c: I2C = plugin.pi4j!!.create(
        I2C.newConfigBuilder(plugin.pi4j)
            .bus(bus)
            .device(address)
            .provider("linuxfs-i2c")
            .build()
    )
    val lcdDriver = Hd44780Driver.withPcf8574Connection(i2c, width, height)

    override fun setValue(value: Any?) {

        val range = value as RangeValues
        for (row in 0 until min(range.height, height)) {
            var sb = StringBuilder()
            for (col in 0 until range.width) {
                val columnWidth = format?.getOrNull(col) ?: ((width - sb.length) / (range.width - col))
                val value = range[col, row]
                val s = if (value is Number)
                    Format.formatNumber(value.toDouble(), columnWidth).padStart(columnWidth)
                    else value.toString().take(columnWidth).padEnd(columnWidth)
                sb.append(s)
            }
            lcdDriver.setCursorPosition(0, row)
            lcdDriver.write(sb.toString())
        }
    }


    override fun detach() {

          //  lcdDriver.close()
    }


    companion object {
        fun spec(plugin: Pi4jPlugin) = OutputPortSpec(
            "Drivers",
            "Lcd",
            "An LCD text display",
            listOf(
                ParameterSpec(
                    "bus",
                    Type.INT,
                    1
                ),
                ParameterSpec(
                    "address",
                    Type.INT,
                    0x27
                ),
                ParameterSpec(
                    "width",
                    Type.INT,
                    16
                ),
                ParameterSpec(
                    "height",
                    Type.INT,
                    2
                ),
                ParameterSpec(
                    "format",
                    Type.STRING,
                    null,
                    setOf(ParameterSpec.Modifier.OPTIONAL)
                )
            )
        ) {
            TextLcd(
                plugin,
                it["bus"] as Int,
                it["address"] as Int,
                it["width"] as Int,
                it["height"] as Int,
                (it["format"] as? String)?.split(",")?.map { it.toIntOrNull() })
        }
    }
}