package com.example.cutwaste.model

enum class CutWasteStatus(
    val title: String,
    val description: String
) {
    PERFECT_USE(
        title = "PERFECT USE",
        description = "All available material is consumed."
    ),
    LEFTOVER(
        title = "LEFTOVER",
        description = "Material remains after the planned cuts."
    ),
    NOT_ENOUGH_MATERIAL(
        title = "NOT ENOUGH MATERIAL",
        description = "More material is required to complete these cuts."
    );

    companion object {
        fun fromString(name: String?): CutWasteStatus {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.title.equals(name, ignoreCase = true) } ?: LEFTOVER
        }
    }
}
