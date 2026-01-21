package org.kobjects.mosaic.plugins.mqtt


object MqttPlugin /* : Plugin {
    override val portSpecs = listOf<PortSpec>(
        PortSpec(
            "mqtt",
            "A Mqtt client",
            listOf(
                ParameterSpec("address", ParameterKind.CONFIGURATION, Type.TEXT, required = true),
                ParameterSpec("port", ParameterKind.CONFIGURATION, Type.INT, required = true),
            )
        ) { name, configuration, tag ->
            MqttPort(name, configuration, tag)
        })

    override val operationSpecs = emptyList<OperationSpec>()

fun main() {
    val client = MQTTClient(
        MQTTVersion.MQTT5,
        "test.mosquitto.org",
        1883,
        null
    ) {
        println(it.payload?.toByteArray()?.decodeToString())
    }
    client.subscribe(listOf(Subscription("/randomTopic", SubscriptionOptions(Qos.EXACTLY_ONCE))))
    client.publish(false, Qos.EXACTLY_ONCE, "/randomTopic", "hello".encodeToByteArray().toUByteArray())
    client.publish(false, Qos.EXACTLY_ONCE, "/randomTopic", "hello".encodeToByteArray().toUByteArray())
    client.run() // Blocking method, use step() if you don't want to block the thread
}
*/
