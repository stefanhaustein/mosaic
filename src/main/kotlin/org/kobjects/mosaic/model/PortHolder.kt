package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.*

// Can't be an abstract class because ExpressionNode already is a superclass of OutputPortHolder.
interface PortHolder:  Node {

    val name: String
    val tag: Long

    val displayName: String?
        get() = null
    val category: String?
        get() = null

    fun toJson(sb: StringBuilder, forClient: Boolean)

    fun attach(token: ModificationToken)

    override fun qualifiedId() = name

}