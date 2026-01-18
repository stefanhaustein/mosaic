package org.kobjects.tablecraft.plugins.homeassistant.client

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.tablecraft.model.Cell.Companion.id

data class HADevice(
    val client: HomeAssistantClient,
    val json: JsonObject
) {
    val id: String = json["id"]!!.jsonPrimitive.content
    val name: String = json["name_by_user"]?.jsonPrimitive?.contentOrNull ?: json["name"]?.jsonPrimitive?.contentOrNull ?: id
    val manufacturer: String? = json["manufacturer"]?.jsonPrimitive?.contentOrNull
    val model: String? = json["model"]?.jsonPrimitive?.contentOrNull
    val areaId: String? = json["area_id"]?.jsonPrimitive?.contentOrNull
    val area: HAArea?
        get() = client.areas[areaId]

    val entities: List<HAEntity>
        get() = client.entities.values.filter { it.deviceId == id }

    val commonEntityIdPrefix: String
        get() {
            val mainEntityIds = entities.filter { it.category == null }.map { it.id.substring(it.id.indexOf(".") + 1) }
            if (mainEntityIds.isEmpty()) {
                return ""
            }
            val first = mainEntityIds.first()
            if (mainEntityIds.size == 1) {
                return mainEntityIds.first()
            }
            val remainder = mainEntityIds.drop(1)
            var i = 0;
            while (i < first.length) {
                val c = first[i]
                if (remainder.any { it.length <= i || it[i] != c } ) {
                    break
                }
                i++
            }
            return first.substring(0, i)
        }

    override fun toString() = "$name ($manufacturer $model)"
}