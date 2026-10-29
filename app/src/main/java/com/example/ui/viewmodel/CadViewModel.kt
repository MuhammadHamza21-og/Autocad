package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CadProjectSummary
import com.example.data.database.CadRepository
import com.example.data.database.ProjectJsonSerializer
import com.example.data.model.*
import com.example.engine.FloorPlanGenerator
import com.example.engine.GeometryEngine
import com.example.engine.LayoutCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    DASHBOARD,
    CAD_EDITOR,
    WIZARD
}

enum class CadTool(val label: String) {
    SELECT("Select"),
    WALL_EXTERIOR("Wall (9\")"),
    WALL_INTERIOR("Partition (4.5\")"),
    ROOM("Add Room"),
    DOOR("Door"),
    WINDOW("Window"),
    STAIRCASE("Stairs"),
    FURNITURE("Furniture"),
    DIMENSION("Dimension"),
    ERASE("Erase"),
    PAN("Pan & Zoom")
}

sealed class SelectedEntity {
    data class WallSelection(val wall: Wall) : SelectedEntity()
    data class RoomSelection(val room: RoomEntity) : SelectedEntity()
    data class DoorSelection(val door: Door) : SelectedEntity()
    data class WindowSelection(val window: WindowEntity) : SelectedEntity()
    data class StaircaseSelection(val staircase: Staircase) : SelectedEntity()
    data class FurnitureSelection(val furniture: FurnitureBlock) : SelectedEntity()
    data class DimensionSelection(val dimension: DimensionEntity) : SelectedEntity()
}

class CadViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = CadRepository(database.cadProjectDao())

    val allProjects: StateFlow<List<CadProjectSummary>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeProject = MutableStateFlow(CadRepository.createDefault5Marla())
    val activeProject: StateFlow<CadProject> = _activeProject.asStateFlow()

    private val _activeTool = MutableStateFlow(CadTool.SELECT)
    val activeTool: StateFlow<CadTool> = _activeTool.asStateFlow()

    private val _selectedEntity = MutableStateFlow<SelectedEntity?>(null)
    val selectedEntity: StateFlow<SelectedEntity?> = _selectedEntity.asStateFlow()

    private val _selectedFurnitureType = MutableStateFlow(FurnitureType.BED_DOUBLE)
    val selectedFurnitureType: StateFlow<FurnitureType> = _selectedFurnitureType.asStateFlow()

    private val _selectedRoomType = MutableStateFlow(RoomType.BEDROOM)
    val selectedRoomType: StateFlow<RoomType> = _selectedRoomType.asStateFlow()

    // Drafting toggles
    private val _snapToGrid = MutableStateFlow(true)
    val snapToGrid: StateFlow<Boolean> = _snapToGrid.asStateFlow()

    private val _snapToObjects = MutableStateFlow(true)
    val snapToObjects: StateFlow<Boolean> = _snapToObjects.asStateFlow()

    private val _orthoMode = MutableStateFlow(false)
    val orthoMode: StateFlow<Boolean> = _orthoMode.asStateFlow()

    private val _showFurniture = MutableStateFlow(true)
    val showFurniture: StateFlow<Boolean> = _showFurniture.asStateFlow()

    private val _showDimensions = MutableStateFlow(true)
    val showDimensions: StateFlow<Boolean> = _showDimensions.asStateFlow()

    private val _cursorCoords = MutableStateFlow<Point2D?>(null)
    val cursorCoords: StateFlow<Point2D?> = _cursorCoords.asStateFlow()

    private val _snapNotice = MutableStateFlow<String?>(null)
    val snapNotice: StateFlow<String?> = _snapNotice.asStateFlow()

    // Dialog flags
    private val _showAreaDialog = MutableStateFlow(false)
    val showAreaDialog: StateFlow<Boolean> = _showAreaDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _editingRoom = MutableStateFlow<RoomEntity?>(null)
    val editingRoom: StateFlow<RoomEntity?> = _editingRoom.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<CadProject>()
    private val redoStack = mutableListOf<CadProject>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Wizard State
    private val _wizardStep = MutableStateFlow(1) // 1..5
    val wizardStep: StateFlow<Int> = _wizardStep.asStateFlow()

    var wizardProjectName = MutableStateFlow("New Residence CAD")
    var wizardClientName = MutableStateFlow("Valued Client")
    var wizardSiteLocation = MutableStateFlow("Lahore, Pakistan")
    var wizardDrawingTitle = MutableStateFlow("ARCHITECTURAL WORKING DRAWING")
    var wizardNotes = MutableStateFlow("Verify all levels on site.")

    var wizardPlotShape = MutableStateFlow(PlotShapeType.RECTANGLE)
    var wizardFrontWidth = MutableStateFlow("30")
    var wizardBackWidth = MutableStateFlow("30")
    var wizardLeftLength = MutableStateFlow("60")
    var wizardRightLength = MutableStateFlow("60")
    var wizardDiagonal = MutableStateFlow("")
    var wizardRoadFacing = MutableStateFlow("Front (South)")
    var wizardNorthAngle = MutableStateFlow("0")

    var wizardFrontSetback = MutableStateFlow("5")
    var wizardRearSetback = MutableStateFlow("4")
    var wizardLeftSetback = MutableStateFlow("0")
    var wizardRightSetback = MutableStateFlow("0")

    var wizardUnitSystem = MutableStateFlow(UnitSystem.FEET_INCHES)
    var wizardMarlaConvention = MutableStateFlow(MarlaConvention.LAHORE_COMMERCIAL)
    var wizardFloorsCount = MutableStateFlow(2)
    var wizardArchStyle = MutableStateFlow(ArchitecturalStyle.MODERN_PAKISTANI)

    private val _wizardRoomRequirements = MutableStateFlow(BuildingRequirements.defaultRequirements())
    val wizardRoomRequirements: StateFlow<List<RoomRequirement>> = _wizardRoomRequirements.asStateFlow()

    private val _wizardAlternatives = MutableStateFlow<List<LayoutCandidate>>(emptyList())
    val wizardAlternatives: StateFlow<List<LayoutCandidate>> = _wizardAlternatives.asStateFlow()

    private val _wizardSelectedAltIndex = MutableStateFlow(0)
    val wizardSelectedAltIndex: StateFlow<Int> = _wizardSelectedAltIndex.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialProjectsIfEmpty()
        }
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openProject(id: String) {
        viewModelScope.launch {
            val proj = repository.getProject(id)
            if (proj != null) {
                _activeProject.value = proj
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()
                _selectedEntity.value = null
                _currentScreen.value = Screen.CAD_EDITOR
            }
        }
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val updated = _activeProject.value.copy(dateModified = System.currentTimeMillis())
            _activeProject.value = updated
            repository.saveProject(updated)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    // Wizard flow
    fun startNewWizard() {
        _wizardStep.value = 1
        wizardProjectName.value = "New Residence CAD"
        wizardClientName.value = "Client"
        wizardSiteLocation.value = "Lahore, Pakistan"
        wizardPlotShape.value = PlotShapeType.RECTANGLE
        wizardFrontWidth.value = "30"
        wizardBackWidth.value = "30"
        wizardLeftLength.value = "60"
        wizardRightLength.value = "60"
        wizardDiagonal.value = ""
        wizardFrontSetback.value = "5"
        wizardRearSetback.value = "4"
        wizardLeftSetback.value = "0"
        wizardRightSetback.value = "0"
        _wizardRoomRequirements.value = BuildingRequirements.defaultRequirements()
        _wizardAlternatives.value = emptyList()
        _currentScreen.value = Screen.WIZARD
    }

    fun setWizardStep(step: Int) {
        _wizardStep.value = step.coerceIn(1, 5)
        if (_wizardStep.value == 5) {
            generateWizardAlternatives()
        }
    }

    fun addWizardRoom(type: RoomType) {
        val current = _wizardRoomRequirements.value.toMutableList()
        current.add(
            RoomRequirement(
                type = type,
                name = type.displayName,
                targetWidthFt = 12f,
                targetLengthFt = 14f
            )
        )
        _wizardRoomRequirements.value = current
    }

    fun removeWizardRoom(id: String) {
        val current = _wizardRoomRequirements.value.toMutableList()
        current.removeAll { it.id == id }
        _wizardRoomRequirements.value = current
    }

    fun updateWizardRoom(id: String, width: Float, length: Float, attachedBath: Boolean) {
        val current = _wizardRoomRequirements.value.map {
            if (it.id == id) it.copy(targetWidthFt = width, targetLengthFt = length, requiresAttachedBath = attachedBath)
            else it
        }
        _wizardRoomRequirements.value = current
    }

    fun generateWizardAlternatives() {
        val frontW = wizardFrontWidth.value.toFloatOrNull() ?: 30f
        val backW = wizardBackWidth.value.toFloatOrNull() ?: frontW
        val leftL = wizardLeftLength.value.toFloatOrNull() ?: 60f
        val rightL = wizardRightLength.value.toFloatOrNull() ?: leftL
        val diag = wizardDiagonal.value.toFloatOrNull()
        val fSet = wizardFrontSetback.value.toFloatOrNull() ?: 5f
        val rSet = wizardRearSetback.value.toFloatOrNull() ?: 4f
        val lSet = wizardLeftSetback.value.toFloatOrNull() ?: 0f
        val rgSet = wizardRightSetback.value.toFloatOrNull() ?: 0f

        val plot = PlotGeometry(
            shapeType = wizardPlotShape.value,
            frontWidth = frontW,
            backWidth = backW,
            leftLength = leftL,
            rightLength = rightL,
            diagonalLength = diag,
            roadFacingSide = wizardRoadFacing.value,
            northAngleDegrees = wizardNorthAngle.value.toFloatOrNull() ?: 0f,
            frontSetback = fSet,
            rearSetback = rSet,
            leftSetback = lSet,
            rightSetback = rgSet
        )

        val reqs = BuildingRequirements(
            style = wizardArchStyle.value,
            floorsCount = wizardFloorsCount.value,
            roomRequirements = _wizardRoomRequirements.value
        )

        val alts = FloorPlanGenerator.generateAlternatives(plot, reqs)
        _wizardAlternatives.value = alts
        _wizardSelectedAltIndex.value = 0
    }

    fun selectWizardAlternative(index: Int) {
        _wizardSelectedAltIndex.value = index.coerceIn(0, (_wizardAlternatives.value.size - 1).coerceAtLeast(0))
    }

    fun applyWizardAlternative() {
        val alts = _wizardAlternatives.value
        if (alts.isEmpty()) return
        val chosen = alts[_wizardSelectedAltIndex.value]

        val frontW = wizardFrontWidth.value.toFloatOrNull() ?: 30f
        val backW = wizardBackWidth.value.toFloatOrNull() ?: frontW
        val leftL = wizardLeftLength.value.toFloatOrNull() ?: 60f
        val rightL = wizardRightLength.value.toFloatOrNull() ?: leftL

        val plot = PlotGeometry(
            shapeType = wizardPlotShape.value,
            frontWidth = frontW,
            backWidth = backW,
            leftLength = leftL,
            rightLength = rightL,
            diagonalLength = wizardDiagonal.value.toFloatOrNull(),
            roadFacingSide = wizardRoadFacing.value,
            frontSetback = wizardFrontSetback.value.toFloatOrNull() ?: 5f,
            rearSetback = wizardRearSetback.value.toFloatOrNull() ?: 4f,
            leftSetback = wizardLeftSetback.value.toFloatOrNull() ?: 0f,
            rightSetback = wizardRightSetback.value.toFloatOrNull() ?: 0f
        )

        val newProject = CadProject(
            id = UUID.randomUUID().toString(),
            name = wizardProjectName.value,
            clientName = wizardClientName.value,
            siteLocation = wizardSiteLocation.value,
            drawingTitle = wizardDrawingTitle.value,
            notes = wizardNotes.value,
            unitSystem = wizardUnitSystem.value,
            marlaConvention = wizardMarlaConvention.value,
            plot = plot,
            floors = listOf(chosen.floor),
            activeFloorId = chosen.floor.id
        )

        _activeProject.value = newProject
        saveCurrentProject()
        _currentScreen.value = Screen.CAD_EDITOR
    }

    // Floor Switching
    fun switchFloor(floorId: String) {
        _activeProject.value = _activeProject.value.copy(activeFloorId = floorId)
        _selectedEntity.value = null
    }

    fun addFloor(name: String, level: Int, copyExisting: Boolean) {
        recordUndo()
        val current = _activeProject.value
        val activeF = current.activeFloor
        val newFloorId = UUID.randomUUID().toString()

        val newFloor = if (copyExisting) {
            activeF.copy(id = newFloorId, name = name, level = level)
        } else {
            CadFloor(id = newFloorId, name = name, level = level)
        }

        val updatedFloors = current.floors + newFloor
        _activeProject.value = current.copy(floors = updatedFloors, activeFloorId = newFloorId)
        saveCurrentProject()
    }

    // CAD Tool & Selection
    fun setTool(tool: CadTool) {
        _activeTool.value = tool
        if (tool != CadTool.SELECT) {
            _selectedEntity.value = null
        }
    }

    fun selectEntity(entity: SelectedEntity?) {
        _selectedEntity.value = entity
    }

    fun setFurnitureType(type: FurnitureType) {
        _selectedFurnitureType.value = type
    }

    fun setRoomType(type: RoomType) {
        _selectedRoomType.value = type
    }

    fun toggleSnapToGrid() {
        _snapToGrid.value = !_snapToGrid.value
    }

    fun toggleSnapToObjects() {
        _snapToObjects.value = !_snapToObjects.value
    }

    fun toggleOrthoMode() {
        _orthoMode.value = !_orthoMode.value
    }

    fun toggleFurnitureVisibility() {
        _showFurniture.value = !_showFurniture.value
    }

    fun toggleDimensionVisibility() {
        _showDimensions.value = !_showDimensions.value
    }

    fun updateCursorCoords(pt: Point2D?, notice: String? = null) {
        _cursorCoords.value = pt
        _snapNotice.value = notice
    }

    // Geometry Modifications with Undo Support
    private fun recordUndo() {
        undoStack.add(_activeProject.value)
        redoStack.clear()
        updateUndoRedoStates()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_activeProject.value)
            val prev = undoStack.removeAt(undoStack.size - 1)
            _activeProject.value = prev
            updateUndoRedoStates()
            saveCurrentProject()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_activeProject.value)
            val next = redoStack.removeAt(redoStack.size - 1)
            _activeProject.value = next
            updateUndoRedoStates()
            saveCurrentProject()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun addWall(start: Point2D, end: Point2D, wallType: WallType) {
        if (start.distanceTo(end) < 0.5f) return
        recordUndo()
        val wall = Wall(
            start = start,
            end = end,
            thickness = wallType.thicknessFeet,
            wallType = wallType,
            isExterior = (wallType == WallType.EXTERIOR_9_INCH)
        )
        updateActiveFloor { it.copy(walls = it.walls + wall) }
    }

    fun addRoom(room: RoomEntity) {
        recordUndo()
        updateActiveFloor { floor ->
            val updatedRooms = floor.rooms + room
            val updatedWalls = GeometryEngine.generateWallsFromRooms(updatedRooms)
            floor.copy(rooms = updatedRooms, walls = updatedWalls)
        }
    }

    fun updateRoom(updatedRoom: RoomEntity) {
        recordUndo()
        updateActiveFloor { floor ->
            val updatedRooms = floor.rooms.map { if (it.id == updatedRoom.id) updatedRoom else it }
            val updatedWalls = GeometryEngine.generateWallsFromRooms(updatedRooms)
            floor.copy(rooms = updatedRooms, walls = updatedWalls)
        }
    }

    fun deleteRoom(roomId: String) {
        recordUndo()
        updateActiveFloor { floor ->
            val updatedRooms = floor.rooms.filterNot { it.id == roomId }
            val updatedWalls = GeometryEngine.generateWallsFromRooms(updatedRooms)
            floor.copy(rooms = updatedRooms, walls = updatedWalls)
        }
        _selectedEntity.value = null
    }

    fun addDoor(wallId: String, ratio: Float, width: Float) {
        recordUndo()
        val door = Door(
            wallId = wallId,
            positionRatio = ratio.coerceIn(0.1f, 0.9f),
            width = width,
            label = "D${_activeProject.value.activeFloor.doors.size + 1}"
        )
        updateActiveFloor { it.copy(doors = it.doors + door) }
    }

    fun addWindow(wallId: String, ratio: Float, width: Float, isVent: Boolean) {
        recordUndo()
        val window = WindowEntity(
            wallId = wallId,
            positionRatio = ratio.coerceIn(0.1f, 0.9f),
            width = width,
            isVentilator = isVent,
            label = if (isVent) "V${_activeProject.value.activeFloor.windows.count { it.isVentilator } + 1}"
            else "W${_activeProject.value.activeFloor.windows.count { !it.isVentilator } + 1}"
        )
        updateActiveFloor { it.copy(windows = it.windows + window) }
    }

    fun addStaircase(rect: Rect2D, steps: Int, style: String) {
        recordUndo()
        val s = Staircase(rect = rect, stepsCount = steps, style = style)
        updateActiveFloor { it.copy(staircases = it.staircases + s) }
    }

    fun addFurniture(type: FurnitureType, center: Point2D) {
        recordUndo()
        val f = FurnitureBlock(type = type, center = center)
        updateActiveFloor { it.copy(furniture = it.furniture + f) }
    }

    fun addDimension(start: Point2D, end: Point2D, text: String?) {
        recordUndo()
        val dim = DimensionEntity(start = start, end = end, textOverride = text)
        updateActiveFloor { it.copy(dimensions = it.dimensions + dim) }
    }

    fun deleteSelected() {
        val sel = _selectedEntity.value ?: return
        recordUndo()
        updateActiveFloor { floor ->
            when (sel) {
                is SelectedEntity.WallSelection -> floor.copy(walls = floor.walls.filterNot { it.id == sel.wall.id })
                is SelectedEntity.RoomSelection -> {
                    val newRooms = floor.rooms.filterNot { it.id == sel.room.id }
                    floor.copy(rooms = newRooms, walls = GeometryEngine.generateWallsFromRooms(newRooms))
                }
                is SelectedEntity.DoorSelection -> floor.copy(doors = floor.doors.filterNot { it.id == sel.door.id })
                is SelectedEntity.WindowSelection -> floor.copy(windows = floor.windows.filterNot { it.id == sel.window.id })
                is SelectedEntity.StaircaseSelection -> floor.copy(staircases = floor.staircases.filterNot { it.id == sel.staircase.id })
                is SelectedEntity.FurnitureSelection -> floor.copy(furniture = floor.furniture.filterNot { it.id == sel.furniture.id })
                is SelectedEntity.DimensionSelection -> floor.copy(dimensions = floor.dimensions.filterNot { it.id == sel.dimension.id })
            }
        }
        _selectedEntity.value = null
    }

    private fun updateActiveFloor(transform: (CadFloor) -> CadFloor) {
        val proj = _activeProject.value
        val updatedFloors = proj.floors.map {
            if (it.id == proj.activeFloorId) transform(it) else it
        }
        _activeProject.value = proj.copy(floors = updatedFloors, dateModified = System.currentTimeMillis())
        saveCurrentProject()
    }

    fun showAreaSchedule(show: Boolean) {
        _showAreaDialog.value = show
    }

    fun showExport(show: Boolean) {
        _showExportDialog.value = show
    }

    fun openRoomEdit(room: RoomEntity) {
        _editingRoom.value = room
    }

    fun closeRoomEdit() {
        _editingRoom.value = null
    }

    fun importProjectFromJson(json: String): Boolean {
        return try {
            val proj = ProjectJsonSerializer.fromJson(json)
            _activeProject.value = proj
            saveCurrentProject()
            _currentScreen.value = Screen.CAD_EDITOR
            true
        } catch (_: Exception) {
            false
        }
    }
}
