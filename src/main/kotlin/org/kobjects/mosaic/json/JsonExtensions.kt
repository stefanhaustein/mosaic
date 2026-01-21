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

fun Any?.toJson() = StringBuilder().also { this.toJson(it) }.toString()

fun Any?.toJson(sb: StringBuilder) {
    when (this) {
        null -> sb.append("null")
        is ToJson -> this.toJson(sb)
        is Double -> if (this.isFinite()) sb.append(this) else this.toString().toJson(sb)
        is Float -> if (this.isFinite()) sb.append(this) else this.toString().toJson(sb)
        is Number -> sb.append(this)
        is Boolean -> sb.append(this)
        is Map<*, *> -> this.toJson(sb)
        is Iterable<*> -> this.toJson(sb)
        else -> sb.append(this.toString().quote())
    }
}

fun Iterable<*>.toJson(sb: StringBuilder) {
    var first = true
    sb.append('[')
    for (v in this) {
        if (first) {
            first = false
        } else {
            sb.append(",")
        }
        v.toJson(sb)
    }
    sb.append(']')
}

fun Map<*, *>.toJson(sb: StringBuilder) {
    var first = true
    sb.append('{')
    for ((k, v) in this) {
        if (first) {
            first = false
        } else {
            sb.append(",")
        }
        sb.append(k.toString().quote()).append(":")
        v.toJson(sb)
    }
    sb.append('}')
}