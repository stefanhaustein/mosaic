package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.FunctionSpec

class Functions : Iterable<FunctionSpec> {
    private val functionMap = mutableMapOf<String, FunctionSpec>()

    fun add(function: FunctionSpec) {
        functionMap[function.name.lowercase()] = function
    }

    override fun iterator() = functionMap.values.iterator()

    operator fun get(name: String): FunctionSpec? = functionMap[name.lowercase()]


    fun serialize(tag: Long): String {
        val sb = StringBuilder()
        for (function in this) {
            if (function.tag > tag) {
                sb.append(function.name).append(": ")
                function.toJson(sb)
                sb.append('\n')
            }
        }
        return if (sb.isEmpty()) "" else "[functions]\n\n$sb"
    }

}