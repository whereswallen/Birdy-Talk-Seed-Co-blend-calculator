package com.birdytalk.seedco.ui.calculator

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.AppViewModel
import com.birdytalk.seedco.ui.anchor.AnchorScreen
import com.birdytalk.seedco.ui.batch.BatchScreen
import com.birdytalk.seedco.ui.components.UnitToggleHeader

private val TABS = listOf("Batch Scaling", "Anchor Dump")

@Composable
fun CalculatorScreen(
    viewModel: AppViewModel,
    windowSizeClass: WindowSizeClass,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val twoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val context = LocalContext.current

    fun share(tabIndex: Int) {
        val text = viewModel.buildShareText(tabIndex) ?: return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Share batch sheet"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(
                unitSystem = state.unitSystem,
                onUnitSelected = viewModel::setUnitSystem,
                selectedTab = selectedTab,
                onTabSelected = viewModel::setSelectedTab,
                onOpenSettings = onOpenSettings,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val contentModifier = Modifier
                .fillMaxWidth()
                .widthIn(max = if (twoPane) 1100.dp else 640.dp)

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab",
            ) { tab ->
                when (tab) {
                    0 -> BatchScreen(
                        state = state.batch,
                        unit = state.unitSystem,
                        blends = state.blends,
                        twoPane = twoPane,
                        onBlendSelected = viewModel::onBatchBlendSelected,
                        onTargetChanged = viewModel::onBatchTargetChanged,
                        onLogBatch = { viewModel.logBatch(0) },
                        onShare = { share(0) },
                        modifier = contentModifier,
                    )

                    else -> AnchorScreen(
                        state = state.anchor,
                        unit = state.unitSystem,
                        blends = state.blends,
                        twoPane = twoPane,
                        onBlendSelected = viewModel::onAnchorBlendSelected,
                        onAnchorSelected = viewModel::onAnchorIngredientSelected,
                        onWeightChanged = viewModel::onAnchorWeightChanged,
                        onCapacityChanged = viewModel::onBinCapacityChanged,
                        onLogBatch = { viewModel.logBatch(1) },
                        onShare = { share(1) },
                        modifier = contentModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    unitSystem: UnitSystem,
    onUnitSelected: (UnitSystem) -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "BIRDY TALK SEED CO.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Blend Manager",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            UnitToggleHeader(selected = unitSystem, onSelect = onUnitSelected)
            Spacer(Modifier.height(6.dp))
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title, style = MaterialTheme.typography.titleSmall) },
                    )
                }
            }
        }
    }
}
