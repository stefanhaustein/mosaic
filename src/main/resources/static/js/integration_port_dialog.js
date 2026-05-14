import {FormController} from "./forms/form_builder.js";
import {post, transformSchema} from "./lib/utils.js";
import {Integration} from "./Integration.js";
import {Operation} from "./Operation.js";


function hidePortDialog() {
    document.getElementById("dialog").close()
}

function renderBinding(targetDiv, constructorSpec, instanceSpec) {

    let bindingFormController = FormController.create(targetDiv, transformSchema(constructorSpec["params"]))

    if (instanceSpec != null) {
        bindingFormController.setValue(instanceSpec)
    }

    return bindingFormController
}

export function showPortDialog(constructorSpec, portSpec) {
    console.log("showPortDialog; ctorSpec: ", constructorSpec, " portSpec: ", portSpec)

    let portEditorContainer = document.getElementById("dialog")
    portEditorContainer.textContent = ""
    let dialogTitleElement = document.createElement("div")
    portEditorContainer.appendChild(dialogTitleElement)

    let kind = constructorSpec.kind
    let instanceSpec = portSpec != null ? portSpec.configuration : {}

    dialogTitleElement.className = "dialogTitle"
    dialogTitleElement.textContent = portSpec == null ? "Add " : "Edit "

    let inputDiv = document.createElement("div")
    inputDiv.classList.add("dialogFields")

    let nameTemplate = constructorSpec.nameTemplate

    let portSchema = nameTemplate ? [] : [{
        "name": "name",
        "type": "String",
        "modifiers": ["CONSTANT"],
        "validation": {
            "Integration name conflict": (name) => Integration.map[name.toLowerCase()] == null,
            "Factory name conflict": (name) => IntegrationFactory.get(name) == null,
            "Function name conflict": (name) => Operation.get(name) == null,
            "Valid: letters, '_', digits after '_'": /^[a-zA-Z]+(_[a-zA-Z0-9_]*)?$/
        }}]


    if (kind == "OUTPUT_PORT") {
        portSchema.push({"name": "source"})
        dialogTitleElement.append(constructorSpec.name == "NamedCell" ? "Named Cell(s)" : "Output Port")
    } else {
        dialogTitleElement.append("Input Port")
    }

    let previousName = portSpec == null ? null : portSpec["name"]

    let portFormController = FormController.create(inputDiv, transformSchema(portSchema))
    portFormController.setValue(portSpec == null ? {name: ""} : portSpec)
    portEditorContainer.appendChild(inputDiv)

    let bindingFormController = renderBinding(inputDiv, constructorSpec, instanceSpec)

    let desc = portEditorContainer.appendChild(document.createElement("p"))
    desc.textContent = constructorSpec.description

    let integrationName = constructorSpec.fqName.substring(0, constructorSpec.fqName.indexOf("."))

    let buttonDiv = document.createElement("div")
    let okButton = document.createElement("button")
    okButton.textContent = portSpec == null ? "Create" : "Ok"
    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let values = portFormController.getValue()

        if (bindingFormController != null) {
            values["configuration"] = bindingFormController.getValue()
        }
        values["kind"] = constructorSpec.name

        if (nameTemplate) {
            let name = nameTemplate
            let config = bindingFormController.getValue()
            for (let key in config) {
                name = name.replace("{" + key + "}", config[key])
            }
            values["name"] = name
        }

        if (previousName != null && previousName != values.name) {
            post("ports/" + integrationName + "/" + previousName, {deleted: true})
        }
        post("ports/" + integrationName + "/" + values.name, values)
        hidePortDialog()
    })
    buttonDiv.appendChild(okButton)

    let cancelButton = document.createElement("button")
    cancelButton.textContent = "Cancel"
    cancelButton.className = "dialogButton"
    cancelButton.addEventListener("click", () => { hidePortDialog() })
    buttonDiv.appendChild(cancelButton)

    if (previousName != null) {
        let deleteButton = document.createElement("button")
        deleteButton.textContent = "Delete"
        deleteButton.className = "dialogButton"
        deleteButton.addEventListener("click", () => {
            post("ports/" + integrationName + "/" + previousName, {deleted: true})
            hidePortDialog()
        })
        buttonDiv.appendChild(deleteButton)
    }

    portEditorContainer.appendChild(buttonDiv)
    portEditorContainer.showModal()
}

