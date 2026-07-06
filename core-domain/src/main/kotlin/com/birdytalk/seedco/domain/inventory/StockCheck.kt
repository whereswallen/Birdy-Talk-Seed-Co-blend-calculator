package com.birdytalk.seedco.domain.inventory

import com.birdytalk.seedco.domain.calc.IngredientWeight
import java.math.BigDecimal

/**
 * A single ingredient whose required weight exceeds the quantity on hand.
 *
 * @property shortPounds How many pounds short the batch is for this ingredient.
 */
data class Shortfall(
    val ingredientName: String,
    val requiredPounds: BigDecimal,
    val onHandPounds: BigDecimal,
    val shortPounds: BigDecimal,
)

/**
 * Pure inventory checking. Ingredients absent from [onHand] are treated as untracked and never
 * flagged, so partial inventory data only ever warns about what the user actually tracks.
 */
object StockCheck {

    /** Returns the ingredients (if any) whose required weight exceeds the tracked on-hand amount. */
    fun shortfalls(
        lines: List<IngredientWeight>,
        onHand: Map<String, BigDecimal>,
    ): List<Shortfall> = lines.mapNotNull { line ->
        val have = onHand[line.ingredient.name] ?: return@mapNotNull null
        val short = line.weightPounds.subtract(have)
        if (short.signum() > 0) {
            Shortfall(
                ingredientName = line.ingredient.name,
                requiredPounds = line.weightPounds,
                onHandPounds = have,
                shortPounds = short,
            )
        } else {
            null
        }
    }
}
