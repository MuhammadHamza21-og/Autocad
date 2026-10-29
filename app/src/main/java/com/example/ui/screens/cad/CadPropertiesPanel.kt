package com.example.ui.screens.cad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Rect2D
import com.example.data.model.RoomEntity
import com.example.data.model.WallType
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.SelectedEntity

@Composable
fun CadPropertiesPanel(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val selectedEntity by viewModel.selectedEntity.collectAsState()
    val project by viewModel.activeProject.collectAsState()
    val activeFloor = project.activeFloor

    var editRoomDialog by remember { mutableStateOf<RoomEntity?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF131B2E),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedEntity == null) {
                // Summary of active floor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${activeFloor.name} Status",
                            color = Color(0xFFF8FAFC),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Covered: ${activeFloor.coveredAreaSqFt.toInt()} sq ft · Open: ${activeFloor.openAreaSqFt.toInt()} sq ft",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${activeFloor.rooms.size} Rooms · ${activeFloor.walls.size} Walls",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Selected Item Detail
                when (val sel = selectedEntity!!) {
                    is SelectedEntity.RoomSelection -> {
                        val room = sel.room
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "SELECTED: ${room.name.uppercase(java.util.Locale.US)}",
                                    color = Color(0xFF00B4D8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Span: ${room.rect.actualWidth.toInt()}'-0\" × ${room.rect.actualHeight.toInt()}'-0\" · Area: ${room.areaSqFt.toInt()} sq ft",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { editRoomDialog = room },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Resize", fontSize = 11.sp, color = Color(0xFFF8FAFC))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteSelected() },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    is SelectedEntity.WallSelection -> {
                        val wall = sel.wall
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "WALL: ${if (wall.isExterior) "Exterior 9\"" else "Interior 4.5\""}",
                                    color = Color(0xFF00B4D8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Length: ${String.format(java.util.Locale.US, "%.1f", wall.length)}' · Angle: ${wall.start.angleTo(wall.end).toInt()}°",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelected() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    is SelectedEntity.DoorSelection -> {
                        val door = sel.door
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "DOOR: ${door.label}",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Width: ${door.width}' · Swing: ${if (door.swingInside) "Inward" else "Outward"}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelected() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    is SelectedEntity.WindowSelection -> {
                        val win = sel.window
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${if (win.isVentilator) "VENTILATOR" else "WINDOW"}: ${win.label}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Width: ${win.width}' · Wall Opening",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelected() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    is SelectedEntity.FurnitureSelection -> {
                        val furn = sel.furniture
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "BLOCK: ${furn.type.label}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dimensions: ${furn.width}' × ${furn.length}'",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelected() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    is SelectedEntity.StaircaseSelection -> {
                        val stair = sel.staircase
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "STAIRCASE (${stair.style})",
                                    color = Color(0xFF00B4D8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Steps: ${stair.stepsCount} · Span: ${stair.rect.actualWidth.toInt()}'×${stair.rect.actualHeight.toInt()}'",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelected() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }

                    is SelectedEntity.DimensionSelection -> {
                        val dim = sel.dimension
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "DIMENSION LINE",
                                    color = Color(0xFF06B6D4),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Measured: ${dim.start.distanceTo(dim.end).toInt()}'-0\"",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelected() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // Room Edit Dimensions Dialog
    editRoomDialog?.let { room ->
        var editName by remember { mutableStateOf(room.name) }
        var editW by remember { mutableStateOf(room.rect.actualWidth.toInt().toString()) }
        var editH by remember { mutableStateOf(room.rect.actualHeight.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editRoomDialog = null },
            title = { Text("Edit Room Properties", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Room Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editW,
                            onValueChange = { editW = it },
                            label = { Text("Width (ft)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editH,
                            onValueChange = { editH = it },
                            label = { Text("Length (ft)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newW = editW.toFloatOrNull() ?: room.rect.actualWidth
                        val newH = editH.toFloatOrNull() ?: room.rect.actualHeight
                        val updated = room.copy(
                            name = editName,
                            rect = Rect2D(room.rect.x, room.rect.y, newW, newH)
                        )
                        viewModel.updateRoom(updated)
                        viewModel.selectEntity(SelectedEntity.RoomSelection(updated))
                        editRoomDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
                ) {
                    Text("Apply", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editRoomDialog = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
