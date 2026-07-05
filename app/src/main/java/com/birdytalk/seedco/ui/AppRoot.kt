package com.birdytalk.seedco.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birdytalk.seedco.ui.anchor.AnchorScreen
import com.birdytalk.seedco.ui.batch.BatchScreen
import com.birdytalk.seedco.ui.components.UnitToggleHeader

private val TABS = listOf("Batch Scaling", "Anchor Dump")

@Composable
fun AppRoot(
    viewModel: CalculatorViewModel,
    windowSizeClass: WindowSizeClass,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val twoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(
                unitSystem = state.unitSystem,
                onUnitSelected = viewModel::setUnitSystem,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { padding ->
        // Constrain very wide single-pane content so lines don't stretch on tablets.
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
                        blends = viewModel.blends,
                        twoPane = twoPane,
                        onBlendSelected = viewModel::onBatchBlendSelected,
                        onTargetChanged = viewModel::onBatchTargetChanged,
                        modifier = contentModifier,
                    )

                    else -> AnchorScreen(
                        state = state.anchor,
                        unit = state.unitSystem,
                        blends = viewModel.blends,
                        twoPane = twoPane,
                        onBlendSelected = viewModel::onAnchorBlendSelected,
                        onAnchorSelected = viewModel::onAnchorIngredientSelected,
                        onWeightChanged = viewModel::onAnchorWeightChanged,
                        onCapacityChanged = viewModel::onBinCapacityChanged,
                        modifier = contentModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    unitSystem: com.birdytalk.seedco.domain.units.UnitSystem,
    onUnitSelected: (com.birdytalk.seedco.domain.units.UnitSystem) -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
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
            Text(
                text = "BIRDY TALK SEED CO.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Production Calculator",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
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
