package org.kobjects.mosaic.pluginapi

import org.kobjects.mosaic.model.Node

/**
 * The token is used to collect changes. After the collection, where the result can't be influenced any longer,
 * only the tag should be used.
 */
class ModificationToken() {

    val tag: Long = System.nanoTime()

    var loading = false
    var formulaChanged = false
    var symbolsChanged = false

    val refreshRoots = mutableSetOf<Node>()
    val refreshNodes = mutableSetOf<Node>()

    fun addRefresh(node: Node) {
        if (node.inputs.isEmpty()) {
            refreshRoots.add(node)
        } else {
            refreshNodes.add(node)
        }
    }

    fun addAllDependencies(node: Node) {
        for (dep in node.outputs) {
            if (refreshNodes.add(dep)) {
                addAllDependencies(dep)
            }
        }
    }


}