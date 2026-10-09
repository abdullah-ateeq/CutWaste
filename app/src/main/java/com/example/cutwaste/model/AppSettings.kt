package com.example.cutwaste.model

import kotlinx.serialization.Serializable

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

enum class HistorySortOption(val displayName: String) {
    RECENTLY_UPDATED("Recently Updated"),
    LOWEST_WASTE("Lowest Waste"),
    HIGHEST_WASTE("Highest Waste"),
    LARGEST_LEFTOVER("Largest Leftover"),
    MATERIAL_NAME("Material Name")
}

@Serializable
data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUnit: MaterialUnit = MaterialUnit.MM,
    val defaultStockLength: String = "3000",
    val defaultKerf: String = "0",
    val showKerf: Boolean = false,
    val showWastePercentage: Boolean = true,
    val showUsedPercentage: Boolean = true,
    val showMaterialVisualization: Boolean = true,
    val showFormula: Boolean = true,
    val showCutTable: Boolean = true,
    val showNotes: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true,
    val historyLimit: Int = 100,
    val defaultHistorySort: HistorySortOption = HistorySortOption.RECENTLY_UPDATED
)
