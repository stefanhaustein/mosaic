package org.kobjects.mosaic.pluginapi

interface Plugin {
    fun notifySimulationModeChanged(token: ModificationToken) {

    }

    val operationSpecs: List<AbstractArtifactSpec>
}