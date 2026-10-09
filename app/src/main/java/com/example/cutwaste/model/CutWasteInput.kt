package com.example.cutwaste.model

import java.math.BigDecimal

data class CutWasteInput(
    val materialName: String = "",
    val notes: String = "",
    val materialType: MaterialType = MaterialType.OTHER,
    val unit: MaterialUnit = MaterialUnit.MM,
    val stockLength: BigDecimal,
    val cuts: List<CutItem>,
    val kerfEnabled: Boolean = false,
    val kerfPerCut: BigDecimal = BigDecimal.ZERO
)
