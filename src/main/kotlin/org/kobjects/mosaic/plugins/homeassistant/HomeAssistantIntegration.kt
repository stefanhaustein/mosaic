package org.kobjects.mosaic.plugins.homeassistant

import org.kobjects.mosaic.model.InputPortHolder
import org.kobjects.mosaic.model.OutputPortHolder
import org.kobjects.mosaic.pluginapi.AbstractArtifactSpec
import org.kobjects.mosaic.pluginapi.InputPortSpec
import org.kobjects.mosaic.pluginapi.Integration
import org.kobjects.mosaic.pluginapi.IntegrationFactory
import org.kobjects.mosaic.pluginapi.ModelInterface
import org.kobjects.mosaic.pluginapi.OutputPortSpec
import org.kobjects.mosaic.pluginapi.ParameterSpec
import org.kobjects.mosaic.pluginapi.Type
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity.Kind
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntityState
import org.kobjects.mosaic.plugins.homeassistant.client.HomeAssistantClient

class HomeAssistantIntegration(
    val model: ModelInterface,
    kind: String,
    name: String,
    tag: Long,
    var host: String,
    var port: Int,
    var token: String,
) : Integration(kind, name, tag) {
    var client: HomeAssistantClient? = null


    override val operationSpecs = Kind.values().map {
        val type = getType(it)
        if (type == null) null else InputPortSpec(
            namespace = this,
            category = "",
            name = it.toString().lowercase(),
            description = "",
            type = type,
            parameters = emptyList(),
            modifiers = setOf(AbstractArtifactSpec.Modifier.UNINSTANTIABLE),
            tag = tag,
            createFn = { _, _ ->
                throw UnsupportedOperationException()
            }
        )
    }.filterNotNull() + listOf(Kind.LIGHT).map {
        OutputPortSpec(
            namespace = this,
            category = "",
            name = it.name.lowercase() + "_out",
            description = "",
            parameters = emptyList(),
            modifiers = setOf(AbstractArtifactSpec.Modifier.UNINSTANTIABLE),
            tag = tag,
            createFn = {
                throw UnsupportedOperationException()
            }
        )
    }

    private fun getInputSpec(kind: Kind): InputPortSpec? =
        operationSpecs.find { it.name == kind.name.lowercase() } as InputPortSpec?


    private fun getOutputSpec(kind: Kind): OutputPortSpec? =
        operationSpecs.find { it.name == kind.name.lowercase() + "_out" } as OutputPortSpec?


    init {
        attach()
    }


    private fun attach() {
        client = HomeAssistantClient(host, port, token)

        for (entity in client?.entities?.values ?: emptyList()) {
            val id =  entity.id.replace('.', '_')
            val fqName = "$name.$id"

            val inputPortSpec = getInputSpec(entity.kind)
            if (inputPortSpec != null) {

                val inputPortHolder = InputPortHolder(
                    this,
                    name = fqName,
                    specification = inputPortSpec,
                    configuration = emptyMap(),
                    displayName = getDisplayName(entity),
                    category = getCategory(entity),
                    tag = tag
                )

                inputPortHolder.instance = HAEntityInputPortInstance(entity, inputPortHolder)
                inputPortHolder.value = getValue(entity)

                nodes.put(id, inputPortHolder)

                if (entity.kind == Kind.LIGHT) {
                    val outputPortHolder = OutputPortHolder(
                        this,
                        name = fqName + "_out",
                        specification = getOutputSpec(entity.kind) ?: throw RuntimeException("OuputPortSpec not found for ${entity.kind}"),
                  //      rawFormula = "",
                        configuration = emptyMap(),
                        displayName = getDisplayName(entity) + "_out",
                        category = getCategory(entity),
                        tag = tag
                    )

                    outputPortHolder.instance = EntityOutputPortInstance(this, entity)
                    nodes.put(id + "_out", outputPortHolder)
                }
            }
        }
    }

        // emptyList<AbstractArtifactSpec>() // client?.entities?.values?.filter { it.disabledBy == null }?.map { entityOperationSpec(it) } ?: emptyList()

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
        attach()
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

        fun getValue(entity: HAEntity, state: HAEntityState = entity.state): Any? {
            return when (entity.kind) {
                Kind.BINARY_SENSOR,
                    Kind.LIGHT -> when (state.state) {
                        "on" -> true
                       "off" -> false
                        else -> IllegalStateException(state.state?.toString() ?: "null")
                    }
                else -> state
            }
        }

        fun spec(model: ModelInterface) = IntegrationFactory(
            category = "HomeAutomation",
            name = "HomeAssistant",
            "HomeAssistant integration",
            parameters = listOf(
                ParameterSpec(name = "host", type = Type.STRING, defaultValue = "homeassistant.local"),
                ParameterSpec(name = "port", type = Type.INT, defaultValue = 8123),
                ParameterSpec(name = "token", type = Type.STRING, defaultValue = null),
            ),
            modifiers = setOf(AbstractArtifactSpec.Modifier.UNINSTANTIABLE),
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