import {registerIntegration, registerPort, getPortFactory} from "./shared_model.js";
import {addOption, insertById} from "./lib/dom.js";
import {updateSpec} from "./artifacts.js";
import {ensureCategory} from "./lib/utils.js";

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

        let isExpandable = spec.kind == "INPUT_PORT" && f.type != null && typeof f.type != "string"
        let portElement = document.createElement("div")
        portElement.id = "port." + f.name
        portElement.className = "port"
        let bulletElement = document.createElement("div")
        let entryElement = document.createElement( "div")
        portElement.append(bulletElement, entryElement)

        let cut = name.indexOf(".")
        let containerName = f.kind == "NamedCells" ? "namedCellListContainer" : cut != -1 ? "integration." + name.substring(0, cut) :
            spec.kind == "OUTPUT_PORT" ?  "outputPortList" : "inputPortList"

        console.log("determined container name ", containerName, " for ", f, "spec", spec)

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