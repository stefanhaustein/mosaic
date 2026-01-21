package org.kobjects.mosaic.plugins.pi4j.devices

import com.pi4j.io.i2c.I2C
import com.pi4j.drivers.sensor.environment.scd4x.Scd4xDriver
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.plugins.pi4j.Pi4jPlugin
import java.util.*

class Scd4xPort(
    val host: InputPortListener,
    val plugin: Pi4jPlugin,
    bus: Int,

) : InputPortInstance {

    val i2c = plugin.pi4j!!.create(
        I2C.newConfigBuilder(plugin.pi4j)
            .bus(bus)
            .device(Scd4xDriver.I2C_ADDRESS)
            .provider("linuxfs-i2c")
            .build()
    )
    var scd4x = Scd4xDriver(i2c)

    val timer = Timer().apply {
        scd4x.safeInit()
        scd4x.startLowPowerPeriodicMeasurement()
        poll()

        schedule(object : TimerTask() {
            override fun run() {
                poll()
            }
        }, 0, 30000)
    }

    override var value = emptyMap<String, Double?>()


    fun poll() {
        val measurement = scd4x.readMeasurement()
        value = mapOf(
            "temperature" to measurement?.getTemperature()?.toDouble(),
            "co2" to measurement?.getCo2()?.toDouble(),
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

        fun spec(plugin: Pi4jPlugin) = InputPortSpec(
            category = "Driver",
            "Scd4x",
            TYPE,
            "SCD 4x sensor port.",
            listOf(
                ParameterSpec(
                    "bus",
                    Type.INT,
                    1,
                    setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)
                ),
            ),
            createFn = { config, host ->
                Scd4xPort(host, plugin, config["bus"] as? Int ?: 1) },
        )
    }
}