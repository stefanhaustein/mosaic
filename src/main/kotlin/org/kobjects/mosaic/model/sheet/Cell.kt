package org.kobjects.mosaic.model.sheet

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.json.LegacyToJson
import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.legacyToJson
import org.kobjects.mosaic.model.ExpressionNode
import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Namespace

class Cell(
    val sheet: Sheet,
    val id: String
) : ExpressionNode(sheet), Iterable<Cell>, LegacyToJson {

    val column: Int
        get() = getColumn(id)
    val row: Int
        get() = getRow(id)

    override val owner: Namespace
        get() = sheet


    var image: String? = null

    var validation: JsonObject? = null

    override val inputs = mutableSetOf<Node>()
    override val outputs = mutableSetOf<Node>()


    fun clear(modificationToken: ModificationToken) {
        setFormula("", modificationToken)
        setImage("", modificationToken)
        setValidation(null, modificationToken)
    }

    fun setImage(path: String, modificationToken: ModificationToken) {
        image = path
        formulaTag = modificationToken.tag
        modificationToken.formulaChanged = true
    }

    fun setJson(json: JsonObject, modificationToken: ModificationToken) {
        val formula = json["f"]?.jsonPrimitive?.contentOrNull
        if (formula != null) {
            setFormula(formula, modificationToken)
        }
        val validation = json["v"]
        setValidation(if (validation is JsonObject) validation else null, modificationToken)

        val image = json["i"]
        if (image is JsonPrimitive) {
            setImage(image.jsonPrimitive.content, modificationToken)
        } else {
            setImage("", modificationToken)
        }
    }


    fun setValidation(validation: JsonObject?, modificationToken: ModificationToken) {
        if (validation != this.validation) {
            this.validation = validation
            modificationToken.formulaChanged = true
            formulaTag = modificationToken.tag
        }
    }


    fun serializeValue(sb: StringBuilder) {
        val value = this.value
        when (value) {
            null,
                is Unit -> {sb.append("null")}
            is Exception -> sb.append("""{"type": "err", "msg": ${(value::class.simpleName.toString() + value.message).quote()}}""")
            is Instant -> {
                val localDateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
                /* sb.append(localDateTime.date.format(LocalDate.Formats.ISO))
                 sb.append(' ') */
                sb.append("""{"type": "instant", "rendered":${localDateTime.time.format(TIME_FORMAT_SECONDS).quote()}}""")
            }
            else -> value.legacyToJson(sb)
        }
    }

    fun serialize(sb: StringBuilder, tag: Long, forClient: Boolean) {
        val id = id
        if (formulaTag > tag) {
            val properties = mutableListOf<String>()
            if (!rawFormula.isNullOrEmpty()) {
                properties.add("\"f\": ${rawFormula.quote()}")
            }
            if (validation?.isNotEmpty() == true) {
                properties.add("\"v\": ${validation.legacyToJson()}")
            }
            if (!image.isNullOrBlank()) {
                properties.add("\"i\": ${image!!.quote()}")
            }
            if (forClient) {
                val inner = StringBuilder()
                inner.append("\"c\":")
                serializeValue(inner)
                serializeDependencies(inner)
                properties.add(inner.toString())
            }
            if (properties.isNotEmpty()) {
                sb.append("$id = {")
                sb.append(properties.joinToString(", "))
                sb.append("}\n")
            }
        } else if (valueTag > tag) {
            sb.append("$id.c: ")
            serializeValue(sb)
            sb.append('\n')
        }
    }

    override fun legacyToJson(sb: StringBuilder) {
        serialize(sb, -1, false)
    }

    override fun qualifiedId() = "${sheet.name}!$id"

    override fun iterator(): Iterator<Cell> = setOf(this).iterator()

    override fun toString() = qualifiedId() + ":" + rawFormula// rawFormula

    companion object {
        val TIME_FORMAT_MINUTES = LocalTime.Format {
            hour(); char(':'); minute(); // char(':'); second()
        }
        val TIME_FORMAT_SECONDS = LocalTime.Format {
            hour(); char(':'); minute(); char(':'); second()
        }

        fun id(column: Int, row: Int) = (column + 65).toChar().toString() + row

        fun getColumn(key: String) = key[0].uppercaseChar().code - 'A'.code

        fun getRow(key: String) = key.substring(1).toInt()

    }
}