package com.birdytalk.seedco.domain.calc

import com.birdytalk.seedco.domain.model.Blend
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Pure, side-effect-free blend arithmetic. Every computation uses [BigDecimal] so weights on
 * the production floor never drift due to binary floating-point error.
 *
 * All results are returned in the canonical unit — **pounds** — at full working precision;
 * rounding to a display unit is the presentation layer's responsibility.
 */
object BlendCalculator {

    private val HUNDRED = BigDecimal("100")

    /**
     * Working precision for divisions (which may be non-terminating, e.g. 1/3). 20 significant
     * digits comfortably exceeds any realistic production weight while keeping results exact for
     * the terminating cases the recipes actually produce.
     */
    private val MC = MathContext(20, RoundingMode.HALF_UP)

    /**
     * Scales an entire [blend] to a [targetTotalPounds] batch.
     *
     * Each ingredient weight = `target × percent / 100`.
     *
     * @throws IllegalArgumentException if the target is not strictly positive.
     */
    fun scaleByBatch(blend: Blend, targetTotalPounds: BigDecimal): BatchResult {
        require(targetTotalPounds.signum() > 0) {
            "Target batch size must be greater than zero."
        }
        val lines = blend.ingredients.map { ingredient ->
            val weight = targetTotalPounds.multiply(ingredient.percent).divide(HUNDRED, MC)
            IngredientWeight(ingredient, weight)
        }
        return BatchResult(blend.id, targetTotalPounds, lines)
    }

    /**
     * Scales a [blend] from the dumped weight of one "anchor" ingredient.
     *
     * `factor = anchorWeight / (anchorPercent / 100)`, then every ingredient weight =
     * `factor × percent / 100`. The total batch weight is the sum of all ingredient lines.
     *
     * @param anchorName must match one of the blend's ingredient names exactly.
     * @throws IllegalArgumentException if the weight is not strictly positive or the anchor is
     *   not part of the blend.
     */
    fun scaleByAnchor(blend: Blend, anchorName: String, anchorWeightPounds: BigDecimal): AnchorResult {
        require(anchorWeightPounds.signum() > 0) {
            "Dumped anchor weight must be greater than zero."
        }
        val anchor = blend.ingredients.firstOrNull { it.name == anchorName }
            ?: throw IllegalArgumentException(
                "Ingredient \"$anchorName\" is not part of ${blend.name}.",
            )

        // factor = anchorWeight * 100 / anchorPercent
        val factor = anchorWeightPounds.multiply(HUNDRED).divide(anchor.percent, MC)

        val lines = blend.ingredients.map { ingredient ->
            val weight = factor.multiply(ingredient.percent).divide(HUNDRED, MC)
            IngredientWeight(ingredient, weight)
        }
        val total = lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.weightPounds) }

        return AnchorResult(
            blendId = blend.id,
            anchor = anchor,
            anchorWeightPounds = anchorWeightPounds,
            factor = factor,
            lines = lines,
            totalPounds = total,
        )
    }
}
