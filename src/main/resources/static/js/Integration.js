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
        return map[name.toLowerCase()]
    }

    static update(name, data) {
        if (data.type == "TOMBSTONE") {
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