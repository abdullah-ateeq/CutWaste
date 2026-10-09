package com.example.cutwaste.model

import java.math.BigDecimal
import java.math.RoundingMode

enum class MaterialUnit(
    val symbol: String,
    val displayName: String,
    val factorToMm: BigDecimal
) {
    MM("mm", "Millimeters (mm)", BigDecimal("1")),
    CM("cm", "Centimeters (cm)", BigDecimal("10")),
    M("m", "Meters (m)", BigDecimal("1000")),
    IN("in", "Inches (in)", BigDecimal("25.4")),
    FT("ft", "Feet (ft)", BigDecimal("304.8"));

    fun toBaseMm(value: BigDecimal): BigDecimal {
        return value.multiply(factorToMm)
    }

    fun fromBaseMm(valueInMm: BigDecimal): BigDecimal {
        if (this == MM) return valueInMm
        return valueInMm.divide(factorToMm, 8, RoundingMode.HALF_UP)
    }

    companion object {
        fun fromString(name: String?): MaterialUnit {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.symbol.equals(name, ignoreCase = true) } ?: MM
        }
    }
}
