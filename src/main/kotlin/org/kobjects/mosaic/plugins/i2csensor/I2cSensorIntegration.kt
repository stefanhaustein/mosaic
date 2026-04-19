package org.kobjects.mosaic.plugins.i2csensor

import com.pi4j.drivers.sensor.Sensor
import com.pi4j.drivers.sensor.SensorDescriptor
import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver
import com.pi4j.io.i2c.I2C
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractPortFactorySpec
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationFactory
import org.kobjects.mosaic.plugins.rpi.devices.Bmp280Port

class I2cSensorIntegration(
    val model: ModelInterface,
    name: String,
    tag: Long,
    val sensorDescriptor: SensorDescriptor
) : Integration(INTEGRATION_NAME, name, tag) {

    var i2c: I2C? = null
    var sensor: Sensor? = null

    override val portFactories: List<AbstractPortFactorySpec>
        get() = emptyList()

    override fun close() {
        sensor?.close()
    }

    override fun configure(configuration: Map<String, Any?>) {
        sensor?.close()

        val bus = configuration["bus"] as Int
        val address = configuration["address"] as Int

        i2c = Model.pi4J.create(I2C.newConfigBuilder(Model.pi4J).bus(bus).device(address))
        sensor = sensorDescriptor.detect(i2c)
    }


    companion object {
        const val INTEGRATION_NAME = "I2cSensor"

        fun spec(model: ModelInterface) = IntegrationFactory(
            category = "Sensor",
            name = INTEGRATION_NAME,
            description = "I2c-Based Sensors",
            parameters = listOf(
                ParameterSpec("bus", Type.INT, 0),
                ParameterSpec("address", Type.INT, 0)),
            modifiers = emptySet(),

        ) { _, name, tag->
            I2cSensorIntegration(model, name, tag, Bmx280Driver.DESCRIPTOR_BME_280)
        }
    }
}