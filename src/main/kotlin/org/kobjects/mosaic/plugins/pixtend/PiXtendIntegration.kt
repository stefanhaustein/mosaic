package org.kobjects.mosaic.plugins.pixtend

import com.pi4j.drivers.plc.pixtend.PiXtendDriver
import org.kobjects.mosaic.model.AbstractDescriptor

import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationFactory

 class PiXtendIntegration(
    val model: ModelInterface,
): Integration("pixt", "pixt") {
    var driver: PiXtendDriver? = null
    var error: Exception? = null
    val inputPorts = mutableSetOf<PiXtendInputPortInstance>()
    var invocationId = 0
    var pixtendModel = PiXtendDriver.Model.V2S


    private fun attach() {

            try {
                driver = PiXtendDriver(Model.pi4J, this@PiXtendIntegration.pixtendModel)
                error = null
                model.runAsync { syncState(driver!!, ++invocationId) }
            } catch (e: Exception) {
                e.printStackTrace()
                error = e
            }

    }

    fun syncState(driver: PiXtendDriver, invocationId: Int) {
        if (invocationId != this.invocationId) {
            return
        }
        driver.syncState()
        model.applySynchronizedWithToken(
            callback = { tag, anyChange ->
                model.runAsync {
                    syncState(driver, invocationId)
                }
            }
        ) {
            for (inputPort in inputPorts) {
                inputPort.syncState(it)
            }
        }
    }


    companion object {
        val piXtendModel = Type.ENUM(PiXtendDriver.Model.entries)

        fun spec(model: ModelInterface) = IntegrationFactory(
            category = "PLC",
            name = "pixt",
            description = "PiXtend PLC Integration",
            parameters = listOf(ParameterSpec("model", piXtendModel, PiXtendDriver.Model.V2S)),
            modifiers = setOf(AbstractDescriptor.Modifier.SINGLETON),
        ) { _ -> PiXtendIntegration(model) }
    }

    override val portFactories = listOf(
            PiXtendAnalogInputPort.spec(this),
            PiXtendAnalogOutputPort.spec(this),
            PiXtendDigitalInputPort.spec(this),
            PiXtendDigitalOutputPort.spec(this),
            PiXtendGpioDigitalInputPort.spec(this),
            PiXtendGpioDigitalOutputPort.spec(this),
            PiXtendRelayPort.spec(this),
        ).associateBy { it.name }

    override fun detach(token: ModificationToken) {
        invocationId++
    }


    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {
        invocationId++
        this@PiXtendIntegration.pixtendModel = configuration["model"] as PiXtendDriver.Model
        attach()
    }


}