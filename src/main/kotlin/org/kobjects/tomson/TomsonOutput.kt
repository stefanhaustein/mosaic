package org.kobjects.tomson

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class TomsonOutput(val target: Appendable = StringBuilder()) {

    fun appendSection(title: String, values: JsonObject) {
        startSection(title)
        for ((key, value) in values) {
            appendValue(key, value)
        }
    }

    fun startSection(title: String) {
        target.appendLine()
        target.appendLine("[$title]")
    }

    fun appendValue(name: String, value: JsonElement) {
        target.appendLine("$name = $value")
    }


    override fun toString() = target.toString()
}