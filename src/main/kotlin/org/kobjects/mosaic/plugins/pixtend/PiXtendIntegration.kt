package org.kobjects.mosaic.plugins.pixtend

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.drivers.plc.pixtend.PiXtendDriver
import org.kobjects.mosaic.pluginapi.*

class PiXtendIntegration(
    val model: ModelInterface,
    kind: String,
    name: String,
    tag: Long,
    var pixtendModel: PiXtendDriver.Model

): Integration(kind, name, tag) {
    var pi4j: Context? = null
    var driver: PiXtendDriver? = null
    var error: Exception? = null
    val inputPorts = mutableSetOf<PiXtendInputPortInstance>()
    var invocationId = 0

    init {
       attach()
    }

    private fun attach() {

            try {
                pi4j = Pi4J.newAutoContext()
                driver = PiXtendDriver(pi4j, this@PiXtendIntegration.pixtendModel)
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
            modifiers = setOf(AbstractArtifactSpec.Modifier.SINGLETON),
        ) { kind, name, tag, config ->
            PiXtendIntegration(model, kind, name, tag, config["model"] as PiXtendDriver.Model)
        }
    }

    override val operationSpecs: List<AbstractFactorySpec>
        get() = listOf(
            PiXtendAnalogInputPort.spec(this),
            PiXtendAnalogOutputPort.spec(this),
            PiXtendDigitalInputPort.spec(this),
            PiXtendDigitalOutputPort.spec(this),
            PiXtendGpioDigitalInputPort.spec(this),
            PiXtendGpioDigitalOutputPort.spec(this),
            PiXtendRelayPort.spec(this),
        )

    override val configuration: Map<String, Any?>
        get() = mapOf("model" to pixtendModel.name)

    override fun detach() {
        invocationId++
        pi4j?.shutdown()
        pi4j = null

    }

    override fun notifySimulationModeChanged(token: ModificationToken) {
        detach()
        attach()
    }

    override fun reconfigure(configuration: Map<String, Any?>) {
        invocationId++
        this@PiXtendIntegration.pixtendModel = configuration["model"] as PiXtendDriver.Model
        attach()
    }


}