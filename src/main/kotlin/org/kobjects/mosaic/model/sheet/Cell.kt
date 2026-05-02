package org.kobjects.mosaic.model.sheet

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.tomson.ToJson
import org.kobjects.mosaic.model.ExpressionNode
import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Namespace

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
        tag = modificationToken.tag
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
            tag = modificationToken.tag
        }
    }

    fun serialize(builder: JsonObjectBuilder, tag: Long, forClient: Boolean) {
        val id = id
        if (this@Cell.tag > tag) {
            val properties = buildJsonObject {
                if (!rawFormula.isNullOrEmpty()) {
                    put("f", JsonPrimitive(rawFormula))
                }
                val validation = validation
                if (validation?.isNotEmpty() == true) {
                    put("v", validation)
                }
                if (!image.isNullOrBlank()) {
                    put("i", JsonPrimitive(image))
                }
                if (forClient) {
                    put("c", serializeValue())
                    serializeDependencies(this)
                }
            }
            if (properties.isNotEmpty()) {
                builder.put(id, properties)
            }

        } else if (valueTag > tag) {
            builder.put("$id.c", serializeValue())
        }
    }

    override fun toJson() = buildJsonObject {
        serialize(this, -1, false)
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