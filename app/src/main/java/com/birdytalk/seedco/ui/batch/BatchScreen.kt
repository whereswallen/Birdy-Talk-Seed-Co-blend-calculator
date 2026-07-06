package com.birdytalk.seedco.ui.batch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.BatchTabState
import com.birdytalk.seedco.ui.components.CompositionDonut
import com.birdytalk.seedco.ui.components.CompositionLegend
import com.birdytalk.seedco.ui.components.CostSummaryCard
import com.birdytalk.seedco.ui.components.HeadlineTotalCard
import com.birdytalk.seedco.ui.components.IngredientResultCard
import com.birdytalk.seedco.ui.components.LabeledDropdown
import com.birdytalk.seedco.ui.components.NumericField
import com.birdytalk.seedco.ui.components.RatioBar
import com.birdytalk.seedco.ui.components.ResultActionsRow
import com.birdytalk.seedco.ui.components.ingredientColor
import com.birdytalk.seedco.ui.format.Money
import com.birdytalk.seedco.ui.toBlendOptions
import com.birdytalk.seedco.ui.toSlices
import kotlin.math.roundToInt

private const val SLIDER_MAX = 500f

@Composable
fun BatchScreen(
    state: BatchTabState,
    unit: UnitSystem,
    blends: List<Blend>,
    twoPane: Boolean,
    onBlendSelected: (String) -> Unit,
    onTargetChanged: (String) -> Unit,
    onLogBatch: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val blend = blends.firstOrNull { it.id == state.blendId } ?: blends.firstOrNull()
    if (blend == null) {
        EmptyBlends(modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (twoPane) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    InputSection(blend, blends, state, unit, onBlendSelected, onTargetChanged)
                    CompositionSection(blend)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultsSection(blend, state, unit, onLogBatch, onShare)
                }
            }
        } else {
            InputSection(blend, blends, state, unit, onBlendSelected, onTargetChanged)
            CompositionSection(blend)
            ResultsSection(blend, state, unit, onLogBatch, onShare)
        }
    }
}

@Composable
private fun EmptyBlends(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No blends yet. Add one from Settings → Blends.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InputSection(
    blend: Blend,
    blends: List<Blend>,
    state: BatchTabState,
    unit: UnitSystem,
    onBlendSelected: (String) -> Unit,
    onTargetChanged: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LabeledDropdown(
            label = "Blend",
            options = blends.toBlendOptions(),
            selectedId = blend.id,
            onSelect = onBlendSelected,
        )

        Column {
            Text(blend.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(blend.tagline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        NumericField(
            value = state.targetInput,
            onValueChange = onTargetChanged,
            label = "Target batch size",
            suffix = unit.label,
            isError = state.error != null,
            supportingText = state.error,
        )

        val sliderPosition = state.targetInput.toFloatOrNull()?.coerceIn(0f, SLIDER_MAX) ?: 0f
        Slider(
            value = sliderPosition,
            onValueChange = { onTargetChanged(it.roundToInt().toString()) },
            valueRange = 0f..SLIDER_MAX,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
private fun CompositionSection(blend: Blend) {
    val slices = blend.toSlices()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Composition", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        RatioBar(slices)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(120.dp)) { CompositionDonut(slices = slices, animationKey = blend.id) }
            Spacer(Modifier.width(20.dp))
            CompositionLegend(slices = slices, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResultsSection(
    blend: Blend,
    state: BatchTabState,
    unit: UnitSystem,
    onLogBatch: () -> Unit,
    onShare: () -> Unit,
) {
    val result = state.result
    AnimatedVisibility(
        visible = result != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (result != null) {
                val costByName = state.cost?.lines?.associateBy { it.ingredient.name }.orEmpty()
                val shortByName = state.shortfalls.associateBy { it.ingredientName }
                val showCosts = state.cost?.hasCosts == true

                HeadlineTotalCard(label = blend.name, totalPounds = result.targetTotalPounds, unit = unit)

                result.lines.forEachIndexed { index, line ->
                    IngredientResultCard(
                        name = line.ingredient.name,
                        percent = line.ingredient.percent,
                        weightPounds = line.weightPounds,
                        accentColor = ingredientColor(index),
                        unit = unit,
                        costText = if (showCosts) costByName[line.ingredient.name]?.let { Money.format(it.cost) } else null,
                        shortMessage = shortByName[line.ingredient.name]?.let {
                            "Short ${UnitConverter.format(it.shortPounds, unit).full}"
                        },
                    )
                }

                state.cost?.let { CostSummaryCard(cost = it) }
                ResultActionsRow(onLogBatch = onLogBatch, onShare = onShare)
            }
        }
    }

    if (result == null && state.error == null) {
        Text(
            text = "Enter a target batch size to see the recipe breakdown.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}
