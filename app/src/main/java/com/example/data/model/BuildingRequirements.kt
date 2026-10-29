package com.example.data.model

import java.util.UUID

enum class ArchitecturalStyle(
    val title: String,
    val description: String,
    val highlights: String
) {
    MODERN_PAKISTANI(
        title = "Modern Pakistani House",
        description = "Porch entrance, guest drawing room with powder bath, central TV lounge hub connecting rear bedrooms and open/closed kitchen.",
        highlights = "Spacious TV lounge, rear OTS ventilation shafts, wide porch"
    ),
    TRADITIONAL_COURTYARD(
        title = "Traditional Courtyard Design",
        description = "Centered around a light-filled courtyard/open patio, providing privacy zoning between formal guest and private family zones.",
        highlights = "Interior courtyard, shaded veranda, separated guest wing"
    ),
    SPACE_EFFICIENT(
        title = "Space-Efficient Smart Layout",
        description = "Minimized circulation corridors, optimized room aspect ratios, combined living-dining, maximum usable floor area.",
        highlights = "Zero wasted corridor space, compact service ducts, multifunctional core"
    ),
    CONTEMPORARY_LUXURY(
        title = "Contemporary Luxury Villa",
        description = "Double-height lobby, expansive master suites with walk-in closets, dirty kitchen/pantry, double car porch, front lawn.",
        highlights = "Front landscaped lawn, wide 2-car porch, lavish master suites"
    ),
    RENTAL_INDEPENDENT(
        title = "Rental-Unit Layout (Dual Entry)",
        description = "Independent exterior staircase placed directly in the porch for upper floor rental tenants without disturbing ground family privacy.",
        highlights = "Separate porch staircase, independent meters, self-contained floors"
    ),
    CUSTOM(
        title = "Custom User Specification",
        description = "Customized parameters tailored to user specified room schedule and zoning.",
        highlights = "Configurable zones"
    )
}

data class RoomRequirement(
    val id: String = UUID.randomUUID().toString(),
    val type: RoomType,
    val name: String = type.displayName,
    val count: Int = 1,
    val targetWidthFt: Float = 14f,
    val targetLengthFt: Float = 16f,
    val floorAssignment: String = "Ground Floor",
    val requiresAttachedBath: Boolean = false,
    val preferredZone: String = "Rear" // "Front", "Center", "Rear", "Service"
)

data class BuildingRequirements(
    val style: ArchitecturalStyle = ArchitecturalStyle.MODERN_PAKISTANI,
    val floorsCount: Int = 2,
    val roomRequirements: List<RoomRequirement> = defaultRequirements(),
    val parkingBays: Int = 1,
    val staircaseLocation: String = "Porch / Front Lobby",
    val ventilationPref: String = "Rear OTS shafts & front lawn",
    val notes: String = ""
) {
    companion object {
        fun defaultRequirements(): List<RoomRequirement> = listOf(
            RoomRequirement(
                type = RoomType.DRAWING_ROOM,
                name = "Drawing Room",
                targetWidthFt = 12f,
                targetLengthFt = 14f,
                preferredZone = "Front",
                requiresAttachedBath = true
            ),
            RoomRequirement(
                type = RoomType.TV_LOUNGE,
                name = "TV Lounge / Living",
                targetWidthFt = 16f,
                targetLengthFt = 18f,
                preferredZone = "Center"
            ),
            RoomRequirement(
                type = RoomType.KITCHEN,
                name = "Kitchen",
                targetWidthFt = 10f,
                targetLengthFt = 12f,
                preferredZone = "Center"
            ),
            RoomRequirement(
                type = RoomType.MASTER_BEDROOM,
                name = "Master Bedroom",
                targetWidthFt = 14f,
                targetLengthFt = 16f,
                preferredZone = "Rear",
                requiresAttachedBath = true
            ),
            RoomRequirement(
                type = RoomType.BEDROOM,
                name = "Bedroom 2",
                targetWidthFt = 12f,
                targetLengthFt = 14f,
                preferredZone = "Rear",
                requiresAttachedBath = true
            ),
            RoomRequirement(
                type = RoomType.CAR_PORCH,
                name = "Car Porch",
                targetWidthFt = 14f,
                targetLengthFt = 18f,
                preferredZone = "Front"
            ),
            RoomRequirement(
                type = RoomType.LAWN_COURTYARD,
                name = "Front Lawn",
                targetWidthFt = 12f,
                targetLengthFt = 6f,
                preferredZone = "Front"
            ),
            RoomRequirement(
                type = RoomType.VENTILATION_OTS,
                name = "Rear OTS Duct",
                targetWidthFt = 6f,
                targetLengthFt = 4f,
                preferredZone = "Rear"
            )
        )
    }
}
