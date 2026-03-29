package org.kobjects.mosaic.model.integration

import kotlinx.serialization.json.JsonObject
import org.kobjects.mosaic.model.AbstractArtifactSpec
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Node

// Can't be an abstract class because ExpressionNode already is a superclass of OutputPortHolder.
interface PortHolder: Node {

    val name: String
    val tag: Long

    val fqName: String
        get() = owner.name + "." + name

    val displayName: String?
        get() = null
    
    val category: String?
        get() = null

    val specification: AbstractArtifactSpec

    override val owner: Integration

    fun attach(token: ModificationToken)

    override fun qualifiedId() = if (owner == null) name else owner?.name + "." + name

    fun toJson(forClient: Boolean): JsonObject
}