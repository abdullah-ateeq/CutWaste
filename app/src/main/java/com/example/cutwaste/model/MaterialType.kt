package com.example.cutwaste.model

enum class MaterialType(val displayName: String) {
    OTHER("Other"),
    WOOD("Wood"),
    METAL("Metal"),
    PIPE("Pipe"),
    PVC("PVC"),
    FABRIC("Fabric"),
    WIRE("Wire"),
    CABLE("Cable"),
    TAPE("Tape"),
    TRIM("Trim");

    companion object {
        fun fromString(name: String?): MaterialType {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}
