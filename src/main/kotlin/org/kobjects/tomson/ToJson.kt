package org.kobjects.tomson

import kotlinx.serialization.json.JsonElement

interface ToJson {
    fun toJson(): JsonElement
}