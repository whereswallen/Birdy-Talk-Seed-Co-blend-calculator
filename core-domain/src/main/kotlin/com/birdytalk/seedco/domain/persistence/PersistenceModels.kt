package com.birdytalk.seedco.domain.persistence

import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/** Which calculator mode produced a logged batch. */
@Serializable
enum class CalcMode { BATCH, ANCHOR }

/**
 * One logged calculation, kept so the owner can revisit or re-run past batches.
 *
 * @property inputPounds The driving input in pounds — target batch size (BATCH) or dumped anchor
 *   weight (ANCHOR).
 */
@Serializable
data class BatchHistoryEntry(
    val id: String,
    val blendId: String,
    val blendName: String,
    val mode: CalcMode,
    val anchorName: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val inputPounds: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPounds: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val totalCost: BigDecimal,
    val timestampEpochMillis: Long,
)

/** On-hand quantity of a single ingredient, in pounds. */
@Serializable
data class InventoryItem(
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val onHandPounds: BigDecimal,
)

/**
 * A complete, portable snapshot of everything the owner has configured — recipes, inventory, and
 * history — used for backup/restore export and import.
 */
@Serializable
data class AppBackup(
    val version: Int = CURRENT_VERSION,
    val blends: List<Blend>,
    val inventory: List<InventoryItem>,
    val history: List<BatchHistoryEntry>,
) {
    companion object {
        const val CURRENT_VERSION: Int = 1
    }
}
