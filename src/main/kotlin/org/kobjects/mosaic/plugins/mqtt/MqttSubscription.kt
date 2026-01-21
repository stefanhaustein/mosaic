package org.kobjects.mosaic.plugins.mqtt

import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.pluginapi.ValueChangeListener
import org.kobjects.mosaic.pluginapi.StatefulFunctionInstance

class MqttSubscription(val port: MqttPort, configuration: Map<String, Any?>) : StatefulFunctionInstance {
    val topic = configuration["topic"].toString()
    var host: ValueChangeListener? = null

    override fun attach(host: ValueChangeListener) {
        port.addListener(topic, host)
        this.host = host
    }

    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        return ""
    }

    override fun detach() {
        port.removeListener(topic, host!!)
    }

}