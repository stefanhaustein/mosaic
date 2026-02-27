import {getColumn, getRow, nullToEmtpy, post, toRangeKey} from "./lib/utils.js";
import {renderCell} from "./cell_renderer.js";
import {getPortFactory, getPortInstance, model} from "./shared_model.js";
import {removeClasses, renderDependencies, renderRangeHighlight} from "./shared_state_internal_renderer.js";

export let portValues = {}
export let currentCell = null
export let currentSheet = null

export let selectionRangeX = 0
export let selectionRangeY = 0

export let runMode = false

let currentCellElement = null

let cellContentChangeListeners = {}  // new content, source
let cellSelectionListeners = [] // new id, edit mode

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

let originElement = document.getElementById("rangeName")


// Only set via sync.js -- run mode changes need to be requested via the server.
export function setRunMode(mode) {
    runMode = mode
    if (runMode) {
        document.body.classList.add("runMode")
    } else {
        document.body.classList.remove("runMode")
    }
}


formulaInputElement.addEventListener("blur", () => {
    console.log("onblur triggered")
    commitCurrentCell()
})

export function getSelectedCellRangeKey() {
    return toRangeKey(getColumn(currentCell.key), getRow(currentCell.key), selectionRangeX, selectionRangeY)
}

export function addCellSelectionListener(listener) {
    cellSelectionListeners.push(listener)
}

export function addCellContentChangeListener(name, listener) {
    cellContentChangeListeners[name] = listener
}

// Called when the selection was changed -- which in turn is called when the selected cell was updated.
function updateErrorMessage() {
    if (currentCell.v) {
        return
    }
    let errorDiv = document.getElementById("globalErrorDiv")
    if (currentCell?.c?.type == "err") {
        errorDiv.textContent = currentCell.c?.msg
        errorDiv.classList.add("error")
    } else {
        errorDiv.classList.remove("error")
    }
}

function commitCurrentCell() {
    committedFormula = currentCell.f
    post("update/" + currentSheet.name + "!" + currentCell.key, currentCell)
}

export function getCurrentCellElement() {
    return currentCellElement
}

export function setCurrentCellFormula(value, source) {
    if (value == currentCell["f"]) {
        return
    }
    currentCell["f"] = value

    if (source != "input") {
        formulaInputElement.value = value
        commitCurrentCell()
    }
    for (let key in cellContentChangeListeners) {
        if (key != source) {
            cellContentChangeListeners[key](value, source)
        }
    }
}

export function setCurrentCellImage(value) {
    currentCell["i"] = value
    commitCurrentCell()
}


export function setCurrentCellValidation(value) {
    currentCell["v"] = value
    commitCurrentCell()
}



export function selectSheet(name) {
    let cellId = currentCell != null ? currentCell.key : "A1"

    if (name == null) {
        for (name in model.sheets) {
            break
        }
    }

    currentSheet = model.sheets[name]

    document.getElementById("sheetSelect").value = name

    for (let tdElement of document.getElementById("spreadsheetTBody").getElementsByTagName("td")) {
        renderCell(tdElement.id)
    }

    selectCell(cellId)
}


let dependenciesShown = []

export function showDependencies(targetKey) {
    removeClasses(dependenciesShown, ["self","input", "input2", "output", "output2"])
    if (targetKey == null) {
        dependenciesShown = []
    } else {
        let seenIn = {}
        renderDependencies(targetKey, "inputs",  seenIn,0, ["self","input", "input2"])
        let seenOut = {}
        renderDependencies(targetKey, "outputs", seenOut,0, ["self","output", "output2"])

        dependenciesShown = Object.getOwnPropertyNames(seenIn).concat(Object.getOwnPropertyNames(seenOut));
    }
}


// Note that this is also called when the current cell is updated by the server (as the server update might
// swap out the whole object.
export function selectCell(id, rangeX = 0, rangeY = 0) {

    if (isNaN(rangeX)) {
        console.log("rangeX is NaN")
        rangeX = 0
    }
    if (isNaN(rangeY)) {
        console.log("rangeY is NaN")
        rangeY = 0
    }

    if (currentCell != null) {
        renderRangeHighlight(currentCell.key, selectionRangeX, selectionRangeY, false)
    }

    let newElement = document.getElementById(id)
    if (!newElement || newElement.localName != "td") {
        return
    }

    let name = toRangeKey(getColumn(id), getRow(id), rangeX, rangeY)
    // let normalizedKey = currentSheet.name + "!" + toRangeKey(getColumn(id), getRow(id), rangeX, rangeY)
    // for (let port of getAllPorts()) {
    //   if (port.type == "NamedCells" && port.source == normalizedKey) {
    //       name = port.name.substring(port.name.indexOf("!"))
    //       break
    //   }
    // }
    originElement.textContent = name

    let newData = currentSheet.cells[id]
    if (newData == null) {
        newData = currentSheet.cells[id] = {key:id}
    }
    let newlySelected = currentCell == null || id != currentCell.key
    if (newlySelected) {
        if (currentCellElement != null) {
            if (committedFormula != currentCell["f"]) {
                commitCurrentCell()
            }
            currentCellElement.classList.remove("selected")
        }
        committedFormula = newData["f"]
    }

    currentCellElement = newElement
    currentCell = newData

    updateErrorMessage()

    formulaInputElement.value = nullToEmtpy(committedFormula)

    //
    if (document.activeElement != currentCellElement && document.activeElement.parentElement != currentCellElement) {
        currentCellElement.focus()
    }

    selectionRangeX = rangeX
    selectionRangeY = rangeY


    renderRangeHighlight(currentCell.key, selectionRangeX, selectionRangeY, true)


    if (newlySelected) {
        currentCellElement.classList.add("selected")
        let targetKey = currentSheet.name + "!" + currentCell.key

        let formula = currentCell.f?.toString() || ""
        if (formula.startsWith("=")) {
            let name = formula.substring(1)
            let type = getPortInstance(name)?.type
            if (type != null) {
                let spec = getPortFactory(type)
                if (spec.kind == "INPUT_PORT") {
                    targetKey = name
                }
            }
        }

        showDependencies(targetKey)

        notifySelectionListeners()
    }
}

function notifySelectionListeners() {
    for (let listener of cellSelectionListeners) {
        listener(currentCell.key)
    }
}

