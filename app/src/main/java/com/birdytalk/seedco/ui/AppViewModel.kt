package com.birdytalk.seedco.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.birdytalk.seedco.data.BackupRepository
import com.birdytalk.seedco.data.BlendRepository
import com.birdytalk.seedco.data.HistoryRepository
import com.birdytalk.seedco.data.InventoryRepository
import com.birdytalk.seedco.data.UnitPreferences
import com.birdytalk.seedco.domain.calc.BlendCalculator
import com.birdytalk.seedco.domain.calc.CostCalculator
import com.birdytalk.seedco.domain.data.DefaultBlends
import com.birdytalk.seedco.domain.inventory.StockCheck
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.persistence.BatchHistoryEntry
import com.birdytalk.seedco.domain.persistence.CalcMode
import com.birdytalk.seedco.domain.persistence.InventoryItem
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.format.BatchSheet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/** Raw, user-entered inputs — the source of truth from which UI state is derived. */
private data class CalculatorInputs(
    val unitSystem: UnitSystem = UnitSystem.DECIMAL_POUNDS,
    val batchBlendId: String,
    val targetInput: String = "",
    val anchorBlendId: String,
    val anchorName: String,
    val anchorWeightInput: String = "",
    val binCapacityInput: String = "",
)

/**
 * Activity-scoped single source of truth for the whole app: the two calculator modes plus recipe
 * editing, inventory, history, and backup. Weight/cost results are derived reactively from raw
 * inputs, the persisted blends, and on-hand inventory; the UI formats to the selected unit at the
 * edge. All money math stays exact via `CostCalculator`.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val unitPrefs = UnitPreferences(app)
    private val blendRepository = BlendRepository(app)
    private val inventoryRepository = InventoryRepository(app)
    private val historyRepository = HistoryRepository(app)
    private val backupRepository =
        BackupRepository(blendRepository, inventoryRepository, historyRepository)

    private val firstDefault = DefaultBlends.blends.first()

    private val _inputs = MutableStateFlow(
        CalculatorInputs(
            batchBlendId = firstDefault.id,
            anchorBlendId = firstDefault.id,
            anchorName = firstDefault.ingredients.first().name,
        ),
    )

    /** Which calculator tab is showing (0 = Batch, 1 = Anchor). Held here so history re-runs can switch it. */
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Eagerly started so `.value` (read when opening the editor) always reflects persisted data.
    val blends: StateFlow<List<Blend>> = blendRepository.blends
        .stateIn(viewModelScope, SharingStarted.Eagerly, DefaultBlends.blends)

    /** All distinct ingredient names across every blend, for the inventory screen. */
    val allIngredientNames: StateFlow<List<String>> = blendRepository.blends
        .map { list -> list.flatMap { b -> b.ingredients.map { it.name } }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val inventory: StateFlow<List<InventoryItem>> = inventoryRepository.items
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val history: StateFlow<List<BatchHistoryEntry>> = historyRepository.entries
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val state: StateFlow<CalculatorUiState> =
        combine(_inputs, blendRepository.blends, inventoryRepository.onHand) { inputs, blends, onHand ->
            deriveState(inputs, blends, onHand)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            deriveState(_inputs.value, DefaultBlends.blends, emptyMap()),
        )

    init {
        // Restore the persisted unit system.
        viewModelScope.launch {
            unitPrefs.unitSystem.collect { saved ->
                if (saved != _inputs.value.unitSystem) applyUnitSystem(saved, persist = false)
            }
        }
        // Keep selections valid as blends are edited/deleted.
        viewModelScope.launch {
            blendRepository.blends.collect { blends -> reconcileSelections(blends) }
        }
    }

    // ---- Global unit toggle --------------------------------------------------------------------

    fun setUnitSystem(unit: UnitSystem) {
        if (unit == _inputs.value.unitSystem) return
        applyUnitSystem(unit, persist = true)
    }

    private fun applyUnitSystem(unit: UnitSystem, persist: Boolean) {
        val old = _inputs.value.unitSystem
        _inputs.update {
            it.copy(
                unitSystem = unit,
                targetInput = reinterpret(it.targetInput, old, unit),
                anchorWeightInput = reinterpret(it.anchorWeightInput, old, unit),
                binCapacityInput = reinterpret(it.binCapacityInput, old, unit),
            )
        }
        if (persist) viewModelScope.launch { unitPrefs.setUnitSystem(unit) }
    }

    // ---- Tab selection -------------------------------------------------------------------------

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    // ---- Batch tab -----------------------------------------------------------------------------

    fun onBatchBlendSelected(id: String) = _inputs.update { it.copy(batchBlendId = id) }

    fun onBatchTargetChanged(text: String) = _inputs.update { it.copy(targetInput = sanitize(text)) }

    // ---- Anchor tab ----------------------------------------------------------------------------

    fun onAnchorBlendSelected(id: String) {
        val blend = blends.value.firstOrNull { it.id == id }
        _inputs.update {
            it.copy(
                anchorBlendId = id,
                anchorName = blend?.ingredients?.firstOrNull()?.name ?: it.anchorName,
            )
        }
    }

    fun onAnchorIngredientSelected(name: String) = _inputs.update { it.copy(anchorName = name) }

    fun onAnchorWeightChanged(text: String) = _inputs.update { it.copy(anchorWeightInput = sanitize(text)) }

    fun onBinCapacityChanged(text: String) = _inputs.update { it.copy(binCapacityInput = sanitize(text)) }

    // ---- Recipe editing ------------------------------------------------------------------------

    fun newBlendId(): String = "custom_${System.currentTimeMillis()}"

    fun saveBlend(blend: Blend) = viewModelScope.launch { blendRepository.upsert(blend) }

    fun deleteBlend(id: String) = viewModelScope.launch { blendRepository.delete(id) }

    fun duplicateBlend(id: String) =
        viewModelScope.launch { blendRepository.duplicate(id, newBlendId()) }

    fun resetBlendsToDefaults() = viewModelScope.launch { blendRepository.resetToDefaults() }

    // ---- Inventory -----------------------------------------------------------------------------

    fun setOnHand(name: String, pounds: BigDecimal?) =
        viewModelScope.launch { inventoryRepository.setOnHand(name, pounds) }

    // ---- History -------------------------------------------------------------------------------

    /** Logs the current result of [tabIndex] (0 = Batch, 1 = Anchor). No-op if nothing computed. */
    fun logBatch(tabIndex: Int) {
        val s = state.value
        val entry = if (tabIndex == 0) {
            val r = s.batch.result ?: return
            val blend = s.blends.firstOrNull { it.id == s.batch.blendId }
            BatchHistoryEntry(
                id = UUID.randomUUID().toString(),
                blendId = s.batch.blendId,
                blendName = blend?.name ?: s.batch.blendId,
                mode = CalcMode.BATCH,
                inputPounds = r.targetTotalPounds,
                totalPounds = r.targetTotalPounds,
                totalCost = s.batch.cost?.totalCost ?: BigDecimal.ZERO,
                timestampEpochMillis = System.currentTimeMillis(),
            )
        } else {
            val r = s.anchor.result ?: return
            val blend = s.blends.firstOrNull { it.id == s.anchor.blendId }
            BatchHistoryEntry(
                id = UUID.randomUUID().toString(),
                blendId = s.anchor.blendId,
                blendName = blend?.name ?: s.anchor.blendId,
                mode = CalcMode.ANCHOR,
                anchorName = s.anchor.anchorName,
                inputPounds = r.anchorWeightPounds,
                totalPounds = r.totalPounds,
                totalCost = s.anchor.cost?.totalCost ?: BigDecimal.ZERO,
                timestampEpochMillis = System.currentTimeMillis(),
            )
        }
        viewModelScope.launch { historyRepository.add(entry) }
    }

    fun deleteHistory(id: String) = viewModelScope.launch { historyRepository.delete(id) }

    fun clearHistory() = viewModelScope.launch { historyRepository.clear() }

    /** Loads a past batch back into the calculator and switches to its tab. */
    fun loadFromHistory(entry: BatchHistoryEntry) {
        val unit = _inputs.value.unitSystem
        val input = poundsToInput(entry.inputPounds, unit)
        if (entry.mode == CalcMode.BATCH) {
            _inputs.update { it.copy(batchBlendId = entry.blendId, targetInput = input) }
            _selectedTab.value = 0
        } else {
            _inputs.update {
                it.copy(
                    anchorBlendId = entry.blendId,
                    anchorName = entry.anchorName ?: it.anchorName,
                    anchorWeightInput = input,
                )
            }
            _selectedTab.value = 1
        }
    }

    // ---- Share / backup ------------------------------------------------------------------------

    /** Builds the plain-text batch sheet for [tabIndex], or null if nothing is computed. */
    fun buildShareText(tabIndex: Int): String? {
        val s = state.value
        return if (tabIndex == 0) {
            val cost = s.batch.cost ?: return null
            val blend = s.blends.firstOrNull { it.id == s.batch.blendId }
            BatchSheet.build(blend?.name ?: "Batch", "Batch scaling", s.unitSystem, cost)
        } else {
            val cost = s.anchor.cost ?: return null
            val blend = s.blends.firstOrNull { it.id == s.anchor.blendId }
            BatchSheet.build(
                blend?.name ?: "Batch",
                "Anchor dump — ${s.anchor.anchorName}",
                s.unitSystem,
                cost,
            )
        }
    }

    suspend fun buildBackupJson(): String = backupRepository.exportJson()

    suspend fun importBackupJson(text: String): Result<Unit> = backupRepository.importJson(text)

    // ---- Derivation ----------------------------------------------------------------------------

    private fun deriveState(
        inputs: CalculatorInputs,
        blends: List<Blend>,
        onHand: Map<String, BigDecimal>,
    ): CalculatorUiState {
        val batchBlend = blends.firstOrNull { it.id == inputs.batchBlendId } ?: blends.firstOrNull()
        val anchorBlend = blends.firstOrNull { it.id == inputs.anchorBlendId } ?: blends.firstOrNull()
        return CalculatorUiState(
            unitSystem = inputs.unitSystem,
            blends = blends,
            batch = deriveBatch(inputs, batchBlend, onHand),
            anchor = deriveAnchor(inputs, anchorBlend, onHand),
        )
    }

    private fun deriveBatch(
        inputs: CalculatorInputs,
        blend: Blend?,
        onHand: Map<String, BigDecimal>,
    ): BatchTabState {
        if (blend == null) return BatchTabState(blendId = inputs.batchBlendId, targetInput = inputs.targetInput)
        val number = parsePositive(inputs.targetInput)
        return when {
            inputs.targetInput.isBlank() -> BatchTabState(blend.id, inputs.targetInput)
            number == null -> BatchTabState(blend.id, inputs.targetInput, error = "Enter a weight greater than zero.")
            else -> {
                val pounds = UnitConverter.toPounds(number, inputs.unitSystem)
                val result = BlendCalculator.scaleByBatch(blend, pounds)
                BatchTabState(
                    blendId = blend.id,
                    targetInput = inputs.targetInput,
                    result = result,
                    cost = CostCalculator.summarize(result.lines, blend.retailPricePerPound),
                    shortfalls = StockCheck.shortfalls(result.lines, onHand),
                )
            }
        }
    }

    private fun deriveAnchor(
        inputs: CalculatorInputs,
        blend: Blend?,
        onHand: Map<String, BigDecimal>,
    ): AnchorTabState {
        if (blend == null) {
            return AnchorTabState(
                blendId = inputs.anchorBlendId,
                anchorName = inputs.anchorName,
                weightInput = inputs.anchorWeightInput,
                binCapacityInput = inputs.binCapacityInput,
            )
        }
        val anchorName =
            if (blend.ingredients.any { it.name == inputs.anchorName }) inputs.anchorName
            else blend.ingredients.first().name
        val capacityPounds = parsePositive(inputs.binCapacityInput)
            ?.let { UnitConverter.toPounds(it, inputs.unitSystem) }
        val base = AnchorTabState(
            blendId = blend.id,
            anchorName = anchorName,
            weightInput = inputs.anchorWeightInput,
            binCapacityInput = inputs.binCapacityInput,
            binCapacityPounds = capacityPounds,
        )
        val number = parsePositive(inputs.anchorWeightInput)
        return when {
            inputs.anchorWeightInput.isBlank() -> base
            number == null -> base.copy(error = "Enter a dumped weight greater than zero.")
            else -> {
                val pounds = UnitConverter.toPounds(number, inputs.unitSystem)
                val result = BlendCalculator.scaleByAnchor(blend, anchorName, pounds)
                base.copy(
                    result = result,
                    cost = CostCalculator.summarize(result.lines, blend.retailPricePerPound),
                    shortfalls = StockCheck.shortfalls(result.lines, onHand),
                )
            }
        }
    }

    private fun reconcileSelections(blends: List<Blend>) {
        if (blends.isEmpty()) return
        _inputs.update { inputs ->
            val batchOk = blends.any { it.id == inputs.batchBlendId }
            val anchorBlend = blends.firstOrNull { it.id == inputs.anchorBlendId }
            val anchorOk = anchorBlend != null && anchorBlend.ingredients.any { it.name == inputs.anchorName }
            inputs.copy(
                batchBlendId = if (batchOk) inputs.batchBlendId else blends.first().id,
                anchorBlendId = if (anchorBlend != null) inputs.anchorBlendId else blends.first().id,
                anchorName = when {
                    anchorOk -> inputs.anchorName
                    anchorBlend != null -> anchorBlend.ingredients.first().name
                    else -> blends.first().ingredients.first().name
                },
            )
        }
    }

    // ---- Input helpers -------------------------------------------------------------------------

    private fun sanitize(text: String): String {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        if (firstDot < 0) return filtered
        return filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
    }

    private fun parsePositive(text: String): BigDecimal? {
        val value = text.toBigDecimalOrNull() ?: return null
        return if (value.signum() > 0) value else null
    }

    private fun reinterpret(text: String, old: UnitSystem, new: UnitSystem): String {
        val value = text.toBigDecimalOrNull() ?: return text
        val pounds = UnitConverter.toPounds(value, old)
        return poundsToInput(pounds, new)
    }

    /** Canonical pounds → the display number shown in an input field for [unit]. */
    private fun poundsToInput(pounds: BigDecimal, unit: UnitSystem): String {
        val display = when (unit) {
            UnitSystem.DECIMAL_POUNDS, UnitSystem.POUNDS_OUNCES ->
                pounds.setScale(2, RoundingMode.HALF_UP)
            UnitSystem.KILOGRAMS ->
                pounds.multiply(BigDecimal("0.45359237")).setScale(2, RoundingMode.HALF_UP)
        }
        return display.stripTrailingZeros().toPlainString()
    }
}
