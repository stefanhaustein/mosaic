package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.AbstractDescriptor
import org.kobjects.mosaic.model.AbstractPortDescriptor
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Node

// Can't be an abstract class because ExpressionNode already is a superclass of OutputPortHolder.
interface PortNode: Node {
    val name: String
    val fqName: String
        get() = owner.name + "." + name

    var jsonConfiguration: JsonObject
    var deleted: Boolean

    val displayName: String?
        get() = null
    
    val category: String?
        get() = null

    val specification: AbstractPortDescriptor

    override val owner: Integration

    fun configure(json: JsonObject, token: ModificationToken) {
        jsonConfiguration =  json["configuration"]?.jsonObject ?: JsonObject(emptyMap())
        tag = token.tag
        val unmodifiable = specification.modifiers.contains(AbstractDescriptor.Modifier.UNINSTANTIABLE)
        if (!unmodifiable) {
            detach()
        }
        deleted = json["deleted"]?.jsonPrimitive?.booleanOrNull ?: false
        if (deleted) {
            token.symbolsChanged = true
        } else if (!unmodifiable) {
            try {
                val config = specification.convertConfiguration(jsonConfiguration)
                configureInternal(config, token)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun configureInternal(config: Map<String, Any?>, token: ModificationToken)

    fun needsSaving() = !specification.modifiers.contains(AbstractDescriptor.Modifier.UNINSTANTIABLE)

    override fun qualifiedId() = if (owner == null) name else owner?.name + "." + name

    fun serialize(builder: JsonObjectBuilder, forClient: Boolean) {
        builder.put("kind", JsonPrimitive(specification.name))
        val type = specification.type
        if (type != null) {
            builder.put("type", type.toJson())
        }
        if (category != null) {
            builder.put("category", JsonPrimitive(category))
        }
        if (displayName != null) {
            builder.put("displayName", JsonPrimitive(displayName))
        }
        builder.put("configuration", jsonConfiguration)
        builder.put("c", serializeValue())
        if (forClient) {
            serializeDependencies(builder)
        }
    }

    fun toJson(forClient: Boolean) = buildJsonObject { serialize(this, forClient) }
}