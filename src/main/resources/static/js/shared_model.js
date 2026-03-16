let integrationFactories = {}
let functions = {}
let integrations = {}

export var model = {
    sheets: {}
}

export function getAllIntegrationFactories() {
    return Object.values(integrationFactories)
}

export function getPortFactory(integration, name) {
    let op = integration.factories[name.toLowerCase()];
    if (op == null) {
        console.log("port factory '" + name + "' not found in " + JSON.stringify(integration.factories))
    }
    return op
}

export function getFunction(name) {
    return functions[name.toLowerCase()]
}

export function getIntegration(name) {
    return integrations[name.toLowerCase()]
}

export function getIntegrationFactory(name) {
    return integrationFactories[name.toLowerCase()]
}

export function getFqPort(name) {
    let cut = name.indexOf(".")
    if (cut == -1) {
        return null
    }
    let integration = getIntegration(name.substring(0, cut))
    if (integration == null) {
        return null
    }
    return getPort(integration, name.substring(cut + 1))
}

export function getPort(integration, name) {
    return integration.ports[name.toLowerCase()]
}

export function registerIntegrationFactory(name, factory) {
    factory.name = name
    let key = factory.key = name.toLowerCase()
    if (factory.modifiers != null && factory.modifiers.indexOf("DELETED") != -1) {
        delete factories[key]
        return false
    }
    integrationFactories[key] = factory
    return true
}

export function registerFunction(name, f) {
    f.name = name
    let key = f.key = f.name.toLowerCase()
    functions[key] = f
}

export function registerIntegration(name, instance) {

    let existing = getIntegration(name)

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


export function registerPortFactory(integration, name, spec) {
    spec.name = name
    spec.fqName = integration.name + "." + name
    let key = name.toLowerCase()
    spec.key = key
    if (spec.kind == "TOMBSTONE") {
        delete integration.ports[key]
        return false
    }
    integration.factories[key] = spec
    return true
}

export function registerPort(integration, name, port) {
    port.name = name
    port.fqName = integration.name + "." + name
    let key = name.toLowerCase()
    port.key = key
    if (port.kind == "TOMBSTONE") {
        delete integration.ports[key]
        return false
    }
    integration.ports[key] = port
    return true
}


