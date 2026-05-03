package org.kobjects.mosaic.plugins.homeassistant

import kotlinx.serialization.json.JsonObject
import org.kobjects.mosaic.model.integration.InputPortNode
import org.kobjects.mosaic.model.integration.OutputPortNode
import org.kobjects.mosaic.model.integration.InputPortDescriptor
import org.kobjects.mosaic.model.integration.Integration
import org.kobjects.mosaic.model.integration.IntegrationDescriptor
import org.kobjects.mosaic.model.ModelInterface
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.integration.OutputPortDescriptor
import org.kobjects.mosaic.model.ParameterSpec
import org.kobjects.mosaic.model.Type
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntityState
import org.kobjects.mosaic.plugins.homeassistant.client.HomeAssistantClient

class HomeAssistantIntegration(
    val model: ModelInterface,
    name: String,

) : Integration("HomeAssistant", name) {
    var client: HomeAssistantClient? = null
    var host = ""
    var port = -1
    var token = ""

    override val portDescriptors = (HAEntity.Kind.entries.map {
        val type = getType(it)
        if (type == null) null else InputPortDescriptor.createUninstantiable(
            namespace = this,
            name = it.toString().lowercase(),
            type = type,
            description = "",
        )
    } + HAEntity.Kind.entries.map {
        if (supportsOutput(it)) {
            OutputPortDescriptor.createUninstantiable(
                namespace = this,
                name = it.name.lowercase() + "_out",
                description = "",
            )
        } else null
    }).filterNotNull().associateBy { it.name }

    private fun getInputPortDescriptor(kind: HAEntity.Kind): InputPortDescriptor? =
        portDescriptors[kind.name.lowercase()] as InputPortDescriptor?

    private fun getOutputPortDescriptor(kind: HAEntity.Kind): OutputPortDescriptor? =
        portDescriptors[kind.name.lowercase() + "_out"] as OutputPortDescriptor?


    private fun attach(modificationToken: ModificationToken) {
        client = HomeAssistantClient(host, port, token)

        for (entity in client?.entities?.values ?: emptyList()) {
            val name =  entity.id.replace('.', '_')

            val inputPortSpec = getInputPortDescriptor(entity.kind)
            if (inputPortSpec != null) {

                val inputPortHolder = InputPortNode(
                    this,
                    name = name,
                    descriptor = inputPortSpec,
                    displayName = getDisplayName(entity),
                    category = getCategory(entity),
                )

                inputPortHolder.instance = HaEntityInputPortInstance(entity, inputPortHolder)
                inputPortHolder.value = getValue(entity)

                nodes.put(name, inputPortHolder)

                if (supportsOutput(entity.kind)) {
                    val outputPortHolder = OutputPortNode(
                        this,
                        name = name + "_out",
                        descriptor = getOutputPortDescriptor(entity.kind) ?: throw RuntimeException("OuputPortSpec not found for ${entity.kind}"),
                        rawFormula = "",
                        displayName = getDisplayName(entity) + "_out",
                        category = getCategory(entity),
                    )
                    outputPortHolder.instance = HaEntityOutputPortInstance(this, entity)
                    nodes.put(name + "_out", outputPortHolder)
                    outputPortHolder.configure(JsonObject(emptyMap()),modificationToken)
                }
            }
        }
    }

    override fun detach(token: ModificationToken) {
        client?.close()
    }

    override fun configureInternal(configuration: Map<String, Any?>, token: ModificationToken) {
        detach(token)
        this.host = configuration["host"] as String
        this.port = configuration["port"].toString().toDouble().toInt()
        this.token = configuration["token"] as String
        attach(token)
    }

    companion object {

        fun getType(kind: HAEntity.Kind): Type? {
            return when (kind) {
                HAEntity.Kind.LIGHT -> Type.BOOL
                HAEntity.Kind.BINARY_SENSOR -> Type.BOOL
                HAEntity.Kind.SENSOR -> Type.REAL
                else -> null
            }
        }

        fun supportsOutput(kind: HAEntity.Kind): Boolean = when (kind) {
            HAEntity.Kind.LIGHT -> true
            else -> false
        }

        fun getValue(entity: HAEntity, state: HAEntityState = entity.state): Any {
            return when (entity.kind) {
                HAEntity.Kind.BINARY_SENSOR,
                HAEntity.Kind.LIGHT -> when (state.state) {
                    "on" -> true
                    "off" -> false
                    else -> IllegalStateException(state.state?.toString() ?: "null")
                }
                else -> state
            }
        }

        fun spec(model: ModelInterface) = IntegrationDescriptor(
            category = "HomeAutomation",
            name = "HomeAssistant",
            "HomeAssistant integration",
            parameters = listOf(
                ParameterSpec(name = "host", type = Type.STRING, defaultValue = "homeassistant.local"),
                ParameterSpec(name = "port", type = Type.INT, defaultValue = 8123),
                ParameterSpec(name = "token", type = Type.STRING, defaultValue = null),
            ),
        ) { name ->
            HomeAssistantIntegration(
                model,
                name,
            )
        }
    }


    fun getCategory(entity: HAEntity): String {
        val device = entity.device
        return buildString {
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
    }

    fun getDisplayName(entity: HAEntity): String {
        val entityId = entity.id
        val cut = entity.id.indexOf('.')
        val idPrefix = entity.device?.commonEntityIdPrefix ?: ""
        val idWithoutType = entityId.substring(cut + 1)
        return if (idPrefix.isEmpty() || !idWithoutType.startsWith(idPrefix)) {
            idWithoutType
        } else if (idWithoutType == idPrefix) {
            entityId.take(cut)
        } else {
            val suffix = idWithoutType.substring(idPrefix.length)
            if (suffix.startsWith("_")) suffix.substring(1) else suffix
        }
    }
}