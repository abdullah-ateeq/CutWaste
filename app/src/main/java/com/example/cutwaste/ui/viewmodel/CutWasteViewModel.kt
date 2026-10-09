package com.example.cutwaste.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cutwaste.calculator.CutWasteCalculator
import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.data.PreferencesManager
import com.example.cutwaste.model.AppSettings
import com.example.cutwaste.model.CalculationRecord
import com.example.cutwaste.model.CutItem
import com.example.cutwaste.model.CutWasteInput
import com.example.cutwaste.model.CutWasteResult
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.HistorySortOption
import com.example.cutwaste.model.MaterialType
import com.example.cutwaste.model.MaterialUnit
import com.example.cutwaste.model.QuickPreset
import com.example.cutwaste.model.SerializedCutItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

data class CutRowState(
    val id: String = UUID.randomUUID().toString(),
    val lengthText: String,
    val quantityText: String,
    val lengthError: String? = null,
    val quantityError: String? = null
)

data class CalculatorUiState(
    val materialName: String = "",
    val materialType: MaterialType = MaterialType.OTHER,
    val notes: String = "",
    val unit: MaterialUnit = MaterialUnit.MM,
    val stockLengthText: String = "3000",
    val stockLengthError: String? = null,
    val cuts: List<CutRowState> = listOf(
        CutRowState(lengthText = "1000", quantityText = "2")
    ),
    val kerfEnabled: Boolean = false,
    val kerfText: String = "0",
    val kerfError: String? = null,
    val result: CutWasteResult? = null,
    val formulaLines: List<String> = emptyList(),
    val statusExplanation: String = "",
    val isFormulaExpanded: Boolean = false
)

class CutWasteViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesManager: PreferencesManager = PreferencesManager(application)
) : AndroidViewModel(application) {

    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val rawHistory: StateFlow<List<CalculationRecord>> = preferencesManager.historyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyStatusFilter = MutableStateFlow<CutWasteStatus?>(null)
    val historyStatusFilter: StateFlow<CutWasteStatus?> = _historyStatusFilter.asStateFlow()

    private val _historySortOption = MutableStateFlow(HistorySortOption.RECENTLY_UPDATED)
    val historySortOption: StateFlow<HistorySortOption> = _historySortOption.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Filtered and sorted history
    val filteredHistory: StateFlow<List<CalculationRecord>> = combine(
        rawHistory,
        _historySearchQuery,
        _historyStatusFilter,
        _historySortOption
    ) { records, query, statusFilter, sortOption ->
        records
            .filter { record ->
                if (query.isBlank()) true
                else {
                    record.materialName.contains(query, ignoreCase = true) ||
                            record.materialType.contains(query, ignoreCase = true) ||
                            record.notes.contains(query, ignoreCase = true)
                }
            }
            .filter { record ->
                if (statusFilter == null) true
                else record.status.equals(statusFilter.name, ignoreCase = true)
            }
            .sortedWith(Comparator { a, b ->
                // Favorites always appear first
                if (a.isFavorite && !b.isFavorite) return@Comparator -1
                if (!a.isFavorite && b.isFavorite) return@Comparator 1

                when (sortOption) {
                    HistorySortOption.RECENTLY_UPDATED -> b.timestamp.compareTo(a.timestamp)
                    HistorySortOption.LOWEST_WASTE -> {
                        val aWaste = FormatUtils.parseBigDecimalOrNull(a.wastePercent) ?: BigDecimal.ZERO
                        val bWaste = FormatUtils.parseBigDecimalOrNull(b.wastePercent) ?: BigDecimal.ZERO
                        aWaste.compareTo(bWaste)
                    }
                    HistorySortOption.HIGHEST_WASTE -> {
                        val aWaste = FormatUtils.parseBigDecimalOrNull(a.wastePercent) ?: BigDecimal.ZERO
                        val bWaste = FormatUtils.parseBigDecimalOrNull(b.wastePercent) ?: BigDecimal.ZERO
                        bWaste.compareTo(aWaste)
                    }
                    HistorySortOption.LARGEST_LEFTOVER -> {
                        val aLeft = FormatUtils.parseBigDecimalOrNull(a.remaining) ?: BigDecimal.ZERO
                        val bLeft = FormatUtils.parseBigDecimalOrNull(b.remaining) ?: BigDecimal.ZERO
                        bLeft.compareTo(aLeft)
                    }
                    HistorySortOption.MATERIAL_NAME -> {
                        a.materialName.compareTo(b.materialName, ignoreCase = true)
                    }
                }
            })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Apply default settings when initialized
        viewModelScope.launch {
            val currentSettings = preferencesManager.settingsFlow.stateIn(this).value
            _historySortOption.value = currentSettings.defaultHistorySort
            _calcState.value = _calcState.value.copy(
                unit = currentSettings.defaultUnit,
                stockLengthText = currentSettings.defaultStockLength,
                kerfEnabled = currentSettings.showKerf,
                kerfText = currentSettings.defaultKerf
            )
            recalculate()
        }
    }

    fun onMaterialNameChanged(name: String) {
        if (name.length <= 60) {
            _calcState.value = _calcState.value.copy(materialName = name)
        }
    }

    fun onMaterialTypeChanged(type: MaterialType) {
        _calcState.value = _calcState.value.copy(materialType = type)
    }

    fun onNotesChanged(notes: String) {
        if (notes.length <= 300) {
            _calcState.value = _calcState.value.copy(notes = notes)
        }
    }

    fun onStockLengthChanged(text: String) {
        _calcState.value = _calcState.value.copy(stockLengthText = text)
        recalculate()
    }

    fun onUnitChanged(newUnit: MaterialUnit) {
        val current = _calcState.value
        if (current.unit == newUnit) return

        val oldUnit = current.unit

        // Convert existing stock length
        val oldStock = FormatUtils.parseBigDecimalOrNull(current.stockLengthText)
        val convertedStock = if (oldStock != null && oldStock > BigDecimal.ZERO) {
            val stockMm = oldUnit.toBaseMm(oldStock)
            val converted = newUnit.fromBaseMm(stockMm)
            FormatUtils.formatNumber(converted, 3)
        } else {
            current.stockLengthText
        }

        // Convert kerf
        val oldKerf = FormatUtils.parseBigDecimalOrNull(current.kerfText)
        val convertedKerf = if (oldKerf != null && oldKerf > BigDecimal.ZERO) {
            val kerfMm = oldUnit.toBaseMm(oldKerf)
            val converted = newUnit.fromBaseMm(kerfMm)
            FormatUtils.formatNumber(converted, 3)
        } else {
            current.kerfText
        }

        // Convert cut row lengths
        val convertedCuts = current.cuts.map { cut ->
            val oldLength = FormatUtils.parseBigDecimalOrNull(cut.lengthText)
            val newLengthStr = if (oldLength != null && oldLength > BigDecimal.ZERO) {
                val lenMm = oldUnit.toBaseMm(oldLength)
                val converted = newUnit.fromBaseMm(lenMm)
                FormatUtils.formatNumber(converted, 3)
            } else {
                cut.lengthText
            }
            cut.copy(lengthText = newLengthStr)
        }

        _calcState.value = current.copy(
            unit = newUnit,
            stockLengthText = convertedStock,
            kerfText = convertedKerf,
            cuts = convertedCuts
        )
        recalculate()
    }

    fun onKerfToggleChanged(enabled: Boolean) {
        _calcState.value = _calcState.value.copy(kerfEnabled = enabled)
        recalculate()
    }

    fun onKerfValueChanged(text: String) {
        _calcState.value = _calcState.value.copy(kerfText = text)
        recalculate()
    }

    fun onCutLengthChanged(cutId: String, text: String) {
        val updatedCuts = _calcState.value.cuts.map {
            if (it.id == cutId) it.copy(lengthText = text) else it
        }
        _calcState.value = _calcState.value.copy(cuts = updatedCuts)
        recalculate()
    }

    fun onCutQuantityChanged(cutId: String, text: String) {
        val updatedCuts = _calcState.value.cuts.map {
            if (it.id == cutId) it.copy(quantityText = text) else it
        }
        _calcState.value = _calcState.value.copy(cuts = updatedCuts)
        recalculate()
    }

    fun addCutRow() {
        val currentCuts = _calcState.value.cuts
        if (currentCuts.size >= 50) {
            emitSnackbar("Maximum of 50 cut types.")
            return
        }
        val newCuts = currentCuts + CutRowState(
            lengthText = "",
            quantityText = "1"
        )
        _calcState.value = _calcState.value.copy(cuts = newCuts)
        recalculate()
    }

    fun duplicateCutRow(cutId: String) {
        val currentCuts = _calcState.value.cuts
        if (currentCuts.size >= 50) {
            emitSnackbar("Maximum of 50 cut types.")
            return
        }
        val index = currentCuts.indexOfFirst { it.id == cutId }
        if (index != -1) {
            val target = currentCuts[index]
            val duplicate = target.copy(
                id = UUID.randomUUID().toString()
            )
            val updated = currentCuts.toMutableList().apply {
                add(index + 1, duplicate)
            }
            _calcState.value = _calcState.value.copy(cuts = updated)
            recalculate()
        }
    }

    fun removeCutRow(cutId: String) {
        val currentCuts = _calcState.value.cuts
        if (currentCuts.size <= 1) {
            // "If only one row exists: do not allow the last row to disappear. Instead reset it."
            val resetCuts = listOf(
                CutRowState(
                    id = currentCuts.first().id,
                    lengthText = "",
                    quantityText = "1"
                )
            )
            _calcState.value = _calcState.value.copy(cuts = resetCuts)
        } else {
            val filtered = currentCuts.filterNot { it.id == cutId }
            _calcState.value = _calcState.value.copy(cuts = filtered)
        }
        recalculate()
    }

    fun toggleFormulaExpanded() {
        _calcState.value = _calcState.value.copy(
            isFormulaExpanded = !_calcState.value.isFormulaExpanded
        )
    }

    fun applyPreset(preset: QuickPreset) {
        _calcState.value = _calcState.value.copy(
            materialName = preset.name,
            materialType = preset.materialType,
            unit = preset.unit,
            stockLengthText = preset.stockLength,
            kerfEnabled = preset.kerfEnabled,
            kerfText = preset.kerfValue,
            cuts = preset.cuts.map {
                CutRowState(
                    lengthText = it.length,
                    quantityText = it.quantity.toString()
                )
            }
        )
        recalculate()
        emitSnackbar("Applied preset: ${preset.name}")
    }

    fun resetCalculator() {
        _calcState.value = CalculatorUiState(
            materialName = "",
            materialType = MaterialType.OTHER,
            notes = "",
            unit = settings.value.defaultUnit,
            stockLengthText = "3000",
            cuts = listOf(
                CutRowState(lengthText = "1000", quantityText = "2")
            ),
            kerfEnabled = false,
            kerfText = "0",
            isFormulaExpanded = false
        )
        recalculate()
        emitSnackbar("Calculator reset")
    }

    private fun recalculate() {
        val state = _calcState.value
        val parsedStock = FormatUtils.parseBigDecimalOrNull(state.stockLengthText)

        var stockError: String? = null
        if (parsedStock == null || parsedStock <= BigDecimal.ZERO) {
            stockError = "Stock length must be greater than zero."
        } else if (parsedStock > BigDecimal("999999999")) {
            stockError = "Calculation is outside the supported range."
        }

        var kerfError: String? = null
        val parsedKerf = if (state.kerfEnabled) {
            val k = FormatUtils.parseBigDecimalOrNull(state.kerfText)
            if (k == null || k < BigDecimal.ZERO) {
                kerfError = "Kerf must be greater than or equal to 0."
                BigDecimal.ZERO
            } else {
                k
            }
        } else {
            BigDecimal.ZERO
        }

        var hasCutError = false
        val validatedCuts = state.cuts.map { row ->
            val pLen = FormatUtils.parseBigDecimalOrNull(row.lengthText)
            val pQty = FormatUtils.parseQuantityOrNull(row.quantityText)

            var lErr: String? = null
            if (row.lengthText.isNotBlank() && (pLen == null || pLen <= BigDecimal.ZERO)) {
                lErr = "Cut length must be greater than zero."
                hasCutError = true
            }

            var qErr: String? = null
            if (row.quantityText.isNotBlank() && (pQty == null || pQty < 1L)) {
                qErr = "Quantity must be at least 1."
                hasCutError = true
            }

            row.copy(lengthError = lErr, quantityError = qErr)
        }

        if (stockError != null || parsedStock == null) {
            _calcState.value = state.copy(
                stockLengthError = stockError,
                kerfError = kerfError,
                cuts = validatedCuts,
                result = null,
                formulaLines = emptyList(),
                statusExplanation = ""
            )
            return
        }

        // Build list of valid cuts
        val activeCuts = validatedCuts.mapNotNull { row ->
            val len = FormatUtils.parseBigDecimalOrNull(row.lengthText)
            val qty = FormatUtils.parseQuantityOrNull(row.quantityText)
            if (len != null && len > BigDecimal.ZERO && qty != null && qty > 0L) {
                CutItem(id = row.id, length = len, quantity = qty)
            } else null
        }

        if (activeCuts.isEmpty() || hasCutError) {
            _calcState.value = state.copy(
                stockLengthError = null,
                kerfError = kerfError,
                cuts = validatedCuts,
                result = null,
                formulaLines = emptyList(),
                statusExplanation = ""
            )
            return
        }

        try {
            val input = CutWasteInput(
                materialName = state.materialName,
                notes = state.notes,
                materialType = state.materialType,
                unit = state.unit,
                stockLength = parsedStock,
                cuts = activeCuts,
                kerfEnabled = state.kerfEnabled,
                kerfPerCut = parsedKerf
            )

            val result = CutWasteCalculator.calculate(input)
            val formula = CutWasteCalculator.generateFormulaExplanation(input, result)
            val statusExpl = CutWasteCalculator.getStatusExplanation(result, state.unit)

            _calcState.value = state.copy(
                stockLengthError = null,
                kerfError = kerfError,
                cuts = validatedCuts,
                result = result,
                formulaLines = formula,
                statusExplanation = statusExpl
            )
        } catch (e: Exception) {
            _calcState.value = state.copy(
                stockLengthError = "Calculation is outside the supported range.",
                kerfError = kerfError,
                cuts = validatedCuts,
                result = null,
                formulaLines = emptyList(),
                statusExplanation = ""
            )
        }
    }

    fun saveCurrentCalculation() {
        val state = _calcState.value
        val result = state.result
        val parsedStock = FormatUtils.parseBigDecimalOrNull(state.stockLengthText)

        if (result == null || parsedStock == null) {
            emitSnackbar("Complete calculation inputs before saving.")
            return
        }

        val record = CalculationRecord(
            materialName = if (state.materialName.isNotBlank()) state.materialName.trim() else "Untitled Material",
            materialType = state.materialType.name,
            notes = state.notes.trim(),
            unit = state.unit.name,
            stockLength = FormatUtils.formatNumber(parsedStock),
            cuts = state.cuts.mapNotNull { row ->
                val len = FormatUtils.parseBigDecimalOrNull(row.lengthText)
                val qty = FormatUtils.parseQuantityOrNull(row.quantityText)
                if (len != null && qty != null) {
                    SerializedCutItem(
                        id = row.id,
                        length = FormatUtils.formatNumber(len),
                        quantity = qty
                    )
                } else null
            },
            kerfEnabled = state.kerfEnabled,
            kerfPerCut = state.kerfText,
            totalCuts = result.totalCuts,
            requiredCutLength = FormatUtils.formatNumber(result.requiredCutLength),
            kerfLoss = FormatUtils.formatNumber(result.kerfLoss),
            totalConsumed = FormatUtils.formatNumber(result.totalConsumed),
            remaining = FormatUtils.formatNumber(result.remaining),
            shortage = FormatUtils.formatNumber(result.shortage),
            wastePercent = FormatUtils.formatNumber(result.wastePercent, 2),
            usedPercent = FormatUtils.formatNumber(result.usedPercent, 2),
            status = result.status.name,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val saveResult = preferencesManager.saveCalculation(record)
            if (saveResult.isSuccess) {
                emitSnackbar("Calculation saved to history")
            } else {
                val err = saveResult.exceptionOrNull()?.message ?: "Failed to save calculation"
                emitSnackbar(err)
            }
        }
    }

    fun openRecordInCalculator(record: CalculationRecord) {
        val unit = MaterialUnit.fromString(record.unit)
        val cuts = if (record.cuts.isNotEmpty()) {
            record.cuts.map {
                CutRowState(
                    id = it.id,
                    lengthText = it.length,
                    quantityText = it.quantity.toString()
                )
            }
        } else {
            listOf(CutRowState(lengthText = "1000", quantityText = "1"))
        }

        _calcState.value = _calcState.value.copy(
            materialName = record.materialName,
            materialType = MaterialType.fromString(record.materialType),
            notes = record.notes,
            unit = unit,
            stockLengthText = record.stockLength,
            kerfEnabled = record.kerfEnabled,
            kerfText = record.kerfPerCut,
            cuts = cuts
        )
        recalculate()
        emitSnackbar("Restored \"${record.materialName}\" to calculator")
    }

    fun toggleHistoryFavorite(recordId: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(recordId)
        }
    }

    fun duplicateHistoryRecord(record: CalculationRecord) {
        viewModelScope.launch {
            val res = preferencesManager.duplicateRecord(record)
            if (res.isSuccess) {
                emitSnackbar("Duplicated \"${record.materialName}\"")
            } else {
                emitSnackbar(res.exceptionOrNull()?.message ?: "Failed to duplicate")
            }
        }
    }

    fun deleteHistoryRecord(recordId: String) {
        viewModelScope.launch {
            preferencesManager.deleteRecord(recordId)
            emitSnackbar("Calculation removed from history")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
            emitSnackbar("All history cleared")
        }
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryStatusFilter(filter: CutWasteStatus?) {
        _historyStatusFilter.value = filter
    }

    fun setHistorySortOption(sort: HistorySortOption) {
        _historySortOption.value = sort
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            preferencesManager.updateSettings(newSettings)
            emitSnackbar("Settings saved")
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferencesManager.resetSettings()
            emitSnackbar("Settings reset to defaults")
        }
    }

    fun emitSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }
}
