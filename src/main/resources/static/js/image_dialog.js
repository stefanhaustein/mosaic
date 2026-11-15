let imageDialog = document.getElementById("imageDialog")
let imageDialogTabs = document.getElementById("imageDialogTabs")
let imageDialogContent = document.getElementById("imageDialogContent")

let selectedElement = null

function selectTab(tabId) {
    for (let button of imageDialogTabs.children) {
        button.classList.toggle("active", button.dataset.tab === tabId)
    }
    for (let panel of imageDialogContent.children) {
        panel.hidden = panel.id !== tabId
    }
}

imageDialogTabs.addEventListener("click", event => {
    let tabId = event.target.dataset.tab
    if (tabId) {
        selectTab(tabId)
    }
})

imageDialogContent.addEventListener("click", event => {
    let svg = event.target.closest("svg")
    if (svg != null) {
        selectElement(svg)
    }
})

function selectElement(svg) {
    if (svg != null) {
        if (svg.localName == "use") {
            svg = svg.parentElement
        }
        if (svg.localName != "svg") {
            svg = null
        }
    }
    if (selectedElement != null) {
        selectedElement.style.backgroundColor = null
    }
    selectedElement = svg
    if (selectedElement != null) {
        selectTab(selectedElement.parentElement.id)
        selectedElement.style.backgroundColor = "#aaf"
    } else {
        selectTab(imageDialogContent.firstElementChild.id)
    }
}

export async function selectImage(currentImage) {
    selectElement(imageDialogContent.querySelector('use[href="img/cell/' + currentImage + '"]'))
    imageDialog.showModal()
    return new Promise(resolve => {
        document.getElementById("imageDialogClearButton").onclick = () => {
            imageDialog.close()
            resolve(null)
        }
        document.getElementById("imageDialogCancelButton").onclick = () => {
            imageDialog.close()
            resolve(currentImage)
        }
        document.getElementById("imageDialogOkButton").onclick = () => {
            imageDialog.close()
            let resultImage = currentImage
            if (selectedElement != null) {
                let use = selectedElement.firstElementChild
                if (use != null) {
                    let href = use.getAttribute("href")
                    if (href != null && href.startsWith("img/cell/")) {
                        resultImage = href.substring(9)
                    }
                }
            }
            resolve(resultImage)
        }
    })
}
