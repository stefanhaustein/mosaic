package org.kobjects.tablecraft.plugins.homeassistant.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class HAEntity(
    val client: HomeAssistantClient,
    val json: JsonObject,
    private var state_: HAEntityState
) {
    var state: HAEntityState
        get() = state_
        set(value) {
            val previousState = state_
            state_ = value
            for (listener in stateListeners) {
                listener.entityStateChanged(this, previousState, state)
            }
        }

    val stateListeners = mutableListOf<StateChangeListener>()

    val id: String = json["entity_id"]!!.jsonPrimitive.content

    val kind: Kind
        get() = Kind.entries.find { it.name.lowercase() == id.substring(0, id.indexOf('.')) } ?: Kind.UNRECOGNIZED

    val category: String?
        get() = json["entity_category"]?.jsonPrimitive?.contentOrNull

    val deviceId = json["device_id"]?.jsonPrimitive?.contentOrNull
    val device: HADevice? = client.devices[deviceId]
    val disabledBy = json["disabled_by"]?.jsonPrimitive?.contentOrNull

    val friendlyName: String?
        get() = state.json?.get("attributes")?.jsonObject?.get("friendly_name")?.jsonPrimitive?.contentOrNull

    val description: String
        get() = (friendlyName?:"") + ".debug:\n" + PRETTY_JSON.encodeToString(json) +
                "\n\nstate:" + PRETTY_JSON.encodeToString(state.json)

    override fun toString(): String = id + " - " + category + " - " + json

    fun addListener(listener: StateChangeListener) {
        stateListeners.add(listener)
    }

    fun removeListener(listener: StateChangeListener) {
        stateListeners.remove(listener)
    }

    enum class Kind {
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
        fun entityStateChanged(entity: HAEntity, oldState: HAEntityState, newState: HAEntityState)
    }
}