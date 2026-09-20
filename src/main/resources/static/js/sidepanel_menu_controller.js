

let panelSelectElement = document.getElementById("panelSelect")
export let currentPanelName = ""
let currentPanelElement = null

selectPanel(panelSelectElement.value)

panelSelectElement.addEventListener("change", (ev) => {
    console.log("Select panel: " + name, ev)
    selectPanel(panelSelectElement.value)
})

export function selectPanel(name) {
    if (name == currentPanelName) {
        return name
    }

    let previous = currentPanelName
    if (currentPanelElement != null) {
        currentPanelElement.style.display = "none"
    }
    currentPanelName = name
    if (name != "PortEditor") {
        panelSelectElement.value = name
    }

    let sidePanelElement = document.getElementById("sidePanel")
    currentPanelElement = document.getElementById(name + "Panel") || document.getElementById(name)
    if (name == "Hide") {
        sidePanelElement.style.display = "none"
    } else {
        sidePanelElement.style.display = ""
        currentPanelElement.style.display = "block"
    }

    return previous
}