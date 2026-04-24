package org.kobjects.mosaic.plugins.i2csensor

import com.pi4j.drivers.sensor.Sensor
import com.pi4j.drivers.sensor.SensorDescriptor
import com.pi4j.drivers.sensor.environment.bmx280.Bmx280Driver
import com.pi4j.io.i2c.I2C
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractArtifactSpec.Modifier
import org.kobjects.mosaic.model.AbstractPortFactorySpec
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.InputPortInstance
import org.kobjects.mosaic.model.integration.InputPortNode
import org.kobjects.mosaic.model.integration.InputPortSpec
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationFactory
import org.kobjects.mosaic.plugins.rpi.devices.Bmp280Port

class I2cSensorIntegration(
    val model: ModelInterface,
    name: String,
    val sensorDescriptor: SensorDescriptor
) : Integration(INTEGRATION_NAME, name) {

    var i2c: I2C? = null
    var sensor: Sensor? = null
    var run = 0

    override val portFactories = listOf(
        InputPortSpec(
            this,
            "",
            "Measurement",
            Type.REAL,
            "",
            emptyList(),
            modifiers = setOf(Modifier.UNINSTANTIABLE),
            createFn = { _, _ -> throw UnsupportedOperationException()}
        )
    ).associateBy { it.name }

    override fun close() {
        run++
        sensor?.close()
    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {
        sensor?.close()

        val bus = configuration["bus"] as Int
        val address = configuration["address"] as Int

        i2c = Model.pi4J?.create(I2C.newConfigBuilder(Model.pi4J).bus(bus).device(address))
        sensor = sensorDescriptor.detect(i2c)

        for (value in sensor?.descriptor?.values ?: emptyList()) {
            val inputPortNode = InputPortNode(
                this,
                value.kind.name.lowercase(),
                specification = portFactories.values.first(),
                configuration = emptyMap(),
                displayName = null,
                category = null,
                tag = token.tag
            )
            nodes[value.kind.name.lowercase()] = inputPortNode
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

        fun spec(model: ModelInterface) = IntegrationFactory(
            category = "Sensor",
            name = INTEGRATION_NAME,
            description = "I2c-Based Sensors",
            parameters = listOf(
                ParameterSpec("bus", Type.INT, 0),
                ParameterSpec("address", Type.INT, 0)),
            modifiers = emptySet(),

        ) { name ->
            I2cSensorIntegration(model, name, Bmx280Driver.DESCRIPTOR_BME_280)
        }
    }
}