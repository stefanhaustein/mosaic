import {currentCell, currentSheet, portValues, setCurrentCellFormula, showDependencies} from "./shared_state.js";
import {showPortDialog} from "./port_editor.js";
import {setDragHandler} from "./lib/dom.js";
import {getIntegrationFactory, getPort, registerPort} from "./shared_model.js";
import {ensureCategory, post} from "./lib/utils.js";
import {updateSpec} from "./artifacts.js";
import {confirmDialog} from "./lib/dialogs.js";


let inputPortSpecListElement = document.getElementById("inputPortSpecList")
let outputPortSpecListElement = document.getElementById("outputPortSpecList")
let sidePanelWidth = 200;

let rangeNameElement = document.getElementById("rangeName")


setDragHandler(document.getElementById("divider"), (dx, dy) => {
    sidePanelWidth -= dx
    document.getElementById("sidePanel").style.flexBasis = sidePanelWidth + "px"
})


export function processPortFactory(spec) {
    let container= spec.kind == "OUTPUT_PORT" ? outputPortSpecListElement : inputPortSpecListElement
    if (spec.name == "NamedCells") {
        document.getElementById("addNamedCellsButton").addEventListener("click", () => { showPortDialog(spec) })
        rangeNameElement.addEventListener("click", async () => {
            let port = getPortInstance(rangeNameElement.textContent)
            showPortDialog(spec, port)
        })
    } else {
        updateSpec(container, "portspec.", spec)
    }
}

export function processPortValue(key, map) {
    let value = map[key]
    portValues[key] = value
    let target = document.getElementById("port." + key + ".value")
    if (target != null) {
        target.textContent = JSON.stringify(value)
    }
}


