import {setCurrentCellFormula, getCurrentCellElement, getSelectedCellRangeKey, selectCell} from "./shared_state.js";
import {nullToEmtpy, post} from "./lib/utils.js";
import {promptDialog} from "./lib/dialogs.js";
import {getAllPorts} from "./shared_model.js";

// Sets up event handlers etc. for shared state. Depends on shared state

let formulaInputElement = document.getElementById("formulaInput")
let committedFormula = null

formulaInputElement.addEventListener("change", event => { setCurrentCellFormula(formulaInputElement.value, "input") } )
formulaInputElement.addEventListener("input", event => { setCurrentCellFormula(formulaInputElement.value, "input") } )
formulaInputElement.addEventListener("keydown", event => {
    if (event.key == "Enter") {
        event.preventDefault()
        event.stopPropagation()
        setCurrentCellFormula(formulaInputElement.value)
        getCurrentCellElement().focus()
    } else if (event.key == "Escape") {
        formulaInputElement.value = committedFormula
        setCurrentCellFormula(nullToEmtpy(committedFormula), "input")
        getCurrentCellElement().focus()
    }
    // Other input is handled by change/input
})

let cells = document.getElementById("spreadsheetTBody").querySelectorAll("td")
for (let cell of cells) {
    cell.addEventListener("focus", () => selectCell(cell.id))
}

