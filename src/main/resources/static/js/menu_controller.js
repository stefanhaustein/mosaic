import {currentCell, currentSheet, selectionRangeX, selectionRangeY} from "./shared_state.js";
import {alertDialog, confirmDialog} from "./lib/dialogs.js";
import {getColumn, getRow, post, toCellId, toRangeKey} from "./lib/utils.js";

let menuSelectElement = document.getElementById("menuSelect")

let pasteBuffer = ""

menuSelectElement.addEventListener("change", async() => {
    switch (menuSelectElement.value) {
        case "Clear All": clearAll(); break;
        case "Cut": copy(true); break;
        case "Copy": copy(false); break;
        case "Paste": paste(); break;
        case "Load File": uploadSpreadsheet(); break;
        case "Load Example": loadExample(); break;
        case "Save File": downloadSpreadsheet(); break;
    }
    menuSelectElement.value = "Menu"
})


function copy(clear) {
    if (currentCell == null) {
        return
    }
    let rootCellId = currentCell.key
    let x0 = getColumn(rootCellId)
    let y0 = getRow(rootCellId)
    let y = y0

    let dx = Math.sign(selectionRangeX)
    let dy = Math.sign(selectionRangeY)

    let rangeKey =   currentSheet.name + "!" + toRangeKey(x0, y0, selectionRangeX, selectionRangeY)
    pasteBuffer = "range: \"" + rangeKey + "\"\n\n[cells]\n\n"

    while(true) {
        let x = x0
        while (true) {
            let key = toCellId(x, y)
            let cell = currentSheet.cells[key]
            if (cell != null) {
                pasteBuffer += key + ": " + JSON.stringify(cell) + "\n"
            }

            if (x == x0 + selectionRangeX) {
                break
            }
            x += dx
        }
        if (y == y0 + selectionRangeY) {
            break
        }
        y += dy
    }

    if (clear) {
        post("/clear/" + rangeKey)
    }

    console.log("copied: ", pasteBuffer)
}

function paste() {
    let key = currentCell.key
    let col = getColumn(key)
    let row = getRow(key)
    let rangeKey = currentSheet.name + "!" + toRangeKey(col, row, selectionRangeX, selectionRangeY)

    post("/paste/" + rangeKey, pasteBuffer)
}

async function clearAll() {
    if (await confirmDialog("Delete all data and start from scratch?")) {
        post('/clearAll')
    }
}


let firstDownload = true
function downloadSpreadsheet() {
    let downloadDialog = document.getElementById("downloadDialog")

    if (firstDownload) {
        firstDownload = false

        let downloadFileNameInputElement = document.getElementById("downloadFileNameInput")
        let downloadLink = document.getElementById("downloadLink")
        let downloadButton = document.getElementById("downloadButton")
        let downloadCancelButton = document.getElementById("downloadCancel")

        downloadFileNameInputElement.addEventListener("input", () => {
            downloadLink.setAttribute("download", downloadFileNameInputElement.value + ".ts")
        })
        downloadCancelButton.addEventListener("click", () => {downloadDialog.close() })
        downloadButton.addEventListener("click", () => {downloadDialog.close() })
    }
    downloadDialog.showModal()
}

let firstUpload = true
function uploadSpreadsheet() {
    let uploadDialog = document.getElementById("uploadDialog")
    if (firstUpload) {
        firstUpload = false

        let uploadFileInputElement = document.getElementById("uploadFileInput")
        let uploadButton = document.getElementById("uploadButton")
        let uploadCancelButton = document.getElementById("uploadCancel")

        uploadCancelButton.addEventListener("click", () => {uploadDialog.close() })
        uploadButton.addEventListener("click", () => {
            if (confirm("Replace the current spreadsheet with the selected file?")) {
                let formData = new FormData();
                formData.append("file", uploadFileInputElement.files[0]);
                fetch('/upload', {
                    method: "POST",
                    body: formData
                }).then(() => {
                    window.location.reload()
                }, () => {
                    alertDialog("Upload Failed")
                })
            }
        })
    }
    uploadDialog.showModal()
}



let firstExample = true
function loadExample() {
    let exampleDialog = document.getElementById("exampleDialog")
    if (firstExample) {
        firstExample = false

        let exampleSelectElement = document.getElementById("exampleSelect")
        let exampleButton = document.getElementById("exampleButton")
        let exampleCancelButton = document.getElementById("exampleCancel")

        exampleCancelButton.addEventListener("click", () => { exampleDialog.close() })
        exampleButton.addEventListener("click", () => {
            if (confirm("Replace the current spreadsheet with the example file '" + exampleSelectElement.value + "'?")) {
            fetch('/loadExample', {
                method: "POST",
                body: exampleSelectElement.value
            }).then(() => {
                window.location.reload()
            }, () => { alertDialog("Loading Example Failed")})
            }
        })
    }
    exampleDialog.showModal()
}
