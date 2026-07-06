package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.calc.BlendCalculator
import com.birdytalk.seedco.domain.calc.CostCalculator
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.model.Ingredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CostCalculatorTest {

    private fun assertEq(expected: String, actual: BigDecimal?) =
        assertEquals(0, BigDecimal(expected).compareTo(actual))

    private val pricedBlend = Blend(
        id = "neat",
        name = "Neat Eats",
        tagline = "",
        ingredients = listOf(
            Ingredient("Sunflower Chips (coarse)", BigDecimal("50"), BigDecimal("1.00")),
            Ingredient("Peanut Pieces", BigDecimal("50"), BigDecimal("2.00")),
        ),
        retailPricePerPound = BigDecimal("3.00"),
    )

    @Test
    fun `computes per-line cost, batch total, and cost per pound`() {
        val batch = BlendCalculator.scaleByBatch(pricedBlend, BigDecimal("100"))
        val summary = CostCalculator.summarize(batch.lines, pricedBlend.retailPricePerPound)

        assertTrue(summary.hasCosts)
        assertEq("50", summary.lines.first { it.ingredient.name.startsWith("Sunflower") }.cost) // 50 lb * $1
        assertEq("100", summary.lines.first { it.ingredient.name.startsWith("Peanut") }.cost)   // 50 lb * $2
        assertEq("150", summary.totalCost)
        assertEq("1.5", summary.costPerPound)
    }

    @Test
    fun `computes revenue, profit, and margin from retail price`() {
        val batch = BlendCalculator.scaleByBatch(pricedBlend, BigDecimal("100"))
        val summary = CostCalculator.summarize(batch.lines, pricedBlend.retailPricePerPound)

        assertEq("300", summary.revenue)      // 100 lb * $3
        assertEq("150", summary.profit)       // 300 - 150
        assertEq("50", summary.marginPercent) // 150 / 300 * 100
    }

    @Test
    fun `no retail price yields null pricing fields`() {
        val batch = BlendCalculator.scaleByBatch(pricedBlend, BigDecimal("100"))
        val summary = CostCalculator.summarize(batch.lines, retailPricePerPound = null)

        assertNull(summary.revenue)
        assertNull(summary.profit)
        assertNull(summary.marginPercent)
        assertEq("150", summary.totalCost)
    }

    @Test
    fun `blend without costs reports hasCosts false and zero total`() {
        val freeBlend = Blend(
            id = "free",
            name = "Free",
            tagline = "",
            ingredients = listOf(
                Ingredient("A", BigDecimal("50")),
                Ingredient("B", BigDecimal("50")),
            ),
        )
        val batch = BlendCalculator.scaleByBatch(freeBlend, BigDecimal("80"))
        val summary = CostCalculator.summarize(batch.lines, null)

        assertFalse(summary.hasCosts)
        assertEq("0", summary.totalCost)
    }
}
