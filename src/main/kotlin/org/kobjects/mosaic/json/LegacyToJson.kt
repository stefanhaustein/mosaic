package org.kobjects.mosaic.json

interface LegacyToJson {

    fun legacyToJson(): String = StringBuilder().also { this.legacyToJson(it) }.toString()

    fun legacyToJson(sb: StringBuilder)
}