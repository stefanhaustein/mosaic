
let imageDialog = document.getElementById("imageDialog")
let imageDialogContent = document.getElementById("imageDialogContent")

let selectedElement = null

imageDialogContent.addEventListener("click", event => {
    selectElement(event.target)
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
        selectedElement.parentElement.open = true
        selectedElement.style.backgroundColor = "#aaf"
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
