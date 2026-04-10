import {registerIntegration, registerPort, getPortFactory} from "./shared_model.js";
import {addOption, insertById} from "./lib/dom.js";
import {updateSpec} from "./artifacts.js";
import {ensureCategory, post} from "./lib/utils.js";
import {currentCell, currentSheet, portValues, setCurrentCellFormula, showDependencies} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {confirmDialog} from "./lib/dialogs.js";

let integrationListElement = document.getElementById("integrationList")
let integrationSpecListElement = document.getElementById("integrationSpecList")
let sidePanel = document.getElementById("sidePanel")
let panelSelectElement = document.getElementById("panelSelect")

export function processIntegrationUpdate(name, integration) {
    let key = name.toLowerCase()
    let elementId = "integration." + key
    let element = document.getElementById(elementId)

    if (!registerIntegration(name, integration)) {
        if (element != null) {
            integrationListElement.removeChild(element)
        }
    } else {
        if (element == null) {
            addOption(panelSelectElement, "- " + name + " (" + integration.type + ")", elementId);

            element = document.createElement("div")
            element.id = elementId
            sidePanel.appendChild(element)

            let factoryElement = document.createElement("div")
            factoryElement.id = elementId + ".factories"
            let portElement = document.createElement("div")
            portElement.id = elementId + ".ports"
            element.append(factoryElement, portElement)
            /*

            let ops = integration["operations"] || []
            console.log ("operations: ", integration["operations"])

            for (const op of ops) {
                console.log("op: ", op, "Modifiers: ", op.modifiers)
                if ((op.modifiers || []).indexOf("UNINSTANTIABLE") == -1) {
                    updateSpec(element, elementId + ".", op)
                }
            }
            let nameSpan = document.createElement("span")
            nameSpan.textContent = name
            nameSpan.style.fontWeight = "bold"

            element.append(nameSpan, " (" + integration.type + ")")


            element.onclick = () => {
                let spec = getFactory(integration.type)
                showIntegrationInstanceConfigurationDialog(spec, integration)
            }

             */
        }
    }
}

export function updateIntegrationFactory(spec) {
    updateSpec(
        integrationSpecListElement,
        "integration.spec.",
        spec)
}


export function processPortUpdate(integration, name, f) {
    if (!registerPort(integration, name, f)) {
        let entryElement = document.getElementById("port." + f.fqName)
        if (entryElement != null) {
            entryElement.parentElement.removeChild(entryElement)
        }
    } else {
        // General setup

        let spec = getPortFactory(integration, f.kind)
        if (spec == null) {
            console.log("Factory " + f.kind + " not found.")
            spec = {kind: "INPUT_PORT", type: "Bool"}
        }
        let modifiers = spec["modifiers"] || []

        let portElement = document.createElement("div")
        portElement.id = "port." + f.name
        portElement.className = "port"
        let containerName = "integration." + integration.key + ".ports"
        let containerElement = document.getElementById(containerName)
        let targetElement = ensureCategory(containerElement, f.category)
        insertById(targetElement, portElement)

        if (!(spec.name || "").endsWith("_out")) {

            // Row 1 Icon: Config

            let setFormulaIconDiv = document.createElement("div")
            let setFormulaElement = document.createElement("img")
            setFormulaElement.src = "/img/arrow_left_alt.svg"
            setFormulaElement.className = "portConfig"
            setFormulaElement.onclick = async () => {
                if (currentCell.f == null || currentCell.f == "" || await confirmDialog("Overwrite Current Formula?", currentCell.key + ": '" + currentCell.f + "'")) {
                    setCurrentCellFormula("=" + f.fqName)
                }
            }
            setFormulaIconDiv.append(setFormulaElement)
            portElement.append(setFormulaIconDiv)


            // Row 1 Content: Title

            let entryTitleElement = document.createElement("div")
            entryTitleElement.className = "portTitle"
            let nameElement = document.createElement("b")
            nameElement.textContent = name
            entryTitleElement.appendChild(nameElement)
            if (f.kind != "NamedCells" && !f.name.startsWith(f.kind)) {
                entryTitleElement.append(": ", f.kind)
            }
            portElement.append(entryTitleElement)
        }

        if (spec.kind == "OUTPUT_PORT") {
            let sourceInput = document.createElement("input")

            let setReferenceImg = document.createElement("img")
            setReferenceImg.src = "/img/arrow_right_alt.svg"
            setReferenceImg.className = "portConfig"
            setReferenceImg.onclick = async () => {
                sourceInput.value = "=" + currentSheet.name + "!" + currentCell.key
                post("ports/" + f.fqName.replace(".", "/"), {source: sourceInput.value})
            }
            let setReferenceDiv = document.createElement("div")
            setReferenceDiv.append(setReferenceImg)
            portElement.append(setReferenceDiv)

            sourceInput.value =  f.source
            sourceInput.addEventListener("change", () => {
                post("ports/" + f.fqName.replace(".", "/"), {source: sourceInput.value})
            })
            let sourceDiv = document.createElement("div")
            sourceDiv.append(sourceInput)
            portElement.append(sourceDiv)
        }


        // Row 2/3: Icon

        let configDiv = document.createElement("div")
        portElement.append(configDiv)

        if ((spec.params || []).length > 0) {
            let entryConfigElement = document.createElement("img")
            entryConfigElement.src = "/img/settings.svg"
            entryConfigElement.className = "portConfig"
            entryConfigElement.onclick = () => {
                showPortDialog(spec, f)
            }
            configDiv.append(entryConfigElement)
        }


        // Row 2/3: Value

        let entryValueElement = document.createElement("span")
        entryValueElement.id = "port." + f.fqName + ".value"
        entryValueElement.className = "portValue"
        entryValueElement.textContent = f.c
        portElement.appendChild(entryValueElement)



        /*
        let isExpandable = spec.kind == "INPUT_PORT" && f.type != null && typeof f.type != "string"
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
        }*/

        console.log("spec: ", spec)



        // console.log("adding port", f, spec)
/*


        portElement.onclick = (event) => {
            if (event.target.localName.toUpperCase() == "DIV") {
                showDependencies(f.name)
            }
        }

 */
    }
}


export function processPortFactory(integration, spec) {
    if (spec.modifiers.indexOf("UNINSTANTIABLE") != -1) {
        return
    }
    let container = document.getElementById("integration." + integration.key + ".factories")
    updateSpec(container, "portspec.", spec)
}

export function processPortValue(fqKey, map) {
    let value = map[fqKey]
    portValues[fqKey] = value
    let target = document.getElementById("port." + fqKey + ".value")
    if (target != null) {
        target.textContent = JSON.stringify(value)
    }
}


