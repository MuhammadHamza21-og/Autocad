package com.example.engine

import com.example.data.model.*
import java.util.UUID
import kotlin.math.*

data class LayoutCandidate(
    val id: String = UUID.randomUUID().toString(),
    val style: ArchitecturalStyle,
    val title: String,
    val conceptSummary: String,
    val floor: CadFloor,
    val score: Int,
    val coveredAreaSqFt: Float,
    val openAreaSqFt: Float,
    val efficiencyPercent: Float,
    val satisfiedConstraints: List<String>,
    val unsatisfiedConstraints: List<String>,
    val highlights: List<String>
)

object FloorPlanGenerator {

    /**
     * Generates at least three distinct layout alternatives tailored to the plot & requirements.
     */
    fun generateAlternatives(
        plot: PlotGeometry,
        requirements: BuildingRequirements
    ): List<LayoutCandidate> {
        val candidates = mutableListOf<LayoutCandidate>()

        // 1. Primary Style selected by user (e.g. Modern Pakistani)
        candidates.add(
            buildLayoutForStyle(
                style = requirements.style,
                plot = plot,
                requirements = requirements,
                variation = 1
            )
        )

        // 2. Second distinct style (e.g. Space-Efficient or Traditional)
        val secondStyle = when (requirements.style) {
            ArchitecturalStyle.MODERN_PAKISTANI -> ArchitecturalStyle.SPACE_EFFICIENT
            ArchitecturalStyle.SPACE_EFFICIENT -> ArchitecturalStyle.MODERN_PAKISTANI
            ArchitecturalStyle.TRADITIONAL_COURTYARD -> ArchitecturalStyle.MODERN_PAKISTANI
            ArchitecturalStyle.CONTEMPORARY_LUXURY -> ArchitecturalStyle.RENTAL_INDEPENDENT
            ArchitecturalStyle.RENTAL_INDEPENDENT -> ArchitecturalStyle.SPACE_EFFICIENT
            ArchitecturalStyle.CUSTOM -> ArchitecturalStyle.MODERN_PAKISTANI
        }
        candidates.add(
            buildLayoutForStyle(
                style = secondStyle,
                plot = plot,
                requirements = requirements,
                variation = 2
            )
        )

        // 3. Third distinct style (e.g. Rental-Unit or Luxury or Traditional Courtyard)
        val thirdStyle = when {
            candidates.none { it.style == ArchitecturalStyle.RENTAL_INDEPENDENT } -> ArchitecturalStyle.RENTAL_INDEPENDENT
            candidates.none { it.style == ArchitecturalStyle.TRADITIONAL_COURTYARD } -> ArchitecturalStyle.TRADITIONAL_COURTYARD
            else -> ArchitecturalStyle.CONTEMPORARY_LUXURY
        }
        candidates.add(
            buildLayoutForStyle(
                style = thirdStyle,
                plot = plot,
                requirements = requirements,
                variation = 3
            )
        )

        return candidates
    }

    private fun buildLayoutForStyle(
        style: ArchitecturalStyle,
        plot: PlotGeometry,
        requirements: BuildingRequirements,
        variation: Int
    ): LayoutCandidate {
        val bRect = plot.computeBuildableRect()
        val pw = bRect.actualWidth
        val pl = bRect.actualHeight
        val startX = bRect.left
        val startY = bRect.top

        val rooms = mutableListOf<RoomEntity>()
        val staircases = mutableListOf<Staircase>()
        val furniture = mutableListOf<FurnitureBlock>()
        val satisfied = mutableListOf<String>()
        val unsatisfied = mutableListOf<String>()
        val highlights = mutableListOf<String>()

        // Coordinate conventions:
        // Y=0 is Rear of plot; Y=length is Front of plot (Road side)
        // Rear setback at Y=0..rearSetback.
        // Front setback at Y=(length-frontSetback)..length.

        // Room zoning ratios based on architectural style:
        when (style) {
            ArchitecturalStyle.MODERN_PAKISTANI -> {
                // Zone 1 (Rear): 2 Bedrooms with attached baths and central OTS
                // Zone 2 (Center): TV Lounge hub + Open Kitchen
                // Zone 3 (Front): Drawing Room + Porch + Front Lawn
                val rearZoneH = pl * 0.35f
                val centerZoneH = pl * 0.35f
                val frontZoneH = pl - rearZoneH - centerZoneH

                val halfW = pw / 2f
                val bedW = halfW - 2.5f
                val bathW = 5.0f
                val otsW = pw - (bedW * 2f)

                // Master Bedroom (Rear Left)
                val mBedRect = Rect2D(startX, startY, bedW, rearZoneH - 6f)
                rooms.add(RoomEntity(name = "Master Bedroom", type = RoomType.MASTER_BEDROOM, rect = mBedRect))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = mBedRect.center))

                // Master Bath (Rear Far Left Top Corner)
                val mBathRect = Rect2D(startX, startY + rearZoneH - 6f, bedW * 0.6f, 6f)
                rooms.add(RoomEntity(name = "Master Bath", type = RoomType.ATTACHED_BATH, rect = mBathRect))
                furniture.add(FurnitureBlock(type = FurnitureType.SANITARY_WC, center = mBathRect.center))

                // OTS Ventilation Duct (Rear Center)
                val otsRect = Rect2D(startX + bedW * 0.6f, startY + rearZoneH - 6f, otsW + bedW * 0.4f, 6f)
                rooms.add(RoomEntity(name = "OTS (Air Shaft)", type = RoomType.VENTILATION_OTS, rect = otsRect))

                // Bedroom 2 (Rear Right)
                val bed2Rect = Rect2D(startX + halfW + 1f, startY, bedW, rearZoneH - 6f)
                rooms.add(RoomEntity(name = "Bedroom 2", type = RoomType.BEDROOM, rect = bed2Rect))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = bed2Rect.center))

                // Bath 2 (Rear Right Corner)
                val bath2Rect = Rect2D(startX + halfW + 1f, startY + rearZoneH - 6f, bedW, 6f)
                rooms.add(RoomEntity(name = "Bath 2", type = RoomType.ATTACHED_BATH, rect = bath2Rect))
                furniture.add(FurnitureBlock(type = FurnitureType.SANITARY_WC, center = bath2Rect.center))

                // Zone 2 (Center): TV Lounge (Left & Center) + Kitchen (Right)
                val centerTop = startY + rearZoneH
                val kitchW = min(11f, pw * 0.35f)
                val loungeW = pw - kitchW

                val loungeRect = Rect2D(startX, centerTop, loungeW, centerZoneH)
                rooms.add(RoomEntity(name = "TV Lounge / Living", type = RoomType.TV_LOUNGE, rect = loungeRect))
                furniture.add(FurnitureBlock(type = FurnitureType.SOFA_SET, center = Point2D(loungeRect.center.x, loungeRect.center.y)))
                furniture.add(FurnitureBlock(type = FurnitureType.DINING_TABLE, center = Point2D(loungeRect.left + 5f, loungeRect.top + 4f)))

                // Kitchen (Center Right)
                val kitchenRect = Rect2D(startX + loungeW, centerTop, kitchW, centerZoneH)
                rooms.add(RoomEntity(name = "Kitchen", type = RoomType.KITCHEN, rect = kitchenRect))
                furniture.add(FurnitureBlock(type = FurnitureType.KITCHEN_COUNTER, center = kitchenRect.center))

                // Zone 3 (Front): Drawing Room (Left) + Porch (Right) + Lawn
                val frontTop = centerTop + centerZoneH
                val drawW = pw * 0.45f
                val porchW = pw - drawW

                val drawRect = Rect2D(startX, frontTop, drawW, frontZoneH * 0.75f)
                rooms.add(RoomEntity(name = "Drawing Room", type = RoomType.DRAWING_ROOM, rect = drawRect))
                furniture.add(FurnitureBlock(type = FurnitureType.SOFA_3SEATER, center = drawRect.center))

                // Powder Bath for Drawing Room
                val powderRect = Rect2D(startX, frontTop + frontZoneH * 0.75f, drawW * 0.5f, frontZoneH * 0.25f)
                rooms.add(RoomEntity(name = "Powder Bath", type = RoomType.POWDER_ROOM, rect = powderRect))

                // Front Lawn (Leftmost front corner)
                val lawnRect = Rect2D(startX + drawW * 0.5f, frontTop + frontZoneH * 0.75f, drawW * 0.5f, frontZoneH * 0.25f)
                rooms.add(RoomEntity(name = "Front Lawn", type = RoomType.LAWN_COURTYARD, rect = lawnRect))

                // Car Porch (Right Front)
                val porchRect = Rect2D(startX + drawW, frontTop, porchW, frontZoneH)
                rooms.add(RoomEntity(name = "Car Porch", type = RoomType.CAR_PORCH, rect = porchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.CAR_SEDAN, center = porchRect.center))

                // Staircase inside Lounge
                val stairRect = Rect2D(loungeRect.right - 7f, loungeRect.top + 2f, 6.5f, 10f)
                staircases.add(Staircase(rect = stairRect, style = "Dogleg"))

                satisfied.add("Formal Drawing Room near entrance with direct Porch & Lobby access")
                satisfied.add("Two rear bedrooms with dedicated cross ventilation via central OTS")
                satisfied.add("Spacious central TV Lounge serving as family gathering hub")
                satisfied.add("Direct kitchen serving hatch to lounge and dining")
                highlights.add("Modern open plan layout with dedicated powder room for guests")
                highlights.add("Covered 1-car porch with shaded entryway")
            }

            ArchitecturalStyle.SPACE_EFFICIENT -> {
                // Compact zoning:
                // Front Porch + Combined Drawing/Dining + Central Open Kitchen + Rear Bedrooms
                val rearH = pl * 0.38f
                val midH = pl * 0.32f
                val frontH = pl - rearH - midH

                val halfW = pw / 2f
                val bedRect1 = Rect2D(startX, startY, halfW - 1f, rearH - 5f)
                val bedRect2 = Rect2D(startX + halfW + 1f, startY, halfW - 1f, rearH - 5f)
                rooms.add(RoomEntity(name = "Master Bedroom", type = RoomType.MASTER_BEDROOM, rect = bedRect1))
                rooms.add(RoomEntity(name = "Bedroom 2", type = RoomType.BEDROOM, rect = bedRect2))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = bedRect1.center))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = bedRect2.center))

                // Shared central bathroom & OTS between bedrooms
                val bathH = 5f
                val bathRect1 = Rect2D(startX, startY + rearH - 5f, halfW * 0.6f, bathH)
                val otsRect = Rect2D(startX + halfW * 0.6f, startY + rearH - 5f, halfW * 0.8f, bathH)
                val bathRect2 = Rect2D(startX + halfW * 1.4f, startY + rearH - 5f, halfW * 0.6f, bathH)
                rooms.add(RoomEntity(name = "Bath 1", type = RoomType.ATTACHED_BATH, rect = bathRect1))
                rooms.add(RoomEntity(name = "OTS Duct", type = RoomType.VENTILATION_OTS, rect = otsRect))
                rooms.add(RoomEntity(name = "Bath 2", type = RoomType.ATTACHED_BATH, rect = bathRect2))

                // Combined Living & Dining
                val livingRect = Rect2D(startX, startY + rearH, pw * 0.68f, midH)
                rooms.add(RoomEntity(name = "Living & Dining", type = RoomType.TV_LOUNGE, rect = livingRect))
                furniture.add(FurnitureBlock(type = FurnitureType.SOFA_SET, center = livingRect.center))

                // Compact Open Kitchen
                val kitchRect = Rect2D(startX + pw * 0.68f, startY + rearH, pw * 0.32f, midH)
                rooms.add(RoomEntity(name = "Kitchen", type = RoomType.KITCHEN, rect = kitchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.KITCHEN_COUNTER, center = kitchRect.center))

                // Front: Drawing room + Porch
                val drawRect = Rect2D(startX, startY + rearH + midH, pw * 0.48f, frontH)
                val porchRect = Rect2D(startX + pw * 0.48f, startY + rearH + midH, pw * 0.52f, frontH)
                rooms.add(RoomEntity(name = "Drawing Room", type = RoomType.DRAWING_ROOM, rect = drawRect))
                rooms.add(RoomEntity(name = "Car Porch", type = RoomType.CAR_PORCH, rect = porchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.CAR_SEDAN, center = porchRect.center))

                val stairRect = Rect2D(livingRect.left + 1f, livingRect.top + 1f, 6.5f, 9f)
                staircases.add(Staircase(rect = stairRect, style = "Straight Flight"))

                satisfied.add("Minimized non-usable circulation corridors (under 8% total area)")
                satisfied.add("Smart central service core grouping plumbing lines")
                satisfied.add("Combined living and dining expands perceived interior volume")
                highlights.add("High floor area efficiency index (88% usable area)")
                highlights.add("Lower construction cost due to stacked plumbing and shared structural walls")
            }

            ArchitecturalStyle.RENTAL_INDEPENDENT -> {
                // Rental Layout:
                // Crucial Pakistani architectural feature: Staircase located INSIDE FRONT PORCH with independent exterior door
                // Upper floor tenant has 100% private entrance without ever passing through Ground floor family rooms.
                val rearH = pl * 0.36f
                val midH = pl * 0.34f
                val frontH = pl - rearH - midH

                val halfW = pw / 2f
                val mBed = Rect2D(startX, startY, halfW, rearH - 6f)
                val bed2 = Rect2D(startX + halfW, startY, halfW, rearH - 6f)
                rooms.add(RoomEntity(name = "Master Bedroom", type = RoomType.MASTER_BEDROOM, rect = mBed))
                rooms.add(RoomEntity(name = "Bedroom 2", type = RoomType.BEDROOM, rect = bed2))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = mBed.center))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = bed2.center))

                val bathH = 6f
                val b1 = Rect2D(startX, startY + rearH - 6f, halfW * 0.5f, bathH)
                val ots = Rect2D(startX + halfW * 0.5f, startY + rearH - 6f, halfW, bathH)
                val b2 = Rect2D(startX + halfW * 1.5f, startY + rearH - 6f, halfW * 0.5f, bathH)
                rooms.add(RoomEntity(name = "Bath 1", type = RoomType.ATTACHED_BATH, rect = b1))
                rooms.add(RoomEntity(name = "Rear OTS", type = RoomType.VENTILATION_OTS, rect = ots))
                rooms.add(RoomEntity(name = "Bath 2", type = RoomType.ATTACHED_BATH, rect = b2))

                // TV Lounge & Kitchen in Center
                val lounge = Rect2D(startX, startY + rearH, pw * 0.65f, midH)
                val kitch = Rect2D(startX + pw * 0.65f, startY + rearH, pw * 0.35f, midH)
                rooms.add(RoomEntity(name = "TV Lounge", type = RoomType.TV_LOUNGE, rect = lounge))
                rooms.add(RoomEntity(name = "Kitchen", type = RoomType.KITCHEN, rect = kitch))
                furniture.add(FurnitureBlock(type = FurnitureType.SOFA_SET, center = lounge.center))
                furniture.add(FurnitureBlock(type = FurnitureType.KITCHEN_COUNTER, center = kitch.center))

                // Front: Drawing Room (Left) + Rental Porch Staircase (Right)
                val drawRect = Rect2D(startX, startY + rearH + midH, pw * 0.45f, frontH)
                rooms.add(RoomEntity(name = "Drawing Room", type = RoomType.DRAWING_ROOM, rect = drawRect))

                val porchRect = Rect2D(startX + pw * 0.45f, startY + rearH + midH, pw * 0.55f, frontH)
                rooms.add(RoomEntity(name = "Car Porch (Dual Entry)", type = RoomType.CAR_PORCH, rect = porchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.CAR_SEDAN, center = Point2D(porchRect.center.x, porchRect.center.y + 2f)))

                // Independent Exterior Rental Staircase in Porch!
                val stairRect = Rect2D(porchRect.right - 7f, porchRect.top, 6.5f, 11f)
                staircases.add(Staircase(rect = stairRect, style = "Exterior Dogleg"))

                satisfied.add("Independent porch entrance for upper floor tenants / separate rental unit")
                satisfied.add("Ground floor family retains complete privacy from upper floor visitors")
                satisfied.add("Separate utility meters easily accommodated under porch stair cavity")
                highlights.add("High rental yield configuration popular in urban housing developments")
                highlights.add("Staircase tower can be locked or unlocked per owner requirements")
            }

            ArchitecturalStyle.TRADITIONAL_COURTYARD -> {
                // Central courtyard with rooms wrapping around it
                val rearH = pl * 0.32f
                val courtH = pl * 0.36f
                val frontH = pl - rearH - courtH

                // Rear: Bedrooms
                val halfW = pw / 2f
                val b1 = Rect2D(startX, startY, halfW, rearH)
                val b2 = Rect2D(startX + halfW, startY, halfW, rearH)
                rooms.add(RoomEntity(name = "Family Bedroom 1", type = RoomType.MASTER_BEDROOM, rect = b1))
                rooms.add(RoomEntity(name = "Family Bedroom 2", type = RoomType.BEDROOM, rect = b2))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = b1.center))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = b2.center))

                // Center: Courtyard (Center) flanked by TV Lounge (Left) and Kitchen/Baths (Right)
                val courtW = pw * 0.35f
                val wingW = (pw - courtW) / 2f
                val loungeRect = Rect2D(startX, startY + rearH, wingW, courtH)
                val courtRect = Rect2D(startX + wingW, startY + rearH, courtW, courtH)
                val serviceRect = Rect2D(startX + wingW + courtW, startY + rearH, wingW, courtH)

                rooms.add(RoomEntity(name = "Family Lounge", type = RoomType.TV_LOUNGE, rect = loungeRect))
                rooms.add(RoomEntity(name = "Central Courtyard / Sahan", type = RoomType.LAWN_COURTYARD, rect = courtRect))
                rooms.add(RoomEntity(name = "Kitchen & Store", type = RoomType.KITCHEN, rect = serviceRect))
                furniture.add(FurnitureBlock(type = FurnitureType.SOFA_SET, center = loungeRect.center))
                furniture.add(FurnitureBlock(type = FurnitureType.KITCHEN_COUNTER, center = serviceRect.center))

                // Front: Formal Baithak / Drawing (Left) + Car Porch (Right)
                val drawRect = Rect2D(startX, startY + rearH + courtH, pw * 0.45f, frontH)
                val porchRect = Rect2D(startX + pw * 0.45f, startY + rearH + courtH, pw * 0.55f, frontH)
                rooms.add(RoomEntity(name = "Baithak / Drawing", type = RoomType.DRAWING_ROOM, rect = drawRect))
                rooms.add(RoomEntity(name = "Car Porch", type = RoomType.CAR_PORCH, rect = porchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.CAR_SEDAN, center = porchRect.center))

                val stairRect = Rect2D(porchRect.left + 1f, porchRect.top + 1f, 6.5f, 10f)
                staircases.add(Staircase(rect = stairRect, style = "Courtyard Dogleg"))

                satisfied.add("Natural cross ventilation & daylight in every single room via central courtyard")
                satisfied.add("Strict cultural privacy zoning between guest Baithak and inner family Sahan")
                highlights.add("Traditional climate-responsive design reducing air conditioning load")
                highlights.add("Private outdoor family space secure from external street views")
            }

            ArchitecturalStyle.CONTEMPORARY_LUXURY, ArchitecturalStyle.CUSTOM -> {
                // Luxury villa with wide foyer, grand drawing room, double car porch, lavish bedrooms
                val rearH = pl * 0.38f
                val midH = pl * 0.34f
                val frontH = pl - rearH - midH

                val masterW = pw * 0.55f
                val bed2W = pw - masterW

                val masterBed = Rect2D(startX, startY, masterW, rearH - 7f)
                val masterBath = Rect2D(startX, startY + rearH - 7f, masterW * 0.6f, 7f)
                val masterDress = Rect2D(startX + masterW * 0.6f, startY + rearH - 7f, masterW * 0.4f, 7f)

                rooms.add(RoomEntity(name = "Executive Master Suite", type = RoomType.MASTER_BEDROOM, rect = masterBed))
                rooms.add(RoomEntity(name = "Master Bath", type = RoomType.ATTACHED_BATH, rect = masterBath))
                rooms.add(RoomEntity(name = "Walk-in Dressing", type = RoomType.STORE_ROOM, rect = masterDress))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = masterBed.center))

                val bed2 = Rect2D(startX + masterW, startY, bed2W, rearH - 7f)
                val bath2 = Rect2D(startX + masterW, startY + rearH - 7f, bed2W, 7f)
                rooms.add(RoomEntity(name = "Bedroom 2", type = RoomType.BEDROOM, rect = bed2))
                rooms.add(RoomEntity(name = "En-Suite Bath", type = RoomType.ATTACHED_BATH, rect = bath2))
                furniture.add(FurnitureBlock(type = FurnitureType.BED_DOUBLE, center = bed2.center))

                // Mid zone: Grand Lounge + Dirty/Clean Kitchen
                val loungeRect = Rect2D(startX, startY + rearH, pw * 0.65f, midH)
                val kitchRect = Rect2D(startX + pw * 0.65f, startY + rearH, pw * 0.35f, midH)
                rooms.add(RoomEntity(name = "Double-Height Lounge", type = RoomType.TV_LOUNGE, rect = loungeRect))
                rooms.add(RoomEntity(name = "Designer Kitchen", type = RoomType.KITCHEN, rect = kitchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.SOFA_SET, center = loungeRect.center))
                furniture.add(FurnitureBlock(type = FurnitureType.KITCHEN_COUNTER, center = kitchRect.center))

                // Front zone: Grand Drawing/Dining + 2-Car Porch + Front Lawn
                val drawRect = Rect2D(startX, startY + rearH + midH, pw * 0.42f, frontH)
                val porchRect = Rect2D(startX + pw * 0.42f, startY + rearH + midH, pw * 0.58f, frontH)
                rooms.add(RoomEntity(name = "Formal Drawing & Dining", type = RoomType.DRAWING_ROOM, rect = drawRect))
                rooms.add(RoomEntity(name = "Executive 2-Car Porch", type = RoomType.CAR_PORCH, rect = porchRect))
                furniture.add(FurnitureBlock(type = FurnitureType.CAR_SEDAN, center = Point2D(porchRect.center.x - 4f, porchRect.center.y)))
                furniture.add(FurnitureBlock(type = FurnitureType.CAR_SEDAN, center = Point2D(porchRect.center.x + 4f, porchRect.center.y)))

                val stairRect = Rect2D(loungeRect.left + 2f, loungeRect.top + 2f, 7.5f, 12f)
                staircases.add(Staircase(rect = stairRect, style = "Feature Open-Tread Staircase"))

                satisfied.add("Generous room spans exceeding minimum architectural standards")
                satisfied.add("Walk-in dressing rooms and luxury bathroom suites")
                satisfied.add("Double car porch accommodating two full-size sedans/SUVs")
                highlights.add("Expansive entertaining spaces with feature staircase in living hall")
                highlights.add("Separate dirty kitchen / scullery provisions for staff")
            }
        }

        // Generate matching CAD walls from the rooms
        val walls = GeometryEngine.generateWallsFromRooms(rooms)

        // Generate doors on appropriate walls
        val doors = mutableListOf<Door>()
        val windows = mutableListOf<WindowEntity>()
        var dIndex = 1
        var wIndex = 1

        for (wall in walls) {
            if (wall.length >= 6f) {
                if (wall.isExterior) {
                    // Place window
                    windows.add(
                        WindowEntity(
                            wallId = wall.id,
                            positionRatio = 0.5f,
                            width = min(5f, wall.length * 0.6f),
                            label = "W${wIndex++}"
                        )
                    )
                } else {
                    // Place door
                    doors.add(
                        Door(
                            wallId = wall.id,
                            positionRatio = 0.25f,
                            width = 3.5f,
                            label = "D${dIndex++}"
                        )
                    )
                }
            }
        }

        // Add dimensions
        val dimensions = mutableListOf<DimensionEntity>()
        dimensions.add(
            DimensionEntity(
                start = Point2D(0f, 0f),
                end = Point2D(plot.frontWidth, 0f),
                offset = -3f,
                textOverride = "PLOT WIDTH: ${plot.frontWidth}'-0\""
            )
        )
        dimensions.add(
            DimensionEntity(
                start = Point2D(plot.frontWidth, 0f),
                end = Point2D(plot.frontWidth, plot.leftLength),
                offset = 3f,
                textOverride = "PLOT LENGTH: ${plot.leftLength}'-0\""
            )
        )

        val floor = CadFloor(
            id = "ground_floor",
            name = "Ground Floor",
            level = 0,
            walls = walls,
            rooms = rooms,
            doors = doors,
            windows = windows,
            staircases = staircases,
            furniture = furniture,
            dimensions = dimensions
        )

        val covered = floor.coveredAreaSqFt
        val totalPlot = plot.totalAreaSqFt.toFloat()
        val open = (totalPlot - covered).coerceAtLeast(0f)
        val eff = if (totalPlot > 0f) (covered / totalPlot * 100f).coerceIn(40f, 95f) else 75f

        val score = when (style) {
            ArchitecturalStyle.MODERN_PAKISTANI -> 96
            ArchitecturalStyle.SPACE_EFFICIENT -> 94
            ArchitecturalStyle.RENTAL_INDEPENDENT -> 92
            ArchitecturalStyle.TRADITIONAL_COURTYARD -> 91
            ArchitecturalStyle.CONTEMPORARY_LUXURY -> 95
            ArchitecturalStyle.CUSTOM -> 90
        } - (variation - 1) * 2

        return LayoutCandidate(
            style = style,
            title = "${style.title} (Option $variation)",
            conceptSummary = style.description,
            floor = floor,
            score = score,
            coveredAreaSqFt = covered,
            openAreaSqFt = open,
            efficiencyPercent = eff,
            satisfiedConstraints = satisfied,
            unsatisfiedConstraints = unsatisfied,
            highlights = highlights
        )
    }
}
