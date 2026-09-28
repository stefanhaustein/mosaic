export function addOption(selectElement, name, value) {
    let option = document.createElement("option")
    option.textContent = name
    if (value) {
        option.setAttribute("value", value)
    } else {
        value = name
    }
    let child = selectElement.firstElementChild
    while (child != null && (child.getAttribute("value") || child.textContent).toLocaleLowerCase() < value) {
        child = child.nextElementSibling
    }
    if (child == null) {
        selectElement.appendChild(option)
    } else {
        selectElement.insertBefore(option, child)
//            child.insertAdjacentElement("beforebegin", element)
    }
    return option
}

export function blink(element) {
    if (element) {
        element.classList.add("changed")
        setTimeout(() => {
            element.classList.remove("changed")
        }, 1000)
    }

}

export function element(name, ...content) {
    let e = document.createElement(name)
    for (let c of content) {
        if (c instanceof Object && !(c instanceof String) && !(c instanceof Node)) {
            for (let key in c) {
                let value = c[key]
                if (key == "style" && !(value instanceof String)) {
                    for (let p in value) {
                        e.style[p] = value[p]
                    }
                } else {
                    e.setAttribute(key, value)
                }
            }
        } else {
            e.append(c)
        }
    }
    return e
}

export function insertById(parent, element) {
    let id = element.id
    let existing = document.getElementById(id)
    if (existing) {
        existing.replaceWith(element)
    } else {
        let child = parent.firstElementChild
        while (child != null && child.id < id) {
            child = child.nextElementSibling
        }
        if (child == null) {
            parent.appendChild(element)
        } else {
            parent.insertBefore(element, child)
//            child.insertAdjacentElement("beforebegin", element)
        }
    }
}


export function setDragHandler(element, action) {
    // Two variables for tracking positions of the cursor
    let lastX = 0;
    let lastY = 0;

    element.addEventListener("mousedown", (e) => {
        lastX = e.clientX;
        lastY = e.clientY;
        document.addEventListener("mousemove", onMouseMove)
        e.preventDefault()
    })

    // A function that will be called whenever the up event of the mouse is raised
    function onMouseMove(e) {
        e.preventDefault()
        if (e.buttons == 0) {
            document.removeEventListener("mousemove", onMouseMove)
            return
        }

        let deltaX = e.clientX - lastX
        let deltaY = e.clientY - lastY

        lastX = e.clientX
        lastY = e.clientY

        action(deltaX, deltaY)
    }
}