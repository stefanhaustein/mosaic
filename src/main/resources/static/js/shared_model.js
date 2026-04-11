import {Integration} from "./Integration.js";

let integrationFactories = {}
let functions = {}


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

export function getIntegrationFactory(name) {
    return integrationFactories[name.toLowerCase()]
}

export function getFqPort(name) {
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

