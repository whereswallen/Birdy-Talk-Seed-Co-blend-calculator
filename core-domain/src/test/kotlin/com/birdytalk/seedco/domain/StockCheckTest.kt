package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.calc.BlendCalculator
import com.birdytalk.seedco.domain.inventory.StockCheck
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.model.Ingredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class StockCheckTest {

    private val blend = Blend(
        id = "neat",
        name = "Neat Eats",
        tagline = "",
        ingredients = listOf(
            Ingredient("Sunflower Chips (coarse)", BigDecimal("50")),
            Ingredient("Peanut Pieces", BigDecimal("50")),
        ),
    )

    private val batch = BlendCalculator.scaleByBatch(blend, BigDecimal("100")) // 50 lb each

    @Test
    fun `flags only ingredients that exceed on-hand stock`() {
        val onHand = mapOf(
            "Sunflower Chips (coarse)" to BigDecimal("40"), // short by 10
            "Peanut Pieces" to BigDecimal("60"),            // plenty
        )
        val shortfalls = StockCheck.shortfalls(batch.lines, onHand)

        assertEquals(1, shortfalls.size)
        assertEquals("Sunflower Chips (coarse)", shortfalls.first().ingredientName)
        assertEquals(0, BigDecimal("10").compareTo(shortfalls.first().shortPounds))
    }

    @Test
    fun `untracked ingredients are never flagged`() {
        // Only one ingredient tracked; the other is absent from the map.
        val onHand = mapOf("Peanut Pieces" to BigDecimal("10")) // short by 40
        val shortfalls = StockCheck.shortfalls(batch.lines, onHand)

        assertEquals(1, shortfalls.size)
        assertEquals("Peanut Pieces", shortfalls.first().ingredientName)
    }

    @Test
    fun `no shortfalls when everything is in stock`() {
        val onHand = mapOf(
            "Sunflower Chips (coarse)" to BigDecimal("50"),
            "Peanut Pieces" to BigDecimal("50"),
        )
        assertTrue(StockCheck.shortfalls(batch.lines, onHand).isEmpty())
    }
}
