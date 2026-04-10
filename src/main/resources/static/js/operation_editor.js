import {
    setCurrentCellFormula,
    addCellContentChangeListener,
    addCellSelectionListener,
} from "./shared_state.js";
import {FormController} from "./forms/form_builder.js";
import {extractParameters} from "./lib/expressions.js";
import {getFunction} from "./shared_model.js";
import {transformSchema} from "./lib/utils.js";

let formulaInputElement = document.getElementById("formulaInput")
let functionPanelElement = document.getElementById("operationEditorContainer")
let currentFunction = null
let currentController = null
let currentParameters = {}

addCellContentChangeListener("panel", (newValue, source) => {
    updateParameterTab()
})

addCellSelectionListener(() => {
    updateParameterTab()
})


function updateParameterTab() {
    let currentInput = formulaInputElement.value

    console.log("currentInput:", currentInput)
    let name = ""
    let found = null
    let cut = -1

    if (currentInput != null && currentInput.startsWith("=")) {
        cut = currentInput.indexOf("(")
        name = currentInput.substring(1, cut === -1 ? currentInput.length : cut)
        found = getFunction(name)
    }

    if (found == null) {
        functionPanelElement.textContent = ""
        currentFunction = null
        return false;
    }

    if (currentFunction !== found) {
        functionPanelElement.textContent = ""

        let titleElement = document.createElement("div")
        titleElement.className = "subtitle"
        titleElement.textContent = found.name
        functionPanelElement.appendChild(titleElement)

        currentController = FormController.create(functionPanelElement, transformSchema(found["params"], true))
        currentFunction = found

        if (found.description != "") {
            let descriptionElement = document.createElement("p")
            descriptionElement.textContent = found.description
            functionPanelElement.appendChild(descriptionElement)
        }


        currentController.addListener((key, value) => {
            currentParameters[key] = value
            let s = "=" + currentFunction["name"] + "("
            for (let param of currentFunction.params) {
                let key = param["name"]
                let value = currentParameters[key]
                if (value != null) {
                    if (!s.endsWith("(")) {
                        s += ", "
                    }
                    s += key + "=" + value
                }
            }
            s += ")"
            setCurrentCellFormula(s, "operation_editor")
        })
    }

    currentParameters = cut === -1 ? {} : extractParameters(currentInput.substring(cut + 1), currentFunction.params)

    currentController.setValue(currentParameters)

    return true
}
