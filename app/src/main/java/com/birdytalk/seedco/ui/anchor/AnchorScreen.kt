package com.birdytalk.seedco.ui.anchor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.AnchorTabState
import com.birdytalk.seedco.ui.components.CapacityWarningBanner
import com.birdytalk.seedco.ui.components.HeadlineTotalCard
import com.birdytalk.seedco.ui.components.IngredientResultCard
import com.birdytalk.seedco.ui.components.LabeledDropdown
import com.birdytalk.seedco.ui.components.NumericField
import com.birdytalk.seedco.ui.components.ingredientColor
import com.birdytalk.seedco.ui.toBlendOptions
import com.birdytalk.seedco.ui.toIngredientOptions

@Composable
fun AnchorScreen(
    state: AnchorTabState,
    unit: UnitSystem,
    blends: List<Blend>,
    twoPane: Boolean,
    onBlendSelected: (String) -> Unit,
    onAnchorSelected: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onCapacityChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val blend = blends.first { it.id == state.blendId }
    val haptics = LocalHapticFeedback.current

    // Tactile feedback when the anchor ingredient changes...
    LaunchedEffect(state.anchorName) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    // ...and when the batch first crosses the bin-capacity threshold.
    LaunchedEffect(state.overCapacity) {
        if (state.overCapacity) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    InputSection(blend, blends, state, unit, onBlendSelected, onAnchorSelected, onWeightChanged, onCapacityChanged)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultsSection(blend, state, unit)
                }
            }
        } else {
            InputSection(blend, blends, state, unit, onBlendSelected, onAnchorSelected, onWeightChanged, onCapacityChanged)
            ResultsSection(blend, state, unit)
        }
    }
}

@Composable
private fun InputSection(
    blend: Blend,
    blends: List<Blend>,
    state: AnchorTabState,
    unit: UnitSystem,
    onBlendSelected: (String) -> Unit,
    onAnchorSelected: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onCapacityChanged: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LabeledDropdown(
            label = "Blend",
            options = blends.toBlendOptions(),
            selectedId = state.blendId,
            onSelect = onBlendSelected,
        )
        Column {
            Text(blend.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "Dump one ingredient, scale the rest.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LabeledDropdown(
            label = "Anchor ingredient",
            options = blend.toIngredientOptions(),
            selectedId = state.anchorName,
            onSelect = onAnchorSelected,
        )
        NumericField(
            value = state.weightInput,
            onValueChange = onWeightChanged,
            label = "Dumped weight of ${state.anchorName}",
            suffix = unit.label,
            isError = state.error != null,
            supportingText = state.error,
        )
        NumericField(
            value = state.binCapacityInput,
            onValueChange = onCapacityChanged,
            label = "Max bin capacity (optional)",
            suffix = unit.label,
        )
    }
}

@Composable
private fun ResultsSection(
    blend: Blend,
    state: AnchorTabState,
    unit: UnitSystem,
) {
    val result = state.result

    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = state.overCapacity,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            val overage = state.overagePounds
            if (overage != null) {
                val cap = UnitConverter.format(state.binCapacityPounds!!, unit).full
                val over = UnitConverter.format(overage, unit).full
                CapacityWarningBanner(
                    message = "Over bin capacity",
                    detail = "This batch exceeds the $cap bin by $over. Split the run or reduce the dump.",
                )
            }
        }

        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (result != null) {
                    HeadlineTotalCard(
                        label = blend.name,
                        totalPounds = result.totalPounds,
                        unit = unit,
                    )
                    result.lines.forEachIndexed { index, line ->
                        val isAnchor = line.ingredient.name == state.anchorName
                        Row {
                            IngredientResultCard(
                                name = if (isAnchor) "${line.ingredient.name}  ⚓" else line.ingredient.name,
                                percent = line.ingredient.percent,
                                weightPounds = line.weightPounds,
                                accentColor = ingredientColor(index),
                                unit = unit,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }

    if (result == null && state.error == null) {
        Text(
            text = "Enter the dumped weight of your anchor ingredient to scale the batch.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}
