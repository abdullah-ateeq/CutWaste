package com.example.cutwaste.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cutwaste.model.CalculationRecord
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.HistorySortOption
import com.example.cutwaste.ui.theme.StatusErrorContainerDark
import com.example.cutwaste.ui.theme.StatusErrorContainerLight
import com.example.cutwaste.ui.theme.StatusErrorRed
import com.example.cutwaste.ui.theme.StatusLeftoverContainerDark
import com.example.cutwaste.ui.theme.StatusLeftoverContainerLight
import com.example.cutwaste.ui.theme.StatusLeftoverTeal
import com.example.cutwaste.ui.theme.StatusPerfectContainerDark
import com.example.cutwaste.ui.theme.StatusPerfectContainerLight
import com.example.cutwaste.ui.theme.StatusPerfectGreen
import com.example.cutwaste.ui.viewmodel.CutWasteViewModel
import com.example.cutwaste.util.HapticUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: CutWasteViewModel,
    onOpenRecord: (CalculationRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val records by viewModel.filteredHistory.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val statusFilter by viewModel.historyStatusFilter.collectAsState()
    val sortOption by viewModel.historySortOption.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val context = LocalContext.current
    val view = LocalView.current

    var deleteRecordTarget by remember { mutableStateOf<CalculationRecord?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CALCULATION HISTORY",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${records.size} saved (${settings.historyLimit} max)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (records.isNotEmpty()) {
                IconButton(onClick = { showClearAllDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all history",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::setHistorySearchQuery,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            placeholder = { Text("Search material, type, notes...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Filter Chips (ALL, PERFECT USE, LEFTOVER, NOT ENOUGH MATERIAL)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = statusFilter == null,
                onClick = { viewModel.setHistoryStatusFilter(null) },
                label = { Text("ALL") }
            )
            CutWasteStatus.entries.forEach { status ->
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { viewModel.setHistoryStatusFilter(status) },
                    label = { Text(status.title) }
                )
            }
        }

        // Sort Dropdown
        ExposedDropdownMenuBox(
            expanded = sortDropdownExpanded,
            onExpandedChange = { sortDropdownExpanded = !sortDropdownExpanded }
        ) {
            OutlinedTextField(
                value = sortOption.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sort By") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = sortDropdownExpanded,
                onDismissRequest = { sortDropdownExpanded = false }
            ) {
                HistorySortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            viewModel.setHistorySortOption(option)
                            sortDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Records List or Empty State
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank() || statusFilter != null) "No matching calculations" else "No saved calculations yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Calculations saved from the calculator will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryCardItem(
                        record = record,
                        onOpen = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.openRecordInCalculator(record)
                            onOpenRecord(record)
                        },
                        onToggleFavorite = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.toggleHistoryFavorite(record.id)
                        },
                        onDuplicate = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.duplicateHistoryRecord(record)
                        },
                        onDelete = {
                            if (settings.confirmDelete) {
                                deleteRecordTarget = record
                            } else {
                                HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                                viewModel.deleteHistoryRecord(record.id)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Delete Single Confirmation Dialog
    deleteRecordTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteRecordTarget = null },
            title = { Text("Delete Calculation") },
            text = { Text("Delete \"${target.materialName}\" from history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                        viewModel.deleteHistoryRecord(target.id)
                        deleteRecordTarget = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteRecordTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Delete all saved cutting calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                        viewModel.clearAllHistory()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryCardItem(
    record: CalculationRecord,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val statusEnum = CutWasteStatus.fromString(record.status)

    val (badgeBg, badgeText) = when (statusEnum) {
        CutWasteStatus.PERFECT_USE -> Pair(
            if (isDark) StatusPerfectContainerDark else StatusPerfectContainerLight,
            StatusPerfectGreen
        )
        CutWasteStatus.LEFTOVER -> Pair(
            if (isDark) StatusLeftoverContainerDark else StatusLeftoverContainerLight,
            StatusLeftoverTeal
        )
        CutWasteStatus.NOT_ENOUGH_MATERIAL -> Pair(
            if (isDark) StatusErrorContainerDark else StatusErrorContainerLight,
            StatusErrorRed
        )
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(record.timestamp) { dateFormatter.format(Date(record.timestamp)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Material Name + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.materialName.ifBlank { "Untitled Material" }.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.materialType} • $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (record.isFavorite) "Unfavorite" else "Favorite",
                            tint = if (record.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onOpen, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in calculator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Metrics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${record.stockLength} ${record.unit} stock  •  ${record.totalCuts} cuts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.requiredCutLength} ${record.unit} required  •  ${record.kerfLoss} ${record.unit} kerf",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusEnum.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Primary Leftover / Waste Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (statusEnum == CutWasteStatus.NOT_ENOUGH_MATERIAL) {
                    Text(
                        text = "Short by: ${record.shortage} ${record.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "Leftover: ${record.remaining} ${record.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Waste: ${record.wastePercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (record.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
