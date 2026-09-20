import {currentSheet, selectCell, setCurrentCellFormula} from "./shared_state.js";

import {getColumn, getRow, toCellId} from "./lib/utils.js";
import {InputController} from "./forms/input_controller.js";

export function renderCell(key) {
    let cellElement = document.getElementById(key)
    if (cellElement == null) {
        console.log("Element for cell key '" + key + "' not found.'")
        return;
    }

    let classes = cellElement.classList
    classes.remove("c", "e", "i", "r", "l", "u")
    cellElement.removeAttribute("title")
    cellElement.textContent = ""
    cellElement.style = ""

    let cellData = currentSheet.cells[key]
    if (cellData == null) {
        cellData = {}
    }

    let targetElement = cellElement
    let styles = cellData["s"]
    if (Array.isArray(styles)) {
        let container = document.createElement("div")
        container.style.display = "grid"
        cellElement.style.padding = "0"
        cellElement.append(container)
        for (let style of styles) {
            let layer = document.createElementNS("http://www.w3.org/2000/svg", "svg")
            layer.style.gridArea = "1/1"
            layer.style.width = "60px";
            layer.style.height = "60px";
            layer.style.rotate = (style.rotation || 0) + "deg"
            if (style.image != null) {
                let use = document.createElementNS("http://www.w3.org/2000/svg", "use")
                use.setAttribute("href", "img/cell/" + style.image)
                layer.append(use)
            }
            container.append(layer)
        }
        targetElement = document.createElement("div")
        targetElement.style.gridArea = "1/1"
        targetElement.style.padding = "0 3px"
        container.append(targetElement)
    }

    let value = cellData["c"]
    if (value == null) {
        value = ""
    }

    if (typeof value == "object" && value?.type == "err") {
        targetElement.textContent = "#REF"
        targetElement.classList.add("e")
        return
    }

    let validation = cellData["v"]
    if (validation?.type != null && validation?.type != "No User Input") {
        renderInput(targetElement, cellData)
        return
    }


    let renderedValue = value
    switch(typeof value) {
        case "bigint":
        case "number":
            classes.add("r")
            break

        case "boolean":
            classes.add("c")
            renderedValue = value ? "True" : "False"
            break

        case "string":
            if (value == "") {
                let col = getColumn(key)
                let row = getRow(key)
                let nextKey = toCellId(col + 1, row)
                let nextCell = currentSheet.cells[nextKey]
                if (nextCell?.f != null && nextCell.f.startsWith("=")) {
                    renderedValue = nextCell.f.substring(1).trim()
                    classes.add("i")
                    // This is necessary because table cells don't respect (max-)height properly.
                    if (renderedValue.length > 8) {
                        targetElement.textContent = ""
                        let div = document.createElement("div")
                        targetElement.appendChild(div)
                        div.textContent = renderedValue
                        return
                    }
                    break
                }
            }
            classes.add("l")
            break

        default:
            switch (value["type"]) {
                case "err":
                    renderedValue = "#REF"
                    classes.add("e")
                    break
                default:
                    classes.add("l")
                    renderedValue = JSON.stringify(value)
            }
    }

    targetElement.textContent = renderedValue
}

function renderInput(targetElement, cellData) {
    targetElement.textContent = ""
    targetElement.classList.add("u")

    let inputController = InputController.create(cellData.v, document.getElementById("globalErrorDiv"))
    let inputElement = inputController.inputElement
    inputElement.style.width = "100%"
    inputElement.style.height = "100%"
    targetElement.appendChild(inputElement)
    inputController.setValue(cellData["c"])
    inputElement.addEventListener("change", () => {
        setCurrentCellFormula(inputElement.value, "renderer")
    })
    inputElement.addEventListener("click", () => {
        selectCell(targetElement.id)
    })

}

