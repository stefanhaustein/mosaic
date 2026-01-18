package org.kobjects.tablecraft.plugins.homeassistant

import kotlinx.coroutines.runBlocking
import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.pluginapi.AbstractArtifactSpec
import org.kobjects.tablecraft.pluginapi.IntegrationInstance
import org.kobjects.tablecraft.pluginapi.IntegrationSpec
import org.kobjects.tablecraft.pluginapi.ModelInterface
import org.kobjects.tablecraft.pluginapi.OutputPortInstance
import org.kobjects.tablecraft.pluginapi.ParameterSpec
import org.kobjects.tablecraft.pluginapi.PropertySpec
import org.kobjects.tablecraft.pluginapi.StatefulFunctionInstance
import org.kobjects.tablecraft.pluginapi.Type
import org.kobjects.tablecraft.pluginapi.ValueChangeListener
import org.kobjects.tablecraft.plugins.homeassistant.client.HAEntity
import org.kobjects.tablecraft.plugins.homeassistant.client.HAEntity.Kind
import org.kobjects.tablecraft.plugins.homeassistant.client.HAEntityState
import org.kobjects.tablecraft.plugins.homeassistant.client.HomeAssistantClient

class HomeAssistantIntegration(
    val model: ModelInterface,
    kind: String,
    name: String,
    tag: Long,
    var host: String,
    var port: Int,
    var token: String,
) : IntegrationInstance(kind, name, tag) {
    var client: HomeAssistantClient? = null

    init {
        attach()
    }

    private fun attach() {
        client = HomeAssistantClient(host, port, token)
    }

    override val operationSpecs: List<AbstractArtifactSpec>
        get() = client?.entities?.values?.filter { it.disabledBy == null }?.map { entityOperationSpec(it) } ?: emptyList()

    override val configuration: Map<String, Any?>
        get() = mapOf("host" to host, "port" to port, "token" to token)

    override fun detach() {
        client?.close()
    }

    override fun reconfigure(configuration: Map<String, Any?>) {
        detach()
        this.host = configuration["host"] as String
        this.port = configuration["port"].toString().toDouble().toInt()
        this.token = configuration["token"] as String
    }

    companion object {

        fun spec(model: ModelInterface) = IntegrationSpec(
            category = "HomeAutomation",
            name = "HomeAssistant",
            "HomeAssistant integration",
            parameters = listOf(
                ParameterSpec(name = "host", type = Type.STRING, defaultValue = "homeassistant.local"),
                ParameterSpec(name = "port", type = Type.INT, defaultValue = 8123),
                ParameterSpec(name = "token", type = Type.STRING, defaultValue = null),
            ),
            modifiers = emptySet(),
        ) { kind, name, tag, config ->
            HomeAssistantIntegration(
                model,
                kind,
                name,
                tag,
                host = config["host"] as String,
                port = config["port"].toString().toDouble().toInt(),
                token = config["token"] as String
            )
        }
    }

    fun entityOperationSpec(entity: HAEntity): PropertySpec {
        val device = entity.device
        val category = buildString {
            val areaName = device?.area?.toString() ?: "Unnamed Area"
            append(areaName)
            append(".")
            val deviceName = device?.name ?: "Unnamed Device"
            if (deviceName.startsWith(areaName)) {
                append(deviceName.substring(areaName.length).trim())
            } else {
                append(deviceName)
            }
            if (entity.category != null) {
                append("." + entity.category)
            }
        }

        val entityId = entity.id
        val cut = entity.id.indexOf('.')
        val idPrefix = device?.commonEntityIdPrefix ?: ""
        val idWithoutType = entityId.substring(cut + 1)
        val displayName = if (idPrefix.isEmpty() || !idWithoutType.startsWith(idPrefix)) {
            idWithoutType
        } else if (idWithoutType == idPrefix) {
            entityId.take(cut)
        } else {
            val suffix = idWithoutType.substring(idPrefix.length)
            if (suffix.startsWith("_")) suffix.substring(1) else suffix
        }

        return PropertySpec(
            category = category,
            name = name + "." + entity.id.replace(".", "_"),
            type = when (entity.kind) {
                Kind.BINARY_SENSOR -> Type.BOOL
                Kind.LIGHT -> Type.BOOL
                Kind.SENSOR -> Type.REAL
                else -> Type.STRING
            },
            description = entity.description,
            displayName = displayName,
            setterCreateFn = if (entity.kind != Kind.LIGHT) null else { {
                EntityOutputPortInstance(this@HomeAssistantIntegration, entity)
            } }
        ) {
            EntityFunctionInstance(this@HomeAssistantIntegration, entity)
        }
    }

    class EntityOutputPortInstance(
        val integration: HomeAssistantIntegration,
        val entity: HAEntity
    ) : OutputPortInstance {
        override fun setValue(value: Any?) {
            runBlocking {
                integration.client?.sendJson(
            """{ "id": ${integration.client?.messageId?.getAndIncrement()}, "type": "call_service",  "domain": "light", "service": "turn_${if(value == true) "on" else "off" }",
                    "target": { "entity_id": "${entity.id}" } }""")
            }
        }

        override fun detach() {

        }

    }


    class EntityFunctionInstance(
        val integration: HomeAssistantIntegration,
        val entity: HAEntity
    ) : StatefulFunctionInstance, HAEntity.StateChangeListener {
        var host: ValueChangeListener? = null

        override fun apply(
            context: EvaluationContext,
            params: Map<String, Any?>
        ): Any? {
            return entity.state.state
        }

        override fun attach(host: ValueChangeListener) {
            this.host = host
            entity.addListener(this)
        }

        override fun detach() {
            entity.removeListener(this)
            host = null
        }

        override fun entityStateChanged(
            entity: HAEntity,
            oldState: HAEntityState,
            newState: HAEntityState
        ) {
            integration.model.notifyValueChanged(host)
        }

    }

}