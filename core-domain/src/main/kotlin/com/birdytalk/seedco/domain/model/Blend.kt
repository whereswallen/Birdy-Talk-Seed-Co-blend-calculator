package com.birdytalk.seedco.domain.model

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
 */
data class Blend(
    val id: String,
    val name: String,
    val tagline: String,
    val ingredients: List<Ingredient>,
) {
    init {
        require(ingredients.isNotEmpty()) { "Blend \"$name\" must contain at least one ingredient." }
        val sum = ingredients.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.percent) }
        // Compare on a normalized scale so 100 == 100.00 == 100.0000.
        require(sum.setScale(2, RoundingMode.HALF_UP).compareTo(HUNDRED) == 0) {
            "Blend \"$name\" percentages must total 100 but were $sum."
        }
    }

    private companion object {
        val HUNDRED: BigDecimal = BigDecimal("100").setScale(2)
    }
}
