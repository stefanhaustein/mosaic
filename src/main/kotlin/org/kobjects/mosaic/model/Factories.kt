package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.AbstractFactorySpec

class Factories : Iterable<AbstractFactorySpec> {

    private val factoryMap = mutableMapOf<String, AbstractFactorySpec>()

    fun add(factory: AbstractFactorySpec) {
        factoryMap[factory.name] = factory
    }

    operator fun get(name: String) = factoryMap[name]

    override fun iterator() = factoryMap.values.iterator()

    fun serialize(tag: Long): String {
        val sb = StringBuilder()
        for (factory in this) {
            if (factory.tag > tag) {
                sb.append(factory.name).append(": ")
                factory.toJson(sb)
                sb.append('\n')
            }
        }
        return if (sb.isEmpty()) "" else "[factories]\n\n$sb"
    }
}