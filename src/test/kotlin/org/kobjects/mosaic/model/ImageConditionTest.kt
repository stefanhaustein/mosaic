package org.kobjects.mosaic.model

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.kobjects.mosaic.model.expression.EvaluationContext
import org.kobjects.mosaic.model.sheet.Sheet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImageConditionTest {

    @Test
    fun testLocalValueInImageConditions() {
        val model = Model()
        val sheet = Sheet(model, "TestSheet")
        val cell = sheet.getOrCreateCell("B1")

        cell.setJson(buildJsonObject {
            put("f", JsonPrimitive("=5 + 6"))
            put("i", buildJsonArray {
                add(buildJsonObject {
                    put("condition", JsonPrimitive("=value > 10"))
                    put("image", JsonPrimitive("symbol.svg#lamp"))
                })
                add(buildJsonObject {
                    put("condition", JsonPrimitive("=VALUE = 11"))
                    put("image", JsonPrimitive("symbol.svg#motor"))
                })
                add(buildJsonObject {
                    put("condition", JsonPrimitive("=value > 20"))
                    put("image", JsonPrimitive("symbol.svg#valve.h"))
                })
            })
        }, ModificationToken(ModificationKind.STRUCTURE))

        val firstResult = cell.calculateValue(EvaluationContext(model, 1L, emptyMap()))
        assertEquals(11, firstResult.value)
        assertEquals(listOf<Any?>(true, true, false), firstResult.imageConditionValues)

        cell.setJson(buildJsonObject {
            put("f", JsonPrimitive("=20 + 6"))
            put("i", buildJsonArray {
                add(buildJsonObject {
                    put("condition", JsonPrimitive("=value > 10"))
                    put("image", JsonPrimitive("symbol.svg#lamp"))
                })
                add(buildJsonObject {
                    put("condition", JsonPrimitive("=VALUE = 11"))
                    put("image", JsonPrimitive("symbol.svg#motor"))
                })
                add(buildJsonObject {
                    put("condition", JsonPrimitive("=value > 20"))
                    put("image", JsonPrimitive("symbol.svg#valve.h"))
                })
            })
        }, ModificationToken(ModificationKind.STRUCTURE))

        val secondResult = cell.calculateValue(EvaluationContext(model, 2L, emptyMap()))
        assertEquals(26, secondResult.value)
        assertEquals(listOf<Any?>(true, false, true), secondResult.imageConditionValues)
    }

    @Test
    fun testValueIsNotResolvedInRegularCellFormula() {
        val model = Model()
        val sheet = Sheet(model, "TestSheet")
        val cell = sheet.getOrCreateCell("C1")

        cell.setJson(buildJsonObject {
            put("f", JsonPrimitive("=value"))
        }, ModificationToken(ModificationKind.STRUCTURE))

        val result = cell.calculateValue(EvaluationContext(model, 1L, emptyMap()))
        assertIs<IllegalArgumentException>(result.value)
    }
}
