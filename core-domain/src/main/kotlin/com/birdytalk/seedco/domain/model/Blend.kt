package com.birdytalk.seedco.domain.model

import com.birdytalk.seedco.domain.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * An immutable seed-blend recipe: an ordered list of [Ingredient]s whose percentages
 * must sum to exactly 100.
 *
 * @property id Stable identifier used by the UI (dropdown selection, state keys).
 * @property name Display name, e.g. "Cardinal Rule".
 * @property tagline Short descriptive subtitle shown under the blend title.
 * @property ingredients The blend composition. Order is preserved for display.
 * @property retailPricePerPound Optional sell price per pound, in dollars, used for margin
 *   calculations. `null` when no retail price has been set.
 */
@Serializable
data class Blend(
    val id: String,
    val name: String,
    val tagline: String,
    val ingredients: List<Ingredient>,
    @Serializable(with = BigDecimalSerializer::class)
    val retailPricePerPound: BigDecimal? = null,
) {
    init {
        require(ingredients.isNotEmpty()) { "Blend \"$name\" must contain at least one ingredient." }
        require(percentagesTotalOneHundred(ingredients)) {
            val sum = ingredients.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.percent) }
            "Blend \"$name\" percentages must total 100 but were $sum."
        }
    }

    companion object {
        val HUNDRED: BigDecimal = BigDecimal("100").setScale(2)

        /** True when the ingredient percentages total exactly 100 (compared at 2-dp scale). */
        fun percentagesTotalOneHundred(ingredients: List<Ingredient>): Boolean {
            val sum = ingredients.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.percent) }
            return sum.setScale(2, RoundingMode.HALF_UP).compareTo(HUNDRED) == 0
        }
    }
}
