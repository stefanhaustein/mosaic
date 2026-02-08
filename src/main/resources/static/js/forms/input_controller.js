import {getType, getOptions, isLiteral, getTypeLabel} from "./input_schema.js";


/**
 * Schema fields:
 *
 * - options: Array of Input options for this element
 * - type: The type of this input element. One of Bool, Integer, Real, String, Enum.
 */

let labelCounter = 0

export class InputController {


    static create(schema, messageElement) {
        if (!schema.isExpression && !schema.isReference) {
            let options = getOptions(schema)
            if (options != null) {
                return new EnumInputController(schema, options, messageElement)
            }
            if (getType(schema) == "Bool") {
                return new EnumInputController(schema, ["True", "False"], messageElement)
            }
        }
        return new TextInputController(schema, messageElement)
    }

    constructor(schema, inputElement, messageElement) {
        this.schema = schema
        this.inputElement = inputElement

        let id = "lbl" + (++labelCounter);
        this.inputElement.setAttribute("id", id)

        this.labelElement = document.createElement("label")
        this.labelElement.setAttribute("for", id)

        let label = schema.label != null ? schema.label
            : schema.name == null ? getTypeLabel(schema) : schema.name + " (" + getTypeLabel(schema) + ")"
        this.labelElement.textContent = label + ": "

        if (messageElement != null) {
            this.messageElement = messageElement
        } else {
            this.messageElement = document.createElement("div")
            this.messageElement.className = "errorMessage"
        }
        this.validation = schema.validation || {}

        this.inputElement.addEventListener("input", () => { this.validate() })
        this.inputElement.addEventListener("focus", () => { this.validate() })

        if (schema["default"] != null) {
            this.setValue(schema["default"])
        }

        this.validate()
    }


    validate() {
        return true
    }

    getValue() {
        // Note that we can't call validate here, as validate for input elements calls
        // getValue()
        let value = this.inputElement.value
        if (isLiteral(this.schema)) {
            let empty = value.trim() === ""
            switch (getType(this.schema)) {
                case "Bool":
                    return empty ? null : value.toLowerCase() === "true"
                case "Real":
                    return empty ? null : Number.parseFloat(this.inputElement.value)
                case "Int":
                    return empty ? null : Number.parseInt(this.inputElement.value)
            }
        }
        return value
    }

    setValue(value) {
        this.inputElement.value = value == null ? "" : value.toString()
        this.validate()
    }
}

class TextInputController extends InputController {

    constructor(schema, messageElement) {
        super(schema, document.createElement(schema?.isMultiLine ? "textarea" : "input"), messageElement)

        if (schema.isReference) {
            switch (getType(schema).toLowerCase()) {
                case "range":
                case "unspecified":
                    this.validation["Cell (range) reference expected"] = /^([a-zA-Z]+[a-zA-Z_0-9]*!)?[a-zA-Z]+[0-9]+(:[a-zA-Z]+[0-9]+)?$/
                    break

                default:
                    this.validation["Cell reference expected"] = /^([a-zA-Z]+[a-zA-Z_0-9]*!)?[a-zA-Z]+[0-9]+$/
            }

        } else if (isLiteral(this.schema)) {
            switch (getType(this.schema).toLowerCase()) {
                case "real":
                    this.validation["Number expected"] = /^[+-]?(\d+([.]\d*)?([eE][+-]?\d+)?|[.]\d+([eE][+-]?\d+)?)$/
                    break
                case "int":
                    this.validation["Integer expected"] = /^[-+]?[0-9]+$/
                    break
            }
        }
    }

    validate() {
        let errorMessage = "\u00a0"
        let value = this.getValue()
        if (getType(this.schema) == "String" ? value == "" : value == null) {
            if (this.schema?.isRequired) {
                errorMessage = "Required Field"
            }
        } else {
            // Not empty
            if (this.schema.max != null && value > this.schema.max) {
                errorMessage = "Max. " + this.schema.max
            } else if (this.schema.min != null && value < this.schema.min) {
                errorMessage = "Min. " + this.schema.min
            } else if (this.validation) {
                for (const msg in this.validation) {
                    let check = this.validation[msg]
                    if (check instanceof RegExp) {
                        if (!this.inputElement.value.match(check)) {
                            errorMessage = msg
                            break
                        }
                    } else if (!check(this.inputElement.value)) {
                        errorMessage = msg
                        break
                    }
                }
            }
        }
        this.messageElement.textContent = errorMessage
        let hasError = errorMessage != "\u00a0"
        if (hasError) {
            this.messageElement.classList.add("error")
            this.inputElement.classList.add("error")
        } else {
            this.messageElement.classList.remove("error")
            this.inputElement.classList.remove("error")
        }
        return !hasError
    }
}


class EnumInputController extends InputController {

    constructor(schema, options, messageElement) {
        super(schema, document.createElement("select"), messageElement);

        for (let option of options) {
            let optionElement = document.createElement("option")
            if (option.label != null && option.value != null) {
                optionElement.textContent = option.label
                optionElement.value = option.value
            } else {
                optionElement.textContent = option
            }
            this.inputElement.appendChild(optionElement)
        }
    }

    validate() {
        let errorMessage = "\u00a0"
        let options = getOptions(this.schema)
        let found = false
        for (let option of options) {
            if ((option.value != null ? option.value : option).toString() == this.inputElement.value) {
                found = true
                break
            }
        }
        if (!found) {
             errorMessage = "Invalid option '" + this.inputElement.value + "'"
        }
        this.messageElement.textContent = errorMessage
        if (!found) {
            this.messageElement.classList.add("error")
            this.inputElement.classList.add("error")
        } else {
            this.messageElement.classList.remove("error")
            this.inputElement.classList.remove("error")
        }
        return found
    }

    setValue(value) {
        let s = value == null ? "null" : value.toString()
        let found = false
        for (let option of this.inputElement.options) {
            if (option.value == s) {
                found = true
                break
            }
        }

        if (!found) {
            let newOption = document.createElement("option")
            newOption.textContent = "(" + s + ")"
            newOption.value = s
            this.inputElement.appendChild(newOption)
        }
        this.inputElement.value = s
        this.validate()
    }

}
