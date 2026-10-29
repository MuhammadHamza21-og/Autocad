package com.example.data.database

import com.example.data.model.*
import com.example.engine.FloorPlanGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CadRepository(private val dao: CadProjectDao) {

    val allProjects: Flow<List<CadProjectSummary>> = dao.getAllProjects().map { entities ->
        entities.map {
            CadProjectSummary(
                id = it.id,
                name = it.name,
                clientName = it.clientName,
                siteLocation = it.siteLocation,
                dateModified = it.dateModified,
                plotDimensionsSummary = it.plotDimensionsSummary,
                marlaSummary = it.marlaSummary
            )
        }
    }

    suspend fun getProject(id: String): CadProject? {
        val entity = dao.getProjectById(id) ?: return null
        return try {
            ProjectJsonSerializer.fromJson(entity.jsonContent)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveProject(project: CadProject) {
        val plotDim = "${project.plot.frontWidth.toInt()}' × ${project.plot.leftLength.toInt()}'"
        val areaReport = UnitConverter.calculateAreaReport(
            project.plot.totalAreaSqFt,
            project.marlaConvention
        )
        val json = ProjectJsonSerializer.toJson(project)
        val entity = CadProjectEntity(
            id = project.id,
            name = project.name,
            clientName = project.clientName,
            siteLocation = project.siteLocation,
            dateModified = System.currentTimeMillis(),
            plotDimensionsSummary = plotDim,
            marlaSummary = areaReport.formattedSummary,
            jsonContent = json
        )
        dao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) {
        dao.deleteProjectById(id)
    }

    suspend fun seedInitialProjectsIfEmpty() {
        // Build 5 Marla default
        val p5Marla = createDefault5Marla()
        saveProject(p5Marla)

        // Build 10 Marla default
        val p10Marla = createDefault10Marla()
        saveProject(p10Marla)

        // Build 1 Kanal default
        val p1Kanal = createDefault1Kanal()
        saveProject(p1Kanal)
    }

    companion object {
        fun createDefault5Marla(): CadProject {
            val plot = PlotGeometry(
                shapeType = PlotShapeType.RECTANGLE,
                frontWidth = 25f,
                backWidth = 25f,
                leftLength = 45f,
                rightLength = 45f,
                frontSetback = 4.0f,
                rearSetback = 3.0f,
                roadFacingSide = "Front (South)"
            )
            val reqs = BuildingRequirements(
                style = ArchitecturalStyle.MODERN_PAKISTANI,
                floorsCount = 2
            )
            val candidates = FloorPlanGenerator.generateAlternatives(plot, reqs)
            val primaryFloor = candidates.first().floor

            return CadProject(
                id = "demo_5_marla",
                name = "5 Marla Smart Villa (25' × 45')",
                clientName = "Mian Tariq",
                siteLocation = "Johar Town, Lahore",
                drawingTitle = "5 MARLA GROUND FLOOR PLAN",
                drawingNumber = "ASD-5M-01",
                plot = plot,
                requirements = reqs,
                floors = listOf(primaryFloor),
                activeFloorId = primaryFloor.id
            )
        }

        fun createDefault10Marla(): CadProject {
            val plot = PlotGeometry(
                shapeType = PlotShapeType.RECTANGLE,
                frontWidth = 35f,
                backWidth = 35f,
                leftLength = 65f,
                rightLength = 65f,
                frontSetback = 5.0f,
                rearSetback = 4.0f,
                roadFacingSide = "Front (East)"
            )
            val reqs = BuildingRequirements(
                style = ArchitecturalStyle.MODERN_PAKISTANI,
                floorsCount = 2
            )
            val candidates = FloorPlanGenerator.generateAlternatives(plot, reqs)
            val primaryFloor = candidates.first().floor

            return CadProject(
                id = "demo_10_marla",
                name = "10 Marla Modern House (35' × 65')",
                clientName = "Chaudhry Hameed",
                siteLocation = "DHA Phase 5, Lahore",
                drawingTitle = "10 MARLA ARCHITECTURAL PLAN",
                drawingNumber = "ASD-10M-01",
                plot = plot,
                requirements = reqs,
                floors = listOf(primaryFloor),
                activeFloorId = primaryFloor.id
            )
        }

        fun createDefault1Kanal(): CadProject {
            val plot = PlotGeometry(
                shapeType = PlotShapeType.RECTANGLE,
                frontWidth = 50f,
                backWidth = 50f,
                leftLength = 90f,
                rightLength = 90f,
                frontSetback = 8.0f,
                rearSetback = 5.0f,
                roadFacingSide = "Front (North)"
            )
            val reqs = BuildingRequirements(
                style = ArchitecturalStyle.CONTEMPORARY_LUXURY,
                floorsCount = 2
            )
            val candidates = FloorPlanGenerator.generateAlternatives(plot, reqs)
            val primaryFloor = candidates.first().floor

            return CadProject(
                id = "demo_1_kanal",
                name = "1 Kanal Luxury Residence (50' × 90')",
                clientName = "Sheikh Mansoor",
                siteLocation = "Bahria Town, Sector C",
                drawingTitle = "1 KANAL GROUND FLOOR CAD DRAFT",
                drawingNumber = "ASD-1K-01",
                plot = plot,
                requirements = reqs,
                floors = listOf(primaryFloor),
                activeFloorId = primaryFloor.id
            )
        }
    }
}

data class CadProjectSummary(
    val id: String,
    val name: String,
    val clientName: String,
    val siteLocation: String,
    val dateModified: Long,
    val plotDimensionsSummary: String,
    val marlaSummary: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date(dateModified))
}
