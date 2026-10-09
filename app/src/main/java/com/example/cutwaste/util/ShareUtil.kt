package com.example.cutwaste.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.model.CutItem
import com.example.cutwaste.model.CutWasteInput
import com.example.cutwaste.model.CutWasteResult
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.MaterialUnit
import java.math.BigDecimal

object ShareUtil {

    fun formatResultText(
        input: CutWasteInput,
        result: CutWasteResult
    ): String {
        val unit = input.unit
        val sb = StringBuilder()
        sb.appendLine("CutWaste")
        val matName = if (input.materialName.isNotBlank()) input.materialName.trim() else "Untitled Material"
        sb.appendLine("Material: $matName (${input.materialType.displayName})")
        sb.appendLine("Stock Length: ${FormatUtils.formatMeasurement(input.stockLength, unit)}")
        sb.appendLine()
        sb.appendLine("Cuts:")
        val validCuts = input.cuts.filter { it.quantity > 0 && it.length > BigDecimal.ZERO }
        if (validCuts.isNotEmpty()) {
            validCuts.forEach { cut ->
                sb.appendLine("${FormatUtils.formatMeasurement(cut.length, unit)} × ${cut.quantity}")
            }
        } else {
            sb.appendLine("None")
        }
        sb.appendLine()
        sb.appendLine("Required Cuts: ${FormatUtils.formatMeasurement(result.requiredCutLength, unit)}")
        if (input.kerfEnabled) {
            sb.appendLine("Kerf Loss: ${FormatUtils.formatMeasurement(result.kerfLoss, unit)}")
        }
        sb.appendLine("Total Consumed: ${FormatUtils.formatMeasurement(result.totalConsumed, unit)}")

        if (result.status == CutWasteStatus.NOT_ENOUGH_MATERIAL) {
            sb.appendLine("Shortage: ${FormatUtils.formatMeasurement(result.shortage, unit)}")
            sb.appendLine("Status: NOT ENOUGH MATERIAL")
        } else if (result.status == CutWasteStatus.PERFECT_USE) {
            sb.appendLine("Remaining: 0 ${unit.symbol}")
            sb.appendLine("Waste: 0%")
            sb.appendLine("Status: PERFECT USE")
        } else {
            sb.appendLine("Remaining: ${FormatUtils.formatMeasurement(result.remaining, unit)}")
            sb.appendLine("Waste: ${FormatUtils.formatPercent(result.wastePercent)}")
            sb.appendLine("Status: LEFTOVER")
        }

        if (input.notes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("Notes: ${input.notes.trim()}")
        }

        return sb.toString().trim()
    }

    fun copyToClipboard(context: Context, text: String, label: String = "CutWaste") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        } catch (_: Exception) {}
    }

    fun shareText(context: Context, text: String, title: String = "CutWaste Calculation") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}
