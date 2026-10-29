package com.example.ui.screens.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.ExportHelper
import com.example.data.export.Orientation
import com.example.data.export.PaperSize
import com.example.data.export.PdfSheetExporter
import com.example.ui.viewmodel.CadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    viewModel: CadViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val project by viewModel.activeProject.collectAsState()
    val activeFloor = project.activeFloor

    var selectedPaperSize by remember { mutableStateOf(PaperSize.A4) }
    var selectedOrientation by remember { mutableStateOf(Orientation.LANDSCAPE) }
    var isExporting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.92f),
        content = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Export & Print CAD Drawing",
                                color = Color(0xFFF8FAFC),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${project.name} · ${activeFloor.name}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFE2E8F0))
                        }
                    }

                    // Format 1: Architectural PDF Drawing Sheet
                    ExportOptionCard(
                        title = "PDF Architectural Drawing Sheet",
                        subtitle = "Professional title block, scale bar, north compass, room schedule table",
                        icon = Icons.Default.PictureAsPdf,
                        badge = "READY TO PRINT",
                        badgeColor = Color(0xFF10B981),
                        onClick = {
                            isExporting = true
                            try {
                                val file = PdfSheetExporter.generatePdfSheet(
                                    context = context,
                                    project = project,
                                    floor = activeFloor,
                                    paperSize = selectedPaperSize,
                                    orientation = selectedOrientation
                                )
                                ExportHelper.shareFile(context, file, "application/pdf", "CAD Sheet: ${project.name}")
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    )

                    // Format 2: AutoCAD DXF Format
                    ExportOptionCard(
                        title = "AutoCAD DXF Format (.dxf)",
                        subtitle = "Release 12/R14 ASCII CAD exchange with standard layers (WALLS, DOORS, DIMS)",
                        icon = Icons.Default.Architecture,
                        badge = "CAD EXCHANGE",
                        badgeColor = Color(0xFF00B4D8),
                        onClick = {
                            isExporting = true
                            try {
                                val file = ExportHelper.exportDxfFile(context, project, activeFloor)
                                ExportHelper.shareFile(context, file, "application/dxf", "AutoCAD DXF: ${project.name}")
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "DXF export error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    )

                    // Format 3: Scalable Vector Graphics SVG
                    ExportOptionCard(
                        title = "Scalable Vector Graphics (.svg)",
                        subtitle = "Crisp blueprint vectors for web, illustrations, and presentations",
                        icon = Icons.Default.Polyline,
                        badge = "VECTOR",
                        badgeColor = Color(0xFFF59E0B),
                        onClick = {
                            isExporting = true
                            try {
                                val file = ExportHelper.exportSvgFile(context, project, activeFloor)
                                ExportHelper.shareFile(context, file, "image/svg+xml", "SVG Vector: ${project.name}")
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "SVG export error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    )

                    // Format 4: Native ASDCAD Project File
                    ExportOptionCard(
                        title = "Smart Draftsman Project (.asdcad)",
                        subtitle = "Native JSON format with full project geometry, setbacks, and revision history",
                        icon = Icons.Default.DataObject,
                        badge = "PROJECT BACKUP",
                        badgeColor = Color(0xFF8B5CF6),
                        onClick = {
                            isExporting = true
                            try {
                                val file = ExportHelper.exportJsonProject(context, project)
                                ExportHelper.shareFile(context, file, "application/json", "ASDCAD Project: ${project.name}")
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Project export error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    )

                    if (isExporting) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF00B4D8)
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    )
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(24.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = Color(0xFFF8FAFC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
