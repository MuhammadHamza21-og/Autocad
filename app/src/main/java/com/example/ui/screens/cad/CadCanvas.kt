package com.example.ui.screens.cad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.engine.GeometryEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.CadTool
import com.example.ui.viewmodel.CadViewModel
import com.example.ui.viewmodel.SelectedEntity
import kotlin.math.*

@Composable
fun CadCanvas(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.activeProject.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val selectedEntity by viewModel.selectedEntity.collectAsState()
    val activeFloor = project.activeFloor
    val snapToGrid by viewModel.snapToGrid.collectAsState()
    val snapToObjects by viewModel.snapToObjects.collectAsState()
    val orthoMode by viewModel.orthoMode.collectAsState()
    val showFurniture by viewModel.showFurniture.collectAsState()
    val showDimensions by viewModel.showDimensions.collectAsState()
    val cursorCoords by viewModel.cursorCoords.collectAsState()
    val snapNotice by viewModel.snapNotice.collectAsState()
    val selectedFurnitureType by viewModel.selectedFurnitureType.collectAsState()
    val selectedRoomType by viewModel.selectedRoomType.collectAsState()

    val textMeasurer = rememberTextMeasurer()

    // Viewport transform (zoom and pan)
    var zoomScale by remember { mutableFloatStateOf(16f) } // Pixels per foot (default 16 px = 1 ft)
    var panOffset by remember { mutableStateOf(Offset(80f, 100f)) }

    // Interactive drafting in-progress states
    var drawingStart by remember { mutableStateOf<Point2D?>(null) }
    var drawingCurrent by remember { mutableStateOf<Point2D?>(null) }
    var draggingRoomId by remember { mutableStateOf<String?>(null) }
    var dragStartOffset by remember { mutableStateOf<Point2D?>(null) }

    // Screen to CAD and CAD to Screen coordinate converters
    fun screenToCad(screenOffset: Offset): Point2D {
        val x = (screenOffset.x - panOffset.x) / zoomScale
        val y = (screenOffset.y - panOffset.y) / zoomScale
        return Point2D(x, y)
    }

    fun cadToScreen(cadPoint: Point2D): Offset {
        val sx = cadPoint.x * zoomScale + panOffset.x
        val sy = cadPoint.y * zoomScale + panOffset.y
        return Offset(sx, sy)
    }

    fun snap(pt: Point2D): Point2D {
        val (snapped, notice) = GeometryEngine.snapPoint(
            point = pt,
            walls = activeFloor.walls,
            rooms = activeFloor.rooms,
            gridSize = if (snapToGrid) 1.0f else 0f,
            snapToGrid = snapToGrid,
            snapToObjects = snapToObjects
        )
        viewModel.updateCursorCoords(snapped, notice)
        return snapped
    }

    fun fitToScreen(canvasSize: Size) {
        val bounds = project.plot.polygon.bounds()
        val pw = bounds.actualWidth.coerceAtLeast(20f)
        val ph = bounds.actualHeight.coerceAtLeast(30f)
        val margin = 80f
        val scaleX = (canvasSize.width - margin * 2) / pw
        val scaleY = (canvasSize.height - margin * 2) / ph
        zoomScale = min(scaleX, scaleY).coerceIn(4f, 40f)
        panOffset = Offset(
            (canvasSize.width - pw * zoomScale) / 2f - bounds.left * zoomScale,
            (canvasSize.height - ph * zoomScale) / 2f - bounds.top * zoomScale
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeTool) {
                    if (activeTool == CadTool.PAN) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(4f, 60f)
                            panOffset += pan
                        }
                    } else {
                        // Drawing and selection gesture handling
                        detectTapGestures(
                            onTap = { tapOffset ->
                                val cadPt = screenToCad(tapOffset)
                                val snapped = snap(cadPt)

                                when (activeTool) {
                                    CadTool.SELECT -> {
                                        // Hit test rooms
                                        val hitRoom = activeFloor.rooms.findLast { it.rect.contains(snapped) }
                                        if (hitRoom != null) {
                                            viewModel.selectEntity(SelectedEntity.RoomSelection(hitRoom))
                                            return@detectTapGestures
                                        }
                                        // Hit test walls
                                        val hitWall = activeFloor.walls.findLast {
                                            it.toLine.distanceToPoint(snapped) <= 1.0f
                                        }
                                        if (hitWall != null) {
                                            viewModel.selectEntity(SelectedEntity.WallSelection(hitWall))
                                            return@detectTapGestures
                                        }
                                        // Hit test furniture
                                        val hitFurn = activeFloor.furniture.findLast {
                                            snapped.distanceTo(it.center) <= max(it.width, it.length) / 2f
                                        }
                                        if (hitFurn != null) {
                                            viewModel.selectEntity(SelectedEntity.FurnitureSelection(hitFurn))
                                            return@detectTapGestures
                                        }
                                        viewModel.selectEntity(null)
                                    }

                                    CadTool.ERASE -> {
                                        // Find item under tap and erase
                                        val hitRoom = activeFloor.rooms.findLast { it.rect.contains(snapped) }
                                        if (hitRoom != null) {
                                            viewModel.deleteRoom(hitRoom.id)
                                            return@detectTapGestures
                                        }
                                        val hitWall = activeFloor.walls.findLast {
                                            it.toLine.distanceToPoint(snapped) <= 1.5f
                                        }
                                        if (hitWall != null) {
                                            viewModel.selectEntity(SelectedEntity.WallSelection(hitWall))
                                            viewModel.deleteSelected()
                                            return@detectTapGestures
                                        }
                                    }

                                    CadTool.DOOR -> {
                                        val nearWall = activeFloor.walls.minByOrNull {
                                            it.toLine.distanceToPoint(snapped)
                                        }
                                        if (nearWall != null && nearWall.toLine.distanceToPoint(snapped) <= 3f) {
                                            val t = (snapped.distanceTo(nearWall.start) / nearWall.length).coerceIn(0.1f, 0.9f)
                                            viewModel.addDoor(nearWall.id, t, 3.5f)
                                        }
                                    }

                                    CadTool.WINDOW -> {
                                        val nearWall = activeFloor.walls.minByOrNull {
                                            it.toLine.distanceToPoint(snapped)
                                        }
                                        if (nearWall != null && nearWall.toLine.distanceToPoint(snapped) <= 3f) {
                                            val t = (snapped.distanceTo(nearWall.start) / nearWall.length).coerceIn(0.1f, 0.9f)
                                            viewModel.addWindow(nearWall.id, t, 4.0f, false)
                                        }
                                    }

                                    CadTool.FURNITURE -> {
                                        viewModel.addFurniture(selectedFurnitureType, snapped)
                                    }

                                    CadTool.STAIRCASE -> {
                                        val stairW = 6.5f
                                        val stairH = 10.0f
                                        val sRect = Rect2D(snapped.x, snapped.y, stairW, stairH)
                                        viewModel.addStaircase(sRect, 18, "Dogleg")
                                    }

                                    CadTool.ROOM -> {
                                        val defW = 12f
                                        val defH = 14f
                                        val rRect = Rect2D(snapped.x, snapped.y, defW, defH)
                                        viewModel.addRoom(
                                            RoomEntity(
                                                name = selectedRoomType.displayName,
                                                type = selectedRoomType,
                                                rect = rRect
                                            )
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        )
                    }
                }
                .pointerInput(activeTool) {
                    if (activeTool == CadTool.WALL_EXTERIOR || activeTool == CadTool.WALL_INTERIOR || activeTool == CadTool.DIMENSION) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val pt = snap(screenToCad(startOffset))
                                drawingStart = pt
                                drawingCurrent = pt
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                var pt = snap(screenToCad(change.position))
                                if (orthoMode && drawingStart != null) {
                                    pt = GeometryEngine.applyOrtho(drawingStart!!, pt)
                                }
                                drawingCurrent = pt
                            },
                            onDragEnd = {
                                val s = drawingStart
                                val e = drawingCurrent
                                if (s != null && e != null && s.distanceTo(e) >= 1.0f) {
                                    if (activeTool == CadTool.DIMENSION) {
                                        viewModel.addDimension(s, e, null)
                                    } else {
                                        val wType = if (activeTool == CadTool.WALL_EXTERIOR) {
                                            WallType.EXTERIOR_9_INCH
                                        } else {
                                            WallType.INTERIOR_4_5_INCH
                                        }
                                        viewModel.addWall(s, e, wType)
                                    }
                                }
                                drawingStart = null
                                drawingCurrent = null
                            },
                            onDragCancel = {
                                drawingStart = null
                                drawingCurrent = null
                            }
                        )
                    }
                }
        ) {
            // Draw CAD Canvas Layers:
            // Layer 1: Background CAD Grid
            drawCadGrid(zoomScale, panOffset, size)

            // Layer 2: Plot Boundary & Setback Envelope
            drawPlotBoundary(project.plot, zoomScale, panOffset, textMeasurer)

            // Layer 3: Rooms (fills and dimension tags)
            for (room in activeFloor.rooms) {
                val isSelected = (selectedEntity as? SelectedEntity.RoomSelection)?.room?.id == room.id
                drawRoom(room, isSelected, zoomScale, panOffset, textMeasurer)
            }

            // Layer 4: Furniture Blocks
            if (showFurniture) {
                for (furn in activeFloor.furniture) {
                    val isSelected = (selectedEntity as? SelectedEntity.FurnitureSelection)?.furniture?.id == furn.id
                    drawFurniture(furn, isSelected, zoomScale, panOffset, textMeasurer)
                }
            }

            // Layer 5: Staircases
            for (stair in activeFloor.staircases) {
                drawStaircase(stair, zoomScale, panOffset, textMeasurer)
            }

            // Layer 6: Walls
            for (wall in activeFloor.walls) {
                val isSelected = (selectedEntity as? SelectedEntity.WallSelection)?.wall?.id == wall.id
                drawWall(wall, isSelected, zoomScale, panOffset)
            }

            // Layer 7: Doors & Windows
            for (door in activeFloor.doors) {
                drawDoor(door, activeFloor.walls, zoomScale, panOffset)
            }
            for (win in activeFloor.windows) {
                drawWindow(win, activeFloor.walls, zoomScale, panOffset)
            }

            // Layer 8: Dimensions
            if (showDimensions) {
                for (dim in activeFloor.dimensions) {
                    drawDimension(dim, zoomScale, panOffset, textMeasurer)
                }
            }

            // Layer 9: Live Interactive Drawing Preview
            if (drawingStart != null && drawingCurrent != null) {
                val sOffset = cadToScreen(drawingStart!!)
                val eOffset = cadToScreen(drawingCurrent!!)
                val lengthFt = drawingStart!!.distanceTo(drawingCurrent!!)
                val lenStr = UnitConverter.formatLength(lengthFt, project.unitSystem)

                drawLine(
                    color = Color(0xFF00B4D8),
                    start = sOffset,
                    end = eOffset,
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )

                // Live dimension badge near mouse cursor
                val midOffset = Offset((sOffset.x + eOffset.x) / 2f, (sOffset.y + eOffset.y) / 2f - 20f)
                drawText(
                    textMeasurer = textMeasurer,
                    text = lenStr,
                    topLeft = midOffset,
                    style = TextStyle(
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        background = Color(0xFF0F172A).copy(alpha = 0.85f)
                    )
                )
            }

            // Layer 10: Snap Indicator Dot
            cursorCoords?.let { pt ->
                val sPt = cadToScreen(pt)
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 5f,
                    center = sPt
                )
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.3f),
                    radius = 12f,
                    center = sPt
                )
            }
        }

        // On-screen HUD Controls
        // Top-Right: Compass North Arrow
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E).copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "North",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(project.plot.northAngleDegrees)
                )
                Text(
                    text = "N",
                    color = Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom-Right: Zoom Controls (Zoom In, Zoom Out, Zoom Fit)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledIconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(60f) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFF8FAFC)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            FilledIconButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(4f) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFF8FAFC)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FilledIconButton(
                onClick = {
                    // Zoom to Fit
                    panOffset = Offset(80f, 100f)
                    zoomScale = 16f
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF00B4D8),
                    contentColor = Color(0xFF0A0F1D)
                )
            ) {
                Icon(Icons.Default.CropFree, contentDescription = "Zoom Fit")
            }
        }

        // Bottom-Left Status Bar: Coordinates & Scale readout
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cursorCoords?.let { pt ->
                    Text(
                        text = "X: ${String.format(java.util.Locale.US, "%.1f", pt.x)}'  Y: ${String.format(java.util.Locale.US, "%.1f", pt.y)}'",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                snapNotice?.let { notice ->
                    Text(
                        text = "[$notice]",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Scale: 1' = ${zoomScale.toInt()}px",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
                if (orthoMode) {
                    Text(
                        text = "ORTHO ON",
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Canvas Drawing Helper Functions
private fun DrawScope.drawCadGrid(zoomScale: Float, panOffset: Offset, size: Size) {
    val minorStepFt = 1.0f
    val majorStepFt = 5.0f

    val minorPx = minorStepFt * zoomScale
    val majorPx = majorStepFt * zoomScale

    val startX = (panOffset.x % majorPx) - majorPx
    val startY = (panOffset.y % majorPx) - majorPx

    // Minor grid
    if (minorPx >= 10f) {
        var x = (panOffset.x % minorPx) - minorPx
        while (x < size.width) {
            drawLine(
                color = Color(0xFF131D32),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.75f
            )
            x += minorPx
        }
        var y = (panOffset.y % minorPx) - minorPx
        while (y < size.height) {
            drawLine(
                color = Color(0xFF131D32),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.75f
            )
            y += minorPx
        }
    }

    // Major grid
    var x = startX
    while (x < size.width) {
        drawLine(
            color = Color(0xFF1E2D4A),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.25f
        )
        x += majorPx
    }
    var y = startY
    while (y < size.height) {
        drawLine(
            color = Color(0xFF1E2D4A),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.25f
        )
        y += majorPx
    }
}

private fun DrawScope.drawPlotBoundary(
    plot: PlotGeometry,
    zoomScale: Float,
    panOffset: Offset,
    textMeasurer: TextMeasurer
) {
    val verts = plot.computePlotVertices()
    if (verts.size < 3) return

    val path = Path()
    val firstScreen = Offset(verts[0].x * zoomScale + panOffset.x, verts[0].y * zoomScale + panOffset.y)
    path.moveTo(firstScreen.x, firstScreen.y)

    for (i in 1 until verts.size) {
        val s = Offset(verts[i].x * zoomScale + panOffset.x, verts[i].y * zoomScale + panOffset.y)
        path.lineTo(s.x, s.y)
    }
    path.close()

    // Outer plot boundary red dashed
    drawPath(
        path = path,
        color = Color(0xFFEF4444),
        style = Stroke(
            width = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
        )
    )

    // Buildable envelope (setbacks)
    val bRect = plot.computeBuildableRect()
    val bx = bRect.left * zoomScale + panOffset.x
    val by = bRect.top * zoomScale + panOffset.y
    val bw = bRect.actualWidth * zoomScale
    val bh = bRect.actualHeight * zoomScale

    drawRect(
        color = Color(0xFF00B4D8).copy(alpha = 0.35f),
        topLeft = Offset(bx, by),
        size = Size(bw, bh),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
        )
    )
}

private fun DrawScope.drawRoom(
    room: RoomEntity,
    isSelected: Boolean,
    zoomScale: Float,
    panOffset: Offset,
    textMeasurer: TextMeasurer
) {
    val r = room.rect
    val sx = r.left * zoomScale + panOffset.x
    val sy = r.top * zoomScale + panOffset.y
    val sw = r.actualWidth * zoomScale
    val sh = r.actualHeight * zoomScale

    // Room fill
    val baseColor = when (room.type) {
        RoomType.LAWN_COURTYARD -> Color(0xFF064E3B).copy(alpha = 0.5f)
        RoomType.VENTILATION_OTS -> Color(0xFF1E1B4B).copy(alpha = 0.5f)
        RoomType.CAR_PORCH -> Color(0xFF1F2937).copy(alpha = 0.6f)
        RoomType.ATTACHED_BATH, RoomType.POWDER_ROOM, RoomType.COMMON_BATH -> Color(0xFF1E293B).copy(alpha = 0.6f)
        else -> Color(0xFF1E293B).copy(alpha = 0.4f)
    }

    drawRect(
        color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.4f) else baseColor,
        topLeft = Offset(sx, sy),
        size = Size(sw, sh)
    )

    drawRect(
        color = if (isSelected) Color(0xFF00B4D8) else Color(0xFF475569),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        style = Stroke(width = if (isSelected) 2.5f else 1.0f)
    )

    // Room text (Name, Dimension, Area)
    val cx = sx + sw / 2f
    val cy = sy + sh / 2f

    val nameStr = room.name.uppercase(java.util.Locale.US)
    val dimStr = "${r.actualWidth.toInt()}'-0\" × ${r.actualHeight.toInt()}'-0\""
    val areaStr = "${room.areaSqFt.toInt()} SQ FT"

    val fontSize = (11f * (zoomScale / 16f).coerceIn(0.7f, 1.3f)).sp

    drawText(
        textMeasurer = textMeasurer,
        text = nameStr,
        topLeft = Offset(cx - 40f, cy - 18f),
        style = TextStyle(
            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFE2E8F0),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    )

    drawText(
        textMeasurer = textMeasurer,
        text = dimStr,
        topLeft = Offset(cx - 35f, cy + 2f),
        style = TextStyle(
            color = Color(0xFF94A3B8),
            fontSize = (fontSize.value * 0.85f).sp
        )
    )

    drawText(
        textMeasurer = textMeasurer,
        text = areaStr,
        topLeft = Offset(cx - 30f, cy + 18f),
        style = TextStyle(
            color = Color(0xFF64748B),
            fontSize = (fontSize.value * 0.75f).sp
        )
    )
}

private fun DrawScope.drawWall(
    wall: Wall,
    isSelected: Boolean,
    zoomScale: Float,
    panOffset: Offset
) {
    val s = Offset(wall.start.x * zoomScale + panOffset.x, wall.start.y * zoomScale + panOffset.y)
    val e = Offset(wall.end.x * zoomScale + panOffset.x, wall.end.y * zoomScale + panOffset.y)

    val thicknessPx = (wall.thickness * zoomScale).coerceAtLeast(2f)

    val wallColor = when {
        isSelected -> Color(0xFF00B4D8)
        wall.isExterior -> Color(0xFFF8FAFC)
        else -> Color(0xFFCBD5E1)
    }

    drawLine(
        color = wallColor,
        start = s,
        end = e,
        strokeWidth = thicknessPx,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawDoor(
    door: Door,
    walls: List<Wall>,
    zoomScale: Float,
    panOffset: Offset
) {
    val wall = walls.find { it.id == door.wallId } ?: return
    val pos = wall.toLine.interpolate(door.positionRatio)
    val s = Offset(pos.x * zoomScale + panOffset.x, pos.y * zoomScale + panOffset.y)

    val doorWidthPx = door.width * zoomScale

    // Door swing arc
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 4f,
        center = s
    )

    drawArc(
        color = Color(0xFFF59E0B),
        startAngle = 0f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(s.x - doorWidthPx / 2f, s.y - doorWidthPx / 2f),
        size = Size(doorWidthPx, doorWidthPx),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )
    )
}

private fun DrawScope.drawWindow(
    win: WindowEntity,
    walls: List<Wall>,
    zoomScale: Float,
    panOffset: Offset
) {
    val wall = walls.find { it.id == win.wallId } ?: return
    val pos = wall.toLine.interpolate(win.positionRatio)
    val s = Offset(pos.x * zoomScale + panOffset.x, pos.y * zoomScale + panOffset.y)

    val winWidthPx = win.width * zoomScale

    drawRect(
        color = if (win.isVentilator) Color(0xFFF59E0B) else Color(0xFF38BDF8),
        topLeft = Offset(s.x - winWidthPx / 2f, s.y - 4f),
        size = Size(winWidthPx, 8f)
    )
}

private fun DrawScope.drawStaircase(
    stair: Staircase,
    zoomScale: Float,
    panOffset: Offset,
    textMeasurer: TextMeasurer
) {
    val r = stair.rect
    val sx = r.left * zoomScale + panOffset.x
    val sy = r.top * zoomScale + panOffset.y
    val sw = r.actualWidth * zoomScale
    val sh = r.actualHeight * zoomScale

    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        style = Stroke(width = 1.5f)
    )

    val stepH = sh / stair.stepsCount
    for (i in 1 until stair.stepsCount) {
        val y = sy + i * stepH
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(sx, y),
            end = Offset(sx + sw, y),
            strokeWidth = 1f
        )
    }

    // Direction arrow
    drawLine(
        color = Color(0xFF00B4D8),
        start = Offset(sx + sw / 2f, sy + sh - 8f),
        end = Offset(sx + sw / 2f, sy + 8f),
        strokeWidth = 2f
    )
    drawText(
        textMeasurer = textMeasurer,
        text = "UP",
        topLeft = Offset(sx + sw / 2f + 4f, sy + 10f),
        style = TextStyle(color = Color(0xFF00B4D8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
}

private fun DrawScope.drawFurniture(
    furn: FurnitureBlock,
    isSelected: Boolean,
    zoomScale: Float,
    panOffset: Offset,
    textMeasurer: TextMeasurer
) {
    val cx = furn.center.x * zoomScale + panOffset.x
    val cy = furn.center.y * zoomScale + panOffset.y
    val fw = furn.width * zoomScale
    val fl = furn.length * zoomScale

    val color = if (isSelected) Color(0xFF00B4D8) else Color(0xFF94A3B8)

    drawRoundRect(
        color = color.copy(alpha = 0.25f),
        topLeft = Offset(cx - fw / 2f, cy - fl / 2f),
        size = Size(fw, fl),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - fw / 2f, cy - fl / 2f),
        size = Size(fw, fl),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = furn.type.label.take(10),
        topLeft = Offset(cx - fw / 2f + 4f, cy - 6f),
        style = TextStyle(color = color, fontSize = 9.sp)
    )
}

private fun DrawScope.drawDimension(
    dim: DimensionEntity,
    zoomScale: Float,
    panOffset: Offset,
    textMeasurer: TextMeasurer
) {
    val s = Offset(dim.start.x * zoomScale + panOffset.x, dim.start.y * zoomScale + panOffset.y)
    val e = Offset(dim.end.x * zoomScale + panOffset.x, dim.end.y * zoomScale + panOffset.y)

    drawLine(
        color = Color(0xFF06B6D4),
        start = s,
        end = e,
        strokeWidth = 1.25f
    )

    // Ticks at ends
    drawCircle(color = Color(0xFF06B6D4), radius = 3f, center = s)
    drawCircle(color = Color(0xFF06B6D4), radius = 3f, center = e)

    val mid = Offset((s.x + e.x) / 2f, (s.y + e.y) / 2f + dim.offset * zoomScale)
    val label = dim.textOverride ?: "${dim.start.distanceTo(dim.end).toInt()}'-0\""

    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = mid,
        style = TextStyle(
            color = Color(0xFF22D3EE),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )
}
