package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.buildJsonObject
import org.kobjects.tomson.TomsonOutput


class IntegrationFactories : Iterable<IntegrationDescriptor> {

    private val factoryMap = mutableMapOf<String, IntegrationDescriptor>()

    fun add(factory: IntegrationDescriptor) {
        factoryMap[factory.fqName] = factory
    }

    operator fun get(name: String) = factoryMap[name]

    override fun iterator() = factoryMap.values.iterator()

    fun serialize(out: TomsonOutput, tag: Long) {
        val values = buildJsonObject {
            for (factory in this@IntegrationFactories) {
               if (factory.tag > tag) {
                   put(factory.fqName, factory.toJson())
               }
            }
        }
        if (!values.isEmpty()) {
            out.appendSection("integrations", values = values)
        }
    }
}