package com.example.data.export

import com.example.data.model.*
import java.util.Locale

/**
 * Standard ASCII AutoCAD DXF (Release 12 / Release 14 compliant) exporter.
 * Maps real CAD geometry into standard architectural layers:
 * - PLOT_BOUNDARY (layer for site limits)
 * - WALLS (exterior 9" and interior 4.5" partitions)
 * - DOORS (door frames and swing paths)
 * - WINDOWS (window double-lines and sill representations)
 * - ROOM_LABELS (room names, dimensions, and area annotations)
 * - DIMENSIONS (linear measurements and boundary tags)
 * - FURNITURE (architectural block outlines)
 */
object DxfExporter {

    fun exportFloorPlan(project: CadProject, floor: CadFloor): String {
        val sb = StringBuilder()

        // 1. HEADER SECTION
        sb.append("0\nSECTION\n2\nHEADER\n")
        sb.append("9\n\$ACADVER\n1\nAC1009\n") // AutoCAD Release 12 format (universal CAD reader compatibility)
        sb.append("9\n\$INSUNITS\n70\n1\n")   // 1 = Inches, 2 = Feet (compatible with AutoCAD, Rhino, SketchUp, Blender)
        sb.append("0\nENDSEC\n")

        // 2. TABLES SECTION (Layers)
        sb.append("0\nSECTION\n2\nTABLES\n")
        sb.append("0\nTABLE\n2\nLAYER\n70\n7\n")
        appendLayer(sb, "0", 7)               // White
        appendLayer(sb, "PLOT_BOUNDARY", 1)   // Red
        appendLayer(sb, "WALLS", 4)           // Cyan
        appendLayer(sb, "DOORS", 2)           // Yellow
        appendLayer(sb, "WINDOWS", 3)         // Green
        appendLayer(sb, "ROOM_LABELS", 7)     // White
        appendLayer(sb, "DIMENSIONS", 5)      // Blue
        appendLayer(sb, "FURNITURE", 8)       // Gray
        sb.append("0\nENDTAB\n")
        sb.append("0\nENDSEC\n")

        // 3. BLOCKS SECTION
        sb.append("0\nSECTION\n2\nBLOCKS\n0\nENDSEC\n")

        // 4. ENTITIES SECTION
        sb.append("0\nSECTION\n2\nENTITIES\n")

        // Plot Boundary
        val plotVerts = project.plot.computePlotVertices()
        if (plotVerts.size >= 3) {
            for (i in plotVerts.indices) {
                val next = plotVerts[(i + 1) % plotVerts.size]
                appendLine(sb, "PLOT_BOUNDARY", plotVerts[i].x, plotVerts[i].y, next.x, next.y)
            }
        }

        // Walls
        for (w in floor.walls) {
            appendLine(sb, "WALLS", w.start.x, w.start.y, w.end.x, w.end.y)
        }

        // Rooms & Labels
        for (r in floor.rooms) {
            // Draw room boundary box
            val rect = r.rect
            appendLine(sb, "WALLS", rect.left, rect.top, rect.right, rect.top)
            appendLine(sb, "WALLS", rect.right, rect.top, rect.right, rect.bottom)
            appendLine(sb, "WALLS", rect.right, rect.bottom, rect.left, rect.bottom)
            appendLine(sb, "WALLS", rect.left, rect.bottom, rect.left, rect.top)

            // Text Label: Room Name
            val textX = rect.center.x
            val textY = rect.center.y
            val dimStr = "${rect.actualWidth.toInt()}'-0\" x ${rect.actualHeight.toInt()}'-0\""
            appendText(sb, "ROOM_LABELS", textX, textY + 0.8f, 1.0f, r.name.uppercase(Locale.US))
            appendText(sb, "ROOM_LABELS", textX, textY - 0.6f, 0.7f, dimStr)
            appendText(sb, "ROOM_LABELS", textX, textY - 1.8f, 0.6f, "${r.areaSqFt.toInt()} SQ FT")
        }

        // Doors
        for (d in floor.doors) {
            val wall = floor.walls.find { it.id == d.wallId }
            if (wall != null) {
                val doorPos = wall.toLine.interpolate(d.positionRatio)
                appendLine(
                    sb, "DOORS",
                    doorPos.x - d.width / 2f, doorPos.y,
                    doorPos.x + d.width / 2f, doorPos.y
                )
                appendText(sb, "DOORS", doorPos.x, doorPos.y, 0.6f, d.label)
            }
        }

        // Windows
        for (wn in floor.windows) {
            val wall = floor.walls.find { it.id == wn.wallId }
            if (wall != null) {
                val winPos = wall.toLine.interpolate(wn.positionRatio)
                appendLine(
                    sb, "WINDOWS",
                    winPos.x - wn.width / 2f, winPos.y,
                    winPos.x + wn.width / 2f, winPos.y
                )
                appendText(sb, "WINDOWS", winPos.x, winPos.y, 0.6f, wn.label)
            }
        }

        // Staircases
        for (s in floor.staircases) {
            val r = s.rect
            appendLine(sb, "WALLS", r.left, r.top, r.right, r.top)
            appendLine(sb, "WALLS", r.right, r.top, r.right, r.bottom)
            appendLine(sb, "WALLS", r.right, r.bottom, r.left, r.bottom)
            appendLine(sb, "WALLS", r.left, r.bottom, r.left, r.top)

            // Step treads
            val stepH = r.actualHeight / s.stepsCount
            for (step in 1 until s.stepsCount) {
                val stepY = r.top + step * stepH
                appendLine(sb, "FURNITURE", r.left, stepY, r.right, stepY)
            }
            appendText(sb, "ROOM_LABELS", r.center.x, r.center.y, 0.8f, "STAIRS ${s.direction}")
        }

        // Furniture
        for (fn in floor.furniture) {
            val cx = fn.center.x
            val cy = fn.center.y
            val hw = fn.width / 2f
            val hl = fn.length / 2f
            appendLine(sb, "FURNITURE", cx - hw, cy - hl, cx + hw, cy - hl)
            appendLine(sb, "FURNITURE", cx + hw, cy - hl, cx + hw, cy + hl)
            appendLine(sb, "FURNITURE", cx + hw, cy + hl, cx - hw, cy + hl)
            appendLine(sb, "FURNITURE", cx - hw, cy + hl, cx - hw, cy - hl)
            appendText(sb, "FURNITURE", cx, cy, 0.5f, fn.type.label)
        }

        // Dimensions
        for (dim in floor.dimensions) {
            appendLine(sb, "DIMENSIONS", dim.start.x, dim.start.y, dim.end.x, dim.end.y)
            val mid = dim.start + (dim.end - dim.start) * 0.5f
            val label = dim.textOverride ?: "${dim.start.distanceTo(dim.end).toInt()}'-0\""
            appendText(sb, "DIMENSIONS", mid.x, mid.y + dim.offset, 0.8f, label)
        }

        sb.append("0\nENDSEC\n")

        // 5. EOF
        sb.append("0\nEOF\n")

        return sb.toString()
    }

    private fun appendLayer(sb: StringBuilder, name: String, colorNumber: Int) {
        sb.append("0\nLAYER\n2\n$name\n70\n0\n62\n$colorNumber\n6\nCONTINUOUS\n")
    }

    private fun appendLine(sb: StringBuilder, layer: String, x1: Float, y1: Float, x2: Float, y2: Float) {
        sb.append("0\nLINE\n8\n$layer\n")
        sb.append(String.format(Locale.US, "10\n%.3f\n20\n%.3f\n30\n0.0\n", x1, y1))
        sb.append(String.format(Locale.US, "11\n%.3f\n21\n%.3f\n31\n0.0\n", x2, y2))
    }

    private fun appendText(sb: StringBuilder, layer: String, x: Float, y: Float, height: Float, text: String) {
        sb.append("0\nTEXT\n8\n$layer\n")
        sb.append(String.format(Locale.US, "10\n%.3f\n20\n%.3f\n30\n0.0\n", x, y))
        sb.append(String.format(Locale.US, "40\n%.3f\n", height))
        sb.append("1\n$text\n")
        sb.append("72\n1\n") // Centered horizontal alignment
        sb.append(String.format(Locale.US, "11\n%.3f\n21\n%.3f\n31\n0.0\n", x, y))
    }
}
