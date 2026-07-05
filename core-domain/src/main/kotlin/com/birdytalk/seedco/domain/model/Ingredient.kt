package com.birdytalk.seedco.domain.model

import java.math.BigDecimal

/**
 * A single component of a [Blend].
 *
 * @property name Human-readable ingredient name, e.g. "Black Oil Sunflower".
 * @property percent The ingredient's share of the blend, expressed 0..100 as an exact
 *   [BigDecimal] (e.g. `16.84`). Kept as [BigDecimal] so recipe ratios never suffer
 *   floating-point drift on the production floor.
 */
data class Ingredient(
    val name: String,
    val percent: BigDecimal,
)
