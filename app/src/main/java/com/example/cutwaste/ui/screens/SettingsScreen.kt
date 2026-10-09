package com.example.cutwaste.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cutwaste.model.AppTheme
import com.example.cutwaste.model.HistorySortOption
import com.example.cutwaste.model.MaterialUnit
import com.example.cutwaste.ui.viewmodel.CutWasteViewModel
import com.example.cutwaste.util.HapticUtil

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CutWasteViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configure defaults, display toggles, and data limits.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance Card
        SettingsSectionCard(title = "APPEARANCE") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppTheme.entries.forEachIndexed { index, theme ->
                        SegmentedButton(
                            selected = settings.theme == theme,
                            onClick = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                viewModel.updateSettings(settings.copy(theme = theme))
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTheme.entries.size)
                        ) {
                            Text(theme.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                SettingsToggleRow(
                    label = "Dynamic Color",
                    subtitle = "Use system wallpaper colors (Android 12+)",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.updateSettings(settings.copy(dynamicColor = it))
                    }
                )
            }
        }

        // Calculator Defaults Card
        SettingsSectionCard(title = "CALCULATOR DEFAULTS") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Default Unit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MaterialUnit.entries.forEach { u ->
                        FilterChip(
                            selected = settings.defaultUnit == u,
                            onClick = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                viewModel.updateSettings(settings.copy(defaultUnit = u))
                            },
                            label = { Text(u.displayName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = settings.defaultStockLength,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(defaultStockLength = it))
                    },
                    label = { Text("Default Stock Length") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = settings.defaultKerf,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(defaultKerf = it))
                    },
                    label = { Text("Default Kerf Per Cut") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SettingsToggleRow(
                    label = "Enable Kerf by Default",
                    subtitle = "Turn on cutting kerf calculation by default",
                    checked = settings.showKerf,
                    onCheckedChange = {
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.updateSettings(settings.copy(showKerf = it))
                    }
                )
            }
        }

        // Display & Visibility Toggles
        SettingsSectionCard(title = "DISPLAY & SECTIONS") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsToggleRow(
                    label = "Show Material Bar",
                    subtitle = "Display conceptual usage visualization bar",
                    checked = settings.showMaterialVisualization,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showMaterialVisualization = it)) }
                )
                SettingsToggleRow(
                    label = "Show Waste Percentage",
                    subtitle = "Display leftover percentage metric",
                    checked = settings.showWastePercentage,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showWastePercentage = it)) }
                )
                SettingsToggleRow(
                    label = "Show Used Percentage",
                    subtitle = "Display consumed percentage metric",
                    checked = settings.showUsedPercentage,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showUsedPercentage = it)) }
                )
                SettingsToggleRow(
                    label = "Show Cut Table",
                    subtitle = "Display compact subtotal table",
                    checked = settings.showCutTable,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showCutTable = it)) }
                )
                SettingsToggleRow(
                    label = "Show Formula Explanation",
                    subtitle = "Collapsible 'How it was calculated' card",
                    checked = settings.showFormula,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showFormula = it)) }
                )
                SettingsToggleRow(
                    label = "Show Material Notes",
                    subtitle = "Allow project notes in material info",
                    checked = settings.showNotes,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showNotes = it)) }
                )
            }
        }

        // Feedback & Behavior
        SettingsSectionCard(title = "BEHAVIOR & FEEDBACK") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsToggleRow(
                    label = "Haptic Feedback",
                    subtitle = "Tactile response on save, copy, and delete",
                    checked = settings.hapticFeedback,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
                )
                SettingsToggleRow(
                    label = "Confirm Delete",
                    subtitle = "Show confirmation dialog before deleting history items",
                    checked = settings.confirmDelete,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                )
            }
        }

        // History Settings
        SettingsSectionCard(title = "HISTORY STORAGE") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "History Limit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(50, 100).forEachIndexed { index, limit ->
                        SegmentedButton(
                            selected = settings.historyLimit == limit,
                            onClick = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                viewModel.updateSettings(settings.copy(historyLimit = limit))
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            Text("$limit items")
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = sortDropdownExpanded,
                    onExpandedChange = { sortDropdownExpanded = !sortDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = settings.defaultHistorySort.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default History Sort") },
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
                                    viewModel.updateSettings(settings.copy(defaultHistorySort = option))
                                    sortDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Actions: Reset Settings & Clear History
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showResetSettingsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Settings to Default")
            }

            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear History")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Reset Settings Confirmation Dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all preferences to default values? Your saved history will be kept.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Delete all saved cutting calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
