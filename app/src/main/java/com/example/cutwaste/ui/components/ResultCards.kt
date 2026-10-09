package com.example.cutwaste.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.model.CutWasteResult
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.MaterialUnit
import com.example.cutwaste.ui.theme.StatusErrorContainerDark
import com.example.cutwaste.ui.theme.StatusErrorContainerLight
import com.example.cutwaste.ui.theme.StatusErrorRed
import com.example.cutwaste.ui.theme.StatusLeftoverContainerDark
import com.example.cutwaste.ui.theme.StatusLeftoverContainerLight
import com.example.cutwaste.ui.theme.StatusLeftoverTeal
import com.example.cutwaste.ui.theme.StatusPerfectContainerDark
import com.example.cutwaste.ui.theme.StatusPerfectContainerLight
import com.example.cutwaste.ui.theme.StatusPerfectGreen
import java.math.BigDecimal

@Composable
fun PrimaryResultCard(
    stockLength: BigDecimal,
    result: CutWasteResult,
    unit: MaterialUnit,
    statusExplanation: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val (badgeBg, badgeText, statusIcon) = when (result.status) {
        CutWasteStatus.PERFECT_USE -> Triple(
            if (isDark) StatusPerfectContainerDark else StatusPerfectContainerLight,
            StatusPerfectGreen,
            Icons.Default.CheckCircle
        )
        CutWasteStatus.LEFTOVER -> Triple(
            if (isDark) StatusLeftoverContainerDark else StatusLeftoverContainerLight,
            StatusLeftoverTeal,
            Icons.Default.Info
        )
        CutWasteStatus.NOT_ENOUGH_MATERIAL -> Triple(
            if (isDark) StatusErrorContainerDark else StatusErrorContainerLight,
            StatusErrorRed,
            Icons.Default.Warning
        )
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.semantics {
                        contentDescription = "Status: ${result.status.title}"
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = result.status.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = "${result.totalCuts} cuts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Big Dominant Result Value
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (result.status == CutWasteStatus.NOT_ENOUGH_MATERIAL) {
                    Text(
                        text = "SHORT BY",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = FormatUtils.formatMeasurement(result.shortage, unit),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics {
                            contentDescription = "Short by ${FormatUtils.formatMeasurement(result.shortage, unit)}"
                        }
                    )
                } else {
                    Text(
                        text = "REMAINING MATERIAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = FormatUtils.formatMeasurement(result.remaining, unit),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics {
                            contentDescription = "${FormatUtils.formatMeasurement(result.remaining, unit)} remaining"
                        }
                    )
                }

                Text(
                    text = statusExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricColumn(
                    label = "STOCK",
                    value = FormatUtils.formatMeasurement(stockLength, unit)
                )
                MetricColumn(
                    label = "CUTS",
                    value = FormatUtils.formatMeasurement(result.requiredCutLength, unit)
                )
                MetricColumn(
                    label = "KERF",
                    value = FormatUtils.formatMeasurement(result.kerfLoss, unit)
                )
                MetricColumn(
                    label = "USED",
                    value = FormatUtils.formatMeasurement(result.totalConsumed, unit)
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MaterialUsageCard(
    stockLength: BigDecimal,
    result: CutWasteResult,
    unit: MaterialUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Consumption breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            UsageRow(label = "Required Cut Length", value = FormatUtils.formatMeasurement(result.requiredCutLength, unit))
            UsageRow(label = "Kerf Loss (Gaps)", value = FormatUtils.formatMeasurement(result.kerfLoss, unit))
            UsageRow(
                label = "Total Consumed",
                value = FormatUtils.formatMeasurement(result.totalConsumed, unit),
                isEmphasized = true
            )
            UsageRow(
                label = if (result.isOverCapacity) "Shortage" else "Remaining Material",
                value = if (result.isOverCapacity) FormatUtils.formatMeasurement(result.shortage, unit) else FormatUtils.formatMeasurement(result.remaining, unit),
                isEmphasized = true,
                valueColor = if (result.isOverCapacity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UsageRow(
    label: String,
    value: String,
    isEmphasized: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isEmphasized) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodyMedium,
            color = if (isEmphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isEmphasized) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}

@Composable
fun WasteUsageCard(
    result: CutWasteResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "WASTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (result.isOverCapacity) "0%" else FormatUtils.formatPercent(result.wastePercent),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isOverCapacity) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "USED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (result.isOverCapacity) "Over capacity" else FormatUtils.formatPercent(result.usedPercent),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isOverCapacity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Proportional indicator bar
            val usedFloat = if (result.isOverCapacity) 1f else (result.usedPercent.toFloat() / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { usedFloat },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (result.isOverCapacity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
