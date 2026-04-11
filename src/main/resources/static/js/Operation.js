export class Operation {
    static map = {}

    constructor(name) {
        this.name = name
        this.key = name.toLowerCase()

    }

    get(name) {
        return map[name.toLowerCase()]
    }

    // Statics

    static register(name, data) {
        let operation = new Operation(name)
        for (const key in data) {
            operation[key] = data[key]
        }
        Operation.map[name] = operation
        return operation
    }
}