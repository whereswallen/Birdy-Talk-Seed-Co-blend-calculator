package com.birdytalk.seedco.domain.model

import com.birdytalk.seedco.domain.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * A single component of a [Blend].
 *
 * @property name Human-readable ingredient name, e.g. "Black Oil Sunflower".
 * @property percent The ingredient's share of the blend, expressed 0..100 as an exact
 *   [BigDecimal] (e.g. `16.84`). Kept as [BigDecimal] so recipe ratios never suffer
 *   floating-point drift on the production floor.
 * @property costPerPound The purchase cost of this ingredient per pound, in dollars. `ZERO` when
 *   no cost has been entered.
 */
@Serializable
data class Ingredient(
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val percent: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val costPerPound: BigDecimal = BigDecimal.ZERO,
)
