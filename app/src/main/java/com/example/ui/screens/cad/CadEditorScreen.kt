package com.example.ui.screens.cad

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.dialogs.AreaScheduleDialog
import com.example.ui.screens.dialogs.ExportDialog
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CadEditorScreen(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val showAreaDialog by viewModel.showAreaDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CadTopBar(viewModel = viewModel)
        },
        bottomBar = {
            Column {
                CadPropertiesPanel(viewModel = viewModel)
                CadToolbar(viewModel = viewModel)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CadCanvas(viewModel = viewModel)
        }
    }

    if (showAreaDialog) {
        AreaScheduleDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAreaSchedule(false) }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showExport(false) }
        )
    }
}
