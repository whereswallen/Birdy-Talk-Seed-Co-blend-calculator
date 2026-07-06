package com.birdytalk.seedco.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.birdytalk.seedco.ui.calculator.CalculatorScreen
import com.birdytalk.seedco.ui.history.HistoryScreen
import com.birdytalk.seedco.ui.inventory.InventoryScreen
import com.birdytalk.seedco.ui.settings.BackupScreen
import com.birdytalk.seedco.ui.settings.BlendEditorScreen
import com.birdytalk.seedco.ui.settings.BlendListScreen
import com.birdytalk.seedco.ui.settings.SettingsScreen

private object Routes {
    const val CALCULATOR = "calculator"
    const val SETTINGS = "settings"
    const val BLENDS = "blends"
    const val BLEND_EDITOR = "blendEditor"
    const val INVENTORY = "inventory"
    const val HISTORY = "history"
    const val BACKUP = "backup"
    const val NEW_BLEND = "new"
}

@Composable
fun AppRoot(
    viewModel: AppViewModel,
    windowSizeClass: WindowSizeClass,
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.CALCULATOR) {
        composable(Routes.CALCULATOR) {
            CalculatorScreen(
                viewModel = viewModel,
                windowSizeClass = windowSizeClass,
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onBlends = { nav.navigate(Routes.BLENDS) },
                onInventory = { nav.navigate(Routes.INVENTORY) },
                onHistory = { nav.navigate(Routes.HISTORY) },
                onBackup = { nav.navigate(Routes.BACKUP) },
                onResetDefaults = viewModel::resetBlendsToDefaults,
            )
        }
        composable(Routes.BLENDS) {
            BlendListScreen(
                viewModel = viewModel,
                onBack = { nav.popBackStack() },
                onEdit = { id -> nav.navigate("${Routes.BLEND_EDITOR}/$id") },
                onNew = { nav.navigate("${Routes.BLEND_EDITOR}/${Routes.NEW_BLEND}") },
            )
        }
        composable(
            route = "${Routes.BLEND_EDITOR}/{blendId}",
            arguments = listOf(navArgument("blendId") { type = NavType.StringType }),
        ) { entry ->
            val arg = entry.arguments?.getString("blendId")
            BlendEditorScreen(
                viewModel = viewModel,
                blendId = arg?.takeIf { it != Routes.NEW_BLEND },
                onDone = { nav.popBackStack() },
            )
        }
        composable(Routes.INVENTORY) {
            InventoryScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { nav.popBackStack() },
                onRerun = { nav.popBackStack(Routes.CALCULATOR, inclusive = false) },
            )
        }
        composable(Routes.BACKUP) {
            BackupScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
        }
    }
}
