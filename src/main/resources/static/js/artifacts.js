import {insertById} from "./lib/dom.js";
import {camelCase, ensureCategory, post} from "./lib/utils.js";
import {showIntegrationCreationDialog} from "./integration_editor.js";
import {confirmDialog} from "./lib/dialogs.js";
import {currentCell, setCurrentCellFormula} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";

export function updateSpec(parent, idPrefix, spec) {
    let id = idPrefix + spec.name

    let createAction = null;
    let icon = "add_circle"
    switch (spec.kind) {
        case "FUNCTION":
        case "PROPERTY":
            icon = "variable_insert"
            createAction = async () => {
                let value = "=" + spec.name + (spec.params && spec.params.length ? "(" : "")
                if (currentCell.f == null || currentCell.f == "" || await confirmDialog("Overwrite Current Formula?", currentCell.key + ": '" + currentCell.f + "'")) {
                    setCurrentCellFormula(value)
                }
            }
            break;
        case "INTEGRATION":
            createAction = () => { showIntegrationCreationDialog(spec) }
        case "INPUT_PORT":
        case "OUTPUT_PORT":
            createAction = () => {
                showPortDialog(spec)
            }
            break;
        default:
            console.log("Unrecognized spec kind: " + spec.kind)
    }


    if (spec.modifiers && spec.modifiers.indexOf("DELETED") != -1) {
        let existing = document.getElementById(id)
        if (existing) {
            existing.parentElement.removeChild(existing)
        }
        return existing  // should be null?
    }

    let element = document.createElement("div")
    element.id = idPrefix + spec.name


    if (createAction) {
        let addButtonElement = document.createElement("img")
        addButtonElement.src = "/img/" + icon + ".svg"
        addButtonElement.style.float = "left"
        addButtonElement.onclick = createAction
        element.appendChild(addButtonElement)
    }

    let titleElement = document.createElement("div")
    let nameSpan = document.createElement("b")
    let name = spec.displayName || spec.name
    let cut = name.indexOf('.')
    nameSpan.append((cut == -1 ? name : name.substring(cut + 1)).replaceAll("_", "_\u200b"))
    titleElement.appendChild(nameSpan)
    if (spec.params && spec.params.length > 0) {
        titleElement.append("(" + spec.params.map((e) => e.name).join(", ") + ")")
    }
    if ((typeof spec?.type == "string") && spec.type.toLowerCase() != "void") {
        titleElement.append(": " + camelCase(spec.type))
    }
    titleElement.style.marginBottom = "5px"
    titleElement.style.marginTop = "10px"
    titleElement.style.paddingLeft = "20px"
    element.appendChild(titleElement)

    if (spec.kind == "PROPERTY" && spec.modifiers && spec.modifiers.indexOf("SETTABLE") != -1) {
        let inputElement = document.createElement("input")
        element.appendChild(inputElement)
        inputElement.id = "port." + spec.name
        inputElement.addEventListener("change", () => {
            post("ports/" + spec.name, {
                kind: spec.name,
                name: spec.name,
                source: inputElement.value
                })
        })
    }

    let descriptionElement = document.createElement("div")
    descriptionElement.style.paddingLeft = "20px"

    let description = spec.description
    cut = description.indexOf(".")

    descriptionElement.textContent = (cut == -1 ? description : description.substring(0, cut + 1))
    element.appendChild(descriptionElement)
    descriptionElement.addEventListener("click", () => {console.log(description)})

    // Find "sub-parent" by category(?)

    let target = ensureCategory(parent, spec.category)

    insertById(target, element)

    return element
}