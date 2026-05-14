import {FormController} from "./forms/form_builder.js";

import {post, transformSchema} from "./lib/utils.js";
import {Integration} from "./Integration.js";
import {IntegrationFactory} from "./IntegrationFactory.js";

let dialogElement = document.getElementById("dialog")

export function showIntegrationInstanceConfigurationDialog(spec, instance) {
    dialogElement.textContent = ""
    let dialogTitleElement = document.createElement("div")
    dialogTitleElement.className = "dialogTitle"
    dialogElement.appendChild(dialogTitleElement)

    let inputDiv = document.createElement("div")
    inputDiv.className = "dialogFields"

    let schema = transformSchema(spec["params"])
    if (instance) {
        dialogTitleElement.textContent = "Configure " + instance.name + (instance.name != instance.type ? " (" + instance.type + ")" : "")
    } else {
        dialogTitleElement.textContent = "Create " + spec.name
        if (spec.modifiers != null && spec.modifiers.indexOf("SINGLETON") != -1) {
            instance = {name: spec.name}
        } else {
            instance = {}
            schema = [{
                name: "name",
                label: "Name",
                type: "String",
                modifiers: ["CONSTANT"],
                validation: {
                    "Integration name conflict": (name) => Integration.get(name) == null && (IntegrationFactory.get(name) == null || IntegrationFactory.get(name) == spec),
                    "Port name conflict": (name) => Integration.getFqPort(name) == null,
                    "Valid: letters, non-leading '_' or digits": /^[a-zA-Z][a-zA-Z_0-9]*$/
                }
            }, ...schema]
            console.log(schema)
        }
    }

    let bindingFormController = FormController.create(inputDiv, schema)

    bindingFormController.setValue(instance)

    dialogElement.appendChild(inputDiv)

    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = "Ok"
    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let configuration = bindingFormController.getValue()
        configuration.kind = spec.name
        let name = instance.name || configuration.name
        delete configuration.name
        if (sendIntegration(name, configuration)) {
            dialogElement.close()
        }
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.className = "dialogButton"
    cancelButton.addEventListener("click", () => { dialogElement.close() })
    buttonDiv.appendChild(cancelButton)
    dialogElement.appendChild(buttonDiv)
    dialogElement.showModal()
}


export function sendIntegration(name, data) {
    post("integrations/" + name, data)
    return true
}

