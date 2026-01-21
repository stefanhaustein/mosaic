package org.kobjects.mosaic.pluginapi

interface RangeValues : Iterable<Any?> {
    val width: Int
    val height: Int

    // Order matches name, e.g A12
    operator fun get(column: Int, row: Int): Any?
}