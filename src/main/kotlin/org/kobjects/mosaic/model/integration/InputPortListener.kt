package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.ModificationToken

interface InputPortListener {
    fun portValueChanged(token: ModificationToken, newValue: Any?)
}