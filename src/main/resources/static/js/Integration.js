export class Integration {

    static map = {}

    constructor(name) {
        this.name = name
        this.key = name.toLowerCase()
        this.ports = []
        this.factories = []
    }

    getPort(name) {
        return this.ports[name.toLowerCase()]
    }

    getPortFactory(name) {
        let op = this.factories[name.toLowerCase()];
        if (op == null) {
            console.log("port factory '" + name + "' not found in " + JSON.stringify(this.factories))
        }
        return op
    }

    updatePort(name, port) {
        port.name = name
        port.fqName = this.name + "." + name
        let key = name.toLowerCase()
        port.key = key
        if (port.kind == "TOMBSTONE") {
            delete this.ports[key]
            return null
        }
        this.ports[key] = port
        return port
    }

    updatePortFactory(name, spec) {
        spec.name = name
        spec.fqName = this.name + "." + name
        let key = name.toLowerCase()
        spec.key = key
        if (spec.kind == "TOMBSTONE") {
            delete this.ports[key]
            return null
        }
        this.factories[key] = spec
        return spec
    }


    // Static methods


    static get(name) {
        return Integration.map[name.toLowerCase()]
    }

    static getFqPort(name) {
        let cut = name.indexOf(".")
        if (cut == -1) {
            return null
        }
        let integration = Integration.map[name.substring(0, cut).toLowerCase()]
        if (integration == null) {
            return null
        }
        return integration.getPort(name.substring(cut + 1))
    }

    static update(name, data) {
        if (data.deleted) {
            delete Integration.map[name.toLowerCase()]
            return null
        }

        let integration = Integration.map[name.toLowerCase()]

        if (integration == null) {
            integration = new Integration(name)
            Integration.map[integration.key] = integration
        }

        for (const key in data) {
            integration[key] = data[key]
        }

        return integration
    }

}