package org.kobjects.mosaic.plugins.homeassistant.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class HAEntity(
    val client: HomeAssistantClient,
    val json: JsonObject,
    val initialStateJson: JsonObject
) {
    private var state_ = initialStateJson

    var state: JsonObject
        get() = state_
        set(value) {
            // println("New state: ${Json.encodeToString(value)}")
            val previousState = state_
            state_ = value
            for (listener in stateListeners) {
                listener.entityStateChanged(this, decodeState(previousState), decodeState(value))
            }
        }

    val stateListeners = mutableListOf<StateChangeListener>()

    val id: String = json["entity_id"]!!.jsonPrimitive.content

    val kind: Kind
        get() = Kind.entries.find { it.name.lowercase() == id.substring(0, id.indexOf('.')) } ?: let {
            System.out.println("Unknown entity kind: $id")
            Kind.UNRECOGNIZED
        }

    val category: String?
        get() = json["entity_category"]?.jsonPrimitive?.contentOrNull

    val deviceId = json["device_id"]?.jsonPrimitive?.contentOrNull
    val device: HADevice? = client.devices[deviceId]
    val disabledBy = json["disabled_by"]?.jsonPrimitive?.contentOrNull

    val friendlyName: String?
        get() = initialStateJson?.get("attributes")?.jsonObject?.get("friendly_name")?.jsonPrimitive?.contentOrNull

    val description: String
        get() = (friendlyName?:"") + ".debug:\n" + PRETTY_JSON.encodeToString(json) +
                "\n\ninitialState:" + PRETTY_JSON.encodeToString(initialStateJson) +
                "\n\nstate:" + PRETTY_JSON.encodeToString(state_)

    override fun toString(): String = id + " - " + category + " - " + json

    fun decodeState(json: JsonObject): Any? =
        when (kind) {
            Kind.EVENT -> {
                println("***** " + Json.encodeToString(json) + "*****")
                (json["a"] ?: json["attributes"])?.jsonObject?.get("event_type")?.jsonPrimitive?.contentOrNull
            }
            Kind.BINARY_SENSOR,
            Kind.LIGHT -> when ((json["s"] ?: json["state"])?.jsonPrimitive?.contentOrNull) {
                "on" -> true
                "off" -> false
                else -> IllegalStateException(Json.encodeToString(json))
            }
            else -> kind.name + ": " + Json.encodeToString(json)
        }


    fun addListener(listener: StateChangeListener) {
        stateListeners.add(listener)
    }

    fun removeListener(listener: StateChangeListener) {
        stateListeners.remove(listener)
    }

    enum class Kind {
        EVENT,
        BUTTON,
        BINARY_SENSOR,
        LIGHT,
        SELECT,
        SENSOR,
        UPDATE,

        UNRECOGNIZED,
    }

    companion object {
        val PRETTY_JSON = Json{prettyPrint = true}
    }

    interface StateChangeListener {
        fun entityStateChanged(entity: HAEntity, oldState: Any?, newState: Any?)
    }
}