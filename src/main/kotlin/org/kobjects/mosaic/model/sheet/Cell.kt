package org.kobjects.mosaic.model.sheet

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.tomson.ToJson
import org.kobjects.mosaic.model.ExpressionNode
import org.kobjects.mosaic.model.Node
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Namespace
import org.kobjects.mosaic.model.parser.ParsingContext

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

    var validation: JsonObject? = null

    override val inputs = mutableSetOf<Node>()
    override val outputs = mutableSetOf<Node>()

    val styles = mutableListOf<Style>()

    fun clear(modificationToken: ModificationToken) {
        setJson(JsonObject(emptyMap()), modificationToken)
        setValidation(null, modificationToken)
        styles.clear()
    }

    fun setJson(json: JsonObject, modificationToken: ModificationToken) {
        this.tag = modificationToken.tag
        rawFormula = json["f"]?.jsonPrimitive?.contentOrNull ?: ""

        val validation = json["v"]
        setValidation(if (validation is JsonObject) validation else null, modificationToken)


        styles.clear()
        // Legacy image support
        val image = json["i"]
        if (image is JsonPrimitive) {
            styles.add(Style(this, image = image.content))
        }
        for (jsonStyle in json["s"]?.jsonArray.orEmpty().filter{ it is JsonObject }) {
                styles.add(Style(this,
                    rawCondition = jsonStyle.jsonObject["condition"]?.jsonPrimitive?.contentOrNull,
                    image = jsonStyle.jsonObject["image"]?.jsonPrimitive?.contentOrNull,
                    rotation = jsonStyle.jsonObject["rotation"]?.jsonPrimitive?.intOrNull,
                    color = jsonStyle.jsonObject["color"]?.jsonPrimitive?.contentOrNull,
                    background = jsonStyle.jsonObject["background"]?.jsonPrimitive?.contentOrNull,
            ))
        }

        reparse()

        modificationToken.formulaChanged = true
        modificationToken.addRefresh(this)
    }

    override fun reparse() {
        super.reparse()
        val parsingContext = ParsingContext(this)
        for (style in styles) {
            style.reparse(parsingContext)
        }
    }


    private fun setValidation(validation: JsonObject?, modificationToken: ModificationToken) {
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
                if (styles.isNotEmpty()) {
                    put("s", buildJsonArray {
                        styles.forEach {
                            add(buildJsonObject {
                                if (it.rawCondition != null) {
                                    put("condition", JsonPrimitive(it.rawCondition))
                                }
                                if (it.image != null) {
                                    put("image", JsonPrimitive(it.image))
                                }
                                if (it.rotation != null) {
                                    put("rotation", JsonPrimitive(it.rotation))
                                }
                                if (it.color != null) {
                                    put("color", JsonPrimitive(it.color))
                                }
                                if (it.background != null) {
                                    put("background", JsonPrimitive(it.background))
                                }
                            })
                        }
                    })
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