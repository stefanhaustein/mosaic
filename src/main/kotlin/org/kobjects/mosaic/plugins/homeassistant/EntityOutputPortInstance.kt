package org.kobjects.mosaic.plugins.homeassistant

import kotlinx.coroutines.runBlocking
import org.kobjects.mosaic.pluginapi.OutputPortInstance
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity

class EntityOutputPortInstance(
    val integration: HomeAssistantIntegration,
    val entity: HAEntity
) : OutputPortInstance {
    override fun setValue(value: Any?) {
        runBlocking {
            integration.client?.sendJson(
                """{ "id": ${integration.client?.messageId?.getAndIncrement()}, "type": "call_service",  "domain": "light", "service": "turn_${if (value == true) "on" else "off"}",
                "target": { "entity_id": "${entity.id}" } }"""
            )
        }
    }

    override fun detach() {

    }

}