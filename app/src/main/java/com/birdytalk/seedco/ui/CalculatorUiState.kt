package com.birdytalk.seedco.ui

import com.birdytalk.seedco.domain.calc.AnchorResult
import com.birdytalk.seedco.domain.calc.BatchResult
import com.birdytalk.seedco.domain.calc.CostSummary
import com.birdytalk.seedco.domain.inventory.Shortfall
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.units.UnitSystem
import java.math.BigDecimal

/** Top-level, fully-derived calculator state. The [unitSystem] and [blends] are shared globally. */
data class CalculatorUiState(
    val unitSystem: UnitSystem = UnitSystem.DECIMAL_POUNDS,
    val blends: List<Blend> = emptyList(),
    val batch: BatchTabState = BatchTabState(),
    val anchor: AnchorTabState = AnchorTabState(),
)

/** Tab 1 — Batch Scaling. */
data class BatchTabState(
    val blendId: String = "",
    val targetInput: String = "",
    val result: BatchResult? = null,
    val cost: CostSummary? = null,
    val shortfalls: List<Shortfall> = emptyList(),
    val error: String? = null,
)

/** Tab 2 — Anchor Dump. */
data class AnchorTabState(
    val blendId: String = "",
    val anchorName: String = "",
    val weightInput: String = "",
    val binCapacityInput: String = "",
    val result: AnchorResult? = null,
    val cost: CostSummary? = null,
    val shortfalls: List<Shortfall> = emptyList(),
    val binCapacityPounds: BigDecimal? = null,
    val error: String? = null,
) {
    /** How much the computed batch exceeds the configured bin capacity, or `null` if within limits. */
    val overagePounds: BigDecimal?
        get() {
            val total = result?.totalPounds ?: return null
            val cap = binCapacityPounds ?: return null
            val over = total.subtract(cap)
            return if (over.signum() > 0) over else null
        }

    val overCapacity: Boolean get() = overagePounds != null
}
