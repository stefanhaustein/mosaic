import {currentCell, currentSheet, portValues, setCurrentCellFormula, showDependencies} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {insertById, setDragHandler} from "./lib/dom.js";
import {getFactory, getPortInstance, registerPortInstance} from "./shared_model.js";
import {ensureCategory, post} from "./lib/utils.js";
import {updateSpec} from "./artifacts.js";
import {confirmDialog} from "./lib/dialogs.js";


let inputPortSpecListElement = document.getElementById("inputPortSpecList")
let outputPortSpecListElement = document.getElementById("outputPortSpecList")
let sidePanelWidth = 200;

let rangeNameElement = document.getElementById("rangeName")


setDragHandler(document.getElementById("divider"), (dx, dy) => {
    sidePanelWidth -= dx
    document.getElementById("sidePanel").style.flexBasis = sidePanelWidth + "px"
})


export function processPortSpec(spec) {
    let container= spec.kind == "OUTPUT_PORT" ? outputPortSpecListElement : inputPortSpecListElement
    if (spec.name == "NamedCells") {
        document.getElementById("addNamedCellsButton").addEventListener("click", () => { showPortDialog(spec) })
        rangeNameElement.addEventListener("click", async () => {
            let port = getPortInstance(rangeNameElement.textContent)
            showPortDialog(spec, port)
        })
    } else {
        updateSpec(container, "portspec.", spec)
    }
}

export function processPortValue(key, map) {
    let value = map[key]
    portValues[key] = value
    let target = document.getElementById("port." + key + ".value")
    if (target != null) {
        target.textContent = JSON.stringify(value)
    }
}

export function processPortUpdate(name, f) {
    if (!registerPortInstance(name, f)) {
        let entryElement = document.getElementById("port." + name)
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        // General setup

        let spec = getFactory(f.kind)
        if (spec == null) {
            console.log("Factory " + f.kind + " not found.")
            spec = {kind: "INPUT_PORT", type: "Bool"}
        }

        let isExpandable = spec.kind == "INPUT_PORT" && f.type != null && typeof f.type != "string"
        let portElement = document.createElement("div")
        portElement.id = "port." + f.name
        portElement.className = "port"
        let bulletElement = document.createElement("div")
        let entryElement = document.createElement( "div")
        portElement.append(bulletElement, entryElement)

        let cut = name.indexOf(".")
        let containerName = cut != -1 ? "integration." + name.substring(0, cut) :
            spec.kind == "OUTPUT_PORT" ? f.kind == "NamedCells" ? "namedCellListContainer" :  "outputPortList" : "inputPortList"


        let containerElement = document.getElementById(containerName)
        let targetElement = ensureCategory(containerElement, f.category)
        insertById(targetElement, portElement)

        let entryContentElement = document.createElement("div")

        if (isExpandable) {
            let showDetailsElement = document.createElement("img")
            showDetailsElement.src = "/img/unfold_more.svg"
            showDetailsElement.className = "portConfig"
            entryContentElement.style.display = "none"
            showDetailsElement.onclick = () => {
                if (entryContentElement.style.display == "none") {
                    entryContentElement.style.display = ""
                    showDetailsElement.src = "/img/unfold_less.svg"
                } else {
                    entryContentElement.style.display = "none"
                    showDetailsElement.src = "/img/unfold_more.svg"
                }
            }
            entryElement.appendChild(showDetailsElement)
        }

        console.log("spec: ", spec)

        if (!(spec.name || "").endsWith("_out")) {
            let entryTitleElement = document.createElement("div")
            entryTitleElement.className = "portTitle"
            let nameElement = document.createElement("b")
            nameElement.textContent = name
            entryTitleElement.appendChild(nameElement)
            if (f.kind != "NamedCells" && !f.name.startsWith(f.kind)) {
                entryTitleElement.append(": ", f.kind)
            }
            entryElement.append(entryTitleElement)
        }

        let modifiers = spec["modifiers"] || []
        // console.log("adding port", f, spec)

        switch (spec.kind) {
            case "INPUT_PORT":
                let entryValueElement = document.createElement("span")
                entryValueElement.id = "port." + name + ".value"
                entryValueElement.className = "portValue"
                entryContentElement.appendChild(entryValueElement)

                let setFormulaElement = document.createElement("img")
                setFormulaElement.src = "/img/variable_insert.svg"
                setFormulaElement.className = "portConfig"
                setFormulaElement.onclick = async () => {
                    if (currentCell.f == null || currentCell.f == "" || await confirmDialog("Overwrite Current Formula?", currentCell.key + ": '" + currentCell.f + "'")) {
                        setCurrentCellFormula("=" + f.name)
                    }
                }
                bulletElement.append(setFormulaElement)
                break;

            case "OUTPUT_PORT":
                let sourceElement = document.createElement("input")
                sourceElement.value =  f.source
                sourceElement.addEventListener("change", () => {
                    post("ports/" + f.name, {source: sourceElement.value})
                })
                entryContentElement.append(sourceElement)

                let setReferenceElement = document.createElement("img")
                setReferenceElement.src = "/img/arrow_right_alt.svg"
                setReferenceElement.className = "portConfig"
                setReferenceElement.onclick = async () => {
                    sourceElement.value = "=" + currentSheet.name + "!" + currentCell.key
                }
                bulletElement.append(setReferenceElement)
                break
        }

        if ((spec.params || []).length > 0) {
            let entryConfigElement = document.createElement("img")
            entryConfigElement.src = "/img/settings.svg"
            entryConfigElement.className = "portConfig"
            entryConfigElement.onclick = () => {
                showPortDialog(spec, f)
            }
            bulletElement.append(entryConfigElement)
        }


        entryElement.appendChild(entryContentElement)

        entryElement.onclick = (event) => {
            if (event.target.localName.toUpperCase() == "DIV") {
               showDependencies(f.name)
            }
        }
    }
}

