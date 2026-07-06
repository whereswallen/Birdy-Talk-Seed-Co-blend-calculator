package com.birdytalk.seedco.domain.calc

import com.birdytalk.seedco.domain.model.Ingredient
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** The dollar cost of one ingredient line within a batch. */
data class IngredientCost(
    val ingredient: Ingredient,
    val weightPounds: BigDecimal,
    val cost: BigDecimal,
)

/**
 * A full cost/pricing breakdown for a computed batch. All money values are in dollars.
 *
 * @property hasCosts True when at least one ingredient has a non-zero cost per pound.
 * @property costPerPound Blended cost of one pound of the finished mix.
 * @property revenue Retail revenue for the batch (`retailPricePerPound × totalPounds`), or `null`.
 * @property profit `revenue − totalCost`, or `null` when no retail price is set.
 * @property marginPercent `profit / revenue × 100`, or `null`.
 */
data class CostSummary(
    val lines: List<IngredientCost>,
    val totalPounds: BigDecimal,
    val totalCost: BigDecimal,
    val costPerPound: BigDecimal,
    val hasCosts: Boolean,
    val retailPricePerPound: BigDecimal?,
    val revenue: BigDecimal?,
    val profit: BigDecimal?,
    val marginPercent: BigDecimal?,
)

/**
 * Pure dollar arithmetic for a batch, kept separate from [BlendCalculator] so weight scaling and
 * money math stay independent. All computation uses [BigDecimal].
 */
object CostCalculator {

    private val MC = MathContext(20, RoundingMode.HALF_UP)
    private val HUNDRED = BigDecimal("100")

    /**
     * Builds a [CostSummary] from computed ingredient [lines] (weights in pounds) and an optional
     * [retailPricePerPound].
     */
    fun summarize(
        lines: List<IngredientWeight>,
        retailPricePerPound: BigDecimal? = null,
    ): CostSummary {
        val costLines = lines.map { line ->
            IngredientCost(
                ingredient = line.ingredient,
                weightPounds = line.weightPounds,
                cost = line.weightPounds.multiply(line.ingredient.costPerPound),
            )
        }
        val totalCost = costLines.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.cost) }
        val totalPounds = lines.fold(BigDecimal.ZERO) { acc, l -> acc.add(l.weightPounds) }
        val costPerPound =
            if (totalPounds.signum() > 0) totalCost.divide(totalPounds, MC) else BigDecimal.ZERO
        val hasCosts = lines.any { it.ingredient.costPerPound.signum() > 0 }

        val revenue = retailPricePerPound?.let { totalPounds.multiply(it) }
        val profit = revenue?.subtract(totalCost)
        val marginPercent =
            if (revenue != null && revenue.signum() > 0) {
                profit!!.divide(revenue, MC).multiply(HUNDRED)
            } else {
                null
            }

        return CostSummary(
            lines = costLines,
            totalPounds = totalPounds,
            totalCost = totalCost,
            costPerPound = costPerPound,
            hasCosts = hasCosts,
            retailPricePerPound = retailPricePerPound,
            revenue = revenue,
            profit = profit,
            marginPercent = marginPercent,
        )
    }
}
