package com.example.cutwaste.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.model.CutWasteResult
import com.example.cutwaste.model.MaterialUnit
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialBarVisualization(
    stockLength: BigDecimal,
    result: CutWasteResult,
    unit: MaterialUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Material usage estimate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Stock: ${FormatUtils.formatMeasurement(stockLength, unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Conceptual Material Bar
            val totalCapacity = if (result.isOverCapacity) {
                result.totalConsumed
            } else {
                stockLength
            }

            val cutsFraction = if (totalCapacity > BigDecimal.ZERO) {
                result.requiredCutLength.toFloat() / totalCapacity.toFloat()
            } else 0f

            val kerfFraction = if (totalCapacity > BigDecimal.ZERO && result.kerfLoss > BigDecimal.ZERO) {
                result.kerfLoss.toFloat() / totalCapacity.toFloat()
            } else 0f

            val remainingFraction = if (!result.isOverCapacity && totalCapacity > BigDecimal.ZERO) {
                result.remaining.toFloat() / totalCapacity.toFloat()
            } else 0f

            val shortageFraction = if (result.isOverCapacity && totalCapacity > BigDecimal.ZERO) {
                result.shortage.toFloat() / totalCapacity.toFloat()
            } else 0f

            val barDescription = if (result.isOverCapacity) {
                "Material bar showing cut length ${FormatUtils.formatMeasurement(result.requiredCutLength, unit)}, kerf ${FormatUtils.formatMeasurement(result.kerfLoss, unit)}, and shortage ${FormatUtils.formatMeasurement(result.shortage, unit)}"
            } else {
                "Material bar showing consumed ${FormatUtils.formatMeasurement(result.totalConsumed, unit)}, remaining ${FormatUtils.formatMeasurement(result.remaining, unit)}"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .semantics { contentDescription = barDescription }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (cutsFraction > 0.001f) {
                        Box(
                            modifier = Modifier
                                .weight(cutsFraction.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (kerfFraction > 0.001f) {
                        Box(
                            modifier = Modifier
                                .weight(kerfFraction.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                    }

                    if (remainingFraction > 0.001f) {
                        Box(
                            modifier = Modifier
                                .weight(remainingFraction.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        )
                    }

                    if (shortageFraction > 0.001f) {
                        Box(
                            modifier = Modifier
                                .weight(shortageFraction.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }

            // Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LegendIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    label = "Cuts: ${FormatUtils.formatMeasurement(result.requiredCutLength, unit)}"
                )

                if (result.kerfLoss > BigDecimal.ZERO) {
                    LegendIndicator(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = "Kerf: ${FormatUtils.formatMeasurement(result.kerfLoss, unit)}"
                    )
                }

                if (!result.isOverCapacity) {
                    LegendIndicator(
                        color = MaterialTheme.colorScheme.outline,
                        label = "Remaining: ${FormatUtils.formatMeasurement(result.remaining, unit)}"
                    )
                } else {
                    LegendIndicator(
                        color = MaterialTheme.colorScheme.error,
                        label = "Shortage: ${FormatUtils.formatMeasurement(result.shortage, unit)}"
                    )
                }
            }

            Text(
                text = "Note: Conceptual estimation only. Does not represent optimized layout.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun LegendIndicator(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
