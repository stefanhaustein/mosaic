package org.kobjects.mosaic.pluginapi

interface StatefulFunctionInstance : FunctionInstance {

    fun attach(host: ValueChangeListener)

    fun detach()
}