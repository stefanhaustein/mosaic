package org.kobjects.tomson

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

object TomsonParser {


    fun parse(input: String): Map<String, JsonObject> {
        val result = mutableMapOf<String, JsonObject>()
        var currentSectionMap = mutableMapOf<String, JsonElement>()
        var currentSectionName = ""

        var pendingKey = ""
        var pendingValue = ""

        for (line in input.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith('#')) {
                // Skip
            } else if (line.startsWith(" ") || line.startsWith("\t")) {
                require (pendingKey.isNotEmpty()) {
                    "Unexpected indented line: '$line'"
                }
                pendingValue += "\n$line"
            } else {
                if (pendingKey.isNotEmpty()) {
                    currentSectionMap[pendingKey] = Json.parseToJsonElement(pendingValue)
                    pendingKey = ""
                    pendingValue = ""
                } else {
                    require(pendingValue.isEmpty()) {
                        "Unexpected pending value '$pendingValue' before line '$line'"
                    }
                }

                if (line.startsWith("[")) {
                    require(line.endsWith("]"))
                    if (currentSectionMap.isNotEmpty()) {
                        result[currentSectionName] = buildJsonObject {
                            for ((key, value) in currentSectionMap) {
                                put(key, value)
                            }
                        }
                    }
                    currentSectionName = line.substring(1, line.length - 1)
                    currentSectionMap.clear()
                } else {
                    val eq = line.indexOf('=')
                    val col = line.indexOf(':')
                    val cut = if (eq == -1) col else if (col == -1) eq else Math.min(col, eq)
                    require(cut != -1) {
                        "Unexpected line: '$line'"
                    }
                    pendingKey = line.substring(0, cut).trim()
                    pendingValue = line.substring(cut + 1).trim()
                }
            }
        }
        if (pendingKey.isNotEmpty()) {
            currentSectionMap[pendingKey] = Json.parseToJsonElement(pendingValue)
        } else {
            require(pendingValue.isEmpty()) {
                "Unexpected pending value '$pendingValue' at EOF"
            }
        }
        if (currentSectionMap.isNotEmpty()) {
            result[currentSectionName] = buildJsonObject {
                for ((key, value) in currentSectionMap) {
                    put(key, value)
                }
            }
        }
        return result.toMap()
    }
}