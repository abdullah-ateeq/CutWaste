package com.example.cutwaste.model

import java.math.BigDecimal
import java.util.UUID

data class CutItem(
    val id: String = UUID.randomUUID().toString(),
    val length: BigDecimal,
    val quantity: Long
)
