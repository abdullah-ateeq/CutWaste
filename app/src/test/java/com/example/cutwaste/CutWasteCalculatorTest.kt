package com.example.cutwaste

import com.example.cutwaste.calculator.CutWasteCalculator
import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.model.CutItem
import com.example.cutwaste.model.CutWasteInput
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.MaterialType
import com.example.cutwaste.model.MaterialUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CutWasteCalculatorTest {

    @Test
    fun testOneCutWithoutKerf() {
        val cuts = listOf(CutItem(length = BigDecimal("500"), quantity = 1))
        val input = CutWasteInput(
            stockLength = BigDecimal("1000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(1L, result.totalCuts)
        assertEquals(BigDecimal("500"), result.requiredCutLength)
        assertEquals(BigDecimal.ZERO, result.kerfLoss)
        assertEquals(BigDecimal("500"), result.totalConsumed)
        assertEquals(BigDecimal("500"), result.remaining)
        assertEquals(BigDecimal.ZERO, result.shortage)
        assertEquals(CutWasteStatus.LEFTOVER, result.status)
        assertFalse(result.isOverCapacity)
        assertEquals("50%", FormatUtils.formatPercent(result.wastePercent))
        assertEquals("50%", FormatUtils.formatPercent(result.usedPercent))
    }

    @Test
    fun testMultipleCutsAndQuantities() {
        val cuts = listOf(
            CutItem(length = BigDecimal("1000"), quantity = 2),
            CutItem(length = BigDecimal("500"), quantity = 1)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("3000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals(BigDecimal("2500"), result.requiredCutLength)
        assertEquals(BigDecimal.ZERO, result.kerfLoss)
        assertEquals(BigDecimal("2500"), result.totalConsumed)
        assertEquals(BigDecimal("500"), result.remaining)
        assertEquals(CutWasteStatus.LEFTOVER, result.status)
        assertEquals("16.67%", FormatUtils.formatPercent(result.wastePercent))
        assertEquals("83.33%", FormatUtils.formatPercent(result.usedPercent))
    }

    @Test
    fun testExactFit() {
        val cuts = listOf(CutItem(length = BigDecimal("1000"), quantity = 3))
        val input = CutWasteInput(
            stockLength = BigDecimal("3000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals(BigDecimal("3000"), result.requiredCutLength)
        assertEquals(BigDecimal.ZERO, result.kerfLoss)
        assertEquals(BigDecimal("3000"), result.totalConsumed)
        assertEquals(BigDecimal.ZERO, result.remaining)
        assertEquals(BigDecimal.ZERO, result.shortage)
        assertEquals(CutWasteStatus.PERFECT_USE, result.status)
        assertFalse(result.isOverCapacity)
        assertEquals("0%", FormatUtils.formatPercent(result.wastePercent))
        assertEquals("100%", FormatUtils.formatPercent(result.usedPercent))
    }

    @Test
    fun testInsufficientMaterial() {
        val cuts = listOf(
            CutItem(length = BigDecimal("2000"), quantity = 1),
            CutItem(length = BigDecimal("1500"), quantity = 1)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("3000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(2L, result.totalCuts)
        assertEquals(BigDecimal("3500"), result.requiredCutLength)
        assertEquals(BigDecimal("3500"), result.totalConsumed)
        assertEquals(BigDecimal.ZERO, result.remaining)
        assertEquals(BigDecimal("500"), result.shortage)
        assertEquals(CutWasteStatus.NOT_ENOUGH_MATERIAL, result.status)
        assertTrue(result.isOverCapacity)
        assertEquals("0%", FormatUtils.formatPercent(result.wastePercent))
    }

    @Test
    fun testBasicExampleWithKerf() {
        // Stock: 3000 mm, Cuts: 1000 mm x 2, 500 mm x 1, Kerf: 3 mm
        // Total cuts: 3 -> Kerf loss: (3 - 1) * 3 = 6 mm
        // Consumed: 2506 mm, Remaining: 494 mm, Waste: 16.47%
        val cuts = listOf(
            CutItem(length = BigDecimal("1000"), quantity = 2),
            CutItem(length = BigDecimal("500"), quantity = 1)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("3000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("3")
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals(BigDecimal("2500"), result.requiredCutLength)
        assertEquals(BigDecimal("6"), result.kerfLoss)
        assertEquals(BigDecimal("2506"), result.totalConsumed)
        assertEquals(BigDecimal("494"), result.remaining)
        assertEquals(BigDecimal.ZERO, result.shortage)
        assertEquals(CutWasteStatus.LEFTOVER, result.status)
        assertFalse(result.isOverCapacity)
        assertEquals("16.47%", FormatUtils.formatPercent(result.wastePercent))
        assertEquals("83.53%", FormatUtils.formatPercent(result.usedPercent))
    }

    @Test
    fun testOneCutWithKerfRule() {
        // One cut: final cut does NOT add kerf!
        // Stock 1000 mm, Cut 1000 mm x 1, Kerf 10 mm -> kerf loss = 0
        val cuts = listOf(CutItem(length = BigDecimal("1000"), quantity = 1))
        val input = CutWasteInput(
            stockLength = BigDecimal("1000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("10")
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(1L, result.totalCuts)
        assertEquals(BigDecimal.ZERO, result.kerfLoss)
        assertEquals(BigDecimal("1000"), result.totalConsumed)
        assertEquals(BigDecimal.ZERO, result.remaining)
        assertEquals(CutWasteStatus.PERFECT_USE, result.status)
    }

    @Test
    fun testFiveCutsKerfRule() {
        // 5 cuts with 3 mm kerf = 4 * 3 mm = 12 mm kerf loss (NOT 15 mm)
        val cuts = listOf(CutItem(length = BigDecimal("100"), quantity = 5))
        val input = CutWasteInput(
            stockLength = BigDecimal("1000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("3")
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(5L, result.totalCuts)
        assertEquals(BigDecimal("500"), result.requiredCutLength)
        assertEquals(BigDecimal("12"), result.kerfLoss)
        assertEquals(BigDecimal("512"), result.totalConsumed)
        assertEquals(BigDecimal("488"), result.remaining)
        assertEquals(CutWasteStatus.LEFTOVER, result.status)
    }

    @Test
    fun testZeroKerf() {
        val cuts = listOf(CutItem(length = BigDecimal("200"), quantity = 4))
        val input = CutWasteInput(
            stockLength = BigDecimal("1000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfPerCut = BigDecimal.ZERO
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(4L, result.totalCuts)
        assertEquals(BigDecimal.ZERO, result.kerfLoss)
        assertEquals(BigDecimal("800"), result.totalConsumed)
        assertEquals(BigDecimal("200"), result.remaining)
    }

    @Test
    fun testCentimeterUnitConversion() {
        // Stock: 300 cm (= 3000 mm)
        // Cut: 100 cm x 2, 50 cm x 1 (= 2500 mm)
        // Kerf: 0.3 cm (= 3 mm) -> Kerf loss: 2 * 0.3 cm = 0.6 cm (= 6 mm)
        // Total consumed: 250.6 cm (= 2506 mm)
        // Remaining: 49.4 cm (= 494 mm)
        val cuts = listOf(
            CutItem(length = BigDecimal("100"), quantity = 2),
            CutItem(length = BigDecimal("50"), quantity = 1)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("300"),
            cuts = cuts,
            unit = MaterialUnit.CM,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("0.3")
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals("250", FormatUtils.formatNumber(result.requiredCutLength))
        assertEquals("0.6", FormatUtils.formatNumber(result.kerfLoss))
        assertEquals("250.6", FormatUtils.formatNumber(result.totalConsumed))
        assertEquals("49.4", FormatUtils.formatNumber(result.remaining))
        assertEquals("16.47%", FormatUtils.formatPercent(result.wastePercent))
        assertEquals("83.53%", FormatUtils.formatPercent(result.usedPercent))
    }

    @Test
    fun testMeterUnitConversion() {
        // Stock: 3 m (= 3000 mm)
        // Cut: 1 m x 2, 0.5 m x 1 (= 2500 mm)
        // Kerf: 0.003 m (= 3 mm) -> Kerf loss: 0.006 m
        // Consumed: 2.506 m
        // Remaining: 0.494 m
        val cuts = listOf(
            CutItem(length = BigDecimal("1"), quantity = 2),
            CutItem(length = BigDecimal("0.5"), quantity = 1)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("3"),
            cuts = cuts,
            unit = MaterialUnit.M,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("0.003")
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals("2.5", FormatUtils.formatNumber(result.requiredCutLength))
        assertEquals("0.01", FormatUtils.formatNumber(result.kerfLoss, 2))
        assertEquals("2.51", FormatUtils.formatNumber(result.totalConsumed, 2))
        assertEquals("0.49", FormatUtils.formatNumber(result.remaining, 2))
        assertEquals("16.47%", FormatUtils.formatPercent(result.wastePercent))
    }

    @Test
    fun testImperialInchesUnit() {
        // Stock: 100 in
        // Cuts: 20 in x 3 = 60 in
        // Kerf: 0.125 in (1/8 in)
        // Kerf loss: 2 * 0.125 = 0.25 in
        // Consumed: 60.25 in
        // Remaining: 39.75 in
        // Waste: 39.75%
        val cuts = listOf(CutItem(length = BigDecimal("20"), quantity = 3))
        val input = CutWasteInput(
            stockLength = BigDecimal("100"),
            cuts = cuts,
            unit = MaterialUnit.IN,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("0.125")
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals("60", FormatUtils.formatNumber(result.requiredCutLength))
        assertEquals("0.25", FormatUtils.formatNumber(result.kerfLoss))
        assertEquals("60.25", FormatUtils.formatNumber(result.totalConsumed))
        assertEquals("39.75", FormatUtils.formatNumber(result.remaining))
        assertEquals("39.75%", FormatUtils.formatPercent(result.wastePercent))
    }

    @Test
    fun testImperialFeetUnit() {
        // Stock: 10 ft
        // Cut: 2.5 ft x 3 = 7.5 ft
        // Kerf: 0 (disabled)
        // Remaining: 2.5 ft
        // Waste: 25%
        val cuts = listOf(CutItem(length = BigDecimal("2.5"), quantity = 3))
        val input = CutWasteInput(
            stockLength = BigDecimal("10"),
            cuts = cuts,
            unit = MaterialUnit.FT,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(3L, result.totalCuts)
        assertEquals("7.5", FormatUtils.formatNumber(result.requiredCutLength))
        assertEquals("2.5", FormatUtils.formatNumber(result.remaining))
        assertEquals("25%", FormatUtils.formatPercent(result.wastePercent))
    }

    @Test
    fun testLargeQuantitiesAndOverflowSafety() {
        val cuts = listOf(
            CutItem(length = BigDecimal("1"), quantity = 500_000L),
            CutItem(length = BigDecimal("2"), quantity = 500_000L)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("2000000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(1_000_000L, result.totalCuts)
        assertEquals(BigDecimal("1500000"), result.requiredCutLength)
        assertEquals(BigDecimal("500000"), result.remaining)
        assertEquals(CutWasteStatus.LEFTOVER, result.status)
    }

    @Test
    fun testDecimalPrecisionWithoutRoundingPrematurely() {
        // 1/3 cut length: 33.333333 mm x 3 = 99.999999 mm from 100 mm stock
        val cuts = listOf(CutItem(length = BigDecimal("33.333333"), quantity = 3))
        val input = CutWasteInput(
            stockLength = BigDecimal("100"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = false
        )
        val result = CutWasteCalculator.calculate(input)

        assertEquals(BigDecimal("99.999999"), result.requiredCutLength)
        assertEquals(BigDecimal("0.000001"), result.remaining)
        assertEquals(CutWasteStatus.LEFTOVER, result.status)
    }

    @Test
    fun testFormulaGeneration() {
        val cuts = listOf(
            CutItem(length = BigDecimal("1000"), quantity = 2),
            CutItem(length = BigDecimal("500"), quantity = 1)
        )
        val input = CutWasteInput(
            stockLength = BigDecimal("3000"),
            cuts = cuts,
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfPerCut = BigDecimal("3")
        )
        val result = CutWasteCalculator.calculate(input)
        val formula = CutWasteCalculator.generateFormulaExplanation(input, result)

        assertTrue(formula.any { it.contains("1000 × 2") })
        assertTrue(formula.any { it.contains("+ 500 × 1") })
        assertTrue(formula.any { it.contains("2500 mm") })
        assertTrue(formula.any { it.contains("(3 − 1) × 3 mm") })
        assertTrue(formula.any { it.contains("2506 mm") })
        assertTrue(formula.any { it.contains("494 mm") })
        assertTrue(formula.any { it.contains("16.47%") })
    }
}
