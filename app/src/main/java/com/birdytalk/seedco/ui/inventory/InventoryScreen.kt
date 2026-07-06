package com.birdytalk.seedco.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birdytalk.seedco.ui.AppViewModel
import com.birdytalk.seedco.ui.components.NumericField
import com.birdytalk.seedco.ui.components.SubScreenScaffold

@Composable
fun InventoryScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val names by viewModel.allIngredientNames.collectAsStateWithLifecycle()
    val items by viewModel.inventory.collectAsStateWithLifecycle()
    val stored = items.associate { it.name to it.onHandPounds.stripTrailingZeros().toPlainString() }

    // Local edit buffer so partial input isn't clobbered by the persisted flow.
    val local = remember { mutableStateMapOf<String, String>() }

    SubScreenScaffold(title = "Inventory", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "On-hand pounds per ingredient. Batches that need more than you have will flag a shortfall. Leave blank to stop tracking an item.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (names.isEmpty()) {
                Text(
                    "No ingredients yet — add a blend first.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                )
            }
            names.forEach { name ->
                val value = local[name] ?: stored[name].orEmpty()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    NumericField(
                        value = value,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() || it == '.' }
                            local[name] = clean
                            viewModel.setOnHand(name, clean.toBigDecimalOrNull())
                        },
                        label = "On hand",
                        suffix = "lb",
                    )
                }
            }
        }
    }
}
