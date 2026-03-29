package org.kobjects.mosaic.model.function

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.tomson.TomsonOutput

class Functions : Iterable<FunctionSpec> {
    private val functionMap = mutableMapOf<String, FunctionSpec>()

    fun add(function: FunctionSpec) {
        functionMap[function.fqName.lowercase()] = function
    }

    override fun iterator() = functionMap.values.iterator()

    operator fun get(name: String): FunctionSpec? = functionMap[name.lowercase()]


    fun serialize(output: TomsonOutput, tag: Long) {
        val json = buildJsonObject() {
            for (function in this@Functions) {
                if (function.tag > tag) {
                    put(function.fqName, function.toJson())
                }
            }
        }
        output.appendSection("functions", json)
    }


}