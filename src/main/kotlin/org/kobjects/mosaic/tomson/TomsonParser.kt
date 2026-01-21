package org.kobjects.mosaic.tomson

import org.kobjects.mosaic.json.JsonParser

object TomsonParser {

    fun parse(input: String): Map<String, Map<String, Any?>> {
        val result = mutableMapOf<String, Map<String, Any?>>()
        var currentSectionMap = mutableMapOf<String, Any?>()
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
                    currentSectionMap[pendingKey] = JsonParser.parse(pendingValue)
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
                        result[currentSectionName] = currentSectionMap.toMap()
                    }
                    currentSectionName = line.substring(1, line.length - 1)
                    currentSectionMap = mutableMapOf<String, Any?>()
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
            currentSectionMap[pendingKey] = JsonParser.parse(pendingValue)
        } else {
            require(pendingValue.isEmpty()) {
                "Unexpected pending value '$pendingValue' at EOF"
            }
        }
        if (currentSectionMap.isNotEmpty()) {
            result[currentSectionName] = currentSectionMap.toMap()
        }
        return result.toMap()
    }

}