import {addCellSelectionListener, currentCell, commitCurrentCell} from "./shared_state.js";
import {selectImage} from "./image_dialog.js";
import {element} from "./lib/dom.js";

addCellSelectionListener(() => update())

let stylesDiv = document.getElementById("stylesDiv")

document.getElementById("addImageDiv").addEventListener("click", () => {
    let e = {image: "x"}
    if (currentCell.s == null) {
        currentCell.s = [e]
    } else {
        currentCell.s.push(e)
    }
    commitCurrentCell()
})
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
            let styleDiv = element("div")
            styleDiv.style.paddingTop="5px"
            styleDiv.style.paddingBottom="5px"

            let removeImg = element("img", {src: "img/cancel.svg", style: {float: "left", paddingTop: "2px"}})
            removeImg.addEventListener("click", () => {
                styles.splice(i, 1)
                commitCurrentCell()
            })
            styleDiv.append(removeImg)

            let contentDiv =  element("div", {style: {paddingLeft: "20px"}})
            contentDiv.style.paddingLeft="20px"
            styleDiv.append(contentDiv)

            let conditionInput = element("input",
                {placeholder: "Condition", value: style.condition || ""})
            conditionInput.addEventListener("change", () => {
                style.condition = conditionInput.value
                commitCurrentCell()
            })
            contentDiv.append(conditionInput)

            let colorPicker = document.createElement("argb-picker")
            colorPicker.value = style.color || "#00000000"
            console.log("color set to ", colorPicker.value, " from ", style.color)
            colorPicker.addEventListener("change",() => {
                style.color = colorPicker.value
                console.log("new color:", style.color)
                commitCurrentCell()
            })

            let backgroundPicker = document.createElement("argb-picker")
            backgroundPicker.value = style.background || "#ffffffff"
            backgroundPicker.addEventListener("change",() => {
                style.background = backgroundPicker.value
                commitCurrentCell()
            })


            if (style.image) {
                let gridDiv = element("div", {style: {display: "grid", gridTemplateColumns: "max-content max-content auto", padding: "8px 0", gap: "4px"}})
                let imageDiv = element("div", {style: {gridRow: "1 / span 2", gridColumn: "2"}})
                let svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")

                // svg.style.display = "block"
                svg.style.backgroundColor = backgroundPicker.value
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
                /*
                let select = document.createElement("select")
                select.innerHTML = "<option value='0'>0°</option><option value='90'>90°</option><option value='180'>180°</option><option value='270'>270°</option>"
                select.value = style.rotation || 0
                select.style.width = "60px";
                select.style.height = "60px";
                select.style.display = "inline-block"
                select.style.verticalAlign = "middle"
                select.addEventListener("change", () =>{
                    style.rotation = parseInt(select.value)
                    commitCurrentCell()
                })*/
                imageDiv.style.backgroundImage = "url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAE0lEQVR4nGNoaGj4//8/AxADWQA+5Aj7yVba5wAAAABJRU5ErkJggg==')"
                imageDiv.style.backgroundSize = "50% 50%"
                imageDiv.style.imageRendering = "pixelated"

                imageDiv.append(svg)

                let clockwiseButton = element("button", "↻")
                clockwiseButton.addEventListener("click", () => {
                    style.rotation = ((style.rotation || 0) / 90 + 1) % 4 * 90
                    commitCurrentCell()
                })
                let antiButton = element("button", "↺")
                antiButton.addEventListener("click", () => {
                    style.rotation = ((style.rotation || 0) / 90 + 3) % 4 * 90
                    commitCurrentCell()
                })

                gridDiv.append(clockwiseButton, imageDiv, colorPicker, antiButton,  backgroundPicker)
                contentDiv.append(gridDiv)
            } else {

                let gridDiv = element("div", {style: {display: "grid", gridTemplateColumns: "max-content max-content", padding: "8px 0", gap: "4px"}})
                gridDiv.append("Color:", colorPicker,  "BG:", backgroundPicker)
                contentDiv.append(gridDiv)

                let imageButton = element("button", "Image")
                imageButton.addEventListener("click", async event => {
                    style.image = await selectImage(style.image)
                    commitCurrentCell()
                })
            }

            stylesDiv.append(styleDiv)
        }
    }

}