package org.kobjects.mosaic.model

import org.kobjects.mosaic.pluginapi.ModificationToken
import java.lang.Integer.max
import java.lang.Integer.min

data class CellRangeReference(
    val sheet: Sheet,
    val fromColumn: Int,
    val fromRow: Int,
    val toColumn: Int,   // Inclusive
    val toRow: Int      // Inclusive

): Iterable<Cell> {

    val width: Int
        get() = toColumn - fromColumn + 1

    val height: Int
        get() = toRow - fromRow + 1

    fun clear(token: ModificationToken) {
        for (cell in iterator(true)) {
            cell.clear(token)
        }
    }

    operator fun get(x: Int, y: Int) =
        sheet.getOrCreateCell(Cell.id(x + fromColumn, y + fromRow))

    fun iterator(filledOnly: Boolean): Iterator<Cell> {
        val result = mutableListOf<Cell>()
        for (r in fromRow..toRow) {
            for (c in fromColumn..toColumn) {
                val key = Cell.id(c, r)
                if (filledOnly) {
                    val cell = sheet.cells[key]
                    if (cell != null) {
                        result.add(cell)
                    }
                } else {
                    result.add(sheet.getOrCreateCell(key))
                }
            }
        }
        return result.iterator()
    }

    fun toStringLocal(): String {
        val from = Cell.id(fromColumn, fromRow)
        if (fromColumn == toColumn && fromRow == toRow) {
            return from
        }
        return from + ":" + Cell.id(toColumn, toRow)
    }

    override fun toString() = sheet.name + "!" + toStringLocal()

    override fun iterator(): Iterator<Cell> = iterator(false)

    fun keyIterator(filledOnly: Boolean = false): Iterator<String> {
        val result = mutableListOf<String>()
        for (r in fromRow..toRow) {
            for (c in fromColumn..toColumn) {
                val key = Cell.id(c, r)
                if (!filledOnly || sheet.cells[key] != null) {
                    result.add(key)
                }
            }
        }
        return result.iterator()
    }

    companion object {


        fun parse(name: String, impliedSheet: Sheet? = null): CellRangeReference {
            val cut0 = name.indexOf("!")
            val sheet: Sheet
            if (cut0 == -1) {
                require(impliedSheet != null) {
                    "Cannot parse cell reference '${name}' without explicit sheet reference"
                }
                sheet = impliedSheet
            } else {
                val sheetName = name.substring(0, cut0)
                sheet = Model.sheets[sheetName] ?: throw IllegalArgumentException(
                    "Sheet '$sheetName' not found."
                )
            }
            val localName = name.substring(name.indexOf('!') + 1)

            val cut = localName.indexOf(":")
            if (cut == -1) {
                val cell = sheet.getOrCreateCell(localName)
                return CellRangeReference(sheet, cell.column, cell.row, cell.column, cell.row)
            }
            val cell1 = sheet.getOrCreateCell(localName.substring(0, cut))
            val cell2 = sheet.getOrCreateCell(localName.substring(cut + 1))
            return CellRangeReference(
                sheet,
                min(cell1.column, cell2.column),
                min(cell1.row, cell2.row),
                max(cell1.column, cell2.column),
                max(cell1.row, cell2.row)
            )
        }


    }


}