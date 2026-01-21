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

class Cell(
    val sheet: Sheet,
    id: String
) : Node, Iterable<Cell>, ToJson {

    val column = getColumn(id)
    val row = getRow(id)


    val id: String
        get() = id(column, row)

    var rawFormula = ""
    var image: String? = null

    var validation: Map<String, Any?>? = null

    var expression: Expression = Literal(Unit)
    override var value: Any? = null

    override var valueTag = 0L
    var formulaTag = 0L

    override val inputs = mutableSetOf<Node>()
    override val outputs = mutableSetOf<Node>()


    override fun recalculateValue(tag: Long): Boolean {
        var newValue: Any?
        try {
            newValue = expression.eval(EvaluationContext(tag))
        } catch (e: Exception) {
            e.printStackTrace()
            newValue = e
        }
        return if (newValue == value) false else {
            value = newValue
            valueTag = tag
            true
        }
    }

    override fun detach() {
        clearDependsOn()
    }

    fun clearDependsOn() {
        for (dep in inputs) {
            dep.outputs.remove(this)
        }
        inputs.clear()
    }

    fun reparse() {
        clearDependsOn()
        expression.detachAll()
        expression = if (rawFormula.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = TcFormulaParser.parseExpression(rawFormula.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                e.printStackTrace()
                Literal(e)
            }
        } else {
            when (rawFormula.lowercase()) {
                "true" -> Literal(true)
                "false" -> Literal(false)
                else -> {
                    try {
                        Literal(Values.parseNumber(rawFormula))
                    } catch (e: Exception) {
                        Literal(rawFormula)
                    }
                }
            }
        }
    }

    fun clear(modificationToken: ModificationToken) {
        setFormula("", modificationToken)
        setImage("", modificationToken)
        setValidation(emptyMap(), modificationToken)
    }

    fun setFormula(value: String, modificationToken: ModificationToken) {
        if (value != rawFormula) {
            rawFormula = value
            reparse()
            formulaTag = modificationToken.tag
            modificationToken.formulaChanged = true
            modificationToken.addRefresh(this)
        }
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