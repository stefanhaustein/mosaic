package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.RangeValues

class CellRangeValues(val cellRange: CellRangeReference): RangeValues {

    val values = Array<Any?>(cellRange.width * cellRange.height) {
        cellRange[it % cellRange.width, it / cellRange.width].value }

    override fun toString() = "[$values]"

    override val width: Int
        get() = cellRange.width
    override val height: Int
        get() = cellRange.height

    override fun get(column: Int, row: Int) = values[row * cellRange.width + column]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RangeValues) return false
        if (width != other.width || height != other.height) return false

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (other[x, y] != this[x, y]) {
                    return false
                }
            }
        }
        return true
    }

    override fun iterator(): Iterator<Any?> = values.iterator()

}