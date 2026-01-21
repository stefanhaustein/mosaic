package org.kobjects.mosaic.pluginapi

interface ValueChangeListener {

    /**
     * This does not provide a new value, as in the case of function parameters, these might have changed in the
     * current iteration as well. Also, a client-drive way to obtain a value is typically required anyway and in
     * the case of stateful functions, the value might depend on other parameters.
     */
    fun notifyValueChanged(token: ModificationToken)
}