package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.*

// Can't be an abstract class because ExpressionNode already is a superclass of OutputPortHolder.
interface PortHolder:  Node {

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

    fun toJson(sb: StringBuilder, forClient: Boolean)

    fun attach(token: ModificationToken)

    override fun qualifiedId() = if (owner == null) name else owner?.name + "." + name

}