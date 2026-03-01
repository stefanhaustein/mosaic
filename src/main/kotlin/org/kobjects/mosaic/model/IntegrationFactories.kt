package org.kobjects.mosaic.model


import org.kobjects.mosaic.pluginapi.IntegrationFactory

class IntegrationFactories : Iterable<IntegrationFactory> {

    private val factoryMap = mutableMapOf<String, IntegrationFactory>()

    fun add(factory: IntegrationFactory) {
        factoryMap[factory.fqName] = factory
    }

    operator fun get(name: String) = factoryMap[name]

    override fun iterator() = factoryMap.values.iterator()

    fun serialize(tag: Long): String {
        val sb = StringBuilder()
        for (factory in this) {
            if (factory.tag > tag) {
                sb.append(factory.fqName).append(": ")
                factory.legacyToJson(sb)
                sb.append('\n')
            }
        }
        return if (sb.isEmpty()) "" else "\n[integrations]\n\n$sb"
    }
}