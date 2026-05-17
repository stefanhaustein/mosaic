import {addOption} from "./lib/dom.js";
import {updateSpec} from "./artifacts.js";
import {confirmDialog} from "./lib/dialogs.js";
import {Integration} from "./Integration.js";
import {sendIntegration, showIntegrationInstanceConfigurationDialog} from "./integration_dialog.js";
import {IntegrationFactory} from "./IntegrationFactory.js";

let integrationSpecListElement = document.getElementById("integrationSpecList")
let sidePanel = document.getElementById("sidePanel")
let panelSelectElement = document.getElementById("panelSelect")

export function processIntegrationUpdate(name, data) {
    let key = name.toLowerCase()
    let elementId = "integration." + key
    let element = document.getElementById(elementId)

    let integration = Integration.update(name, data)
    if (integration == null) {
        if (element != null) {
            element.parentElement.removeChild(element)
            let option = document.getElementById(("option." + elementId))
            panelSelectElement.value = "Integration"
            panelSelectElement.removeChild(option)
            panelSelectElement.dispatchEvent(new Event('change'))
        }
    } else {
        if (element == null) {
            let option = addOption(panelSelectElement, "- " + name + " (" + integration.kind + ")", elementId);
            option.id = "option." + elementId

            element = document.createElement("div")
            element.id = elementId
            sidePanel.appendChild(element)

            let editButton = document.createElement("button")
            editButton.style.margin = "10px 0 10px 10px"
            editButton.append("Configure")
            editButton.addEventListener("click", () => {
                console.log("click", integration)
                showIntegrationInstanceConfigurationDialog(IntegrationFactory.get(integration.kind), integration)
            })
            element.append(editButton)

            let deleteButton = document.createElement("button")
            deleteButton.append("Delete")
            deleteButton.style.margin = "10px 0 10px 10px"
            element.append(deleteButton)
            deleteButton.addEventListener("click", async () => {
                if (await confirmDialog("Delete " + name)) {
                    sendIntegration(name, {kind: integration.kind, deleted: true})
                }
            })

            let factoryElement = document.createElement("div")
            factoryElement.id = elementId + ".factories"

            let portElement = document.createElement("div")
            portElement.id = elementId + ".ports"

            element.append(portElement, factoryElement)
        }
    }
}

export function updateIntegrationFactory(spec) {
    updateSpec(
        integrationSpecListElement,
        "integration.spec.",
        spec)
}


