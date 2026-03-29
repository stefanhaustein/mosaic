package org.kobjects.mosaic.json

import org.kobjects.tomson.ToJson

interface LegacyToJson : ToJson {

    fun legacyToJson(): String = StringBuilder().also { this.legacyToJson(it) }.toString()

    fun legacyToJson(sb: StringBuilder) {
        sb.append(toJson().toString())
    }
}