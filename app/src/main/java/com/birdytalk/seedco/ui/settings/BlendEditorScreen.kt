package com.birdytalk.seedco.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.domain.model.EditableBlend
import com.birdytalk.seedco.domain.model.EditableIngredient
import com.birdytalk.seedco.ui.AppViewModel
import com.birdytalk.seedco.ui.components.NumericField
import com.birdytalk.seedco.ui.components.SubScreenScaffold
import com.birdytalk.seedco.ui.theme.JetBrainsMono
import com.birdytalk.seedco.ui.theme.LocalSeedCoAccents

@Composable
fun BlendEditorScreen(
    viewModel: AppViewModel,
    blendId: String?,
    onDone: () -> Unit,
) {
    val initial = remember(blendId) {
        val existing = blendId?.let { id -> viewModel.blends.value.firstOrNull { it.id == id } }
        if (existing != null) EditableBlend.from(existing) else EditableBlend.blank(viewModel.newBlendId())
    }
    var draft by remember { mutableStateOf(initial) }

    val valid = draft.isValid()

    SubScreenScaffold(
        title = if (blendId == null) "New blend" else "Edit blend",
        onBack = onDone,
        actions = {
            TextButton(
                enabled = valid,
                onClick = {
                    viewModel.saveBlend(draft.toBlend())
                    onDone()
                },
            ) { Text("Save") }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { draft = draft.copy(name = it) },
                label = { Text("Blend name") },
                singleLine = true,
                isError = draft.name.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft.tagline,
                onValueChange = { draft = draft.copy(tagline = it) },
                label = { Text("Tagline (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            NumericField(
                value = draft.retailPriceInput,
                onValueChange = { draft = draft.copy(retailPriceInput = it.filter { c -> c.isDigit() || c == '.' }) },
                label = "Retail price per lb (optional)",
                suffix = "$/lb",
            )

            PercentTotalBanner(draft)

            draft.ingredients.forEachIndexed { index, row ->
                IngredientEditor(
                    row = row,
                    canRemove = draft.ingredients.size > 1,
                    onChange = { updated ->
                        draft = draft.copy(
                            ingredients = draft.ingredients.toMutableList().also { it[index] = updated },
                        )
                    },
                    onRemove = {
                        draft = draft.copy(
                            ingredients = draft.ingredients.toMutableList().also { it.removeAt(index) },
                        )
                    },
                )
            }

            OutlinedButton(
                onClick = { draft = draft.copy(ingredients = draft.ingredients + EditableIngredient()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text("  Add ingredient")
            }
        }
    }
}

@Composable
private fun PercentTotalBanner(draft: EditableBlend) {
    val valid = draft.isPercentValid()
    val accents = LocalSeedCoAccents.current
    val total = draft.percentTotal().stripTrailingZeros().toPlainString()
    val container = if (valid) MaterialTheme.colorScheme.primaryContainer else accents.warningContainer
    val onContainer = if (valid) MaterialTheme.colorScheme.onPrimaryContainer else accents.onWarningContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (valid) "Percentages total 100% ✓" else "Must total exactly 100% to save",
                style = MaterialTheme.typography.titleSmall,
                color = onContainer,
            )
            Text(
                text = "$total%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = onContainer,
            )
        }
    }
}

@Composable
private fun IngredientEditor(
    row: EditableIngredient,
    canRemove: Boolean,
    onChange: (EditableIngredient) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = row.name,
                    onValueChange = { onChange(row.copy(name = it)) },
                    label = { Text("Ingredient") },
                    singleLine = true,
                    isError = row.name.isBlank(),
                    modifier = Modifier.weight(1f),
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumericField(
                    value = row.percentInput,
                    onValueChange = { onChange(row.copy(percentInput = it.filter { c -> c.isDigit() || c == '.' })) },
                    label = "Percent",
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                )
                NumericField(
                    value = row.costInput,
                    onValueChange = { onChange(row.copy(costInput = it.filter { c -> c.isDigit() || c == '.' })) },
                    label = "Cost/lb",
                    suffix = "$",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
