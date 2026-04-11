export class IntegrationFactory {
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
        let factory = new IntegrationFactory(name)
        for (const key in data) {
            factory[key] = data[key]
        }
        IntegrationFactory.map[name] = factory
        return factory
    }
}