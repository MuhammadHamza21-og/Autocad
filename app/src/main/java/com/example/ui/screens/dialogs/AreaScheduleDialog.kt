package com.example.ui.screens.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitConverter
import com.example.ui.viewmodel.CadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaScheduleDialog(
    viewModel: CadViewModel,
    onDismiss: () -> Unit
) {
    val project by viewModel.activeProject.collectAsState()
    val activeFloor = project.activeFloor

    val plotAreaReport = UnitConverter.calculateAreaReport(
        project.plot.totalAreaSqFt,
        project.marlaConvention
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Architectural Area Schedule",
                                color = Color(0xFFF8FAFC),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${project.name} · ${project.marlaConvention.label}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFE2E8F0))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Plot & Coverage Summary Cards
                        item {
                            Text(
                                text = "PLOT & COVERAGE SUMMARY",
                                color = Color(0xFF00B4D8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    label = "Total Plot Area",
                                    value = "${project.plot.totalAreaSqFt.toInt()} sq ft",
                                    subtext = plotAreaReport.formattedSummary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Ground Coverage",
                                    value = "${project.groundCoveragePercentage.toInt()}%",
                                    subtext = "${activeFloor.coveredAreaSqFt.toInt()} sq ft covered",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    label = "Total Project Area",
                                    value = "${project.totalCoveredAreaSqFt.toInt()} sq ft",
                                    subtext = "${project.floors.size} Floors total",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Open / Lawn Space",
                                    value = "${activeFloor.openAreaSqFt.toInt()} sq ft",
                                    subtext = "OTS & Porch / Courtyard",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Section 2: Room Schedule Table
                        item {
                            Text(
                                text = "ROOM SCHEDULE (${activeFloor.name})",
                                color = Color(0xFF00B4D8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Room Name", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                Text("Dimensions", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                Text("Area (sq ft)", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Area (sq m)", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                        }

                        items(activeFloor.rooms) { room ->
                            val sqM = room.areaSqFt * UnitConverter.SQFT_TO_SQM
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(room.name, color = Color(0xFFF8FAFC), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
                                Text(
                                    "${room.rect.actualWidth.toInt()}'-0\" × ${room.rect.actualHeight.toInt()}'-0\"",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text("${room.areaSqFt.toInt()} ft²", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(String.format(java.util.Locale.US, "%.1f m²", sqM), color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp)
                        }

                        // Section 3: Openings Schedule (Doors & Windows)
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "OPENINGS SCHEDULE (DOORS & WINDOWS)",
                                color = Color(0xFF00B4D8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mark", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("Type", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                Text("Width", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Quantity", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }

                            // Door count
                            val doorCount = activeFloor.doors.size
                            if (doorCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("D1", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                    Text("Standard Door", color = Color(0xFFE2E8F0), fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                    Text("3'-6\"", color = Color(0xFFCBD5E1), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("$doorCount Nos", color = Color(0xFFF8FAFC), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                            }

                            val winCount = activeFloor.windows.count { !it.isVentilator }
                            if (winCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("W1", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                    Text("Exterior Window", color = Color(0xFFE2E8F0), fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                    Text("4'-0\"", color = Color(0xFFCBD5E1), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("$winCount Nos", color = Color(0xFFF8FAFC), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                            }

                            val ventCount = activeFloor.windows.count { it.isVentilator }
                            if (ventCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("V1", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                    Text("High Ventilator", color = Color(0xFFE2E8F0), fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                    Text("2'-0\"", color = Color(0xFFCBD5E1), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("$ventCount Nos", color = Color(0xFFF8FAFC), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close Schedule", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp)
            Text(value, color = Color(0xFFF8FAFC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtext, color = Color(0xFF38BDF8), fontSize = 10.sp, maxLines = 1)
        }
    }
}
