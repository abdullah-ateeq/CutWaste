package com.example.cutwaste.calculator

import com.example.cutwaste.model.CutItem
import com.example.cutwaste.model.CutWasteInput
import com.example.cutwaste.model.CutWasteResult
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.MaterialUnit
import java.math.BigDecimal
import java.math.RoundingMode

object CutWasteCalculator {

    /**
     * Converts a measurement value in the specified unit to base unit (millimeters).
     */
    fun convertToBaseUnit(value: BigDecimal, unit: MaterialUnit): BigDecimal {
        return unit.toBaseMm(value)
    }

    /**
     * Converts a measurement value in base millimeters back to the target unit.
     */
    fun convertFromBaseUnit(valueInMm: BigDecimal, targetUnit: MaterialUnit): BigDecimal {
        return targetUnit.fromBaseMm(valueInMm)
    }

    /**
     * Calculates the total number of cuts by summing all quantities.
     * Uses overflow-safe addition.
     */
    fun calculateTotalCuts(cuts: List<CutItem>): Long {
        var total = 0L
        for (cut in cuts) {
            total = try {
                Math.addExact(total, cut.quantity)
            } catch (e: ArithmeticException) {
                Long.MAX_VALUE
            }
        }
        return total
    }

    /**
     * Calculates the total required cut length: sum(length * quantity).
     */
    fun calculateRequiredCutLength(cuts: List<CutItem>): BigDecimal {
        var total = BigDecimal.ZERO
        for (cut in cuts) {
            if (cut.quantity <= 0L || cut.length.compareTo(BigDecimal.ZERO) <= 0) continue
            val qty = BigDecimal.valueOf(cut.quantity)
            total = total.add(cut.length.multiply(qty))
        }
        return total
    }

    /**
     * Calculates kerf loss:
     * If totalCuts <= 1: 0
     * otherwise: (totalCuts - 1) * kerfPerCut
     * The final cut does not require kerf after it.
     */
    fun calculateKerfLoss(totalCuts: Long, kerfPerCut: BigDecimal): BigDecimal {
        if (totalCuts <= 1L || kerfPerCut.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        val cutGaps = BigDecimal.valueOf(totalCuts - 1L)
        return cutGaps.multiply(kerfPerCut)
    }

    /**
     * Calculates total material consumed: requiredCutLength + kerfLoss.
     */
    fun calculateTotalConsumed(requiredCutLength: BigDecimal, kerfLoss: BigDecimal): BigDecimal {
        return requiredCutLength.add(kerfLoss)
    }

    /**
     * Calculates remaining material:
     * If stockLength >= totalConsumed: stockLength - totalConsumed
     * If stockLength < totalConsumed: 0 (shortage is calculated separately)
     */
    fun calculateRemaining(stockLength: BigDecimal, totalConsumed: BigDecimal): BigDecimal {
        return if (stockLength.compareTo(totalConsumed) >= 0) {
            stockLength.subtract(totalConsumed)
        } else {
            BigDecimal.ZERO
        }
    }

    /**
     * Calculates shortage when total consumed exceeds stock length:
     * If totalConsumed > stockLength: totalConsumed - stockLength
     * Otherwise: 0
     */
    fun calculateShortage(stockLength: BigDecimal, totalConsumed: BigDecimal): BigDecimal {
        return if (totalConsumed.compareTo(stockLength) > 0) {
            totalConsumed.subtract(stockLength)
        } else {
            BigDecimal.ZERO
        }
    }

    /**
     * Determines whether cuts fit perfectly, leave leftover material, or exceed stock length.
     */
    fun determineStatus(stockLength: BigDecimal, totalConsumed: BigDecimal): CutWasteStatus {
        val cmp = totalConsumed.compareTo(stockLength)
        return when {
            cmp == 0 -> CutWasteStatus.PERFECT_USE
            cmp < 0 -> CutWasteStatus.LEFTOVER
            else -> CutWasteStatus.NOT_ENOUGH_MATERIAL
        }
    }

    /**
     * Calculates waste percentage: (remaining / stockLength) * 100.
     * When cuts do not fit, shortage is not presented as waste (returns 0).
     */
    fun calculateWastePercent(remaining: BigDecimal, stockLength: BigDecimal): BigDecimal {
        if (stockLength.compareTo(BigDecimal.ZERO) <= 0 || remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        return remaining.multiply(BigDecimal("100")).divide(stockLength, 6, RoundingMode.HALF_UP)
    }

    /**
     * Calculates used percentage: (totalConsumed / stockLength) * 100.
     */
    fun calculateUsedPercent(totalConsumed: BigDecimal, stockLength: BigDecimal): BigDecimal {
        if (stockLength.compareTo(BigDecimal.ZERO) <= 0 || totalConsumed.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        return totalConsumed.multiply(BigDecimal("100")).divide(stockLength, 6, RoundingMode.HALF_UP)
    }

    /**
     * Main calculation function.
     * Converts inputs to base unit (mm), calculates metrics, and converts back to input unit.
     */
    fun calculate(input: CutWasteInput): CutWasteResult {
        val unit = input.unit

        // Base unit conversions (all in millimeters)
        val stockLengthMm = convertToBaseUnit(input.stockLength, unit)
        val kerfPerCutMm = if (input.kerfEnabled) {
            convertToBaseUnit(input.kerfPerCut, unit)
        } else {
            BigDecimal.ZERO
        }

        val cutsInMm = input.cuts.map { cut ->
            CutItem(
                id = cut.id,
                length = convertToBaseUnit(cut.length, unit),
                quantity = cut.quantity
            )
        }

        val totalCuts = calculateTotalCuts(cutsInMm)
        val requiredCutLengthMm = calculateRequiredCutLength(cutsInMm)
        val kerfLossMm = calculateKerfLoss(totalCuts, kerfPerCutMm)
        val totalConsumedMm = calculateTotalConsumed(requiredCutLengthMm, kerfLossMm)
        val remainingMm = calculateRemaining(stockLengthMm, totalConsumedMm)
        val shortageMm = calculateShortage(stockLengthMm, totalConsumedMm)

        val status = determineStatus(stockLengthMm, totalConsumedMm)
        val isOverCapacity = totalConsumedMm.compareTo(stockLengthMm) > 0

        val wastePercent = if (isOverCapacity) {
            BigDecimal.ZERO
        } else {
            calculateWastePercent(remainingMm, stockLengthMm)
        }

        val usedPercent = calculateUsedPercent(totalConsumedMm, stockLengthMm)

        // Convert lengths back to the input unit for display and presentation
        return CutWasteResult(
            totalCuts = totalCuts,
            requiredCutLength = convertFromBaseUnit(requiredCutLengthMm, unit),
            kerfLoss = convertFromBaseUnit(kerfLossMm, unit),
            totalConsumed = convertFromBaseUnit(totalConsumedMm, unit),
            remaining = convertFromBaseUnit(remainingMm, unit),
            shortage = convertFromBaseUnit(shortageMm, unit),
            wastePercent = wastePercent,
            usedPercent = usedPercent,
            status = status,
            isOverCapacity = isOverCapacity
        )
    }

    /**
     * Generates a step-by-step human-readable formula breakdown using standard math symbols:
     * ×, ÷, −, +, =, %
     */
    fun generateFormulaExplanation(
        input: CutWasteInput,
        result: CutWasteResult
    ): List<String> {
        val lines = mutableListOf<String>()
        val unitSym = input.unit.symbol

        // Step 1: Required cuts
        lines.add("Required cuts:")
        val cutParts = input.cuts.filter { it.quantity > 0 && it.length > BigDecimal.ZERO }.map {
            "${FormatUtils.formatNumber(it.length)} × ${it.quantity}"
        }
        if (cutParts.isNotEmpty()) {
            cutParts.forEachIndexed { index, part ->
                if (index == 0) {
                    lines.add(part)
                } else {
                    lines.add("+ $part")
                }
            }
            lines.add("= ${FormatUtils.formatNumber(result.requiredCutLength)} $unitSym")
        } else {
            lines.add("0 $unitSym")
        }

        lines.add("")

        // Step 2: Number of cuts
        lines.add("Number of cuts:")
        lines.add("${result.totalCuts}")

        lines.add("")

        // Step 3: Kerf loss
        lines.add("Kerf loss:")
        if (input.kerfEnabled && result.totalCuts > 1L && input.kerfPerCut > BigDecimal.ZERO) {
            lines.add("(${result.totalCuts} − 1) × ${FormatUtils.formatNumber(input.kerfPerCut)} $unitSym")
            lines.add("= ${FormatUtils.formatNumber(result.kerfLoss)} $unitSym")
        } else {
            lines.add("0 $unitSym (final cut does not require kerf)")
        }

        lines.add("")

        // Step 4: Total consumed
        lines.add("Total consumed:")
        lines.add("${FormatUtils.formatNumber(result.requiredCutLength)} + ${FormatUtils.formatNumber(result.kerfLoss)}")
        lines.add("= ${FormatUtils.formatNumber(result.totalConsumed)} $unitSym")

        lines.add("")

        // Step 5: Remaining or Shortage
        if (result.isOverCapacity) {
            lines.add("Shortage:")
            lines.add("${FormatUtils.formatNumber(result.totalConsumed)} − ${FormatUtils.formatNumber(input.stockLength)}")
            lines.add("= ${FormatUtils.formatNumber(result.shortage)} $unitSym short")
        } else {
            lines.add("Remaining:")
            lines.add("${FormatUtils.formatNumber(input.stockLength)} − ${FormatUtils.formatNumber(result.totalConsumed)}")
            lines.add("= ${FormatUtils.formatNumber(result.remaining)} $unitSym")

            lines.add("")

            lines.add("Waste percentage:")
            lines.add("${FormatUtils.formatNumber(result.remaining)} ÷ ${FormatUtils.formatNumber(input.stockLength)} × 100")
            lines.add("= ${FormatUtils.formatPercent(result.wastePercent)}")
        }

        return lines
    }

    /**
     * Generates a status explanation sentence.
     */
    fun getStatusExplanation(result: CutWasteResult, unit: MaterialUnit): String {
        return when (result.status) {
            CutWasteStatus.PERFECT_USE -> {
                "All available material is consumed."
            }
            CutWasteStatus.LEFTOVER -> {
                "${FormatUtils.formatMeasurement(result.remaining, unit)} remains after the planned cuts."
            }
            CutWasteStatus.NOT_ENOUGH_MATERIAL -> {
                "You need ${FormatUtils.formatMeasurement(result.shortage, unit)} more material to complete these cuts."
            }
        }
    }
}
