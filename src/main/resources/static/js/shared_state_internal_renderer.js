import {getColumn, getRow, toCellId} from "./lib/utils.js";
import {model} from "./shared_state.js";

// Use via sharedState.showDependencies(name)


export function removeClasses(keys, classList) {
    for (let key of keys) {
        let cut = key.indexOf("!")
        let id = cut == -1 ? "port." + key : key.substring(cut + 1)
        let element = document.getElementById(id)
        if (element != null) {
            element.classList.remove(...classList)
        }
    }
}

export function renderDependencies(
    key, propertyName, seen, depth, classNames) {
    if (seen[key] != null && seen[key] < depth) {
        return
    }
    seen[key] = depth
    let cut = key.indexOf("!")
    let id = cut == -1 ? "port." + key : key.substring(cut + 1)

    let element = document.getElementById(id)
    if (element != null) {
        element.classList.add(classNames[depth >= classNames.length ? classNames.length - 1 : depth])
    }

    let entity = model.sheets[key.substring(0, cut)].cells[key.substring(cut + 1)]
    if (entity != null) {
        let depList = entity[propertyName]
        if (depList != null) {
            for (let childKey of depList) {
                renderDependencies(childKey, propertyName, seen, depth + 1, classNames)
            }
        }
    }
}


export function renderRangeHighlight(rootCellId, rangeX, rangeY, setReset) {
    if (rootCellId == null) {
        return
    }
    let x0 = getColumn(rootCellId)
    let y0 = getRow(rootCellId)
    let y = y0

    let dx = Math.sign(rangeX)
    let dy = Math.sign(rangeY)
    while(true) {
        let x = x0
        while (true) {
            if (x != x0 || y != y0) {
                let cellId = toCellId(x, y)
                let cellElement = document.getElementById(cellId)
                if (cellElement) {
                    if (setReset) {
                        cellElement.classList.add("selected2")
                    } else {
                        cellElement.classList.remove("selected2")
                    }
                }
            }

            if (x == x0 + rangeX) {
                break
            }
            x += dx
        }
        if (y == y0 + rangeY) {
            break
        }
        y += dy
    }
}
