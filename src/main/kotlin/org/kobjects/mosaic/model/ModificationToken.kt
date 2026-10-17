package org.kobjects.mosaic.model

/**
 * The token is used to collect changes. After the collection, where the result can't be influenced any longer,
 * only the tag should be used.
 */
class ModificationToken(private var _modificationKind: ModificationKind = ModificationKind.UNMODIFIED) {

    val tag: Long = System.nanoTime()
    val modificatioKind: ModificationKind
    get() = _modificationKind


    val refreshNodes = mutableSetOf<Node>()

    fun addRefresh(kind: ModificationKind, vararg node: Node) {
        refreshNodes.addAll(node)
        if (kind > _modificationKind) {
            _modificationKind = kind
        }
    }


    enum class ModificationKind {
        UNMODIFIED,
        VALUES,
        FORMULAS,
        STRUCTURE
    }

}