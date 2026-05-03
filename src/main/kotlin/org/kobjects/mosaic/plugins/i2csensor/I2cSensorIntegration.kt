package org.kobjects.mosaic.plugins.i2csensor

import com.pi4j.drivers.sensor.Sensor
import com.pi4j.drivers.sensor.SensorDescriptor.MeasurementUnit
import com.pi4j.drivers.sensor.SensorDetector
import com.pi4j.io.i2c.I2C
import kotlinx.serialization.json.JsonObject
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.InputPortNode
import org.kobjects.mosaic.model.integration.InputPortDescriptor
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationDescriptor

class I2cSensorIntegration(
    val model: ModelInterface,
    name: String,
) : Integration(INTEGRATION_NAME, name) {

    var i2c: I2C? = null
    var sensor: Sensor? = null
    var run = 0

    override val portDescriptors = MeasurementUnit.values().associate { it.name to
        InputPortDescriptor.createUninstantiable(
            this,
            it.name,
            Type.REAL,
            "",
        )
    }

    override fun detach(token: ModificationToken) {
        run++
        sensor?.close()
    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {
        sensor?.close()

        val sensorName = configuration["sensor"] as String
        val bus = configuration["bus"] as Int
        val address = configuration["address"] as Int

        val sensorDescriptor = SensorDetector.DESCRIPTORS.first { it.sensorName == sensorName }
        i2c = Model.pi4J?.create(I2C.newConfigBuilder(Model.pi4J).bus(bus).device(address))
        sensor = sensorDescriptor.detect(i2c)

        for (value in sensor?.descriptor?.values ?: emptyList()) {
            val inputPortNode = InputPortNode(
                this,
                value.kind.name.lowercase(),
                descriptor = portDescriptors[value.kind.measurementUnit.name]!!
            )
            nodes[value.kind.name.lowercase()] = inputPortNode
            inputPortNode.configure(JsonObject(emptyMap()), token)
        }
        val localRun = ++run
        Model.scheduleAsync(5000) {
            if (localRun != run) {
                false
            } else {
                val measurements = DoubleArray(sensorDescriptor.values.size)
                sensor?.readMeasurement(measurements)
                Model.requestSynchronizedWithToken { token ->
                    for (value in sensorDescriptor.values) {
                        (nodes[value.kind.name.lowercase()] as? InputPortNode)?.portValueChanged(measurements[value.index], token)
                    }
                }
                true
            }
        }
    }
    
    companion object {
        const val INTEGRATION_NAME = "I2cSensor"

        val sensorType: Type = Type.Options(SensorDetector.DESCRIPTORS.map{ it.sensorName } )

        fun descriptor(model: ModelInterface) = IntegrationDescriptor(
            category = "Sensor",
            name = INTEGRATION_NAME,
            description = "I2c-Based Sensors",
            parameters = listOf(
                ParameterSpec("sensor", sensorType, null),
                ParameterSpec("bus", Type.INT, 1),
                ParameterSpec("address", Type.INT, 0)),
            modifiers = emptySet(),

        ) { name ->
            I2cSensorIntegration(model, name)
        }
    }
}