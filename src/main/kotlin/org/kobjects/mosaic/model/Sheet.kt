package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.ModificationToken


class Sheet(
    val name: String,
    var tag: Long = 0L
) {
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

    fun serialize(tag: Long, forClient: Boolean): String {
        val sb = StringBuilder()
        if (deleted) {
            if (forClient) {
                sb.append("[sheets.$name]\n\ndeleted: true\n\n")
            }
        } else {
            if (highlightTag > tag) {
                sb.append("[sheets.$name]\n\nhighlighted: [")
                    .append(highlighted.joinToString(",") { """"${it.toStringLocal()}""""})
                    .append("]\n\n")
            }

            sb.append("[sheets.$name.cells]\n")
            var previousRow = -1
            for (cell in cells.values.sortedBy { it.row * 10000 + it.column }) {
                if (cell.row != previousRow) {
                    previousRow = cell.row
                    sb.append("\n")
                }
                cell.serialize(sb, tag, forClient)
            }
        }
        return sb.toString()
    }

    fun parseToml(cells: Map<String, Any?>, token: ModificationToken) {
        for ((key, value) in cells) {
            try {
                getOrCreateCell(key).setJson(value as Map<String, Any>, token)
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

    fun paste(token: ModificationToken, targetSelectionRange: CellRangeReference, tomson: Map<String, Map<String, Any?>>) {
        val rawSourceRange = tomson[""]!!["range"] as String
        val sourceRange = CellRangeReference.parse(rawSourceRange)

        val targetRange = CellRangeReference(
            targetSelectionRange.sheet,
            targetSelectionRange.fromColumn,
            targetSelectionRange.fromRow,
            targetSelectionRange.fromColumn + sourceRange.width - 1,
            targetSelectionRange.fromRow + sourceRange.width - 1)

        targetRange.clear(token)

        val offsetX = targetSelectionRange.fromColumn - sourceRange.fromColumn
        val offsetY = targetSelectionRange.fromRow - sourceRange.fromRow

        for ((key, value) in tomson["cells"]!!) {
            try {
                val column = Cell.getColumn(key) + offsetX
                val row = Cell.getRow(key) + offsetY
                getOrCreateCell(Cell.id(column, row)).setJson(value as Map<String, Any>, token)
            } catch (e: Exception) {
                System.err.println("Error parsing cell $key = $value")
                e.printStackTrace()
            }
        }
    }

}