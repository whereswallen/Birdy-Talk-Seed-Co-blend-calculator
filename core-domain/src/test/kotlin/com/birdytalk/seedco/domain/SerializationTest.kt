package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.data.DefaultBlends
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.model.Ingredient
import com.birdytalk.seedco.domain.persistence.AppBackup
import com.birdytalk.seedco.domain.persistence.BatchHistoryEntry
import com.birdytalk.seedco.domain.persistence.CalcMode
import com.birdytalk.seedco.domain.persistence.InventoryItem
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SerializationTest {

    private val json = Json { prettyPrint = false }

    @Test
    fun `blend with costs and price round-trips exactly`() {
        val blend = Blend(
            id = "yard",
            name = "Yard Party Mix",
            tagline = "test",
            ingredients = listOf(
                Ingredient("Black Oil Sunflower", BigDecimal("54.5"), BigDecimal("0.85")),
                Ingredient("White Millet", BigDecimal("16.84"), BigDecimal("0.42")),
                Ingredient("Striped Sunflower", BigDecimal("14.33")),
                Ingredient("Peanut Pickouts", BigDecimal("14.33"), BigDecimal("1.10")),
            ),
            retailPricePerPound = BigDecimal("2.99"),
        )
        val restored = json.decodeFromString(Blend.serializer(), json.encodeToString(Blend.serializer(), blend))
        assertEquals(blend, restored)
        // Exact BigDecimal scale is preserved.
        assertEquals("54.5", restored.ingredients.first().percent.toPlainString())
        assertEquals("2.99", restored.retailPricePerPound!!.toPlainString())
    }

    @Test
    fun `every default blend round-trips`() {
        DefaultBlends.blends.forEach { blend ->
            val restored = json.decodeFromString(Blend.serializer(), json.encodeToString(Blend.serializer(), blend))
            assertEquals(blend, restored)
        }
    }

    @Test
    fun `full app backup round-trips`() {
        val backup = AppBackup(
            blends = DefaultBlends.blends,
            inventory = listOf(
                InventoryItem("Black Oil Sunflower", BigDecimal("120.5")),
                InventoryItem("Safflower", BigDecimal("40")),
            ),
            history = listOf(
                BatchHistoryEntry(
                    id = "h1",
                    blendId = "cardinal_rule",
                    blendName = "Cardinal Rule",
                    mode = CalcMode.BATCH,
                    inputPounds = BigDecimal("200"),
                    totalPounds = BigDecimal("200"),
                    totalCost = BigDecimal("0"),
                    timestampEpochMillis = 1_700_000_000_000L,
                ),
            ),
        )
        val restored = json.decodeFromString(AppBackup.serializer(), json.encodeToString(AppBackup.serializer(), backup))
        assertEquals(backup, restored)
        assertEquals(AppBackup.CURRENT_VERSION, restored.version)
        assertTrue(restored.inventory.any { it.name == "Black Oil Sunflower" })
    }
}
