package com.example.cutwaste.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SerializedCutItem(
    val id: String,
    val length: String,
    val quantity: Long
)

@Serializable
data class CalculationRecord(
    val id: String = UUID.randomUUID().toString(),
    val materialName: String,
    val materialType: String,
    val notes: String = "",
    val unit: String,
    val stockLength: String,
    val cuts: List<SerializedCutItem>,
    val kerfEnabled: Boolean,
    val kerfPerCut: String,
    val totalCuts: Long,
    val requiredCutLength: String,
    val kerfLoss: String,
    val totalConsumed: String,
    val remaining: String,
    val shortage: String,
    val wastePercent: String,
    val usedPercent: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
