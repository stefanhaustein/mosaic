package org.kobjects.mosaic.plugins.homeassistant

import org.kobjects.mosaic.model.InputPortHolder
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.pluginapi.InputPortInstance
import org.kobjects.mosaic.plugins.homeassistant.HomeAssistantIntegration.Companion.getValue
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntityState

class HAEntityInputPortInstance(
    val entity: HAEntity,
    val portHolder: InputPortHolder,
) : InputPortInstance, HAEntity.StateChangeListener {

    init {
        entity.addListener(this)
    }

    override fun entityStateChanged(
        entity: HAEntity,
        oldState: HAEntityState,
        newState: HAEntityState
    ) {
        Model.requestSynchronizedWithToken {
            portHolder.portValueChanged(it, getValue(entity, newState))

        }

    }

    override val value: Any
        get() = getValue(entity) ?: Unit

    override fun detach() {
       entity.removeListener(this)
    }


}