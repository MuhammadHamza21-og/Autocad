package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.cad.CadEditorScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.wizard.ProjectWizardScreen
import com.example.ui.theme.SmartDraftsmanTheme
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartDraftsmanTheme(darkTheme = true) {
                val viewModel: CadViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        Screen.CAD_EDITOR -> CadEditorScreen(viewModel = viewModel)
                        Screen.WIZARD -> ProjectWizardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
