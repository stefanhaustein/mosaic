package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Node
import org.kobjects.tomson.toJson

// Can't be an abstract class because ExpressionNode already is a superclass of OutputPortHolder.
interface PortHolder: Node {

    val name: String
    val tag: Long
    val configuration: Map<String, Any?>
    val fqName: String
        get() = owner.name + "." + name

    val displayName: String?
        get() = null
    
    val category: String?
        get() = null

    val specification: AbstractArtifactSpec

    override val owner: Integration

    fun attach(token: ModificationToken)

    fun needsSaving() = !specification.modifiers.contains(AbstractArtifactSpec.Modifier.UNINSTANTIABLE)

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
        builder.put("configuration", configuration.toJson())
        builder.put("c", serializeValue())
        if (forClient) {
            serializeDependencies(builder)
        }
    }

    fun toJson(forClient: Boolean) = buildJsonObject { serialize(this, forClient) }
}