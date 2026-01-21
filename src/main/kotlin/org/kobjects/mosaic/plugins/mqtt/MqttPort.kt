package org.kobjects.mosaic.plugins.mqtt

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import org.kobjects.mosaic.pluginapi.*

class MqttPort(
    val name: String,
     val configuration: Map<String, Any>,
     val tag: Long
)  {

    var client: MQTTClient? = null
    var exception: Exception? = null
    val listeners = mutableMapOf<String, MutableSet<ValueChangeListener>>()


    init {
        Thread {
            try {
                client = MQTTClient(MQTTVersion.MQTT5, configuration["address"].toString(), configuration["port"]!! as Int, null) {
                    println("MQTT packet received: $it")
                }
                for (topic in listeners.keys) {
                    client!!.subscribe(listOf(Subscription(topic)))
                }
                client!!.run()
            } catch (e: Exception) {
                e.printStackTrace()
                exception = e
            }
        }.start()
    }

    val operationSpecs: List<AbstractArtifactSpec> = listOf(
        FunctionSpec(
            "MQTT",
            Type.STRING,
            "$name.subscribe",
            "Subscribe to the given topic and receive update messages",
            listOf(ParameterSpec("topic", Type.STRING, null, setOf(ParameterSpec.Modifier.CONSTANT))),
            emptySet(),
            tag,
        ) {
            MqttSubscription(this, it)
        },
        FunctionSpec(
            "MQTT",
            Type.STRING,
            "$name.publish",
            "Publish / update a message for a given topic",
            listOf(
                ParameterSpec("topic", Type.STRING, null, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("payload", Type.STRING, null),
                ),
            emptySet(),
            tag,
        ) {
            MqttPublisher(this, it)
        }
    )

    fun addListener(topic: String, changeListener: ValueChangeListener) {
        val hosts = listeners.getOrPut(topic) {
            client?.subscribe(listOf(Subscription(topic)))
            mutableSetOf()
        }
        hosts.add(changeListener)
    }

    fun removeListener(topic: String, changeListener: ValueChangeListener) {
        val hosts = listeners[topic]
        if (hosts != null) {
            hosts.remove(changeListener)
            if (hosts.isEmpty()) {
                client?.unsubscribe(listOf(topic))
            }
        }
    }
}