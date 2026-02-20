package org.kobjects.mosaic.svg

import org.kobjects.mosaic.pluginapi.*
import java.io.File

class SvgManager(root: File) {

    val map = mutableMapOf<String, ParameterizableSvg>()

    init {
        loadDirectory(root, "img/")
    }

    fun loadDirectory(dir: File, basePath: String) {
        for (file in dir.listFiles() ?: emptyArray()) {
            if (file.isDirectory) {
                loadDirectory(file, basePath + file.name + "/")
            } else if (file.name.endsWith(".svg")) {
                println("Loading file ${file.name}; local base path: $basePath")
                val path = basePath + file.name
                map[path] = ParameterizableSvg.load(file)
            }
        }
    }

    val operationSpecs = emptyList<AbstractArtifactSpec>()
    /*
    override val operationSpecs: List<OperationSpec>
        get() {
            val result = mutableListOf<OperationSpec>()
            for ((path, svg) in map) {
                if (svg.parameters.isNotEmpty()) {
                    val cut = path.lastIndexOf(".")
                    val spec = OperationSpec(
                        OperationKind.FUNCTION,
                        Type.IMAGE,
                        path.substring("img/".length, cut).replace("/", "."),
                        "Parameterized Symbol",
                        svg.parameters
                    ) { SvgFunction(path) }
                    result.add(spec)
                }
            }
            return result
        }
*/

}