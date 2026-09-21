import {currentSheet, selectCell, setCurrentCellFormula} from "./shared_state.js";

import {getColumn, getRow, toCellId} from "./lib/utils.js";
import {InputController} from "./forms/input_controller.js";

export function renderCell(key) {
    let cellElement = document.getElementById(key)
    if (cellElement == null) {
        console.log("Element for cell key '" + key + "' not found.'")
        return;
    }
    cellElement.textContent = ""

    // The container is always necessary to work around TD elements INTENTIONALLY IGNORING HEIGHT RESTRICTIONS...
    let container = document.createElement("div")
    cellElement.append(container)

    let cellData = currentSheet.cells[key]
    if (cellData == null) {
        cellData = {}
    }

    let styles = cellData.s
    let formula = cellData.f || ""

    // Check if we need inner divs and fill everything but the content
    if (Array.isArray(styles)) {
        container.style.display = "grid"
        container.style.alignItems = "stretch"
        if (Array.isArray(styles)) {
            for (let style of styles) {
                let layer = document.createElementNS("http://www.w3.org/2000/svg", "svg")
                layer.style.gridArea = "1/1"
                layer.style.width = "100%"
                layer.style.height = "100%"
                layer.style.rotate = (style.rotation || 0) + "deg"
                if (style.image != null) {
                    let use = document.createElementNS("http://www.w3.org/2000/svg", "use")
                    use.setAttribute("href", "img/cell/" + style.image)
                    layer.append(use)
                }
                container.append(layer)
            }
        }
        let innerContainer = document.createElement("div")
        innerContainer.style.gridArea = "1/1"
        innerContainer.style.width = "100%"
        innerContainer.style.height = "100%"

        container.append(innerContainer)
        container = innerContainer
        container.style.zIndex = "2"
    }

    if (formula.startsWith("=")) {
        container.style.display = "flex"
        container.style.flexDirection = "column"
        container.style.justifyContent = "center"
        container.style.alignItems = "stretch"

        let formulaElement = document.createElement("div")
        formulaElement.style.color = "#aaa"
        formulaElement.style.fontSize = "12px"
        formulaElement.textContent = formula.substring(1)
        formulaElement.style.paddingLeft = "3px"
        formulaElement.style.flex = "0 1"
        container.append(formulaElement)

        let innerContainer = document.createElement("div")
        innerContainer.style.flex = "1"
        container.append(innerContainer)
        container = innerContainer
    }

    container.style.display = "flex"
    container.style.flexDirection = "column"
    container.style.justifyContent = "center"
    container.style.alignItems = "center"

    let targetElement = document.createElement("div")
    targetElement.style.textOverflow = "ellipsis"
    targetElement.style.minHeight = "0"

    container.append(targetElement)

    let classes = targetElement.classList

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
            classes.add("c")
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

