package com.example.data.export

import com.example.data.model.*
import java.util.Locale

/**
 * Exports architectural floor plan as standard SVG vector graphic.
 */
object SvgExporter {

    fun exportFloorPlan(project: CadProject, floor: CadFloor): String {
        val plotBounds = project.plot.polygon.bounds()
        val margin = 8f
        val minX = plotBounds.left - margin
        val minY = plotBounds.top - margin
        val width = plotBounds.actualWidth + margin * 2
        val height = plotBounds.actualHeight + margin * 2

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append(String.format(
            Locale.US,
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"%.2f %.2f %.2f %.2f\" width=\"100%%\" height=\"100%%\" style=\"background-color:#0D1B2A;\">\n",
            minX, minY, width, height
        ))

        // CSS Styles
        sb.append("""
            <defs>
              <style>
                .grid-line { stroke: #1E293B; stroke-width: 0.05; }
                .plot-line { stroke: #EF4444; stroke-width: 0.35; stroke-dasharray: 0.8,0.4; fill: none; }
                .wall-ext { stroke: #F8FAFC; stroke-width: 0.65; stroke-linecap: square; }
                .wall-int { stroke: #CBD5E1; stroke-width: 0.35; stroke-linecap: square; }
                .room-fill { fill: #1E293B; fill-opacity: 0.7; stroke: #475569; stroke-width: 0.15; }
                .text-title { font-family: sans-serif; font-weight: bold; font-size: 0.9px; fill: #38BDF8; text-anchor: middle; }
                .text-dim { font-family: sans-serif; font-size: 0.65px; fill: #94A3B8; text-anchor: middle; }
                .text-area { font-family: sans-serif; font-size: 0.55px; fill: #64748B; text-anchor: middle; }
                .door-arc { stroke: #F59E0B; stroke-width: 0.2; fill: none; stroke-dasharray: 0.4,0.2; }
                .window-line { stroke: #38BDF8; stroke-width: 0.25; }
                .furniture-line { stroke: #94A3B8; stroke-width: 0.15; fill: #334155; fill-opacity: 0.4; }
                .stair-tread { stroke: #E2E8F0; stroke-width: 0.15; }
                .dim-line { stroke: #06B6D4; stroke-width: 0.15; }
                .dim-text { font-family: monospace; font-size: 0.65px; fill: #22D3EE; text-anchor: middle; }
              </style>
            </defs>
        """.trimIndent()).append("\n")

        // 1. Plot boundary polygon
        val plotVerts = project.plot.computePlotVertices()
        if (plotVerts.size >= 3) {
            val ptsStr = plotVerts.joinToString(" ") { String.format(Locale.US, "%.2f,%.2f", it.x, it.y) }
            sb.append("<polygon points=\"$ptsStr\" class=\"plot-line\" />\n")
        }

        // 2. Rooms
        for (r in floor.rooms) {
            val rect = r.rect
            sb.append(String.format(
                Locale.US,
                "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" class=\"room-fill\" />\n",
                rect.left, rect.top, rect.actualWidth, rect.actualHeight
            ))

            // Room text
            val cx = rect.center.x
            val cy = rect.center.y
            sb.append(String.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" class=\"text-title\">%s</text>\n", cx, cy - 0.4f, r.name))
            sb.append(String.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" class=\"text-dim\">%d'-0\" × %d'-0\"</text>\n", cx, cy + 0.4f, rect.actualWidth.toInt(), rect.actualHeight.toInt()))
            sb.append(String.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" class=\"text-area\">%d SQ FT</text>\n", cx, cy + 1.1f, r.areaSqFt.toInt()))
        }

        // 3. Walls
        for (w in floor.walls) {
            val cls = if (w.isExterior) "wall-ext" else "wall-int"
            sb.append(String.format(
                Locale.US,
                "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" class=\"$cls\" />\n",
                w.start.x, w.start.y, w.end.x, w.end.y
            ))
        }

        // 4. Doors & Windows
        for (d in floor.doors) {
            val wall = floor.walls.find { it.id == d.wallId } ?: continue
            val pos = wall.toLine.interpolate(d.positionRatio)
            sb.append(String.format(
                Locale.US,
                "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"0.4\" fill=\"#F59E0B\" />\n",
                pos.x, pos.y
            ))
        }

        for (wn in floor.windows) {
            val wall = floor.walls.find { it.id == wn.wallId } ?: continue
            val pos = wall.toLine.interpolate(wn.positionRatio)
            sb.append(String.format(
                Locale.US,
                "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"0.4\" fill=\"#38BDF8\" />\n",
                pos.x - wn.width / 2f, pos.y - 0.2f, wn.width
            ))
        }

        // 5. Staircases
        for (s in floor.staircases) {
            val r = s.rect
            sb.append(String.format(
                Locale.US,
                "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"none\" stroke=\"#E2E8F0\" stroke-width=\"0.2\" />\n",
                r.left, r.top, r.actualWidth, r.actualHeight
            ))
            val stepH = r.actualHeight / s.stepsCount
            for (i in 1 until s.stepsCount) {
                val sy = r.top + i * stepH
                sb.append(String.format(
                    Locale.US,
                    "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" class=\"stair-tread\" />\n",
                    r.left, sy, r.right, sy
                ))
            }
        }

        // 6. Furniture
        for (fn in floor.furniture) {
            val cx = fn.center.x
            val cy = fn.center.y
            val hw = fn.width / 2f
            val hl = fn.length / 2f
            sb.append(String.format(
                Locale.US,
                "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" rx=\"0.2\" class=\"furniture-line\" />\n",
                cx - hw, cy - hl, fn.width, fn.length
            ))
        }

        // 7. Dimensions
        for (dim in floor.dimensions) {
            sb.append(String.format(
                Locale.US,
                "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" class=\"dim-line\" />\n",
                dim.start.x, dim.start.y, dim.end.x, dim.end.y
            ))
            val mid = dim.start + (dim.end - dim.start) * 0.5f
            val label = dim.textOverride ?: "${dim.start.distanceTo(dim.end).toInt()}'-0\""
            sb.append(String.format(
                Locale.US,
                "<text x=\"%.2f\" y=\"%.2f\" class=\"dim-text\">%s</text>\n",
                mid.x, mid.y + dim.offset, label
            ))
        }

        sb.append("</svg>")
        return sb.toString()
    }
}
