package com.example.cutwaste.calculator

import com.example.cutwaste.model.MaterialUnit
import java.math.BigDecimal
import java.math.RoundingMode

object FormatUtils {

    /**
     * Formats a BigDecimal to up to [maxDecimals] decimal places,
     * stripping unnecessary trailing zeros without scientific notation.
     */
    fun formatNumber(value: BigDecimal, maxDecimals: Int = 2): String {
        return try {
            val rounded = value.setScale(maxDecimals, RoundingMode.HALF_UP)
            val stripped = rounded.stripTrailingZeros()
            stripped.toPlainString()
        } catch (e: Exception) {
            "0"
        }
    }

    /**
     * Formats a measurement with its unit symbol, e.g. "2500 mm" or "494.5 mm".
     */
    fun formatMeasurement(value: BigDecimal, unit: MaterialUnit): String {
        return "${formatNumber(value)} ${unit.symbol}"
    }

    /**
     * Formats a percentage value, e.g. "16.47%" or "0%".
     */
    fun formatPercent(value: BigDecimal): String {
        return "${formatNumber(value, 2)}%"
    }

    /**
     * Safely parses a string into a BigDecimal.
     * Replaces comma with dot for international input keyboards.
     * Returns null if blank or invalid.
     */
    fun parseBigDecimalOrNull(input: String?): BigDecimal? {
        if (input.isNullOrBlank()) return null
        return try {
            val sanitized = input.trim().replace(',', '.')
            BigDecimal(sanitized)
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Safely parses a string into a Long quantity.
     * Returns null if blank, invalid, or <= 0.
     */
    fun parseQuantityOrNull(input: String?): Long? {
        if (input.isNullOrBlank()) return null
        return try {
            val sanitized = input.trim()
            val parsed = sanitized.toLong()
            if (parsed > 0) parsed else null
        } catch (e: NumberFormatException) {
            null
        }
    }
}
