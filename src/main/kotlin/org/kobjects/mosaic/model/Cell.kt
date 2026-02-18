package org.kobjects.mosaic.model

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.kobjects.mosaic.json.ToJson
import org.kobjects.mosaic.json.quote
import org.kobjects.mosaic.json.toJson
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.expression.Expression
import org.kobjects.mosaic.model.expression.Literal
import org.kobjects.mosaic.model.parser.ParsingContext
import org.kobjects.mosaic.model.parser.TcFormulaParser
import org.kobjects.mosaic.pluginapi.ModificationToken
import org.kobjects.mosaic.pluginapi.Namespace

class Cell(
    val sheet: Sheet,
    val id: String
) : ExpressionNode(sheet), Iterable<Cell>, ToJson {

    val column: Int
        get() = getColumn(id)
    val row: Int
        get() = getRow(id)

    override val owner: Namespace
        get() = sheet


    var image: String? = null

    var validation: Map<String, Any?>? = null

    override val inputs = mutableSetOf<Node>()
    override val outputs = mutableSetOf<Node>()


    fun clear(modificationToken: ModificationToken) {
        setFormula("", modificationToken)
        setImage("", modificationToken)
        setValidation(emptyMap(), modificationToken)
    }

    fun setImage(path: String, modificationToken: ModificationToken) {
        image = path
        formulaTag = modificationToken.tag
        modificationToken.formulaChanged = true
    }

    fun setJson(json: Map<String, Any?>, modificationToken: ModificationToken) {
       val formula = json["f"]
       if (formula != null) {
           setFormula(formula.toString(), modificationToken)
       }
       val validation = json["v"]
        setValidation(if (validation == null || validation == Unit || (validation as Map<*,*>).isEmpty()) null
            else validation as Map<String, Any?>, modificationToken)

        val image = json["i"]
        if (image is String) {
            setImage(image, modificationToken)
        } else {
            setImage("", modificationToken)
        }
    }


    fun setValidation(validation: Map<String, Any?>?, modificationToken: ModificationToken) {
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
            else -> value.toJson(sb)
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
                properties.add("\"v\": ${validation.toJson()}")
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

    override fun toJson(sb: StringBuilder) {
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