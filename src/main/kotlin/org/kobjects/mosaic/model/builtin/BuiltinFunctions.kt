package org.kobjects.mosaic.model.builtin

import org.kobjects.mosaic.pluginapi.*

object BuiltinFunctions : Plugin {
    override val operationSpecs = listOf<AbstractArtifactSpec>(
        FunctionSpec(
            "Time",
            Type.DATE,
            "now",
            "The current local time",
            listOf(ParameterSpec("interval", Type.REAL, null, setOf(ParameterSpec.Modifier.CONSTANT))),
            createFn = NowFunction::create,
        ),

        FunctionSpec(
            "Math",
            Type.REAL,
            "pi",
            "The value of pi",
            emptyList(),
            createFn = { PiFunction },
        ),

        FunctionSpec(
            "PLC",
            Type.BOOL,
            "toff",
            "Timed Off",
            listOf(ParameterSpec("input", Type.BOOL, null), ParameterSpec(
                "delay",
                Type.REAL,
                null,
                setOf(ParameterSpec.Modifier.CONSTANT)
            )),
            createFn = TimedOnOff::createToff,
        ),

        FunctionSpec(
            "PLC",

            Type.BOOL,
            "ton",
            "Timed On",
            listOf(ParameterSpec("input", Type.BOOL, null), ParameterSpec(
                "delay",
                Type.REAL,
                null,
                setOf(ParameterSpec.Modifier.CONSTANT)
            )),
            createFn = TimedOnOff::createTon,
        ),

        FunctionSpec(
            "PLC",

            Type.BOOL,
            "tp",
            "Timed Pulse",
            listOf(ParameterSpec("input", Type.BOOL, null), ParameterSpec(
                "delay",
                Type.REAL,
                null,
                setOf(ParameterSpec.Modifier.CONSTANT)
            )),
            createFn = TimedPulse::create,
        ),

        FunctionSpec(
            "PLC",

            Type.BOOL,
            "rs",
            "RS-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL, null),
                ParameterSpec("r", Type.BOOL, null)),
            emptySet(),
            0,
        ) { FlipflopFunction.createRs() },

        FunctionSpec(
            "PLC",
            Type.BOOL,
            "sr",
            "SR-Flipflop",
            listOf(
                ParameterSpec("s", Type.BOOL, null),
                ParameterSpec("r", Type.BOOL, null)),
            emptySet(),
            0,
        ) { FlipflopFunction.createSr() },

        FunctionSpec(
            "PLC",
            Type.STRING,
            "statemachine",
            """A state machine specified by the given cell range. 
                |Rows consist of the current state, the transition condition and the new state""".trimMargin(),
            listOf(ParameterSpec("transitions", Type.RANGE, null, setOf(ParameterSpec.Modifier.REFERENCE))),
            createFn = {
                StateMachine.create(it)
            },
        ),

        RestOut.SPEC,

        OutputPortSpec(
            "GPIO",
            "NamedCells",
            "A named range of cells",
            emptyList(),
            emptySet(),
            0
        ) {
                object : OutputPortInstance {
                    override fun setValue(value: Any?) {}
                    override fun detach() {}
                }
            }
    )
}