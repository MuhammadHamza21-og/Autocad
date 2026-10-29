package com.example.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CadProjectSummary
import com.example.data.database.CadRepository
import com.example.data.model.*
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.allProjects.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    val filteredProjects = if (searchQuery.isBlank()) {
        projects
    } else {
        projects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.clientName.contains(searchQuery, ignoreCase = true) ||
            it.siteLocation.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0F1D),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startNewWizard() },
                containerColor = Color(0xFF00B4D8),
                contentColor = Color(0xFF0A0F1D),
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Draft Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                // Hero Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF131B2E), Color(0xFF0369A1).copy(alpha = 0.3f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF00B4D8).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "PROFESSIONAL CAD SUITE",
                                        color = Color(0xFF00B4D8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "AI GENERATOR",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "AI Smart Draftsman",
                                color = Color(0xFFF8FAFC),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Architectural floor plan drafting, Pakistani bye-laws & plot area calculation, DXF, PDF and vector exports.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.startNewWizard() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A0F1D))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Start Wizard", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = { showImportDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
                                    border = BorderStroke(1.dp, Color(0xFF475569)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Import JSON", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Start Templates Row
            item {
                Text(
                    text = "QUICK START STANDARD PLOTS",
                    color = Color(0xFF00B4D8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickPlotCard(
                        title = "5 Marla Villa",
                        dimensions = "25' × 45'",
                        marla = "5 Marla (1,125 sq ft)",
                        onClick = {
                            viewModel.openProject("demo_5_marla")
                        }
                    )

                    QuickPlotCard(
                        title = "10 Marla House",
                        dimensions = "35' × 65'",
                        marla = "10 Marla (2,275 sq ft)",
                        onClick = {
                            viewModel.openProject("demo_10_marla")
                        }
                    )

                    QuickPlotCard(
                        title = "1 Kanal Residence",
                        dimensions = "50' × 90'",
                        marla = "1 Kanal (4,500 sq ft)",
                        onClick = {
                            viewModel.openProject("demo_1_kanal")
                        }
                    )
                }
            }

            // Search Bar & Project Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SAVED CAD PROJECTS (${filteredProjects.size})",
                        color = Color(0xFFF8FAFC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects by name, client or location...", color = Color(0xFF64748B), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF00B4D8),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedTextColor = Color(0xFFF8FAFC),
                        unfocusedTextColor = Color(0xFFF8FAFC)
                    )
                )
            }

            // Project Items
            if (filteredProjects.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("No CAD projects found", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Click 'New Draft Plan' or pick a quick standard plot template above.", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredProjects) { item ->
                    ProjectCard(
                        summary = item,
                        onOpen = { viewModel.openProject(item.id) },
                        onDelete = { viewModel.deleteProject(item.id) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(80.dp)) // FAB spacing
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        var importJsonText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import ASDCAD Project", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste raw JSON exported from another CAD Smart Draftsman device:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 10,
                        placeholder = { Text("{\"id\": ...}", color = Color(0xFF475569)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importProjectFromJson(importJsonText)
                        if (success) {
                            Toast.makeText(context, "Project imported successfully!", Toast.LENGTH_SHORT).show()
                            showImportDialog = false
                        } else {
                            Toast.makeText(context, "Invalid ASDCAD JSON data.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                ) {
                    Text("Import & Open", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun QuickPlotCard(
    title: String,
    dimensions: String,
    marla: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.width(180.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00B4D8).copy(alpha = 0.15f)
                ) {
                    Icon(
                        Icons.Default.HomeWork,
                        contentDescription = null,
                        tint = Color(0xFF00B4D8),
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
                Text("STANDARD", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color(0xFFF8FAFC), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(dimensions, color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(marla, color = Color(0xFF94A3B8), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProjectCard(
    summary: CadProjectSummary,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF131B2E),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Architecture, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.name,
                    color = Color(0xFFF8FAFC),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Plot: ${summary.plotDimensionsSummary} · ${summary.marlaSummary}",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = "Client: ${summary.clientName} · ${summary.formattedDate}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            }
        }
    }
}
