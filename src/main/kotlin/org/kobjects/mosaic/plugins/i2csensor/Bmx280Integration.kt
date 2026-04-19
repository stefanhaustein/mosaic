package org.kobjects.mosaic.plugins.i2csensor

import org.kobjects.mosaic.model.ModelInterface

class Bmx280Integration(model: ModelInterface, name: String, tag: Long) : I2cSensorIntegration(model, "BMX280", name, tag) {
}