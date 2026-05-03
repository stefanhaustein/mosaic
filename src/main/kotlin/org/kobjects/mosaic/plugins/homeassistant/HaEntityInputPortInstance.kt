package org.kobjects.mosaic.plugins.homeassistant

import org.kobjects.mosaic.model.integration.InputPortNode
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.integration.InputPortInstance
import org.kobjects.mosaic.plugins.homeassistant.HomeAssistantIntegration.Companion.getValue
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntityState

class HaEntityInputPortInstance(
    val entity: HAEntity,
    val portHolder: InputPortNode,
) : InputPortInstance(portHolder), HAEntity.StateChangeListener {

    init {
        entity.addListener(this)
    }

    override fun entityStateChanged(
        entity: HAEntity,
        oldState: HAEntityState,
        newState: HAEntityState
    ) {
        Model.requestSynchronizedWithToken {
            portHolder.portValueChanged(getValue(entity, newState), it)

        }

    }

    override fun detach() {
       entity.removeListener(this)
    }


}