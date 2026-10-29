package com.example.ui.screens.cad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadTopBar(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.activeProject.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    var showAddFloorDialog by remember { mutableStateOf(false) }
    var newFloorName by remember { mutableStateOf("First Floor") }
    var copyFromActive by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to Dashboard
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Projects",
                        tint = Color(0xFFE2E8F0)
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text = project.name,
                        color = Color(0xFFF8FAFC),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${project.plot.frontWidth.toInt()}'×${project.plot.leftLength.toInt()}' · ${project.siteLocation} · ${project.marlaConvention.label.take(15)}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Undo / Redo
                IconButton(
                    onClick = { viewModel.undo() },
                    enabled = canUndo
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color(0xFF38BDF8) else Color(0xFF475569)
                    )
                }

                IconButton(
                    onClick = { viewModel.redo() },
                    enabled = canRedo
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color(0xFF38BDF8) else Color(0xFF475569)
                    )
                }

                // Area Report & Schedules
                Button(
                    onClick = { viewModel.showAreaSchedule(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = "Schedule", modifier = Modifier.size(16.dp), tint = Color(0xFFF59E0B))
                    Spacer(Modifier.width(4.dp))
                    Text("Schedule", color = Color(0xFFF8FAFC), fontSize = 12.sp)
                }

                Spacer(Modifier.width(6.dp))

                // Export Button
                Button(
                    onClick = { viewModel.showExport(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export", modifier = Modifier.size(16.dp), tint = Color(0xFF0A0F1D))
                    Spacer(Modifier.width(4.dp))
                    Text("Export", color = Color(0xFF0A0F1D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Floor Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F1D))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "FLOORS:",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )

                project.floors.forEach { floor ->
                    val isActive = floor.id == project.activeFloorId
                    FilterChip(
                        selected = isActive,
                        onClick = { viewModel.switchFloor(floor.id) },
                        label = {
                            Text(
                                floor.name,
                                fontSize = 12.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00B4D8),
                            selectedLabelColor = Color(0xFF0A0F1D),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                }

                // Add Floor Button
                AssistChip(
                    onClick = { showAddFloorDialog = true },
                    label = { Text("+ Add Floor", fontSize = 11.sp, color = Color(0xFF38BDF8)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF131B2E)),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }
    }

    // Add Floor Dialog
    if (showAddFloorDialog) {
        AlertDialog(
            onDismissRequest = { showAddFloorDialog = false },
            title = { Text("Add Floor Plan", color = Color(0xFFF8FAFC), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newFloorName,
                        onValueChange = { newFloorName = it },
                        label = { Text("Floor Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFFF8FAFC),
                            unfocusedTextColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFF00B4D8),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = copyFromActive,
                            onCheckedChange = { copyFromActive = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00B4D8))
                        )
                        Text(
                            "Copy walls & rooms from current floor as baseline",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val level = project.floors.size
                        viewModel.addFloor(newFloorName, level, copyFromActive)
                        showAddFloorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                ) {
                    Text("Add Floor", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFloorDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
