package org.kobjects.mosaic.plugins.i2csensor

import com.pi4j.drivers.sensor.SensorDescriptor
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.AbstractPortFactorySpec
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationFactory

open class I2cSensorIntegration(val model: ModelInterface, factoryName: String, name: String, tag: Long) : Integration(factoryName, name, tag) {
    override val portFactories: List<AbstractPortFactorySpec>
        get() = TODO("Not yet implemented")


    override fun close() {
        TODO("Not yet implemented")
    }

    override fun configure(configuration: Map<String, Any?>) {
        TODO("Not yet implemented")
    }


    companion object {
        fun factory(model: ModelInterface, name: String, description: String, sensorDescriptor: SensorDescriptor) = IntegrationFactory(
            category = "Sensor",
            name = name,
            description = description,
            parameters = listOf(ParameterSpec("address", Type.INT, 0)),
            modifiers = emptySet(),

        ) { kind, name, tag->
            I2cSensorIntegration(model, kind, name, tag)
        }
    }
}