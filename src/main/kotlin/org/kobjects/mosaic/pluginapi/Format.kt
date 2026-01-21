package org.kobjects.mosaic.pluginapi

import kotlin.math.roundToLong

object Format {

    fun formatNumber(value: Double, maxLength: Int = Int.MAX_VALUE): String {
        if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
            return "#"
        }
        var s = (value * 10).roundToLong().toString()
        if (s.length + 1 > maxLength) {
            return if (s.length - 1 <= maxLength) value.roundToLong().toString() else "#"
        }
        return s.substring(0, s.length - 1) + "." + s.substring(s.length - 1)
    }

}