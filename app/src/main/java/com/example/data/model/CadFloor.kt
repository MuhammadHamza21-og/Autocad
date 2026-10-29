package com.example.data.model

import java.util.UUID

data class CadFloor(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Ground Floor",
    val level: Int = 0, // -1: Basement, 0: Ground, 1: First, 2: Second, 3: Roof
    val walls: List<Wall> = emptyList(),
    val rooms: List<RoomEntity> = emptyList(),
    val doors: List<Door> = emptyList(),
    val windows: List<WindowEntity> = emptyList(),
    val staircases: List<Staircase> = emptyList(),
    val furniture: List<FurnitureBlock> = emptyList(),
    val dimensions: List<DimensionEntity> = emptyList()
) {
    val coveredAreaSqFt: Float
        get() = rooms.filter {
            it.type != RoomType.LAWN_COURTYARD && it.type != RoomType.VENTILATION_OTS
        }.sumOf { it.areaSqFt.toDouble() }.toFloat()

    val openAreaSqFt: Float
        get() = rooms.filter {
            it.type == RoomType.LAWN_COURTYARD || it.type == RoomType.VENTILATION_OTS
        }.sumOf { it.areaSqFt.toDouble() }.toFloat()
}

data class CadProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Residential Villa Plan",
    val clientName: String = "Mr. Ahmed Khan",
    val siteLocation: String = "DHA Phase 6, Lahore",
    val drawingTitle: String = "ARCHITECTURAL FLOOR PLAN",
    val drawingNumber: String = "ASD-2026-001",
    val revision: String = "R-0",
    val notes: String = "All dimensions are in feet and inches. Verify structural coordinates before excavation.",
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val unitSystem: UnitSystem = UnitSystem.FEET_INCHES,
    val marlaConvention: MarlaConvention = MarlaConvention.LAHORE_COMMERCIAL,
    val plot: PlotGeometry = PlotGeometry(),
    val requirements: BuildingRequirements = BuildingRequirements(),
    val floors: List<CadFloor> = listOf(CadFloor(id = "ground_floor", name = "Ground Floor", level = 0)),
    val activeFloorId: String = "ground_floor"
) {
    val activeFloor: CadFloor
        get() = floors.find { it.id == activeFloorId } ?: floors.firstOrNull() ?: CadFloor()

    val totalCoveredAreaSqFt: Float
        get() = floors.sumOf { it.coveredAreaSqFt.toDouble() }.toFloat()

    val groundCoveragePercentage: Float
        get() {
            val plotArea = plot.totalAreaSqFt
            if (plotArea <= 0.0) return 0f
            val groundFloor = floors.find { it.level == 0 } ?: return 0f
            return ((groundFloor.coveredAreaSqFt / plotArea) * 100f).toFloat().coerceIn(0f, 100f)
        }
}
