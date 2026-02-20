package org.kobjects.mosaic.plugins.rpi.devices

import com.pi4j.io.i2c.I2C
import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver
// import org.kobjects.pi4jdriver.sensor.bmx280.Bmx280Driver
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.plugins.rpi.RpiIntegration
import java.util.*

class Bmp280Port(
    val host: InputPortListener,
    val plugin: RpiIntegration,
    bus: Int,
    address: Int

) : InputPortInstance {

    val i2c = plugin.pi4j!!.create(
        I2C.newConfigBuilder(plugin.pi4j)
            .bus(bus)
            .device(address)
            .provider("linuxfs-i2c")
            .build()
    )
    var bmp280 = Bmx280Driver(i2c)
    val timer = Timer().apply {
        schedule(object : TimerTask() {
            override fun run() {
                poll()
            }
        }, 0, 10000)
    }

    override var value = emptyMap<String, Double?>()


    fun poll() {
        val measurement = bmp280.readMeasurement()
        value = mapOf(
            "temperature" to measurement?.getTemperature()?.toDouble(),
            "pressure" to measurement?.getPressure()?.toDouble(),
            "humidity" to measurement?.getHumidity()?.toDouble(),
        )
        plugin.model.setPortValue(host, value)
    }

    override fun detach() {
        i2c.close()
        timer.cancel()
    }


    companion object {
        val TYPE = Type.Struct(listOf(
            Type.Field("temperature", Type.REAL),
            Type.Field("pressure", Type.REAL),
            Type.Field("humidity", Type.REAL),
        ))

        fun spec(plugin: RpiIntegration) = InputPortSpec(
            null,
            category = "Driver",
            "Bmp280",
            TYPE,
            "BMP 280 sensor port.",
            listOf(
                ParameterSpec(
                    "bus",
                    Type.INT,
                    1,
                    setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)
                ),
                ParameterSpec(
                    "address",
                    Type.INT,
                    0x77,
                    setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)
                )),
            createFn = { config, host ->
                Bmp280Port(host, plugin, config["bus"] as? Int ?: 1, config ["address"] as? Int ?:  0x77) },
        )
    }
}