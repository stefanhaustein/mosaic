package org.kobjects.mosaic.model.builtin

import org.kobjects.mosaic.model.Model
import org.kobjects.mosaic.pluginapi.*

class RestOut(val path: String) : OutputPortInstance {

    override fun detach() {
        Model.restValues.remove(path)
    }

    override fun setValue(value: Any?) {
        if (value is RangeValues) {
            val json = mutableMapOf<String, Any?>()
            for (row in 0 until value.height) {
                var col = 0
                while (col < value.width - 1) {
                    json[value[col, row].toString()] = value[col+1, row]
                    col += 2
                }
            }
            Model.restValues[path] = json
        } else {
            Model.restValues[path] = value
        }
    }

    companion object {
        val SPEC = OutputPortSpec(
            null,
            "Network",
            "rest_out",
            """Makes the given value(s) accessible via an JSON object under the '/rest/path' path of this server.""",
            listOf(
//                ParameterSpec("format", Type.ENUM(JsonFormat.entries), setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec(
                    "path",
                    Type.STRING,
                    null,
                    setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)
                ),
            ),
            setOf(AbstractArtifactSpec.Modifier.NO_SIMULATION)
        ) {
            RestOut(it["path"] as String)
        }
    }

    /*
    enum class JsonFormat {
        JSON_KEY_VALUE, JSON_TABLE
    }*/
}