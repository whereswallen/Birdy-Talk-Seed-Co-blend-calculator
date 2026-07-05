package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.calc.BlendCalculator
import com.birdytalk.seedco.domain.data.RecipeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BlendCalculatorTest {

    private fun blend(id: String) = RecipeRepository.blendById(id)!!

    private fun weightOf(lines: List<com.birdytalk.seedco.domain.calc.IngredientWeight>, name: String) =
        lines.first { it.ingredient.name == name }.weightPounds

    /** Two BigDecimals are "equal" here if numerically equal regardless of scale. */
    private fun assertEq(expected: String, actual: BigDecimal) =
        assertEquals(0, BigDecimal(expected).compareTo(actual))

    @Test
    fun `batch scaling of Cardinal Rule to 200 lb yields exact weights`() {
        val result = BlendCalculator.scaleByBatch(blend("cardinal_rule"), BigDecimal("200"))

        assertEq("92", weightOf(result.lines, "Safflower"))
        assertEq("60", weightOf(result.lines, "Black Oil Sunflower"))
        assertEq("20", weightOf(result.lines, "Striped Sunflower"))
        assertEq("20", weightOf(result.lines, "Peanut Pickouts"))
        assertEq("8", weightOf(result.lines, "Dried Mealworms"))

        val sum = result.lines.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.weightPounds) }
        assertEq("200", sum)
    }

    @Test
    fun `anchor dump of Yard Party Mix on White Millet reproduces full 100 lb batch`() {
        val result = BlendCalculator.scaleByAnchor(
            blend("yard_party_mix"),
            anchorName = "White Millet",
            anchorWeightPounds = BigDecimal("16.84"),
        )

        // 16.84 is exactly 16.84% of the batch, so factor == 100 and total == 100.
        assertEq("100", result.factor)
        assertEq("100", result.totalPounds)
        assertEq("54.5", weightOf(result.lines, "Black Oil Sunflower"))
        assertEq("16.84", weightOf(result.lines, "White Millet"))
        assertEq("14.33", weightOf(result.lines, "Striped Sunflower"))
        assertEq("14.33", weightOf(result.lines, "Peanut Pickouts"))
    }

    @Test
    fun `anchor dump reproduces the anchor weight on its own line`() {
        val result = BlendCalculator.scaleByAnchor(
            blend("finch_frenzy"),
            anchorName = "Nyjer Seed",
            anchorWeightPounds = BigDecimal("45"),
        )
        assertEq("45", weightOf(result.lines, "Nyjer Seed"))
        assertEq("5", weightOf(result.lines, "Sunflower Chips (medium)"))
        assertEq("50", result.totalPounds)
    }

    @Test
    fun `non-terminating ratios stay precise`() {
        // Neat Eats is 50/50; anchoring 33.333 lb of chips must double to a 66.666 lb batch.
        val result = BlendCalculator.scaleByAnchor(
            blend("neat_eats"),
            anchorName = "Sunflower Chips (coarse)",
            anchorWeightPounds = BigDecimal("33.333"),
        )
        assertEq("33.333", weightOf(result.lines, "Peanut Pieces"))
        assertEq("66.666", result.totalPounds)
    }

    @Test
    fun `zero or negative batch target is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            BlendCalculator.scaleByBatch(blend("neat_eats"), BigDecimal.ZERO)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BlendCalculator.scaleByBatch(blend("neat_eats"), BigDecimal("-5"))
        }
    }

    @Test
    fun `zero anchor weight is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            BlendCalculator.scaleByAnchor(blend("neat_eats"), "Peanut Pieces", BigDecimal.ZERO)
        }
    }

    @Test
    fun `unknown anchor ingredient is rejected`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            BlendCalculator.scaleByAnchor(blend("neat_eats"), "Cracked Corn", BigDecimal("10"))
        }
        assertTrue(ex.message!!.contains("Cracked Corn"))
    }
}
