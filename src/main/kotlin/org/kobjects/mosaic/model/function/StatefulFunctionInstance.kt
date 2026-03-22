package org.kobjects.mosaic.model.function

import org.kobjects.mosaic.model.ValueChangeListener

interface StatefulFunctionInstance : FunctionInstance {

    fun attach(host: ValueChangeListener)

    fun detach()
}