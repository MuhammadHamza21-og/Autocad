package com.example

import com.example.data.export.DxfExporter
import com.example.data.model.*
import com.example.engine.FloorPlanGenerator
import com.example.engine.GeometryEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testShoelacePolygonArea() {
        val rectPoly = Polygon2D(
            listOf(
                Point2D(0f, 0f),
                Point2D(30f, 0f),
                Point2D(30f, 60f),
                Point2D(0f, 60f)
            )
        )
        val area = rectPoly.calculateArea()
        assertEquals(1800.0, area, 0.01)
    }

    @Test
    fun testPakistaniMarlaConversions() {
        // Test Lahore LDA convention (225 sq ft / Marla)
        val ldaMarla = MarlaConvention.LAHORE_COMMERCIAL.toMarla(2250.0)
        assertEquals(10.0, ldaMarla, 0.001)

        val ldaKanal = MarlaConvention.LAHORE_COMMERCIAL.toKanal(4500.0)
        assertEquals(1.0, ldaKanal, 0.001)

        // Test Traditional Revenue convention (272.25 sq ft / Marla)
        val revMarla = MarlaConvention.TRADITIONAL_REVENUE.toMarla(5445.0)
        assertEquals(20.0, revMarla, 0.001)

        val revKanal = MarlaConvention.TRADITIONAL_REVENUE.toKanal(5445.0)
        assertEquals(1.0, revKanal, 0.001)
    }

    @Test
    fun testQuadrilateralGeometryValidation() {
        // Valid rectangular plot
        val validRect = GeometryEngine.validateQuadrilateral(30f, 30f, 60f, 60f, null)
        assertTrue(validRect.isValid)
        assertEquals(1800.0, validRect.calculatedAreaSqFt, 0.1)

        // Invalid sides violating polygon inequality
        val invalid = GeometryEngine.validateQuadrilateral(100f, 10f, 10f, 10f, null)
        assertFalse(invalid.isValid)

        // Surveyed diagonal verification (3-4-5 right triangle pair = 30-40-50)
        val diagonalValid = GeometryEngine.validateQuadrilateral(30f, 30f, 40f, 40f, 50f)
        assertTrue(diagonalValid.isValid)
        assertEquals(1200.0, diagonalValid.calculatedAreaSqFt, 0.1)
    }

    @Test
    fun testFloorPlanGeneratorProducesDistinctAlternatives() {
        val plot = PlotGeometry(frontWidth = 30f, backWidth = 30f, leftLength = 60f, rightLength = 60f)
        val reqs = BuildingRequirements(style = ArchitecturalStyle.MODERN_PAKISTANI, floorsCount = 2)
        val alternatives = FloorPlanGenerator.generateAlternatives(plot, reqs)

        assertTrue(alternatives.size >= 3)
        // Ensure styles are genuinely distinct
        val styles = alternatives.map { it.style }
        assertEquals(styles.size, styles.distinct().size)

        for (alt in alternatives) {
            assertTrue(alt.floor.rooms.isNotEmpty())
            assertTrue(alt.floor.walls.isNotEmpty())
            assertTrue(alt.coveredAreaSqFt > 0f)
            assertTrue(alt.score in 80..100)
        }
    }

    @Test
    fun testDxfExportContainsStandardCadLayers() {
        val plot = PlotGeometry(frontWidth = 25f, backWidth = 25f, leftLength = 45f, rightLength = 45f)
        val reqs = BuildingRequirements()
        val alts = FloorPlanGenerator.generateAlternatives(plot, reqs)
        val floor = alts.first().floor
        val project = CadProject(plot = plot, floors = listOf(floor))

        val dxf = DxfExporter.exportFloorPlan(project, floor)
        assertTrue(dxf.contains("AC1009")) // AutoCAD R12 header
        assertTrue(dxf.contains("WALLS"))
        assertTrue(dxf.contains("DOORS"))
        assertTrue(dxf.contains("ROOM_LABELS"))
        assertTrue(dxf.contains("EOF"))
    }
}
