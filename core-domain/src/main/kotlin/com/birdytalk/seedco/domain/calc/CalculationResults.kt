package com.birdytalk.seedco.domain.calc

import com.birdytalk.seedco.domain.model.Ingredient
import java.math.BigDecimal

/**
 * The computed weight of a single ingredient, always expressed in the canonical unit: **pounds**.
 * Presentation-layer conversion/rounding is applied at the edge (see `UnitConverter`).
 */
data class IngredientWeight(
    val ingredient: Ingredient,
    val weightPounds: BigDecimal,
)

/** Result of scaling a whole blend to a target batch size (Tab 1 — Batch Scaling). */
data class BatchResult(
    val blendId: String,
    val targetTotalPounds: BigDecimal,
    val lines: List<IngredientWeight>,
)

/**
 * Result of scaling a blend from a single "anchor" ingredient's dumped weight
 * (Tab 2 — Anchor Dump).
 *
 * @property factor The scaling factor, `anchorWeight / (anchorPercent / 100)`.
 * @property totalPounds The resulting total batch weight (sum of all ingredient lines).
 */
data class AnchorResult(
    val blendId: String,
    val anchor: Ingredient,
    val anchorWeightPounds: BigDecimal,
    val factor: BigDecimal,
    val lines: List<IngredientWeight>,
    val totalPounds: BigDecimal,
)
