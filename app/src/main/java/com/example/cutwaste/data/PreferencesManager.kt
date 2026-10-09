package com.example.cutwaste.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cutwaste.model.AppSettings
import com.example.cutwaste.model.AppTheme
import com.example.cutwaste.model.CalculationRecord
import com.example.cutwaste.model.HistorySortOption
import com.example.cutwaste.model.MaterialUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cutwaste_preferences")

class PreferencesManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val DEFAULT_STOCK_LENGTH = stringPreferencesKey("default_stock_length")
        val DEFAULT_KERF = stringPreferencesKey("default_kerf")
        val SHOW_KERF = booleanPreferencesKey("show_kerf")
        val SHOW_WASTE_PERCENTAGE = booleanPreferencesKey("show_waste_percentage")
        val SHOW_USED_PERCENTAGE = booleanPreferencesKey("show_used_percentage")
        val SHOW_MATERIAL_VISUALIZATION = booleanPreferencesKey("show_material_visualization")
        val SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val SHOW_CUT_TABLE = booleanPreferencesKey("show_cut_table")
        val SHOW_NOTES = booleanPreferencesKey("show_notes")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val HISTORY_LIMIT = intPreferencesKey("history_limit")
        val DEFAULT_HISTORY_SORT = stringPreferencesKey("default_history_sort")

        val HISTORY_JSON = stringPreferencesKey("history_records_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = try {
                AppTheme.valueOf(prefs[Keys.THEME] ?: AppTheme.SYSTEM.name)
            } catch (e: Exception) {
                AppTheme.SYSTEM
            },
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            defaultUnit = MaterialUnit.fromString(prefs[Keys.DEFAULT_UNIT]),
            defaultStockLength = prefs[Keys.DEFAULT_STOCK_LENGTH] ?: "3000",
            defaultKerf = prefs[Keys.DEFAULT_KERF] ?: "0",
            showKerf = prefs[Keys.SHOW_KERF] ?: false,
            showWastePercentage = prefs[Keys.SHOW_WASTE_PERCENTAGE] ?: true,
            showUsedPercentage = prefs[Keys.SHOW_USED_PERCENTAGE] ?: true,
            showMaterialVisualization = prefs[Keys.SHOW_MATERIAL_VISUALIZATION] ?: true,
            showFormula = prefs[Keys.SHOW_FORMULA] ?: true,
            showCutTable = prefs[Keys.SHOW_CUT_TABLE] ?: true,
            showNotes = prefs[Keys.SHOW_NOTES] ?: true,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            confirmDelete = prefs[Keys.CONFIRM_DELETE] ?: true,
            historyLimit = prefs[Keys.HISTORY_LIMIT] ?: 100,
            defaultHistorySort = try {
                HistorySortOption.valueOf(prefs[Keys.DEFAULT_HISTORY_SORT] ?: HistorySortOption.RECENTLY_UPDATED.name)
            } catch (e: Exception) {
                HistorySortOption.RECENTLY_UPDATED
            }
        )
    }

    val historyFlow: Flow<List<CalculationRecord>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[Keys.HISTORY_JSON] ?: return@map emptyList()
        try {
            json.decodeFromString<List<CalculationRecord>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = settings.theme.name
            prefs[Keys.DYNAMIC_COLOR] = settings.dynamicColor
            prefs[Keys.DEFAULT_UNIT] = settings.defaultUnit.name
            prefs[Keys.DEFAULT_STOCK_LENGTH] = settings.defaultStockLength
            prefs[Keys.DEFAULT_KERF] = settings.defaultKerf
            prefs[Keys.SHOW_KERF] = settings.showKerf
            prefs[Keys.SHOW_WASTE_PERCENTAGE] = settings.showWastePercentage
            prefs[Keys.SHOW_USED_PERCENTAGE] = settings.showUsedPercentage
            prefs[Keys.SHOW_MATERIAL_VISUALIZATION] = settings.showMaterialVisualization
            prefs[Keys.SHOW_FORMULA] = settings.showFormula
            prefs[Keys.SHOW_CUT_TABLE] = settings.showCutTable
            prefs[Keys.SHOW_NOTES] = settings.showNotes
            prefs[Keys.HAPTIC_FEEDBACK] = settings.hapticFeedback
            prefs[Keys.CONFIRM_DELETE] = settings.confirmDelete
            prefs[Keys.HISTORY_LIMIT] = settings.historyLimit
            prefs[Keys.DEFAULT_HISTORY_SORT] = settings.defaultHistorySort.name
        }
    }

    suspend fun resetSettings() {
        updateSettings(AppSettings())
    }

    /**
     * Saves a calculation record.
     * Enforces the history limit (e.g. 50 or 100).
     * If limit reached:
     * - Removes the oldest non-favorite record.
     * - If all existing records are favorites: returns failure with message.
     */
    suspend fun saveCalculation(record: CalculationRecord): Result<Unit> {
        val currentSettings = settingsFlow.first()
        val limit = currentSettings.historyLimit
        val currentList = historyFlow.first().toMutableList()

        if (currentList.size >= limit) {
            // Find oldest non-favorite
            val oldestNonFavoriteIndex = currentList
                .withIndex()
                .filter { !it.value.isFavorite }
                .minByOrNull { it.value.timestamp }
                ?.index

            if (oldestNonFavoriteIndex != null) {
                currentList.removeAt(oldestNonFavoriteIndex)
            } else {
                return Result.failure(
                    IllegalStateException("History limit reached. Remove an item before saving.")
                )
            }
        }

        // Add newest record at beginning
        currentList.add(0, record)

        saveHistoryList(currentList)
        return Result.success(Unit)
    }

    suspend fun toggleFavorite(recordId: String) {
        val currentList = historyFlow.first().toMutableList()
        val index = currentList.indexOfFirst { it.id == recordId }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(isFavorite = !item.isFavorite)
            saveHistoryList(currentList)
        }
    }

    suspend fun duplicateRecord(record: CalculationRecord): Result<Unit> {
        val duplicate = record.copy(
            id = UUID.randomUUID().toString(),
            materialName = if (record.materialName.isBlank() || record.materialName == "Untitled Material") {
                "Copy of Untitled Material"
            } else {
                "Copy of ${record.materialName}"
            },
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
        return saveCalculation(duplicate)
    }

    suspend fun deleteRecord(recordId: String) {
        val currentList = historyFlow.first().toMutableList()
        currentList.removeAll { it.id == recordId }
        saveHistoryList(currentList)
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.HISTORY_JSON)
        }
    }

    private suspend fun saveHistoryList(list: List<CalculationRecord>) {
        val encoded = json.encodeToString(list)
        context.dataStore.edit { prefs ->
            prefs[Keys.HISTORY_JSON] = encoded
        }
    }
}
