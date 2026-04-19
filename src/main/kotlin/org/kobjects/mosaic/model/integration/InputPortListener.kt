package org.kobjects.mosaic.model.integration

import org.kobjects.mosaic.model.ModificationToken

interface InputPortListener {
    /**
     * Notifies the node that the value has changed. The node will request a modification token and update the
     * value asynchronously. Use for individual changes.
     */
    fun portValueChanged(newValue: Any?)

    /**
     * Notifies the node that the value has changed in the skope of the given token. Used where a plugin performs
     * a synchronized batch change of ports.
     */
    fun portValueChanged(newValue: Any?, token: ModificationToken)
}