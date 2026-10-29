package com.example.data.model

import java.util.UUID

enum class WallType(val label: String, val thicknessFeet: Float) {
    EXTERIOR_9_INCH("Exterior Wall (9\")", 0.75f),
    INTERIOR_4_5_INCH("Interior Partition (4.5\")", 0.375f),
    BOUNDARY_WALL("Boundary Wall (9\")", 0.75f)
}

enum class RoomType(val displayName: String, val defaultColorHex: Long) {
    BEDROOM("Bedroom", 0xFFE0F2FE),
    MASTER_BEDROOM("Master Bedroom", 0xFFBAE6FD),
    DRAWING_ROOM("Drawing Room", 0xFFFEF3C7),
    TV_LOUNGE("TV Lounge / Living", 0xFFDCFCE7),
    KITCHEN("Kitchen", 0xFFFFEDD5),
    ATTACHED_BATH("Attached Bath", 0xFFE2E8F0),
    COMMON_BATH("Common Bath", 0xFFE2E8F0),
    POWDER_ROOM("Powder Room", 0xFFF1F5F9),
    DINING_ROOM("Dining Room", 0xFFFEF9C3),
    CAR_PORCH("Car Porch", 0xFFF3F4F6),
    LAWN_COURTYARD("Lawn / Courtyard", 0xFFD1FAE5),
    VENTILATION_OTS("OTS (Open To Sky)", 0xFFE0E7FF),
    STAIRCASE_ROOM("Staircase Lobby", 0xFFEDE9FE),
    STORE_ROOM("Store", 0xFFF3E8FF),
    LAUNDRY("Laundry", 0xFFF1F5F9),
    BALCONY_TERRACE("Balcony / Terrace", 0xFFCCFBF1),
    CORRIDOR("Passage / Corridor", 0xFFF8FAFC),
    CUSTOM("Custom Room", 0xFFF8FAFC)
}

data class Wall(
    val id: String = UUID.randomUUID().toString(),
    val start: Point2D,
    val end: Point2D,
    val thickness: Float = WallType.EXTERIOR_9_INCH.thicknessFeet,
    val wallType: WallType = WallType.EXTERIOR_9_INCH,
    val isExterior: Boolean = true,
    val layer: String = "WALLS"
) {
    val length: Float get() = start.distanceTo(end)
    val toLine: Line2D get() = Line2D(start, end)
}

data class RoomEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: RoomType,
    val rect: Rect2D,
    val floorId: String = "ground_floor",
    val tag: String = "",
    val targetWidth: Float = rect.actualWidth,
    val targetLength: Float = rect.actualHeight,
    val colorHex: Long = type.defaultColorHex,
    val isCustomPlaced: Boolean = false
) {
    val areaSqFt: Float get() = rect.area
}

data class Door(
    val id: String = UUID.randomUUID().toString(),
    val wallId: String,
    val positionRatio: Float = 0.5f,
    val width: Float = 3.5f,
    val swingInside: Boolean = true,
    val swingLeft: Boolean = true,
    val label: String = "D1"
)

data class WindowEntity(
    val id: String = UUID.randomUUID().toString(),
    val wallId: String,
    val positionRatio: Float = 0.5f,
    val width: Float = 4.0f,
    val label: String = "W1",
    val isVentilator: Boolean = false
)

data class Staircase(
    val id: String = UUID.randomUUID().toString(),
    val rect: Rect2D,
    val stepsCount: Int = 18,
    val direction: String = "UP",
    val style: String = "Dogleg"
)

enum class FurnitureType(val label: String, val defaultWidth: Float, val defaultLength: Float) {
    BED_DOUBLE("Double Bed (King)", 6.5f, 6.5f),
    BED_SINGLE("Single Bed", 3.5f, 6.5f),
    SOFA_SET("Lounge Sofa Set", 8.0f, 6.0f),
    SOFA_3SEATER("3-Seater Sofa", 7.0f, 3.0f),
    DINING_TABLE("Dining Table (6-Seat)", 4.0f, 6.5f),
    KITCHEN_COUNTER("Kitchen Counter / Sink", 2.0f, 8.0f),
    CAR_SEDAN("Car / Sedan", 6.0f, 15.0f),
    SANITARY_WC("WC / Commode", 2.0f, 2.5f),
    SANITARY_VANITY("Washbasin / Vanity", 2.0f, 3.0f)
}

data class FurnitureBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: FurnitureType,
    val center: Point2D,
    val rotationDegrees: Float = 0f,
    val width: Float = type.defaultWidth,
    val length: Float = type.defaultLength
)

data class DimensionEntity(
    val id: String = UUID.randomUUID().toString(),
    val start: Point2D,
    val end: Point2D,
    val offset: Float = 1.5f,
    val textOverride: String? = null
)
