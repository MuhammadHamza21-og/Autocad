package com.example.engine

import com.example.data.model.*
import kotlin.math.*

object GeometryEngine {

    /**
     * Snap a given coordinate to nearest grid point.
     */
    fun snapToGrid(point: Point2D, gridSize: Float = 0.5f): Point2D {
        if (gridSize <= 0f) return point
        val sx = round(point.x / gridSize) * gridSize
        val sy = round(point.y / gridSize) * gridSize
        return Point2D(sx, sy)
    }

    /**
     * Snaps point to nearby existing endpoints (walls, rooms), midpoints, or grid.
     */
    fun snapPoint(
        point: Point2D,
        walls: List<Wall>,
        rooms: List<RoomEntity>,
        gridSize: Float = 0.5f,
        snapThreshold: Float = 1.0f,
        snapToGrid: Boolean = true,
        snapToObjects: Boolean = true
    ): Pair<Point2D, String?> {
        if (snapToObjects) {
            // Check wall endpoints
            for (w in walls) {
                if (point.distanceTo(w.start) <= snapThreshold) {
                    return Pair(w.start, "Endpoint")
                }
                if (point.distanceTo(w.end) <= snapThreshold) {
                    return Pair(w.end, "Endpoint")
                }
                val mid = w.toLine.midpoint
                if (point.distanceTo(mid) <= snapThreshold) {
                    return Pair(mid, "Midpoint")
                }
            }

            // Check room corners
            for (r in rooms) {
                val corners = listOf(
                    Point2D(r.rect.left, r.rect.top),
                    Point2D(r.rect.right, r.rect.top),
                    Point2D(r.rect.right, r.rect.bottom),
                    Point2D(r.rect.left, r.rect.bottom)
                )
                for (c in corners) {
                    if (point.distanceTo(c) <= snapThreshold) {
                        return Pair(c, "Corner")
                    }
                }
            }
        }

        if (snapToGrid) {
            val snapped = snapToGrid(point, gridSize)
            if (point.distanceTo(snapped) <= snapThreshold * 1.5f) {
                return Pair(snapped, "Grid")
            }
        }

        return Pair(point, null)
    }

    /**
     * Applies Orthogonal lock (horizontal or vertical constraint) if ortho mode is on.
     */
    fun applyOrtho(start: Point2D, current: Point2D): Point2D {
        val dx = abs(current.x - start.x)
        val dy = abs(current.y - start.y)
        return if (dx >= dy) {
            Point2D(current.x, start.y)
        } else {
            Point2D(start.x, current.y)
        }
    }

    /**
     * Checks quadrilateral geometric validity using four side lengths and diagonal.
     * Bretschneider's or triangle inequality check:
     * Triangle 1: sides a, b, diagonal d
     * Triangle 2: sides c, d2, diagonal d
     */
    data class QuadrilateralValidationResult(
        val isValid: Boolean,
        val calculatedAreaSqFt: Double,
        val message: String
    )

    fun validateQuadrilateral(
        front: Float,
        back: Float,
        left: Float,
        right: Float,
        diagonal: Float?
    ): QuadrilateralValidationResult {
        if (front <= 0 || back <= 0 || left <= 0 || right <= 0) {
            return QuadrilateralValidationResult(false, 0.0, "All 4 side lengths must be positive numbers.")
        }

        // Polygon inequality: No side can be >= sum of other 3
        val maxSide = max(max(front, back), max(left, right))
        val sumOther = (front + back + left + right) - maxSide
        if (maxSide >= sumOther) {
            return QuadrilateralValidationResult(
                false, 0.0,
                "Inconsistent dimensions: side length $maxSide exceeds sum of other sides ($sumOther)."
            )
        }

        if (diagonal != null && diagonal > 0) {
            // Triangle 1: left, back, diagonal
            if (left + back <= diagonal || left + diagonal <= back || back + diagonal <= left) {
                return QuadrilateralValidationResult(
                    false, 0.0,
                    "Invalid diagonal $diagonal for sides (Left: $left, Back: $back). Does not form a valid triangle."
                )
            }
            // Triangle 2: front, right, diagonal
            if (front + right <= diagonal || front + diagonal <= right || right + diagonal <= front) {
                return QuadrilateralValidationResult(
                    false, 0.0,
                    "Invalid diagonal $diagonal for sides (Front: $front, Right: $right). Does not form a valid triangle."
                )
            }

            // Heron's formula for each triangle
            val s1 = (left + back + diagonal) / 2.0
            val area1 = sqrt(s1 * (s1 - left) * (s1 - back) * (s1 - diagonal))

            val s2 = (front + right + diagonal) / 2.0
            val area2 = sqrt(s2 * (s2 - front) * (s2 - right) * (s2 - diagonal))

            val totalArea = area1 + area2
            return QuadrilateralValidationResult(
                true, totalArea,
                "Survey validated with diagonal ($diagonal ft). Calculated Area: ${round(totalArea)} sq ft."
            )
        }

        // Approximate if diagonal not provided
        val approxArea = ((front + back) / 2.0) * ((left + right) / 2.0)
        return QuadrilateralValidationResult(
            true, approxArea,
            "Calculated using trapezoidal approximation. Provide diagonal length for exact survey accuracy."
        )
    }

    /**
     * Converts a set of rooms into real CAD walls (walls along perimeters and shared internal boundaries).
     */
    fun generateWallsFromRooms(rooms: List<RoomEntity>): List<Wall> {
        val walls = mutableListOf<Wall>()
        val segments = mutableListOf<Line2D>()

        for (room in rooms) {
            val r = room.rect
            segments.add(Line2D(Point2D(r.left, r.top), Point2D(r.right, r.top)))
            segments.add(Line2D(Point2D(r.right, r.top), Point2D(r.right, r.bottom)))
            segments.add(Line2D(Point2D(r.right, r.bottom), Point2D(r.left, r.bottom)))
            segments.add(Line2D(Point2D(r.left, r.bottom), Point2D(r.left, r.top)))
        }

        // Deduplicate overlapping segments and identify exterior vs interior
        for (seg in segments) {
            // Count overlaps
            var count = 0
            for (other in segments) {
                if (isSegmentEquivalent(seg, other)) {
                    count++
                }
            }
            val isInterior = count > 1
            val wallType = if (isInterior) WallType.INTERIOR_4_5_INCH else WallType.EXTERIOR_9_INCH

            // Ensure not already added
            if (!walls.any { isSegmentEquivalent(it.toLine, seg) }) {
                walls.add(
                    Wall(
                        start = seg.start,
                        end = seg.end,
                        thickness = wallType.thicknessFeet,
                        wallType = wallType,
                        isExterior = !isInterior
                    )
                )
            }
        }
        return walls
    }

    private fun isSegmentEquivalent(l1: Line2D, l2: Line2D): Boolean {
        val eps = 0.2f
        val sameDir = l1.start.distanceTo(l2.start) < eps && l1.end.distanceTo(l2.end) < eps
        val oppDir = l1.start.distanceTo(l2.end) < eps && l1.end.distanceTo(l2.start) < eps
        return sameDir || oppDir
    }
}
