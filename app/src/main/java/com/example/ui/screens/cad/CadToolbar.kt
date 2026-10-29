package com.example.ui.screens.cad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FurnitureType
import com.example.data.model.RoomType
import com.example.ui.viewmodel.CadTool
import com.example.ui.viewmodel.CadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadToolbar(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val activeTool by viewModel.activeTool.collectAsState()
    val snapToGrid by viewModel.snapToGrid.collectAsState()
    val snapToObjects by viewModel.snapToObjects.collectAsState()
    val orthoMode by viewModel.orthoMode.collectAsState()
    val showFurniture by viewModel.showFurniture.collectAsState()
    val showDimensions by viewModel.showDimensions.collectAsState()
    val selectedFurnitureType by viewModel.selectedFurnitureType.collectAsState()
    val selectedRoomType by viewModel.selectedRoomType.collectAsState()

    var showFurnitureMenu by remember { mutableStateOf(false) }
    var showRoomMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF131B2E),
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Drafting Tools Group
            ToolButton(
                icon = Icons.Default.NearMe,
                label = "Select",
                isSelected = activeTool == CadTool.SELECT,
                onClick = { viewModel.setTool(CadTool.SELECT) }
            )

            ToolButton(
                icon = Icons.Default.PanTool,
                label = "Pan / Zoom",
                isSelected = activeTool == CadTool.PAN,
                onClick = { viewModel.setTool(CadTool.PAN) }
            )

            VerticalDivider(modifier = Modifier.height(28.dp).padding(horizontal = 4.dp), color = Color(0xFF334155))

            ToolButton(
                icon = Icons.Default.BorderOuter,
                label = "Wall 9\"",
                isSelected = activeTool == CadTool.WALL_EXTERIOR,
                onClick = { viewModel.setTool(CadTool.WALL_EXTERIOR) }
            )

            ToolButton(
                icon = Icons.Default.BorderInner,
                label = "Wall 4.5\"",
                isSelected = activeTool == CadTool.WALL_INTERIOR,
                onClick = { viewModel.setTool(CadTool.WALL_INTERIOR) }
            )

            Box {
                ToolButton(
                    icon = Icons.Default.MeetingRoom,
                    label = "Room: ${selectedRoomType.displayName.take(7)}",
                    isSelected = activeTool == CadTool.ROOM,
                    onClick = {
                        viewModel.setTool(CadTool.ROOM)
                        showRoomMenu = true
                    }
                )
                DropdownMenu(
                    expanded = showRoomMenu,
                    onDismissRequest = { showRoomMenu = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    RoomType.values().forEach { rType ->
                        DropdownMenuItem(
                            text = { Text(rType.displayName, color = Color(0xFFF8FAFC), fontSize = 13.sp) },
                            onClick = {
                                viewModel.setRoomType(rType)
                                showRoomMenu = false
                            }
                        )
                    }
                }
            }

            ToolButton(
                icon = Icons.Default.SensorDoor,
                label = "Door",
                isSelected = activeTool == CadTool.DOOR,
                onClick = { viewModel.setTool(CadTool.DOOR) }
            )

            ToolButton(
                icon = Icons.Default.Window,
                label = "Window",
                isSelected = activeTool == CadTool.WINDOW,
                onClick = { viewModel.setTool(CadTool.WINDOW) }
            )

            ToolButton(
                icon = Icons.Default.Stairs,
                label = "Stairs",
                isSelected = activeTool == CadTool.STAIRCASE,
                onClick = { viewModel.setTool(CadTool.STAIRCASE) }
            )

            Box {
                ToolButton(
                    icon = Icons.Default.Chair,
                    label = "Block: ${selectedFurnitureType.label.take(7)}",
                    isSelected = activeTool == CadTool.FURNITURE,
                    onClick = {
                        viewModel.setTool(CadTool.FURNITURE)
                        showFurnitureMenu = true
                    }
                )
                DropdownMenu(
                    expanded = showFurnitureMenu,
                    onDismissRequest = { showFurnitureMenu = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    FurnitureType.values().forEach { fType ->
                        DropdownMenuItem(
                            text = { Text(fType.label, color = Color(0xFFF8FAFC), fontSize = 13.sp) },
                            onClick = {
                                viewModel.setFurnitureType(fType)
                                showFurnitureMenu = false
                            }
                        )
                    }
                }
            }

            ToolButton(
                icon = Icons.Default.Straighten,
                label = "Dimension",
                isSelected = activeTool == CadTool.DIMENSION,
                onClick = { viewModel.setTool(CadTool.DIMENSION) }
            )

            ToolButton(
                icon = Icons.Default.DeleteOutline,
                label = "Erase",
                isSelected = activeTool == CadTool.ERASE,
                onClick = { viewModel.setTool(CadTool.ERASE) }
            )

            VerticalDivider(modifier = Modifier.height(28.dp).padding(horizontal = 4.dp), color = Color(0xFF334155))

            // Snapping Toggles
            ToggleChip(
                label = "Grid Snap",
                isActive = snapToGrid,
                onClick = { viewModel.toggleSnapToGrid() }
            )

            ToggleChip(
                label = "Object Snap",
                isActive = snapToObjects,
                onClick = { viewModel.toggleSnapToObjects() }
            )

            ToggleChip(
                label = "Ortho 90°",
                isActive = orthoMode,
                onClick = { viewModel.toggleOrthoMode() }
            )

            ToggleChip(
                label = "Furniture",
                isActive = showFurniture,
                onClick = { viewModel.toggleFurnitureVisibility() }
            )

            ToggleChip(
                label = "Dims",
                isActive = showDimensions,
                onClick = { viewModel.toggleDimensionVisibility() }
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) Color(0xFF00B4D8) else Color(0xFF1E293B),
            contentColor = if (isSelected) Color(0xFF0A0F1D) else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun ToggleChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isActive,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.25f),
            selectedLabelColor = Color(0xFF10B981),
            containerColor = Color(0xFF1E293B),
            labelColor = Color(0xFF94A3B8)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
