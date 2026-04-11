
import {post} from "./lib/utils.js"
import {currentSheet, model, runMode, selectSheet} from "./shared_state.js";

let sheetDialogElement = document.getElementById("sheetDialog")
let sheetSelectElement = document.getElementById("sheetSelect")
let sheetDialogTitleElement = document.getElementById("sheetDialogTitle")
let sheetDialogOkButtonElement = document.getElementById("sheetOkButton")
let sheetNameInputElement = document.getElementById("sheetNameInput")
let sheetDialogDeleteButton = document.getElementById("sheetDeleteButton")

document.getElementById("sheetCancelButton").addEventListener("click", () => sheetDialogElement.close())

sheetSelectElement.addEventListener("change", () => {
    let selectedValue = sheetSelectElement.value
    sheetDialogTitleElement.textContent = selectedValue
    if (selectedValue == "Edit Sheet Metadata") {
        sheetSelectElement.value = currentSheet.name
        sheetDialogOkButtonElement.textContent = "Ok"
        editSheetMetadata(currentSheet.name)
    } else if (selectedValue == "Add New Sheet") {
        sheetSelectElement.value = currentSheet.name
        sheetDialogOkButtonElement.textContent = "Add"
        editSheetMetadata()
    } else if (selectedValue == "Run Mode") {
        sheetSelectElement.value = currentSheet.name
        post("runMode", !runMode)
    } else {
        selectSheet(selectedValue)
    }
})


function editSheetMetadata(sheetName) {
    let previousName = sheetName
    let sheetCount = 0
    for (let key in model.sheets) {
        sheetCount++
    }

    if (sheetName == null) {
        let n = sheetCount
        do {
            n++
            sheetName = "Sheet" + n
        } while (model.sheets[sheetName] != null)
        sheetDialogDeleteButton.style.display = "none"
    } else {
        sheetDialogDeleteButton.style.display = ""
        sheetDialogDeleteButton.disabled = sheetCount == 1
    }

    sheetNameInputElement.value = sheetName

    sheetDialogElement.showModal()

    sheetDialogDeleteButton.onclick = () => {
        if (confirm("Delete Sheet " + name)) {
            fetch(new Request("sheet", {method: "POST", body: JSON.stringify({previousName: previousName})}))
        }
        sheetDialogElement.close()
    }

    sheetDialogOkButtonElement.onclick = () => {
        let body = {
            previousName: previousName,
            name: sheetNameInputElement.value }
        fetch(new Request("sheet", {method: "POST", body: JSON.stringify(body)}))
        sheetDialogElement.close()
    }

}


