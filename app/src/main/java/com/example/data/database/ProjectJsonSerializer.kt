package com.example.data.database

import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonSerializer {

    fun toJson(project: CadProject): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("name", project.name)
        root.put("clientName", project.clientName)
        root.put("siteLocation", project.siteLocation)
        root.put("drawingTitle", project.drawingTitle)
        root.put("drawingNumber", project.drawingNumber)
        root.put("revision", project.revision)
        root.put("notes", project.notes)
        root.put("dateCreated", project.dateCreated)
        root.put("dateModified", project.dateModified)
        root.put("unitSystem", project.unitSystem.name)
        root.put("marlaConvention", project.marlaConvention.name)
        root.put("activeFloorId", project.activeFloorId)

        // Plot
        val plotObj = JSONObject()
        plotObj.put("shapeType", project.plot.shapeType.name)
        plotObj.put("frontWidth", project.plot.frontWidth.toDouble())
        plotObj.put("backWidth", project.plot.backWidth.toDouble())
        plotObj.put("leftLength", project.plot.leftLength.toDouble())
        plotObj.put("rightLength", project.plot.rightLength.toDouble())
        project.plot.diagonalLength?.let { plotObj.put("diagonalLength", it.toDouble()) }
        plotObj.put("roadFacingSide", project.plot.roadFacingSide)
        plotObj.put("northAngleDegrees", project.plot.northAngleDegrees.toDouble())
        plotObj.put("frontSetback", project.plot.frontSetback.toDouble())
        plotObj.put("rearSetback", project.plot.rearSetback.toDouble())
        plotObj.put("leftSetback", project.plot.leftSetback.toDouble())
        plotObj.put("rightSetback", project.plot.rightSetback.toDouble())

        val verticesArr = JSONArray()
        for (v in project.plot.customVertices) {
            val vObj = JSONObject()
            vObj.put("x", v.x.toDouble())
            vObj.put("y", v.y.toDouble())
            verticesArr.put(vObj)
        }
        plotObj.put("customVertices", verticesArr)
        root.put("plot", plotObj)

        // Floors
        val floorsArr = JSONArray()
        for (floor in project.floors) {
            val fObj = JSONObject()
            fObj.put("id", floor.id)
            fObj.put("name", floor.name)
            fObj.put("level", floor.level)

            // Walls
            val wallsArr = JSONArray()
            for (w in floor.walls) {
                val wObj = JSONObject()
                wObj.put("id", w.id)
                wObj.put("startX", w.start.x.toDouble())
                wObj.put("startY", w.start.y.toDouble())
                wObj.put("endX", w.end.x.toDouble())
                wObj.put("endY", w.end.y.toDouble())
                wObj.put("thickness", w.thickness.toDouble())
                wObj.put("wallType", w.wallType.name)
                wObj.put("isExterior", w.isExterior)
                wObj.put("layer", w.layer)
                wallsArr.put(wObj)
            }
            fObj.put("walls", wallsArr)

            // Rooms
            val roomsArr = JSONArray()
            for (r in floor.rooms) {
                val rObj = JSONObject()
                rObj.put("id", r.id)
                rObj.put("name", r.name)
                rObj.put("type", r.type.name)
                rObj.put("x", r.rect.x.toDouble())
                rObj.put("y", r.rect.y.toDouble())
                rObj.put("width", r.rect.width.toDouble())
                rObj.put("height", r.rect.height.toDouble())
                rObj.put("colorHex", r.colorHex)
                rObj.put("tag", r.tag)
                roomsArr.put(rObj)
            }
            fObj.put("rooms", roomsArr)

            // Doors
            val doorsArr = JSONArray()
            for (d in floor.doors) {
                val dObj = JSONObject()
                dObj.put("id", d.id)
                dObj.put("wallId", d.wallId)
                dObj.put("positionRatio", d.positionRatio.toDouble())
                dObj.put("width", d.width.toDouble())
                dObj.put("swingInside", d.swingInside)
                dObj.put("swingLeft", d.swingLeft)
                dObj.put("label", d.label)
                doorsArr.put(dObj)
            }
            fObj.put("doors", doorsArr)

            // Windows
            val winsArr = JSONArray()
            for (wn in floor.windows) {
                val wnObj = JSONObject()
                wnObj.put("id", wn.id)
                wnObj.put("wallId", wn.wallId)
                wnObj.put("positionRatio", wn.positionRatio.toDouble())
                wnObj.put("width", wn.width.toDouble())
                wnObj.put("label", wn.label)
                wnObj.put("isVentilator", wn.isVentilator)
                winsArr.put(wnObj)
            }
            fObj.put("windows", winsArr)

            // Staircases
            val stairsArr = JSONArray()
            for (s in floor.staircases) {
                val sObj = JSONObject()
                sObj.put("id", s.id)
                sObj.put("x", s.rect.x.toDouble())
                sObj.put("y", s.rect.y.toDouble())
                sObj.put("width", s.rect.width.toDouble())
                sObj.put("height", s.rect.height.toDouble())
                sObj.put("stepsCount", s.stepsCount)
                sObj.put("direction", s.direction)
                sObj.put("style", s.style)
                stairsArr.put(sObj)
            }
            fObj.put("staircases", stairsArr)

            // Furniture
            val furnArr = JSONArray()
            for (fn in floor.furniture) {
                val fnObj = JSONObject()
                fnObj.put("id", fn.id)
                fnObj.put("type", fn.type.name)
                fnObj.put("cx", fn.center.x.toDouble())
                fnObj.put("cy", fn.center.y.toDouble())
                fnObj.put("rotation", fn.rotationDegrees.toDouble())
                fnObj.put("width", fn.width.toDouble())
                fnObj.put("length", fn.length.toDouble())
                furnArr.put(fnObj)
            }
            fObj.put("furniture", furnArr)

            // Dimensions
            val dimsArr = JSONArray()
            for (dm in floor.dimensions) {
                val dmObj = JSONObject()
                dmObj.put("id", dm.id)
                dmObj.put("startX", dm.start.x.toDouble())
                dmObj.put("startY", dm.start.y.toDouble())
                dmObj.put("endX", dm.end.x.toDouble())
                dmObj.put("endY", dm.end.y.toDouble())
                dmObj.put("offset", dm.offset.toDouble())
                dm.textOverride?.let { dmObj.put("textOverride", it) }
                dimsArr.put(dmObj)
            }
            fObj.put("dimensions", dimsArr)

            floorsArr.put(fObj)
        }
        root.put("floors", floorsArr)

        return root.toString(2)
    }

    fun fromJson(jsonStr: String): CadProject {
        val root = JSONObject(jsonStr)
        val id = root.optString("id", java.util.UUID.randomUUID().toString())
        val name = root.optString("name", "CAD Project")
        val clientName = root.optString("clientName", "")
        val siteLocation = root.optString("siteLocation", "")
        val drawingTitle = root.optString("drawingTitle", "ARCHITECTURAL FLOOR PLAN")
        val drawingNumber = root.optString("drawingNumber", "ASD-2026-001")
        val revision = root.optString("revision", "R-0")
        val notes = root.optString("notes", "")
        val dateCreated = root.optLong("dateCreated", System.currentTimeMillis())
        val dateModified = root.optLong("dateModified", System.currentTimeMillis())
        val unitSystem = try {
            UnitSystem.valueOf(root.optString("unitSystem", UnitSystem.FEET_INCHES.name))
        } catch (_: Exception) {
            UnitSystem.FEET_INCHES
        }
        val marlaConvention = try {
            MarlaConvention.valueOf(root.optString("marlaConvention", MarlaConvention.LAHORE_COMMERCIAL.name))
        } catch (_: Exception) {
            MarlaConvention.LAHORE_COMMERCIAL
        }
        val activeFloorId = root.optString("activeFloorId", "ground_floor")

        // Parse Plot
        val plotObj = root.optJSONObject("plot")
        val plot = if (plotObj != null) {
            val shapeType = try {
                PlotShapeType.valueOf(plotObj.optString("shapeType", PlotShapeType.RECTANGLE.name))
            } catch (_: Exception) {
                PlotShapeType.RECTANGLE
            }
            val customVerts = mutableListOf<Point2D>()
            val vArr = plotObj.optJSONArray("customVertices")
            if (vArr != null) {
                for (i in 0 until vArr.length()) {
                    val vo = vArr.getJSONObject(i)
                    customVerts.add(Point2D(vo.getDouble("x").toFloat(), vo.getDouble("y").toFloat()))
                }
            }
            PlotGeometry(
                shapeType = shapeType,
                frontWidth = plotObj.optDouble("frontWidth", 30.0).toFloat(),
                backWidth = plotObj.optDouble("backWidth", 30.0).toFloat(),
                leftLength = plotObj.optDouble("leftLength", 60.0).toFloat(),
                rightLength = plotObj.optDouble("rightLength", 60.0).toFloat(),
                diagonalLength = if (plotObj.has("diagonalLength")) plotObj.getDouble("diagonalLength").toFloat() else null,
                roadFacingSide = plotObj.optString("roadFacingSide", "Front (South)"),
                northAngleDegrees = plotObj.optDouble("northAngleDegrees", 0.0).toFloat(),
                frontSetback = plotObj.optDouble("frontSetback", 5.0).toFloat(),
                rearSetback = plotObj.optDouble("rearSetback", 4.0).toFloat(),
                leftSetback = plotObj.optDouble("leftSetback", 0.0).toFloat(),
                rightSetback = plotObj.optDouble("rightSetback", 0.0).toFloat(),
                customVertices = customVerts
            )
        } else {
            PlotGeometry()
        }

        // Parse Floors
        val floorsList = mutableListOf<CadFloor>()
        val floorsArr = root.optJSONArray("floors")
        if (floorsArr != null && floorsArr.length() > 0) {
            for (fIdx in 0 until floorsArr.length()) {
                val fObj = floorsArr.getJSONObject(fIdx)
                val fId = fObj.optString("id", "floor_$fIdx")
                val fName = fObj.optString("name", "Floor $fIdx")
                val level = fObj.optInt("level", 0)

                // Walls
                val walls = mutableListOf<Wall>()
                val wArr = fObj.optJSONArray("walls")
                if (wArr != null) {
                    for (i in 0 until wArr.length()) {
                        val wo = wArr.getJSONObject(i)
                        val wType = try {
                            WallType.valueOf(wo.optString("wallType", WallType.EXTERIOR_9_INCH.name))
                        } catch (_: Exception) {
                            WallType.EXTERIOR_9_INCH
                        }
                        walls.add(
                            Wall(
                                id = wo.optString("id", java.util.UUID.randomUUID().toString()),
                                start = Point2D(wo.getDouble("startX").toFloat(), wo.getDouble("startY").toFloat()),
                                end = Point2D(wo.getDouble("endX").toFloat(), wo.getDouble("endY").toFloat()),
                                thickness = wo.optDouble("thickness", wType.thicknessFeet.toDouble()).toFloat(),
                                wallType = wType,
                                isExterior = wo.optBoolean("isExterior", true),
                                layer = wo.optString("layer", "WALLS")
                            )
                        )
                    }
                }

                // Rooms
                val rooms = mutableListOf<RoomEntity>()
                val rArr = fObj.optJSONArray("rooms")
                if (rArr != null) {
                    for (i in 0 until rArr.length()) {
                        val ro = rArr.getJSONObject(i)
                        val rType = try {
                            RoomType.valueOf(ro.optString("type", RoomType.CUSTOM.name))
                        } catch (_: Exception) {
                            RoomType.CUSTOM
                        }
                        rooms.add(
                            RoomEntity(
                                id = ro.optString("id", java.util.UUID.randomUUID().toString()),
                                name = ro.optString("name", rType.displayName),
                                type = rType,
                                rect = Rect2D(
                                    ro.getDouble("x").toFloat(),
                                    ro.getDouble("y").toFloat(),
                                    ro.getDouble("width").toFloat(),
                                    ro.getDouble("height").toFloat()
                                ),
                                floorId = fId,
                                colorHex = ro.optLong("colorHex", rType.defaultColorHex),
                                tag = ro.optString("tag", "")
                            )
                        )
                    }
                }

                // Doors
                val doors = mutableListOf<Door>()
                val dArr = fObj.optJSONArray("doors")
                if (dArr != null) {
                    for (i in 0 until dArr.length()) {
                        val doObj = dArr.getJSONObject(i)
                        doors.add(
                            Door(
                                id = doObj.optString("id", java.util.UUID.randomUUID().toString()),
                                wallId = doObj.optString("wallId", ""),
                                positionRatio = doObj.optDouble("positionRatio", 0.5).toFloat(),
                                width = doObj.optDouble("width", 3.5).toFloat(),
                                swingInside = doObj.optBoolean("swingInside", true),
                                swingLeft = doObj.optBoolean("swingLeft", true),
                                label = doObj.optString("label", "D")
                            )
                        )
                    }
                }

                // Windows
                val windows = mutableListOf<WindowEntity>()
                val wnArr = fObj.optJSONArray("windows")
                if (wnArr != null) {
                    for (i in 0 until wnArr.length()) {
                        val wno = wnArr.getJSONObject(i)
                        windows.add(
                            WindowEntity(
                                id = wno.optString("id", java.util.UUID.randomUUID().toString()),
                                wallId = wno.optString("wallId", ""),
                                positionRatio = wno.optDouble("positionRatio", 0.5).toFloat(),
                                width = wno.optDouble("width", 4.0).toFloat(),
                                label = wno.optString("label", "W"),
                                isVentilator = wno.optBoolean("isVentilator", false)
                            )
                        )
                    }
                }

                // Staircases
                val stairs = mutableListOf<Staircase>()
                val sArr = fObj.optJSONArray("staircases")
                if (sArr != null) {
                    for (i in 0 until sArr.length()) {
                        val so = sArr.getJSONObject(i)
                        stairs.add(
                            Staircase(
                                id = so.optString("id", java.util.UUID.randomUUID().toString()),
                                rect = Rect2D(
                                    so.getDouble("x").toFloat(),
                                    so.getDouble("y").toFloat(),
                                    so.getDouble("width").toFloat(),
                                    so.getDouble("height").toFloat()
                                ),
                                stepsCount = so.optInt("stepsCount", 18),
                                direction = so.optString("direction", "UP"),
                                style = so.optString("style", "Dogleg")
                            )
                        )
                    }
                }

                // Furniture
                val furniture = mutableListOf<FurnitureBlock>()
                val fnArr = fObj.optJSONArray("furniture")
                if (fnArr != null) {
                    for (i in 0 until fnArr.length()) {
                        val fno = fnArr.getJSONObject(i)
                        val fnType = try {
                            FurnitureType.valueOf(fno.optString("type", FurnitureType.BED_DOUBLE.name))
                        } catch (_: Exception) {
                            FurnitureType.BED_DOUBLE
                        }
                        furniture.add(
                            FurnitureBlock(
                                id = fno.optString("id", java.util.UUID.randomUUID().toString()),
                                type = fnType,
                                center = Point2D(fno.getDouble("cx").toFloat(), fno.getDouble("cy").toFloat()),
                                rotationDegrees = fno.optDouble("rotation", 0.0).toFloat(),
                                width = fno.optDouble("width", fnType.defaultWidth.toDouble()).toFloat(),
                                length = fno.optDouble("length", fnType.defaultLength.toDouble()).toFloat()
                            )
                        )
                    }
                }

                // Dimensions
                val dimensions = mutableListOf<DimensionEntity>()
                val dmArr = fObj.optJSONArray("dimensions")
                if (dmArr != null) {
                    for (i in 0 until dmArr.length()) {
                        val dmo = dmArr.getJSONObject(i)
                        dimensions.add(
                            DimensionEntity(
                                id = dmo.optString("id", java.util.UUID.randomUUID().toString()),
                                start = Point2D(dmo.getDouble("startX").toFloat(), dmo.getDouble("startY").toFloat()),
                                end = Point2D(dmo.getDouble("endX").toFloat(), dmo.getDouble("endY").toFloat()),
                                offset = dmo.optDouble("offset", 1.5).toFloat(),
                                textOverride = if (dmo.has("textOverride")) dmo.getString("textOverride") else null
                            )
                        )
                    }
                }

                floorsList.add(
                    CadFloor(
                        id = fId,
                        name = fName,
                        level = level,
                        walls = walls,
                        rooms = rooms,
                        doors = doors,
                        windows = windows,
                        staircases = stairs,
                        furniture = furniture,
                        dimensions = dimensions
                    )
                )
            }
        } else {
            floorsList.add(CadFloor(id = "ground_floor", name = "Ground Floor", level = 0))
        }

        return CadProject(
            id = id,
            name = name,
            clientName = clientName,
            siteLocation = siteLocation,
            drawingTitle = drawingTitle,
            drawingNumber = drawingNumber,
            revision = revision,
            notes = notes,
            dateCreated = dateCreated,
            dateModified = dateModified,
            unitSystem = unitSystem,
            marlaConvention = marlaConvention,
            plot = plot,
            floors = floorsList,
            activeFloorId = activeFloorId
        )
    }
}
