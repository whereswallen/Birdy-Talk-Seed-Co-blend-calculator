package com.birdytalk.seedco.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.ui.AppViewModel
import com.birdytalk.seedco.ui.components.SubScreenScaffold
import com.birdytalk.seedco.ui.format.Money

@Composable
fun BlendListScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onNew: () -> Unit,
) {
    val blends by viewModel.blends.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Blend?>(null) }

    SubScreenScaffold(
        title = "Blends",
        onBack = onBack,
        actions = {
            IconButton(onClick = onNew) {
                Icon(Icons.Rounded.Add, contentDescription = "New blend")
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
            blends.forEach { blend ->
                BlendRow(
                    blend = blend,
                    onEdit = { onEdit(blend.id) },
                    onDuplicate = { viewModel.duplicateBlend(blend.id) },
                    onDelete = { pendingDelete = blend },
                    canDelete = blends.size > 1,
                )
            }
        }
    }

    pendingDelete?.let { blend ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${blend.name}?") },
            text = { Text("This permanently removes the blend. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBlend(blend.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun BlendRow(
    blend: Blend,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val retail = blend.retailPricePerPound
    val subtitle = buildString {
        append("${blend.ingredients.size} ingredients")
        if (retail != null) append("  •  ${Money.perPound(retail)} retail")
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(blend.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menuOpen = false; onEdit() })
                    DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menuOpen = false; onDuplicate() })
                    if (canDelete) {
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete() })
                    }
                }
            }
        }
    }
}
