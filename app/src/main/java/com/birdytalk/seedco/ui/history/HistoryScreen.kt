package com.birdytalk.seedco.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birdytalk.seedco.domain.persistence.BatchHistoryEntry
import com.birdytalk.seedco.domain.persistence.CalcMode
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.AppViewModel
import com.birdytalk.seedco.ui.components.SubScreenScaffold
import com.birdytalk.seedco.ui.format.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)

@Composable
fun HistoryScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onRerun: () -> Unit,
) {
    val entries by viewModel.history.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unit = state.unitSystem
    var showClear by remember { mutableStateOf(false) }

    SubScreenScaffold(
        title = "Batch history",
        onBack = onBack,
        actions = {
            if (entries.isNotEmpty()) {
                IconButton(onClick = { showClear = true }) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear all")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (entries.isEmpty()) {
                Text(
                    "No logged batches yet. Compute a batch and tap \"Log batch\" to save it here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                )
            }
            entries.forEach { entry ->
                HistoryRow(
                    entry = entry,
                    unit = unit,
                    onRerun = { viewModel.loadFromHistory(entry); onRerun() },
                    onDelete = { viewModel.deleteHistory(entry.id) },
                )
            }
        }
    }

    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            title = { Text("Clear all history?") },
            text = { Text("This removes every logged batch. This can't be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.clearHistory(); showClear = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { showClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun HistoryRow(
    entry: BatchHistoryEntry,
    unit: UnitSystem,
    onRerun: () -> Unit,
    onDelete: () -> Unit,
) {
    val modeLabel = if (entry.mode == CalcMode.BATCH) "Batch scaling" else "Anchor: ${entry.anchorName}"
    val total = UnitConverter.format(entry.totalPounds, unit).full
    val costPart = if (entry.totalCost.signum() > 0) "  •  ${Money.format(entry.totalCost)}" else ""

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onRerun),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(entry.blendName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "$modeLabel  •  $total$costPart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    dateFormat.format(Date(entry.timestampEpochMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
