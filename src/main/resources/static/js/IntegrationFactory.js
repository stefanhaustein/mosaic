export class IntegrationFactory {
    static map = {}

    constructor(name) {
        this.name = name
        this.key = name.toLowerCase()
    }

    // Statics

    static get(name) {
        return IntegrationFactory.map[name.toLowerCase()]
    }

    static register(name, data) {
        let factory = new IntegrationFactory(name)
        for (const key in data) {
            factory[key] = data[key]
        }
        IntegrationFactory.map[name.toLowerCase()] = factory
        return factory
    }
}