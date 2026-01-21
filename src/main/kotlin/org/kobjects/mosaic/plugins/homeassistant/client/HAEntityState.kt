package org.kobjects.mosaic.plugins.homeassistant.client

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HAEntityState(
    val id: String,
    val state: Any?,
    val json: JsonObject
) {
    constructor(json: JsonObject) : this(
         json["entity_id"]!!.jsonPrimitive.content, json["state"]?.jsonPrimitive?.contentOrNull, json
    )

    fun update(update: JsonObject): HAEntityState {
        val state = update["+"]?.jsonObject["s"]?.jsonPrimitive?.contentOrNull
        return HAEntityState(id, state, json)
    }
}