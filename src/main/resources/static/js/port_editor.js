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

    let desc = targetDiv.appendChild(document.createElement("p"))
    desc.textContent = constructorSpec.description

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
    //inputDiv.className = "dialogFields"

    let portSchema = [{
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

    let bindingDiv = document.createElement("div")

    let bindingFormController = renderBinding(bindingDiv, constructorSpec, instanceSpec)
    portEditorContainer.appendChild(bindingDiv)


    let buttonDiv = document.createElement("div")

    let okButton = document.createElement("button")
    okButton.textContent = portSpec == null ? "Create" : "Ok"

    okButton.className = "dialogButton"
    okButton.addEventListener("click", () => {
        let values = portFormController.getValue()
        let source = values["source"]
        if (bindingFormController != null) {
            values["configuration"] = bindingFormController.getValue()
        }
        values["kind"] = constructorSpec.name
        values["previousName"] = previousName

        let integrationName = constructorSpec.fqName.substring(0, constructorSpec.fqName.indexOf("."))
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
            post("ports/" + previousName, {deleted: true})
            hidePortDialog()
        })
        buttonDiv.appendChild(deleteButton)
    }

    portEditorContainer.appendChild(buttonDiv)
    portEditorContainer.showModal()
}

