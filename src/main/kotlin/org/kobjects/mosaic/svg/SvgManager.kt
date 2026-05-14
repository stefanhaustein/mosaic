package org.kobjects.mosaic.svg

import java.io.File

class SvgManager(root: File) {

    val map = buildMap {
        loadDirectory(this, root, "img/")
    }


    fun loadDirectory(map: MutableMap<String, ParameterizableSvg>,  dir: File, webPath: String) {
        for (file in dir.listFiles() ?: emptyArray()) {
            if (file.isDirectory) {
                loadDirectory(map, file, webPath + file.name + "/")
            } else if (file.name.endsWith(".svg")) {
                println("Loading file ${file.name}; local base path: $webPath")
                val path = webPath + file.name
                map[path] = ParameterizableSvg.load(file)
            }
        }
    }
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