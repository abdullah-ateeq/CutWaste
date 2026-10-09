package com.example.cutwaste.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.model.CutWasteInput
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.MaterialType
import com.example.cutwaste.model.MaterialUnit
import com.example.cutwaste.model.QuickPresets
import com.example.cutwaste.ui.components.CompactCutTableCard
import com.example.cutwaste.ui.components.CutRowItem
import com.example.cutwaste.ui.components.FormulaCard
import com.example.cutwaste.ui.components.MaterialBarVisualization
import com.example.cutwaste.ui.components.MaterialUsageCard
import com.example.cutwaste.ui.components.PrimaryResultCard
import com.example.cutwaste.ui.components.WasteUsageCard
import com.example.cutwaste.ui.viewmodel.CutWasteViewModel
import com.example.cutwaste.util.HapticUtil
import com.example.cutwaste.util.ShareUtil
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CutWasteViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.calcState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current

    var materialDetailsExpanded by remember { mutableStateOf(false) }
    var materialTypeDropdownExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp

        if (isWide) {
            // Two column landscape / tablet layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Inputs
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorHeader()

                    PresetsSection(onPresetSelected = { preset ->
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.applyPreset(preset)
                    })

                    MaterialDetailsCard(
                        materialName = state.materialName,
                        materialType = state.materialType,
                        notes = state.notes,
                        showNotes = settings.showNotes,
                        isExpanded = materialDetailsExpanded,
                        onToggleExpand = { materialDetailsExpanded = !materialDetailsExpanded },
                        onNameChanged = viewModel::onMaterialNameChanged,
                        onTypeChanged = viewModel::onMaterialTypeChanged,
                        onNotesChanged = viewModel::onNotesChanged
                    )

                    StockMaterialCard(
                        stockLengthText = state.stockLengthText,
                        stockError = state.stockLengthError,
                        unit = state.unit,
                        onStockLengthChanged = viewModel::onStockLengthChanged,
                        onUnitChanged = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.onUnitChanged(it)
                        }
                    )

                    RequiredCutsCard(
                        cuts = state.cuts,
                        unit = state.unit,
                        onAddCut = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.addCutRow()
                        },
                        onDuplicateCut = { id ->
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.duplicateCutRow(id)
                        },
                        onDeleteCut = { id ->
                            HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                            viewModel.removeCutRow(id)
                        },
                        onCutLengthChanged = viewModel::onCutLengthChanged,
                        onCutQuantityChanged = viewModel::onCutQuantityChanged
                    )

                    KerfCard(
                        kerfEnabled = state.kerfEnabled,
                        kerfText = state.kerfText,
                        kerfError = state.kerfError,
                        unit = state.unit,
                        onToggle = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.onKerfToggleChanged(it)
                        },
                        onValueChanged = viewModel::onKerfValueChanged
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Right Column: Results & Actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val parsedStock = FormatUtils.parseBigDecimalOrNull(state.stockLengthText)
                    val result = state.result

                    if (result != null && parsedStock != null) {
                        PrimaryResultCard(
                            stockLength = parsedStock,
                            result = result,
                            unit = state.unit,
                            statusExplanation = state.statusExplanation
                        )

                        if (settings.showMaterialVisualization) {
                            MaterialBarVisualization(
                                stockLength = parsedStock,
                                result = result,
                                unit = state.unit
                            )
                        }

                        if (settings.showWastePercentage || settings.showUsedPercentage) {
                            WasteUsageCard(result = result)
                        }

                        MaterialUsageCard(
                            stockLength = parsedStock,
                            result = result,
                            unit = state.unit
                        )

                        if (settings.showCutTable) {
                            CompactCutTableCard(
                                cuts = state.cuts,
                                unit = state.unit
                            )
                        }

                        if (settings.showFormula) {
                            FormulaCard(
                                isExpanded = state.isFormulaExpanded,
                                formulaLines = state.formulaLines,
                                onToggleExpand = viewModel::toggleFormulaExpanded
                            )
                        }

                        ActionButtonsCard(
                            onSave = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                viewModel.saveCurrentCalculation()
                            },
                            onCopyResult = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                val text = ShareUtil.formatResultText(
                                    input = CutWasteInput(
                                        materialName = state.materialName,
                                        notes = state.notes,
                                        materialType = state.materialType,
                                        unit = state.unit,
                                        stockLength = parsedStock,
                                        cuts = emptyList(),
                                        kerfEnabled = state.kerfEnabled,
                                        kerfPerCut = FormatUtils.parseBigDecimalOrNull(state.kerfText) ?: BigDecimal.ZERO
                                    ),
                                    result = result
                                )
                                ShareUtil.copyToClipboard(context, text)
                                viewModel.emitSnackbar("Result copied")
                            },
                            onCopyRemaining = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                val remText = if (result.status == CutWasteStatus.NOT_ENOUGH_MATERIAL) {
                                    "Short by ${FormatUtils.formatMeasurement(result.shortage, state.unit)}"
                                } else {
                                    FormatUtils.formatMeasurement(result.remaining, state.unit)
                                }
                                ShareUtil.copyToClipboard(context, remText)
                                viewModel.emitSnackbar("Remaining material copied")
                            },
                            onShare = {
                                HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                                val text = ShareUtil.formatResultText(
                                    input = CutWasteInput(
                                        materialName = state.materialName,
                                        notes = state.notes,
                                        materialType = state.materialType,
                                        unit = state.unit,
                                        stockLength = parsedStock,
                                        cuts = emptyList(),
                                        kerfEnabled = state.kerfEnabled,
                                        kerfPerCut = FormatUtils.parseBigDecimalOrNull(state.kerfText) ?: BigDecimal.ZERO
                                    ),
                                    result = result
                                )
                                ShareUtil.shareText(context, text)
                            },
                            onReset = {
                                HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                                viewModel.resetCalculator()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Single column portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CalculatorHeader()

                PresetsSection(onPresetSelected = { preset ->
                    HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                    viewModel.applyPreset(preset)
                })

                MaterialDetailsCard(
                    materialName = state.materialName,
                    materialType = state.materialType,
                    notes = state.notes,
                    showNotes = settings.showNotes,
                    isExpanded = materialDetailsExpanded,
                    onToggleExpand = { materialDetailsExpanded = !materialDetailsExpanded },
                    onNameChanged = viewModel::onMaterialNameChanged,
                    onTypeChanged = viewModel::onMaterialTypeChanged,
                    onNotesChanged = viewModel::onNotesChanged
                )

                StockMaterialCard(
                    stockLengthText = state.stockLengthText,
                    stockError = state.stockLengthError,
                    unit = state.unit,
                    onStockLengthChanged = viewModel::onStockLengthChanged,
                    onUnitChanged = {
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.onUnitChanged(it)
                    }
                )

                RequiredCutsCard(
                    cuts = state.cuts,
                    unit = state.unit,
                    onAddCut = {
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.addCutRow()
                    },
                    onDuplicateCut = { id ->
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.duplicateCutRow(id)
                    },
                    onDeleteCut = { id ->
                        HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                        viewModel.removeCutRow(id)
                    },
                    onCutLengthChanged = viewModel::onCutLengthChanged,
                    onCutQuantityChanged = viewModel::onCutQuantityChanged
                )

                KerfCard(
                    kerfEnabled = state.kerfEnabled,
                    kerfText = state.kerfText,
                    kerfError = state.kerfError,
                    unit = state.unit,
                    onToggle = {
                        HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                        viewModel.onKerfToggleChanged(it)
                    },
                    onValueChanged = viewModel::onKerfValueChanged
                )

                val parsedStock = FormatUtils.parseBigDecimalOrNull(state.stockLengthText)
                val result = state.result

                if (result != null && parsedStock != null) {
                    PrimaryResultCard(
                        stockLength = parsedStock,
                        result = result,
                        unit = state.unit,
                        statusExplanation = state.statusExplanation
                    )

                    if (settings.showMaterialVisualization) {
                        MaterialBarVisualization(
                            stockLength = parsedStock,
                            result = result,
                            unit = state.unit
                        )
                    }

                    if (settings.showWastePercentage || settings.showUsedPercentage) {
                        WasteUsageCard(result = result)
                    }

                    MaterialUsageCard(
                        stockLength = parsedStock,
                        result = result,
                        unit = state.unit
                    )

                    if (settings.showCutTable) {
                        CompactCutTableCard(
                            cuts = state.cuts,
                            unit = state.unit
                        )
                    }

                    if (settings.showFormula) {
                        FormulaCard(
                            isExpanded = state.isFormulaExpanded,
                            formulaLines = state.formulaLines,
                            onToggleExpand = viewModel::toggleFormulaExpanded
                        )
                    }

                    ActionButtonsCard(
                        onSave = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            viewModel.saveCurrentCalculation()
                        },
                        onCopyResult = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            val text = ShareUtil.formatResultText(
                                input = CutWasteInput(
                                    materialName = state.materialName,
                                    notes = state.notes,
                                    materialType = state.materialType,
                                    unit = state.unit,
                                    stockLength = parsedStock,
                                    cuts = emptyList(),
                                    kerfEnabled = state.kerfEnabled,
                                    kerfPerCut = FormatUtils.parseBigDecimalOrNull(state.kerfText) ?: BigDecimal.ZERO
                                ),
                                result = result
                            )
                            ShareUtil.copyToClipboard(context, text)
                            viewModel.emitSnackbar("Result copied")
                        },
                        onCopyRemaining = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            val remText = if (result.status == CutWasteStatus.NOT_ENOUGH_MATERIAL) {
                                "Short by ${FormatUtils.formatMeasurement(result.shortage, state.unit)}"
                            } else {
                                FormatUtils.formatMeasurement(result.remaining, state.unit)
                            }
                            ShareUtil.copyToClipboard(context, remText)
                            viewModel.emitSnackbar("Remaining material copied")
                        },
                        onShare = {
                            HapticUtil.performLightHaptic(view, context, settings.hapticFeedback)
                            val text = ShareUtil.formatResultText(
                                input = CutWasteInput(
                                    materialName = state.materialName,
                                    notes = state.notes,
                                    materialType = state.materialType,
                                    unit = state.unit,
                                    stockLength = parsedStock,
                                    cuts = emptyList(),
                                    kerfEnabled = state.kerfEnabled,
                                    kerfPerCut = FormatUtils.parseBigDecimalOrNull(state.kerfText) ?: BigDecimal.ZERO
                                ),
                                result = result
                            )
                            ShareUtil.shareText(context, text)
                        },
                        onReset = {
                            HapticUtil.performMediumHaptic(view, context, settings.hapticFeedback)
                            viewModel.resetCalculator()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CalculatorHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "CUTWASTE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Calculate material remaining after cuts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetsSection(onPresetSelected: (com.example.cutwaste.model.QuickPreset) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        QuickPresets.all.forEach { preset ->
            FilterChip(
                selected = false,
                onClick = { onPresetSelected(preset) },
                label = { Text(preset.name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialDetailsCard(
    materialName: String,
    materialType: MaterialType,
    notes: String,
    showNotes: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTypeChanged: (MaterialType) -> Unit,
    onNotesChanged: (String) -> Unit
) {
    var typeDropdownOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MATERIAL INFO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (materialName.isNotBlank()) "$materialName (${materialType.displayName})" else "Optional details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                OutlinedTextField(
                    value = materialName,
                    onValueChange = onNameChanged,
                    label = { Text("Material Name") },
                    placeholder = { Text("e.g. Pine Board, Aluminum Bar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeDropdownOpen,
                    onExpandedChange = { typeDropdownOpen = !typeDropdownOpen }
                ) {
                    OutlinedTextField(
                        value = materialType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Material Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownOpen) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = typeDropdownOpen,
                        onDismissRequest = { typeDropdownOpen = false }
                    ) {
                        MaterialType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    onTypeChanged(type)
                                    typeDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                if (showNotes) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChanged,
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("Project notes, blade specs, etc.") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StockMaterialCard(
    stockLengthText: String,
    stockError: String?,
    unit: MaterialUnit,
    onStockLengthChanged: (String) -> Unit,
    onUnitChanged: (MaterialUnit) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "STOCK MATERIAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = stockLengthText,
                onValueChange = onStockLengthChanged,
                label = { Text("Stock Length") },
                isError = stockError != null,
                supportingText = stockError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Stock length $stockLengthText ${unit.displayName}"
                    }
            )

            // Unit Selection Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MaterialUnit.entries.forEach { u ->
                    FilterChip(
                        selected = unit == u,
                        onClick = { onUnitChanged(u) },
                        label = { Text(u.symbol) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequiredCutsCard(
    cuts: List<com.example.cutwaste.ui.viewmodel.CutRowState>,
    unit: MaterialUnit,
    onAddCut: () -> Unit,
    onDuplicateCut: (String) -> Unit,
    onDeleteCut: (String) -> Unit,
    onCutLengthChanged: (String, String) -> Unit,
    onCutQuantityChanged: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REQUIRED CUTS (${cuts.size}/50)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            cuts.forEachIndexed { index, cutRow ->
                CutRowItem(
                    index = index + 1,
                    cutRow = cutRow,
                    unit = unit,
                    isOnlyRow = cuts.size == 1,
                    onLengthChanged = { onCutLengthChanged(cutRow.id, it) },
                    onQuantityChanged = { onCutQuantityChanged(cutRow.id, it) },
                    onDuplicate = { onDuplicateCut(cutRow.id) },
                    onDelete = { onDeleteCut(cutRow.id) }
                )
            }

            FilledTonalButton(
                onClick = onAddCut,
                enabled = cuts.size < 50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (cuts.size >= 50) "Maximum 50 cuts reached" else "+ ADD CUT")
            }
        }
    }
}

@Composable
private fun KerfCard(
    kerfEnabled: Boolean,
    kerfText: String,
    kerfError: String?,
    unit: MaterialUnit,
    onToggle: (Boolean) -> Unit,
    onValueChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUTTING KERF",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Blade thickness loss between cuts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = kerfEnabled,
                    onCheckedChange = onToggle
                )
            }

            if (kerfEnabled) {
                OutlinedTextField(
                    value = kerfText,
                    onValueChange = onValueChanged,
                    label = { Text("Kerf per Cut (${unit.symbol})") },
                    placeholder = { Text("e.g. 3") },
                    isError = kerfError != null,
                    supportingText = kerfError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Cutting kerf $kerfText ${unit.displayName}"
                        }
                )

                Text(
                    text = "Kerf is the material removed by each cut. The final cut does not add another kerf gap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsCard(
    onSave: () -> Unit,
    onCopyResult: () -> Unit,
    onCopyRemaining: () -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE")
                }

                FilledTonalButton(
                    onClick = onCopyResult,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COPY")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyRemaining,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("COPY REMAINING")
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SHARE")
                }
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESET")
            }
        }
    }
}
