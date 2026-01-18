import {portValues, showDependencies, simulationValues} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {InputController} from "./forms/input_controller.js";
import {insertById, setDragHandler} from "./lib/dom.js";
import {getFactory, getPortInstance, registerPortInstance} from "./shared_model.js";
import {camelCase, post} from "./lib/utils.js";
import {FormController} from "./forms/form_builder.js";
import {updateSpec} from "./artifacts.js";


let inputPortSpecListElement = document.getElementById("inputPortSpecList")
let outputPortSpecListElement = document.getElementById("outputPortSpecList")
let sidePanelWidth = 200;

let rangeNameElement = document.getElementById("rangeName")


setDragHandler(document.getElementById("divider"), (dx, dy) => {
    sidePanelWidth -= dx
    document.getElementById("sidePanel").style.flexBasis = sidePanelWidth + "px"
})


export function processPortSpec(spec) {
    let container = spec.kind == "OUTPUT_PORT" ? outputPortSpecListElement : inputPortSpecListElement
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

export function processSimulationValue(key, map) {
    let value = map[key]
    simulationValues[key] = value
    let port = getPortInstance(key)
    if (port != null) {
        let controller = port.valueController
        if (controller instanceof InputController) {
            controller.setValue(value)
        }
    }
}

export function processPortUpdate(name, f) {
    if (!registerPortInstance(name, f)) {
        let entryElement = document.getElementById("port." + name)
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        let spec = getFactory(f.kind)
        if (spec == null) {
            console.log("Factory " + f.kind + " not found.")
            return
        }
        if (spec.kind == "PROPERTY") {
            let inputElement = document.getElementById("port." + f.name)
            inputElement.value = f.source
            return
        }

        let isExpandable = spec.kind == "INPUT_PORT" && typeof f.type != "string"
        let entryElement = document.createElement( "div")
        entryElement.id = "port." + f.name
        entryElement.className = "port"
        insertById(document.getElementById(spec.kind == "OUTPUT_PORT" ? f.kind == "NamedCells" ? "namedCellListContainer" :  "outputPortList" : "inputPortList"), entryElement)

        let entryContentElement = document.createElement("div")


        let entryConfigElement = document.createElement("img")
        entryConfigElement.src = "/img/settings.svg"
        entryConfigElement.className = "portConfig"
        entryConfigElement.onclick = () => {
            showPortDialog(spec, f)
        }
        entryElement.append(entryConfigElement)

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

        let entryTitleElement = document.createElement("div")
        entryTitleElement.className = "portTitle"
        let nameElement = document.createElement("b")
        nameElement.textContent = name

        entryTitleElement.appendChild(nameElement)
        if (f.kind != "NamedCells") {
            entryTitleElement.append(": ", f.kind)
        }
        entryElement.append(entryTitleElement)

        let modifiers = spec["modifiers"] || []
        // console.log("adding port", f, spec)

        entryContentElement.style.paddingLeft = "10px"
        entryContentElement.style.clear = "both"

        let showValue = true
        if (spec.kind == "INPUT_PORT") {
            let entrySimulationElement = document.createElement("span")
            entrySimulationElement.id = "port." + name + ".simulationValue"
            entrySimulationElement.className = "portSimulationValue"
            if (!isExpandable) {
                let controller = f.valueController = InputController.create(
                    {type: camelCase(f.type)})
                entrySimulationElement.appendChild(controller.inputElement)
                controller.inputElement.addEventListener("change", () => {
                    post("portSimulation?name=" + name, controller.getValue())
                })
            } else {
                console.log(spec)
                let controller = f.valueController = FormController.create(entrySimulationElement, f.type)
                controller.addListener(() => {
                    post("portSimulation?name=" + name, controller.getValue())
                })
            }
            showValue = !document.getElementById("simulationMode").checked
            entrySimulationElement.style.display = showValue ? "none" : "inline"
            entryContentElement.appendChild(entrySimulationElement)

            let entryValueElement = document.createElement("span")
            entryValueElement.id = "port." + name + ".value"
            entryValueElement.className = "portValue"
            entryValueElement.style.display = showValue ? "inline" : "none"
            entryContentElement.appendChild(entryValueElement)
        } else {
           // let sourceElement = document.createElement("div")
           // sourceElement.style.float = "right"
           // sourceElement.style.paddingRight = "5px"
           // sourceElement.textContent =  f.source

            entryContentElement.append(f.source)
        }



        entryElement.appendChild(entryContentElement)

        entryElement.onclick = (event) => {
            if (event.target.localName.toUpperCase() == "DIV") {
               showDependencies(f.name)
            }
        }
    }
}

