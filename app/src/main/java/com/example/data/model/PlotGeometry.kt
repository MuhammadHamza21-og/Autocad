package com.example.data.model

enum class PlotShapeType(val label: String) {
    RECTANGLE("Standard Rectangular Plot"),
    SQUARE("Square Plot"),
    TRAPEZOID("Trapezoidal Plot (Corner / Slanted Front)"),
    IRREGULAR_4_SIDED("Irregular Quadrilateral (Surveyed Diagonals)"),
    CUSTOM_POLYGON("Custom Surveyed Polygon")
}

data class PlotGeometry(
    val shapeType: PlotShapeType = PlotShapeType.RECTANGLE,
    val frontWidth: Float = 30f,
    val backWidth: Float = 30f,
    val leftLength: Float = 60f,
    val rightLength: Float = 60f,
    val diagonalLength: Float? = null,
    val roadFacingSide: String = "Front (South)",
    val northAngleDegrees: Float = 0f,
    val frontSetback: Float = 5.0f,
    val rearSetback: Float = 4.0f,
    val leftSetback: Float = 0.0f,
    val rightSetback: Float = 0.0f,
    val customVertices: List<Point2D> = emptyList()
) {
    /**
     * Generates the boundary polygon vertices clockwise starting from Top-Left.
     * In plot coordinate space, Y increases downwards:
     * (0,0) is Rear-Left, (backWidth, 0) is Rear-Right,
     * (frontWidth, length) is Front-Right, (0, length) is Front-Left.
     */
    fun computePlotVertices(): List<Point2D> {
        return if (customVertices.size >= 3) {
            customVertices
        } else {
            when (shapeType) {
                PlotShapeType.RECTANGLE, PlotShapeType.SQUARE -> {
                    listOf(
                        Point2D(0f, 0f),
                        Point2D(frontWidth, 0f),
                        Point2D(frontWidth, leftLength),
                        Point2D(0f, leftLength)
                    )
                }
                PlotShapeType.TRAPEZOID -> {
                    val avgLen = (leftLength + rightLength) / 2f
                    listOf(
                        Point2D(0f, 0f),
                        Point2D(backWidth, 0f),
                        Point2D(frontWidth, avgLen),
                        Point2D(0f, avgLen)
                    )
                }
                PlotShapeType.IRREGULAR_4_SIDED -> {
                    listOf(
                        Point2D(0f, 0f),
                        Point2D(backWidth, 0f),
                        Point2D(frontWidth, rightLength),
                        Point2D(0f, leftLength)
                    )
                }
                else -> {
                    listOf(
                        Point2D(0f, 0f),
                        Point2D(frontWidth, 0f),
                        Point2D(frontWidth, leftLength),
                        Point2D(0f, leftLength)
                    )
                }
            }
        }
    }

    val polygon: Polygon2D get() = Polygon2D(computePlotVertices())
    val totalAreaSqFt: Double get() = polygon.calculateArea()

    /**
     * Compute buildable area rectangle respecting front, rear, left, right setbacks.
     */
    fun computeBuildableRect(): Rect2D {
        val minX = leftSetback
        val maxX = frontWidth - rightSetback
        val minY = rearSetback
        val maxY = leftLength - frontSetback
        val w = (maxX - minX).coerceAtLeast(10f)
        val h = (maxY - minY).coerceAtLeast(10f)
        return Rect2D(minX, minY, w, h)
    }
}
