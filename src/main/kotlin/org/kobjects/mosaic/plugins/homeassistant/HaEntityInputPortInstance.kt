package org.kobjects.mosaic.plugins.homeassistant

import org.kobjects.mosaic.model.integration.InputPortNode
import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.model.integration.InputPortInstance

import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity


class HaEntityInputPortInstance(
    val entity: HAEntity,
    val portHolder: InputPortNode,
) : InputPortInstance(portHolder), HAEntity.StateChangeListener {

    init {
        entity.addListener(this)
    }

    override fun entityStateChanged(
        entity: HAEntity,
        oldState: Any?,
        newState: Any?
    ) {
        Model.requestSynchronizedWithToken {
            portHolder.portValueChanged(newState, it)

        }

    }

    override fun detach() {
       entity.removeListener(this)
    }


}