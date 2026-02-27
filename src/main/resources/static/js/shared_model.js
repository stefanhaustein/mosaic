let factories = {}
let functions = {}
let integrations = {}

export var model = {
    sheets: {}
}

export function getAllFactories() {
    return Object.values(factories)
}

export function getFactory(name) {
    name = name.toLowerCase()
    let cut = name.indexOf(".")
    if (cut == -1) {
        return functions[name] || factories[name]
    }
    let integration = getIntegrationInstance(name.substring(0, cut))
    if (integration == null) {
        console.log("integration not found: " + name.substring(0, cut))
        return null
    }
    let op = integration.operations.find((entry) => entry.name == name )
    if (op == null) {
        console.log("operation '" + name + "' not found in " + JSON.stringify(integration.operations))
    }
    return op
}

export function getFunction(name) {
    return functions[name.toLowerCase()]
}

export function getIntegrationInstance(name) {
    return integrations[name.toLowerCase()]
}

export function getIntegrationFactory(name) {
    return getFactory(name)
}

export function getPortFactory(fqName) {
    let parts = fqName.split(".")
    let integration = getIntegrationInstance(parts[0].toLowerCase())
    return integration.factories[parts[1].toLowerCase()]
}

export function getPortInstance(fqName) {
    let parts = fqName.split(".")
    let integration = getIntegrationInstance(parts[0].toLowerCase())
    return integration.ports[parts[1].toLowerCase()]
}

export function registerFactory(name, factory) {
    factory.name = name
    let key = factory.key = name.toLowerCase()
    if (factory.modifiers != null && factory.modifiers.indexOf("DELETED") != -1) {
        delete factories[key]
        return false
    }
    factories[key] = factory
    return true
}

export function registerFunction(name, f) {
    f.name = name
    let key = f.key = f.name.toLowerCase()
    functions[key] = f
}

export function registerIntegrationInstance(name, instance) {

    let existing = getIntegrationInstance(name)

    instance.name = name
    let key = name.toLowerCase()

    if (instance.type == "TOMBSTONE") {
        delete integrations[key]
        return false
    }

    instance.key = key
    if (instance.ports == null) {
        instance.ports = existing == null ? [] : existing.ports
    }
    if (instance.factories == null) {
        instance.factories = existing == null ? [] : existing.factories
    }


    integrations[key] = instance
    return true
}

export function registerPortInstance(fqName, port) {
    let parts = fqName.split(".")

    if (parts.length != 2) {
        console.log("fqName ", fqName, " does not contain a dot for port ", port)
        throw Error("fqName "+ fqName+ " does not contain a dot for port "+JSON.stringify(port))
    }

    port.name = parts[1]
    let key = port.name.toLowerCase()
    port.key = key
    let integration = getIntegrationInstance(parts[0].toLowerCase())
    if (port.kind == "TOMBSTONE") {
        delete integration.ports[key]
        return false
    }
    integration.ports[key] = port
    return true
}


