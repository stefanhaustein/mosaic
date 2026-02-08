package org.kobjects.mosaic.plugins.homeassistant

import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.pluginapi.StatefulFunctionInstance
import org.kobjects.mosaic.pluginapi.ValueChangeListener
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntity
import org.kobjects.mosaic.plugins.homeassistant.client.HAEntityState

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