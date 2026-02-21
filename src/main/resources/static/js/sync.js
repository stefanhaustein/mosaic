import {renderCell} from "./cell_renderer.js"
import {model} from "./shared_model.js"
import {
    currentCell, currentSheet,
    portValues, selectCell, selectionRangeX, selectionRangeY,
    selectSheet, setRunMode,
} from "./shared_state.js";
import { registerFactory } from "./shared_model.js"
import { blink } from "./lib/dom.js";


import {addOption} from "./lib/dom.js";
import {processFunction} from "./operation_panel_controller.js";
import {processIntegrationInstanceUpdate, updateIntegrationSpec} from "./integration_panel_controller.js";
import {
    processPortSpec,
    processPortUpdate,
    processPortValue,
} from "./port_panel_controller.js";
import {getColumn, getRow, iterateKeys, toCellId} from "./lib/utils.js";

let sheetSelectElement = document.getElementById("sheetSelect")

var currentTag = -1
fetchData(0)


function fetchData(count) {
    var xmlhttp = new XMLHttpRequest();
    var url = "data?tag=" + currentTag;

    xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            proccessUpdateResponseText(this.responseText)
            if (count == 0) {
                // Wiggle cell to get user validation initialized
                selectCell("B1")
                selectCell("A1")
            }
        }
    };
    xmlhttp.open("GET", url, true);
    xmlhttp.send();
    xmlhttp.onloadend = function () {
        setTimeout(() => fetchData(count + 1), 100)
    }
}

function proccessUpdateResponseText(responseText) {
    let lines = responseText.split("\n")
    let sectionMap = {}
    let sectionTitle = ""
    for (const line of lines) {
        let trimmed = line.trim()
        if (trimmed.length == 0) {
            // skip
        } else if (trimmed.startsWith("[")) {
            processSection(sectionTitle, sectionMap)
            sectionTitle = trimmed.substring(1, trimmed.length - 1)
            sectionMap = {}
        } else {
            let eq = trimmed.indexOf("=")
            let col = trimmed.indexOf(":")
            let cut = eq == -1 ? col : (col == -1 ? eq : Math.min(col, eq))
            if (cut != -1) {
                let key = trimmed.substring(0, cut).trim()
                let rawValue = trimmed.substring(cut + 1).trim()
                let value = rawValue == "" ? null : JSON.parse(rawValue)
                sectionMap[key] = value
            }
        }
    }
    processSection(sectionTitle, sectionMap)

    if (currentCell == null) {
       selectSheet()
    }
}

function processSection(sectionName, map) {
    if (sectionName.startsWith("sheets."))  {
        if (sectionName.endsWith(".cells") && sectionName != "sheets.cells") {
            processSheetCellsUpdate(sectionName.substring("sheets.".length, sectionName.length - ".cells".length), map)
        } else {
            processSheetUpdate(sectionName.substring("sheets.".length), map)
        }
    } else switch (sectionName) {
        case "":
            currentTag = map["tag"]
            let runMode = map["runMode"]
            if (runMode != null) {
                setRunMode(runMode)
                updateSheetSelectElement()
            }
            break
        case "factories":
            for (let name in map) {
                processFactoryUpdate(name, map[name])
            }
            break
        case "functions":
            for (let name in map) {
                processFunction(name, map[name])
            }
            break
        case "ports":
            for (let name in map) {
                processPortUpdate(name, map[name])
            }
            break
        case "portValues":
            for (let key in map) {
                processPortValue(key, map)
            }
            break
        case "integrations":
            for (let name in map) {
                processIntegrationInstanceUpdate(name, map[name])
            }
            break
        default:
            console.log("Unrecognizes section: ", sectionName, map)
    }
}


function updateSheetSelectElement() {
    sheetSelectElement.textContent = ""
    for (let key in model.sheets) {
        let option = document.createElement("option")
        option.textContent = key
        sheetSelectElement.appendChild(option)
    }
    addOption(sheetSelectElement, "Edit Sheet Metadata")
    addOption(sheetSelectElement, "Add New Sheet")
    addOption(sheetSelectElement, "Run Mode")
}

function processSheetUpdate(name, map) {
    let sheet = model.sheets[name]
    if (sheet == null || sheetSelectElement.firstElementChild == null) {
        if (sheet == null ) {
            sheet = model.sheets[name] = {
                name: name,
                cells: {}
            }
        }

        updateSheetSelectElement()
        selectSheet()
    }

    let current =  sheet == currentSheet

    if (map["deleted"]) {
        delete model.sheets[name]
        updateSheetSelectElement()
        if (current) {
            selectSheet()
        }
        return
    }

    let highlighted = map["highlighted"]
    if (highlighted != null) {
        if (current && sheet != null) {
            let previous = {}
            let blinking = false
            for (let range of (sheet.highlighted || [])) {
                iterateKeys(range, (key) => {
                    previous[key] = blinking = true
                })
            }
            for (let range of highlighted) {
                iterateKeys(range, (key) => {
                    if (previous[key]) {
                        delete previous[key]
                    } else {
                        let element = document.getElementById(key)
                        if (element != null) {
                            element.classList.add("highlight")
                            if (blinking) blink(element)
                        }
                    }
                })
            }
            for (let key in previous) {
                let element = document.getElementById(key)
                if (element != null) {
                    element.classList.remove("highlight")
                    blink(element)
                }
            }
        }
        sheet.highlighted = highlighted
    }
}

function processSheetCellsUpdate(name, map) {
    let sheet = model.sheets[name]

    let cells = sheet.cells
    for (let key in map) {
        let newValue = map[key]
        if (key.endsWith(".c")) {
            key = key.substring(0, key.length - 2)
            let cell = cells[key]
            if (cell == null) {
                cell = cells[key] = {key: key}
            }
            cell.c = newValue

            blink(document.getElementById(key))
        } else if (key.indexOf(".") == -1) {
            newValue.key = key
            cells[key] = newValue
        } else {
            console.log("Unrecognized suffix for key ", key, "value", newValue)
        }

        if (sheet == currentSheet) {
            renderCell(key)
            if (key == currentCell.key) {
                selectCell(key, selectionRangeX, selectionRangeY)
            }

            let col = getColumn(key)
            if (col > 1) {
                let row = getRow(key)
                let prevKey = toCellId(col - 1, row)
                // we'd need to order cells backwards to avoid double rendering or
                // separate rendering from filling --
                // otherwise, if the prevKey was rendered before this cell was filled,
                // it might still have been empty at this point.
                //if (map[prevKey] == null) {
                    renderCell(prevKey)
                //}
            }
        }
    }
}


function processFactoryUpdate(name, f) {
    registerFactory(name, f)

    switch (f.kind) {
        case "INTEGRATION":
            updateIntegrationSpec(f)
            break
        case "INPUT_PORT":
        case "OUTPUT_PORT":
            processPortSpec(f)
            break
        default:
            console.log("Unrecognized Factory: ", name, f)
    }
}

