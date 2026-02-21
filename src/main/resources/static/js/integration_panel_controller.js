import {registerIntegrationInstance} from "./shared_model.js";
import {addOption} from "./lib/dom.js";
import {updateSpec} from "./artifacts.js";

let integrationListElement = document.getElementById("integrationList")
let integrationSpecListElement = document.getElementById("integrationSpecList")
let sidePanel = document.getElementById("sidePanel")
let panelSelectElement = document.getElementById("panelSelect")

export function processIntegrationInstanceUpdate(name, integration) {
    let key = name.toLowerCase()
    let id = "integration." + key
    let element = document.getElementById(id)

    if (!registerIntegrationInstance(name, integration)) {
        if (element != null) {
            integrationListElement.removeChild(element)
        }
    } else {
        if (element == null) {
            addOption(panelSelectElement, "- " + name + " (" + integration.type + ")", id);

            element = document.createElement("div")
            element.id = id
            sidePanel.appendChild(element)

            let ops = integration["operations"] || []
            console.log ("operations: ", integration["operations"])

            for (const op of ops) {
                console.log("op: ", op, "Modifiers: ", op.modifiers)
                if ((op.modifiers || []).indexOf("UNINSTANTIABLE") == -1) {
                    updateSpec(element, id + ".", op)
                }
            }
            /*
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

export function updateIntegrationSpec(spec) {
    updateSpec(
        integrationSpecListElement,
        "integration.spec.",
        spec)
}
