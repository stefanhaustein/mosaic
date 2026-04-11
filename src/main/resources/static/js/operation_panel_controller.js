import {updateSpec} from "./artifacts.js";
import {Operation} from "./Operation.js";


let operationListContainerElement = document.getElementById("operationListContainer")


export function processFunction(name, spec) {
    Operation.register(name, spec)
    updateSpec(operationListContainerElement, "op.details.", spec)
}