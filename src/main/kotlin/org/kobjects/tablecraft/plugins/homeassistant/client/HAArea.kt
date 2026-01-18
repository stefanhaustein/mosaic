package org.kobjects.tablecraft.plugins.homeassistant.client

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class HAArea(
    val client: HomeAssistantClient,
    val json: JsonObject
) {
    val id: String = json["area_id"]!!.jsonPrimitive.content
    val name: String = json["name"]?.jsonPrimitive?.contentOrNull ?: id

    override fun toString() = name
}