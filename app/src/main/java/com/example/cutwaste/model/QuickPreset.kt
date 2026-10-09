package com.example.cutwaste.model

data class PresetCut(
    val length: String,
    val quantity: Long
)

data class QuickPreset(
    val name: String,
    val materialType: MaterialType,
    val stockLength: String,
    val unit: MaterialUnit,
    val kerfEnabled: Boolean,
    val kerfValue: String,
    val cuts: List<PresetCut>
)

object QuickPresets {
    val all = listOf(
        QuickPreset(
            name = "Timber",
            materialType = MaterialType.WOOD,
            stockLength = "3000",
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfValue = "3",
            cuts = listOf(
                PresetCut("1000", 2),
                PresetCut("500", 1)
            )
        ),
        QuickPreset(
            name = "Metal Bar",
            materialType = MaterialType.METAL,
            stockLength = "6000",
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfValue = "2",
            cuts = listOf(
                PresetCut("1200", 3),
                PresetCut("800", 1)
            )
        ),
        QuickPreset(
            name = "PVC Trim",
            materialType = MaterialType.PVC,
            stockLength = "2400",
            unit = MaterialUnit.MM,
            kerfEnabled = true,
            kerfValue = "1",
            cuts = listOf(
                PresetCut("600", 3)
            )
        )
    )
}
