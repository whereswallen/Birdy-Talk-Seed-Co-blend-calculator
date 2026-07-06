package com.birdytalk.seedco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import com.birdytalk.seedco.ui.AppRoot
import com.birdytalk.seedco.ui.AppViewModel
import com.birdytalk.seedco.ui.theme.SeedCoTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeedCoTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val viewModel: AppViewModel = viewModel()
                AppRoot(viewModel = viewModel, windowSizeClass = windowSizeClass)
            }
        }
    }
}
