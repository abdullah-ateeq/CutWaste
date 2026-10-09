package com.example.cutwaste.model

import java.math.BigDecimal

data class CutWasteResult(
    val totalCuts: Long,
    val requiredCutLength: BigDecimal,
    val kerfLoss: BigDecimal,
    val totalConsumed: BigDecimal,
    val remaining: BigDecimal,
    val shortage: BigDecimal,
    val wastePercent: BigDecimal,
    val usedPercent: BigDecimal,
    val status: CutWasteStatus,
    val isOverCapacity: Boolean
)
