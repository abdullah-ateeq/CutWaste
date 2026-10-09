package com.example.cutwaste

import com.example.cutwaste.calculator.FormatUtils
import com.example.cutwaste.model.CalculationRecord
import com.example.cutwaste.model.CutWasteStatus
import com.example.cutwaste.model.MaterialType
import com.example.cutwaste.model.MaterialUnit
import com.example.cutwaste.model.SerializedCutItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class FormatAndSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testFormatNumber() {
        assertEquals("2500", FormatUtils.formatNumber(BigDecimal("2500.00")))
        assertEquals("2500.5", FormatUtils.formatNumber(BigDecimal("2500.50")))
        assertEquals("16.67", FormatUtils.formatNumber(BigDecimal("16.666666")))
        assertEquals("0", FormatUtils.formatNumber(BigDecimal("0.000")))
    }

    @Test
    fun testFormatMeasurement() {
        assertEquals("2500 mm", FormatUtils.formatMeasurement(BigDecimal("2500"), MaterialUnit.MM))
        assertEquals("250 cm", FormatUtils.formatMeasurement(BigDecimal("250.0"), MaterialUnit.CM))
        assertEquals("2.5 m", FormatUtils.formatMeasurement(BigDecimal("2.5"), MaterialUnit.M))
        assertEquals("100 in", FormatUtils.formatMeasurement(BigDecimal("100"), MaterialUnit.IN))
        assertEquals("10 ft", FormatUtils.formatMeasurement(BigDecimal("10"), MaterialUnit.FT))
    }

    @Test
    fun testParseBigDecimalSafe() {
        assertEquals(BigDecimal("123.45"), FormatUtils.parseBigDecimalOrNull("123.45"))
        // Comma replacement for European keyboards
        assertEquals(BigDecimal("123.45"), FormatUtils.parseBigDecimalOrNull("123,45"))
        assertNull(FormatUtils.parseBigDecimalOrNull(""))
        assertNull(FormatUtils.parseBigDecimalOrNull("abc"))
        assertNull(FormatUtils.parseBigDecimalOrNull("   "))
    }

    @Test
    fun testParseQuantitySafe() {
        assertEquals(5L, FormatUtils.parseQuantityOrNull("5"))
        assertEquals(1L, FormatUtils.parseQuantityOrNull("  1  "))
        assertNull(FormatUtils.parseQuantityOrNull("0"))
        assertNull(FormatUtils.parseQuantityOrNull("-2"))
        assertNull(FormatUtils.parseQuantityOrNull("1.5"))
        assertNull(FormatUtils.parseQuantityOrNull("abc"))
    }

    @Test
    fun testCalculationRecordSerializationAndDeserialization() {
        val record = CalculationRecord(
            id = "test-id-123",
            materialName = "Pine Board",
            materialType = MaterialType.WOOD.name,
            notes = "Cabinet project",
            unit = MaterialUnit.MM.name,
            stockLength = "3000",
            cuts = listOf(
                SerializedCutItem("c1", "1000", 2),
                SerializedCutItem("c2", "500", 1)
            ),
            kerfEnabled = true,
            kerfPerCut = "3",
            totalCuts = 3,
            requiredCutLength = "2500",
            kerfLoss = "6",
            totalConsumed = "2506",
            remaining = "494",
            shortage = "0",
            wastePercent = "16.47",
            usedPercent = "83.53",
            status = CutWasteStatus.LEFTOVER.name,
            timestamp = 1700000000000L,
            isFavorite = true
        )

        val serialized = json.encodeToString(record)
        val deserialized = json.decodeFromString<CalculationRecord>(serialized)

        assertEquals(record.id, deserialized.id)
        assertEquals(record.materialName, deserialized.materialName)
        assertEquals(record.materialType, deserialized.materialType)
        assertEquals(record.stockLength, deserialized.stockLength)
        assertEquals(2, deserialized.cuts.size)
        assertEquals("1000", deserialized.cuts[0].length)
        assertEquals(2L, deserialized.cuts[0].quantity)
        assertEquals("494", deserialized.remaining)
        assertEquals("16.47", deserialized.wastePercent)
        assertTrue(deserialized.isFavorite)
    }

    @Test
    fun testEvictionRuleWhenHistoryLimitReached() {
        val limit = 5
        val list = mutableListOf<CalculationRecord>()

        // Add 5 records with timestamps
        for (i in 1..5) {
            list.add(
                CalculationRecord(
                    id = "id-$i",
                    materialName = "Item $i",
                    materialType = MaterialType.OTHER.name,
                    unit = MaterialUnit.MM.name,
                    stockLength = "3000",
                    cuts = emptyList(),
                    kerfEnabled = false,
                    kerfPerCut = "0",
                    totalCuts = 1,
                    requiredCutLength = "1000",
                    kerfLoss = "0",
                    totalConsumed = "1000",
                    remaining = "2000",
                    shortage = "0",
                    wastePercent = "66.67",
                    usedPercent = "33.33",
                    status = CutWasteStatus.LEFTOVER.name,
                    timestamp = i * 1000L,
                    isFavorite = (i == 1) // First item is favorite
                )
            )
        }

        assertEquals(5, list.size)

        // When item #6 arrives and limit is 5:
        // Item 1 is favorite, so oldest non-favorite is Item 2 (timestamp 2000)
        val oldestNonFavorite = list
            .withIndex()
            .filter { !it.value.isFavorite }
            .minByOrNull { it.value.timestamp }

        assertNotNull(oldestNonFavorite)
        assertEquals("id-2", oldestNonFavorite!!.value.id)

        // Evict oldest non-favorite and insert new
        list.removeAt(oldestNonFavorite.index)
        val newRecord = CalculationRecord(
            id = "id-6",
            materialName = "Item 6",
            materialType = MaterialType.OTHER.name,
            unit = MaterialUnit.MM.name,
            stockLength = "3000",
            cuts = emptyList(),
            kerfEnabled = false,
            kerfPerCut = "0",
            totalCuts = 1,
            requiredCutLength = "1000",
            kerfLoss = "0",
            totalConsumed = "1000",
            remaining = "2000",
            shortage = "0",
            wastePercent = "66.67",
            usedPercent = "33.33",
            status = CutWasteStatus.LEFTOVER.name,
            timestamp = 6000L,
            isFavorite = false
        )
        list.add(0, newRecord)

        assertEquals(5, list.size)
        assertTrue(list.any { it.id == "id-1" }) // Favorite was kept!
        assertFalse(list.any { it.id == "id-2" }) // Item 2 was removed
        assertTrue(list.any { it.id == "id-6" }) // New item was added
    }

    @Test
    fun testEvictionFailsWhenAllRecordsAreFavorites() {
        val list = mutableListOf<CalculationRecord>()
        for (i in 1..5) {
            list.add(
                CalculationRecord(
                    id = "id-$i",
                    materialName = "Item $i",
                    materialType = MaterialType.OTHER.name,
                    unit = MaterialUnit.MM.name,
                    stockLength = "3000",
                    cuts = emptyList(),
                    kerfEnabled = false,
                    kerfPerCut = "0",
                    totalCuts = 1,
                    requiredCutLength = "1000",
                    kerfLoss = "0",
                    totalConsumed = "1000",
                    remaining = "2000",
                    shortage = "0",
                    wastePercent = "66.67",
                    usedPercent = "33.33",
                    status = CutWasteStatus.LEFTOVER.name,
                    timestamp = i * 1000L,
                    isFavorite = true // All are favorites!
                )
            )
        }

        val oldestNonFavorite = list
            .withIndex()
            .filter { !it.value.isFavorite }
            .minByOrNull { it.value.timestamp }

        assertNull(oldestNonFavorite)
        // Verified: cannot silently evict when all are favorites
    }
}
