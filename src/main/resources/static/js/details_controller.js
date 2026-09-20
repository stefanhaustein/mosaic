import {addCellSelectionListener, currentCell, commitCurrentCell} from "./shared_state.js";
import {selectImage} from "./image_dialog.js";

addCellSelectionListener(() => update())

let stylesDiv = document.getElementById("stylesDiv")

document.getElementById("addStyleDiv").addEventListener("click", () => {
    if (currentCell.s == null) {
        currentCell.s = [{}]
    } else {
        currentCell.s.push({})
    }
    commitCurrentCell()
})

function update() {
    stylesDiv.textContent = ""

    let styles = currentCell.s

    stylesDiv.textContent = ""

    if (Array.isArray(styles)) {
        for (let i = 0; i < styles.length; i++) {
            let style = styles[i]
            let styleDiv = document.createElement("div")
            styleDiv.style.paddingTop="5px"
            styleDiv.style.paddingBottom="5px"

            let removeImg = document.createElement("img")
            removeImg.src = "img/cancel.svg"
            removeImg.style.float = "left"
            removeImg.style.paddingTop="2px"
            removeImg.addEventListener("click", () => {
                styles.splice(i, 1)
                commitCurrentCell()
            })
            styleDiv.append(removeImg)

            let contentDiv = document.createElement("div")
            contentDiv.style.paddingLeft="20px"
            styleDiv.append(contentDiv)

            if (style.image) {
                let svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
                svg.style.background = "url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAE0lEQVR4nGNoaGj4//8/AxADWQA+5Aj7yVba5wAAAABJRU5ErkJggg==')"
                svg.style.imageRendering = "pixelated"
                svg.style.backgroundSize = "50% 50%"
                // svg.style.display = "block"
                svg.style.width = "60px";
                svg.style.height = "60px";
                svg.style.rotate = (style.rotation || 0) + "deg"
                let use = document.createElementNS("http://www.w3.org/2000/svg", "use")
                use.setAttribute("href", "img/cell/" + style.image)
                svg.append(use)
                svg.addEventListener("click", async event => {
                    style.image = await selectImage(style.image)
                    commitCurrentCell()
                })
                svg.style.display = "inline-block"
                svg.style.verticalAlign = "middle"

                let select = document.createElement("select")
                select.innerHTML = "<option value='0'>0°</option><option value='90'>90°</option><option value='180'>180°</option><option value='270'>270°</option>"
                select.value = style.rotation
                select.style.width = "60px";
                select.style.height = "60px";
                select.style.display = "inline-block"
                select.style.verticalAlign = "middle"
                select.addEventListener("change", () =>{
                    style.rotation = parseInt(select.value)
                    commitCurrentCell()
                })
                contentDiv.append(svg, " ", select)
            } else {
                contentDiv.append("[Set Image]")
                contentDiv.addEventListener("click", async event => {
                    style.image = await selectImage(style.image)
                    commitCurrentCell()
                })
            }

            stylesDiv.append(styleDiv)
        }
    }

}