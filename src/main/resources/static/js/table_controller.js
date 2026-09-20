import {
    getCurrentCellElement,
    currentCell,
    selectCell,
    selectionRangeY,
    selectionRangeX,
} from "./shared_state.js"
import {
    selectPanel
} from "./sidepanel_menu_controller.js";

import {getColumn, getRow, toCellId} from "./lib/utils.js";

let spreadsheetTBodyElement = document.getElementById("spreadsheetTBody")
let formulaInputElement = document.getElementById("formulaInput")



spreadsheetTBodyElement.addEventListener("keydown", tableKeyPress)


var dragOrigin = null
var dragRangeSelection = false

spreadsheetTBodyElement.addEventListener(
    "click", (event) => {
        dragRangeSelection = false
    })

spreadsheetTBodyElement.addEventListener(
    "dblclick", (event) => {
        selectCell(event.target.id|| event.target.parentNode.id)
        selectPanel("Details")
        formulaInputElement.focus()
    })


function findCell(element) {
    let cell = (element.localName == "td") ? element : element.parentNode
    return (cell.localName == "td" && cell.id) ? cell : null

}

spreadsheetTBodyElement.addEventListener("mousedown", (event) => {
    let cell = findCell(event.target)
    if (cell != null) {
        dragOrigin = cell.id
    }
    dragRangeSelection = false
});

spreadsheetTBodyElement.addEventListener("mouseup", (event) => {
    dragOrigin = null
    if (dragRangeSelection) {
        spreadsheetTBodyElement.style.userSelect = ""
    }
});


spreadsheetTBodyElement.addEventListener("mousemove", (event) => {
    // reset the transparency

    if (!dragOrigin) {
        return
    }
    let cell = findCell(event.target)
    if (cell == null) {
        return
    }
    if (cell == dragOrigin) {
        if (dragRangeSelection) {
            selectCell(dragOrigin)
        }
        return
    }
    let targetId = cell.id

    if (!dragRangeSelection) {
        dragRangeSelection = true
        spreadsheetTBodyElement.style.userSelect = "none"
    }

    let targetColumn = getColumn(targetId)
    let targetRow = getRow(targetId)

    let originColum = getColumn(dragOrigin)
    let originRow = getRow(dragOrigin)

    console.log("dragOrigin", dragOrigin, [originColum, originRow], "target", targetId, [targetColumn, targetRow])

    if (targetRow != originRow || targetColumn != originColum) {
        cell.scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })
    }

    selectCell(dragOrigin,  targetColumn - originColum, targetRow - originRow)
});



function tableKeyPress(event) {
    let matched = true
    switch (event.key) {
        case "ArrowDown":
            moveCursor(0, 1, event)
            break

        case "ArrowUp":
            moveCursor(0, -1, event)
            break

        case "ArrowLeft":
            moveCursor(-1, 0, event)
            break

        case "ArrowRight":
            moveCursor(1, 0, event)
            break

        case "Enter":
            formulaInputElement.focus()
            break

        default:
            matched = false
    }
    if (matched) {
        event.preventDefault()
    }
}

function moveCursor(dx, dy, event) {
    let cellId = toCellId(getColumn(currentCell.key) + dx, getRow(currentCell.key) + dy)

    if (event.shiftKey) {
        selectCell(cellId, selectionRangeX - dx, selectionRangeY - dy)
    } else {
        selectCell(cellId)
    }

    getCurrentCellElement().scrollIntoView({ behavior: "smooth", block: "center", inline: "center" })

}


