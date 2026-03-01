package org.kobjects.mosaic.json

fun String.escape(sb: StringBuilder) {
    for (c in this) {
        when (c) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> sb.append(c)
        }
    }
}

fun String.escape() = StringBuilder().also { escape(it) }.toString()

fun String.quote() = """"${escape()}""""

fun Any?.legacyToJson() = StringBuilder().also { this.legacyToJson(it) }.toString()

fun Any?.legacyToJson(sb: StringBuilder) {
    when (this) {
        null -> sb.append("null")
        is LegacyToJson -> this.legacyToJson(sb)
        is Double -> if (this.isFinite()) sb.append(this) else this.toString().legacyToJson(sb)
        is Float -> if (this.isFinite()) sb.append(this) else this.toString().legacyToJson(sb)
        is Number -> sb.append(this)
        is Boolean -> sb.append(this)
        is Map<*, *> -> this.legacyToJson(sb)
        is Iterable<*> -> this.legacyToJson(sb)
        else -> sb.append(this.toString().quote())
    }
}

fun Iterable<*>.legacyToJson(sb: StringBuilder) {
    var first = true
    sb.append('[')
    for (v in this) {
        if (first) {
            first = false
        } else {
            sb.append(",")
        }
        v.legacyToJson(sb)
    }
    sb.append(']')
}

fun Map<*, *>.legacyToJson(sb: StringBuilder) {
    var first = true
    sb.append('{')
    for ((k, v) in this) {
        if (first) {
            first = false
        } else {
            sb.append(",")
        }
        sb.append(k.toString().quote()).append(":")
        v.legacyToJson(sb)
    }
    sb.append('}')
}