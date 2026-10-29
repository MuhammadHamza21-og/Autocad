package com.example.data.model

import kotlin.math.*

/**
 * 2D Point with geometric operations in CAD space (coordinates are in feet by default).
 */
data class Point2D(
    val x: Float = 0f,
    val y: Float = 0f
) {
    operator fun plus(other: Point2D): Point2D = Point2D(x + other.x, y + other.y)
    operator fun minus(other: Point2D): Point2D = Point2D(x - other.x, y - other.y)
    operator fun times(factor: Float): Point2D = Point2D(x * factor, y * factor)
    operator fun div(divisor: Float): Point2D = Point2D(x / divisor, y / divisor)

    fun distanceTo(other: Point2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun angleTo(other: Point2D): Float {
        val rad = atan2((other.y - y).toDouble(), (other.x - x).toDouble())
        var deg = Math.toDegrees(rad).toFloat()
        if (deg < 0) deg += 360f
        return deg
    }

    fun rotateAround(pivot: Point2D, degrees: Float): Point2D {
        val rad = Math.toRadians(degrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val dx = x - pivot.x
        val dy = y - pivot.y
        return Point2D(
            pivot.x + (dx * cosA - dy * sinA),
            pivot.y + (dx * sinA + dy * cosA)
        )
    }

    fun roundTo(decimals: Int = 2): Point2D {
        val factor = 10.0.pow(decimals).toFloat()
        return Point2D(
            round(x * factor) / factor,
            round(y * factor) / factor
        )
    }
}

data class Line2D(
    val start: Point2D,
    val end: Point2D
) {
    val length: Float get() = start.distanceTo(end)
    val angleDegrees: Float get() = start.angleTo(end)
    val midpoint: Point2D get() = Point2D((start.x + end.x) / 2f, (start.y + end.y) / 2f)

    fun interpolate(ratio: Float): Point2D {
        val clamped = ratio.coerceIn(0f, 1f)
        return Point2D(
            start.x + (end.x - start.x) * clamped,
            start.y + (end.y - start.y) * clamped
        )
    }

    fun distanceToPoint(pt: Point2D): Float {
        val l2 = (end.x - start.x).pow(2) + (end.y - start.y).pow(2)
        if (l2 == 0f) return start.distanceTo(pt)
        val t = (((pt.x - start.x) * (end.x - start.x) + (pt.y - start.y) * (end.y - start.y)) / l2).coerceIn(0f, 1f)
        val proj = Point2D(start.x + t * (end.x - start.x), start.y + t * (end.y - start.y))
        return pt.distanceTo(proj)
    }
}

data class Rect2D(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f
) {
    val left: Float get() = min(x, x + width)
    val right: Float get() = max(x, x + width)
    val top: Float get() = min(y, y + height)
    val bottom: Float get() = max(y, y + height)

    val actualWidth: Float get() = abs(width)
    val actualHeight: Float get() = abs(height)
    val area: Float get() = actualWidth * actualHeight

    val center: Point2D get() = Point2D(left + actualWidth / 2f, top + actualHeight / 2f)

    fun contains(pt: Point2D): Boolean =
        pt.x in left..right && pt.y in top..bottom

    fun overlaps(other: Rect2D): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    fun toPolygon(): Polygon2D = Polygon2D(
        listOf(
            Point2D(left, top),
            Point2D(right, top),
            Point2D(right, bottom),
            Point2D(left, bottom)
        )
    )
}

data class Polygon2D(
    val vertices: List<Point2D>
) {
    /**
     * Shoelace formula for accurate polygon area calculation.
     */
    fun calculateArea(): Double {
        if (vertices.size < 3) return 0.0
        var sum = 0.0
        val n = vertices.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            sum += (vertices[i].x.toDouble() * vertices[j].y.toDouble()) -
                    (vertices[j].x.toDouble() * vertices[i].y.toDouble())
        }
        return abs(sum) / 2.0
    }

    fun calculatePerimeter(): Float {
        if (vertices.size < 2) return 0f
        var perimeter = 0f
        val n = vertices.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            perimeter += vertices[i].distanceTo(vertices[j])
        }
        return perimeter
    }

    fun bounds(): Rect2D {
        if (vertices.isEmpty()) return Rect2D()
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (v in vertices) {
            minX = min(minX, v.x)
            maxX = max(maxX, v.x)
            minY = min(minY, v.y)
            maxY = max(maxY, v.y)
        }
        return Rect2D(minX, minY, maxX - minX, maxY - minY)
    }
}
