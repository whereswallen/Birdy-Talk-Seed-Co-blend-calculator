package com.birdytalk.seedco.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.birdytalk.seedco.data.UnitPreferences
import com.birdytalk.seedco.domain.calc.BlendCalculator
import com.birdytalk.seedco.domain.data.RecipeRepository
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Single source of truth for both calculator modes. Holds raw user input plus the [BigDecimal]
 * results (always in canonical pounds); the UI formats to the selected [UnitSystem] at the edge.
 */
class CalculatorViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UnitPreferences(app)

    val blends: List<Blend> = RecipeRepository.blends

    private val firstBlend = blends.first()

    private val _state = MutableStateFlow(
        CalculatorUiState(
            batch = BatchTabState(blendId = firstBlend.id),
            anchor = AnchorTabState(
                blendId = firstBlend.id,
                anchorName = firstBlend.ingredients.first().name,
            ),
        ),
    )
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    init {
        // Restore the persisted unit system.
        viewModelScope.launch {
            prefs.unitSystem.collect { saved ->
                if (saved != _state.value.unitSystem) applyUnitSystem(saved, persist = false)
            }
        }
    }

    // ---- Global unit toggle --------------------------------------------------------------------

    fun setUnitSystem(unit: UnitSystem) {
        if (unit == _state.value.unitSystem) return
        applyUnitSystem(unit, persist = true)
    }

    private fun applyUnitSystem(unit: UnitSystem, persist: Boolean) {
        val old = _state.value.unitSystem
        _state.update { s ->
            s.copy(
                unitSystem = unit,
                batch = s.batch.copy(targetInput = reinterpret(s.batch.targetInput, old, unit)),
                anchor = s.anchor.copy(
                    weightInput = reinterpret(s.anchor.weightInput, old, unit),
                    binCapacityInput = reinterpret(s.anchor.binCapacityInput, old, unit),
                ),
            )
        }
        recomputeBatch()
        recomputeAnchor()
        if (persist) viewModelScope.launch { prefs.setUnitSystem(unit) }
    }

    // ---- Batch Scaling tab ---------------------------------------------------------------------

    fun onBatchBlendSelected(id: String) {
        _state.update { it.copy(batch = it.batch.copy(blendId = id)) }
        recomputeBatch()
    }

    fun onBatchTargetChanged(text: String) {
        _state.update { it.copy(batch = it.batch.copy(targetInput = sanitize(text))) }
        recomputeBatch()
    }

    private fun recomputeBatch() {
        val s = _state.value
        val blend = RecipeRepository.blendById(s.batch.blendId) ?: return
        val number = parsePositive(s.batch.targetInput)
        val newBatch = when {
            s.batch.targetInput.isBlank() -> s.batch.copy(result = null, error = null)
            number == null -> s.batch.copy(result = null, error = "Enter a weight greater than zero.")
            else -> {
                val pounds = UnitConverter.toPounds(number, s.unitSystem)
                s.batch.copy(result = BlendCalculator.scaleByBatch(blend, pounds), error = null)
            }
        }
        _state.update { it.copy(batch = newBatch) }
    }

    // ---- Anchor Dump tab -----------------------------------------------------------------------

    fun onAnchorBlendSelected(id: String) {
        val blend = RecipeRepository.blendById(id) ?: return
        _state.update {
            it.copy(
                anchor = it.anchor.copy(
                    blendId = id,
                    anchorName = blend.ingredients.first().name,
                ),
            )
        }
        recomputeAnchor()
    }

    fun onAnchorIngredientSelected(name: String) {
        _state.update { it.copy(anchor = it.anchor.copy(anchorName = name)) }
        recomputeAnchor()
    }

    fun onAnchorWeightChanged(text: String) {
        _state.update { it.copy(anchor = it.anchor.copy(weightInput = sanitize(text))) }
        recomputeAnchor()
    }

    fun onBinCapacityChanged(text: String) {
        _state.update { it.copy(anchor = it.anchor.copy(binCapacityInput = sanitize(text))) }
        recomputeAnchor()
    }

    private fun recomputeAnchor() {
        val s = _state.value
        val blend = RecipeRepository.blendById(s.anchor.blendId) ?: return
        val number = parsePositive(s.anchor.weightInput)

        val capacityPounds = parsePositive(s.anchor.binCapacityInput)
            ?.let { UnitConverter.toPounds(it, s.unitSystem) }

        val base = s.anchor.copy(binCapacityPounds = capacityPounds)
        val newAnchor = when {
            s.anchor.weightInput.isBlank() -> base.copy(result = null, error = null)
            number == null -> base.copy(result = null, error = "Enter a dumped weight greater than zero.")
            else -> {
                val pounds = UnitConverter.toPounds(number, s.unitSystem)
                val result = BlendCalculator.scaleByAnchor(blend, s.anchor.anchorName, pounds)
                base.copy(result = result, error = null)
            }
        }
        _state.update { it.copy(anchor = newAnchor) }
    }

    // ---- Input helpers -------------------------------------------------------------------------

    /** Keeps only digits and a single decimal point from raw keyboard input. */
    private fun sanitize(text: String): String {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        if (firstDot < 0) return filtered
        // Drop any decimal points after the first.
        return filtered.substring(0, firstDot + 1) +
            filtered.substring(firstDot + 1).replace(".", "")
    }

    private fun parsePositive(text: String): BigDecimal? {
        val value = text.toBigDecimalOrNull() ?: return null
        return if (value.signum() > 0) value else null
    }

    /** Re-expresses a field's numeric value from [old] units into [new] units, keeping mass constant. */
    private fun reinterpret(text: String, old: UnitSystem, new: UnitSystem): String {
        val value = text.toBigDecimalOrNull() ?: return text
        val pounds = UnitConverter.toPounds(value, old)
        // Convert canonical pounds -> the display number shown in the input field for [new].
        val display = when (new) {
            UnitSystem.DECIMAL_POUNDS, UnitSystem.POUNDS_OUNCES ->
                pounds.setScale(2, RoundingMode.HALF_UP)
            UnitSystem.KILOGRAMS ->
                pounds.multiply(BigDecimal("0.45359237")).setScale(2, RoundingMode.HALF_UP)
        }
        return display.stripTrailingZeros().toPlainString()
    }
}
