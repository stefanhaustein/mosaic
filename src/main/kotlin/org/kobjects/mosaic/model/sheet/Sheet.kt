package org.kobjects.mosaic.model.sheet

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.mosaic.model.ModificationToken
import org.kobjects.mosaic.model.Namespace
import org.kobjects.tomson.TomsonOutput
import org.kobjects.tomson.toJson
import kotlin.collections.iterator

class Sheet(
    name: String,
    var tag: Long = 0L
) : Namespace(name) {
    val highlighted = mutableSetOf<CellRangeReference>()
    var highlightTag: Long = 0L
    val cells = mutableMapOf<String, Cell>()
    var deleted = false

    fun set(cellId: String, value: String, modificationToken: ModificationToken) {
        val cell = getOrCreateCell(cellId)
        cell.setFormula(value, modificationToken)
    }

    fun delete(token: ModificationToken) {
        deleted = true
        tag = token.tag
        cells.clear()
        token.symbolsChanged = true
    }

    fun setHighlight(tag: Long, cellRangeReference: CellRangeReference, value: Boolean) {
        if (value) {
            highlighted.add(cellRangeReference)
        } else {
            highlighted.remove(cellRangeReference)
        }
        highlightTag = tag
    }

    fun serialize(tomson: TomsonOutput, tag: Long, forClient: Boolean) {
        if (deleted) {
            if (forClient) {
                tomson.appendSection("sheets.$name", JsonObject(mapOf("deleted" to JsonPrimitive(true))))
            }
        } else {
            if (highlightTag > tag) {
                tomson.appendSection("sheets.$name", JsonObject(mapOf("highlighted" to JsonArray(highlighted.map { it.toJson() }))))
            }

            val json = buildJsonObject {
                for (cell in cells.values.sortedBy { it.row * 10000 + it.column }) {
                    cell.serialize(this, tag, forClient)
                }
            }
            tomson.appendSection("sheets.$name.cells", json)
        }
    }

    fun parseToml(cells: JsonObject, token: ModificationToken) {
        for ((key, value) in cells) {
            try {
                getOrCreateCell(key).setJson(value as JsonObject, token)
            } catch (e: Exception) {
                System.err.println("Error parsing cell $key = $value")
                e.printStackTrace()
            }
        }
    }

    fun getOrCreateCell(cellId: String): Cell {
        return cells.getOrPut(cellId) { Cell(this, cellId) }
    }

    fun clear(modificationToken: ModificationToken) {
        for (cell in cells.values) {
            cell.setFormula("", modificationToken)
        }
    }

    fun paste(token: ModificationToken, targetSelectionRange: CellRangeReference, tomson: Map<String, JsonObject>) {
        val rawSourceRange = tomson[""]!!["range"]!!.jsonPrimitive.content
        val sourceRange = CellRangeReference.parse(rawSourceRange)

        val targetRange = CellRangeReference(
            targetSelectionRange.sheet,
            targetSelectionRange.fromColumn,
            targetSelectionRange.fromRow,
            targetSelectionRange.fromColumn + sourceRange.width - 1,
            targetSelectionRange.fromRow + sourceRange.width - 1
        )

        targetRange.clear(token)

        val offsetX = targetSelectionRange.fromColumn - sourceRange.fromColumn
        val offsetY = targetSelectionRange.fromRow - sourceRange.fromRow

        for ((key, value) in tomson["cells"]!!) {
            try {
                val column = Cell.getColumn(key) + offsetX
                val row = Cell.getRow(key) + offsetY
                getOrCreateCell(Cell.id(column, row)).setJson(if (value is JsonObject) value else buildJsonObject {  }, token)
            } catch (e: Exception) {
                System.err.println("Error parsing cell $key = $value")
                e.printStackTrace()
            }
        }
    }

}