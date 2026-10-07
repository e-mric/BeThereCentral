package com.betherecentral.features.building.data

import com.betherecentral.features.building.domain.Bounds2D
import com.betherecentral.features.building.domain.Checkpoint
import com.betherecentral.features.building.domain.ConceptFloorRegistration as Registration
import com.betherecentral.features.building.domain.Floor
import com.betherecentral.features.building.domain.Point2D
import com.betherecentral.features.building.domain.Room
import com.betherecentral.features.navigation.domain.NavigationEdge
import com.betherecentral.features.navigation.domain.Route
import com.betherecentral.features.navigation.domain.RouteMode
import com.betherecentral.features.navigation.domain.RoutePlanner
import com.betherecentral.features.navigation.domain.RoutePreference

/** Fictional sample map for navigation demonstrations; geometry is illustrative, in meters. */
object DemoBuilding {
    const val id: String = "sample-building"
    const val name: String = "Central House · Sample building"

    val floors: List<Floor> = listOf(
        Floor("floor-1", "Ground floor", 0.0, Registration.floorWidthMeters, Registration.floorHeightMeters),
        Floor("floor-2", "First floor", 4.0, Registration.floorWidthMeters, Registration.floorHeightMeters),
        Floor("floor-3", "Second floor", 8.0, Registration.floorWidthMeters, Registration.floorHeightMeters),
        Floor("floor-4", "Third floor", 12.0, Registration.floorWidthMeters, Registration.floorHeightMeters),
    )

    private data class RoomSpec(
        val slug: String, val label: String, val category: String,
        val left: Int, val top: Int, val right: Int, val bottom: Int,
        val door: Point2D, val hallPath: List<Point2D>,
    )

    private fun px(x: Int, y: Int) = Registration.imageToWorld(Point2D(x.toDouble(), y.toDouble()))

    private val roomSpecs = listOf(
        RoomSpec("orbit", "Orbit", "Workspace", 50, 80, 510, 347, px(242, 347), listOf(px(242, 347), px(242, 374), px(399, 374))),
        RoomSpec("meeting-01", "Meet 01", "Meeting room", 570, 85, 760, 302, px(658, 302), listOf(px(658, 302), px(658, 329), px(765, 329))),
        RoomSpec("meeting-02", "Meet 02", "Meeting room", 780, 85, 975, 302, px(879, 302), listOf(px(879, 302), px(879, 329), px(765, 329))),
        RoomSpec("moss", "Moss", "Workspace", 1035, 80, 1480, 347, px(1294, 347), listOf(px(1294, 347), px(1294, 375), px(1170, 375))),
        RoomSpec("cafe", "Cafe", "Cafe", 430, 355, 730, 590, px(710, 590), listOf(px(710, 590), px(710, 595), px(755, 595))),
        RoomSpec("lounge", "Lounge", "Lounge", 805, 390, 1110, 580, px(1008, 580), listOf(px(1008, 580), px(1008, 595), px(755, 595))),
        RoomSpec("quiet", "Quiet", "Quiet room", 1210, 400, 1385, 584, px(1309, 584), listOf(px(1309, 584), px(1309, 595), px(1140, 595))),
        RoomSpec("phone-booth-1", "Phone booth 1", "Phone booth", 245, 402, 307, 550, px(275, 550), listOf(px(275, 550), px(275, 570), px(399, 570), px(399, 595))),
        RoomSpec("phone-booth-2", "Phone booth 2", "Phone booth", 315, 402, 375, 550, px(345, 550), listOf(px(345, 550), px(345, 570), px(399, 570), px(399, 595))),
        RoomSpec("spark", "Spark", "Workspace", 50, 615, 470, 890, px(242, 615), listOf(px(242, 615), px(242, 595), px(399, 595))),
        RoomSpec("reception", "Welcome reception", "Reception", 675, 656, 855, 890, px(760, 656), listOf(px(760, 656), px(760, 595), px(755, 595))),
        RoomSpec("north", "North", "Workspace", 1060, 615, 1480, 890, px(1297, 615), listOf(px(1297, 615), px(1297, 595), px(1140, 595))),
    )

    val rooms: List<Room> = floors.flatMap { floor ->
        roomSpecs.map { spec ->
            val topLeft = px(spec.left, spec.top)
            val bottomRight = px(spec.right, spec.bottom)
            Room(
                id = roomId(floor, spec.slug), floorId = floor.id,
                name = "${spec.label} · ${floor.name}", category = spec.category,
                center = Point2D((topLeft.xMeters + bottomRight.xMeters) / 2, (topLeft.yMeters + bottomRight.yMeters) / 2),
                widthMeters = bottomRight.xMeters - topLeft.xMeters,
                heightMeters = topLeft.yMeters - bottomRight.yMeters,
                entrance = spec.door,
            )
        }
    }

    private fun roomId(floor: Floor, slug: String) = "room-l${floor.id.removePrefix("floor-")}-${slug}"

    val checkpoints: List<Checkpoint> = floors.mapIndexed { index, floor ->
        Checkpoint(
            id = "cp-l${index + 1}-lobby", floorId = floor.id,
            name = "Level ${index + 1} lobby", position = px(755, 595),
        )
    } + Checkpoint("cp-l1-west", "floor-1", "West entrance", px(200, 595))

    val floorBounds: Map<String, Bounds2D> = floors.associate { floor ->
        floor.id to Bounds2D(0.0, 0.0, floor.widthMeters, floor.heightMeters)
    }

    val navigationEdges: List<NavigationEdge> by lazy { buildNavigationEdges() }
    private val checkpointById = checkpoints.associateBy { it.id }
    private val roomById = rooms.associateBy { it.id }

    fun findRoute(fromCheckpointId: String, destinationRoomId: String, stepFree: Boolean): Route? =
        findRoute(fromCheckpointId, destinationRoomId, if (stepFree) RoutePreference.STEP_FREE else RoutePreference.DEFAULT)

    fun findRoute(fromCheckpointId: String, destinationRoomId: String, preference: RoutePreference): Route? {
        val checkpoint = checkpointById[fromCheckpointId] ?: return null
        val room = roomById[destinationRoomId] ?: return null
        return RoutePlanner.findRoute(
            edges = navigationEdges,
            startNodeId = "checkpoint:${checkpoint.id}",
            destinationNodeId = "room:${room.id}",
            fromCheckpointId = checkpoint.id,
            destinationRoomId = room.id,
            preference = preference,
        )
    }

    private fun buildNavigationEdges(): List<NavigationEdge> {
        val edges = mutableListOf<NavigationEdge>()
        fun add(from: String, to: String, floorId: String, mode: RouteMode, points: List<Point2D>, cost: Double? = null, reverseFloorId: String = floorId) {
            val distance = when (mode) {
                RouteMode.WALK -> polylineLength(points)
                RouteMode.STAIRS -> kotlin.math.hypot(floors.first { it.id == floorId }.elevationMeters - floors.first { it.id == reverseFloorId }.elevationMeters, 2.0)
                RouteMode.LIFT -> kotlin.math.abs(floors.first { it.id == floorId }.elevationMeters - floors.first { it.id == reverseFloorId }.elevationMeters)
            }
            edges += NavigationEdge(from, to, floorId, mode, distance, cost ?: distance, points, isStepFree = mode != RouteMode.STAIRS)
            edges += NavigationEdge(to, from, reverseFloorId, mode, distance, cost ?: distance, points.reversed(), isStepFree = mode != RouteMode.STAIRS)
        }

        val upperLeft = px(399, 374); val upperCenter = px(765, 329); val upperRight = px(1170, 375)
        val lowerLeft = px(399, 595); val lowerCenter = px(755, 595); val lowerRight = px(1140, 595)
        for (floor in floors) {
            val id = floor.id
            fun walk(a: String, b: String, vararg points: Point2D) = add(a, b, id, RouteMode.WALK, points.toList())
            // Separate west/east aisles avoid crossing the Orbit and Moss walls.
            walk("hall:$id:upper-left", "hall:$id:lower-left", upperLeft, lowerLeft)
            walk("hall:$id:upper-center", "hall:$id:lower-center", upperCenter, lowerCenter)
            walk("hall:$id:upper-right", "hall:$id:lower-right", upperRight, lowerRight)
            walk("hall:$id:west", "hall:$id:lower-left", px(200, 595), lowerLeft)
            walk("hall:$id:lower-left", "hall:$id:lower-center", lowerLeft, px(500, 595), lowerCenter)
            walk("hall:$id:lower-center", "hall:$id:lower-right", lowerCenter, lowerRight)
            for (spec in roomSpecs) {
                val roomNode = "room:${roomId(floor, spec.slug)}"
                val path = spec.hallPath
                val nearest = when {
                    spec.slug == "orbit" -> "hall:$id:upper-left"
                    spec.slug in setOf("phone-booth-1", "phone-booth-2", "spark") -> "hall:$id:lower-left"
                    spec.slug in setOf("moss", "quiet", "north") -> if (spec.slug == "moss") "hall:$id:upper-right" else "hall:$id:lower-right"
                    spec.slug in setOf("cafe", "lounge", "reception") -> "hall:$id:lower-center"
                    else -> "hall:$id:upper-center"
                }
                val adjusted = path + when (spec.slug) {
                    "orbit" -> upperLeft
                    "meeting-01", "meeting-02" -> upperCenter
                    "moss" -> upperRight
                    "cafe", "lounge" -> lowerCenter
                    "quiet" -> lowerRight
                    "phone-booth-1", "phone-booth-2" -> lowerLeft
                    "spark" -> lowerLeft
                    "reception" -> lowerCenter
                    else -> lowerRight
                }
                add(roomNode, nearest, id, RouteMode.WALK, adjusted)
            }
            for (checkpoint in checkpoints.filter { it.floorId == id }) {
                val target = if (checkpoint.id == "cp-l1-west") "hall:$id:west" else "hall:$id:lower-center"
                val targetPoint = if (checkpoint.id == "cp-l1-west") px(200, 595) else lowerCenter
                add("checkpoint:${checkpoint.id}", target, id, RouteMode.WALK, listOf(checkpoint.position, targetPoint))
            }
        }
        val stairsPosition = px(900, 650)
        val liftApproach = px(990, 630)
        val liftPosition = px(990, 650)
        for (index in 0 until floors.lastIndex) {
            val lower = floors[index]; val upper = floors[index + 1]
            val lowerStairs = "stairs:${lower.id}"; val upperStairs = "stairs:${upper.id}"
            val lowerLift = "lift:${lower.id}"; val upperLift = "lift:${upper.id}"
            for (floor in listOf(lower, upper)) {
                add("hall:${floor.id}:lower-center", "stairs:${floor.id}", floor.id, RouteMode.WALK, listOf(lowerCenter, px(900, 595), stairsPosition))
                add("hall:${floor.id}:lower-center", "lift:${floor.id}", floor.id, RouteMode.WALK, listOf(lowerCenter, px(990, 595), liftApproach, liftPosition))
            }
            add(lowerStairs, upperStairs, lower.id, RouteMode.STAIRS, listOf(stairsPosition), cost = 8.0, reverseFloorId = upper.id)
            add(lowerLift, upperLift, lower.id, RouteMode.LIFT, listOf(liftPosition), cost = 9.0, reverseFloorId = upper.id)
        }
        return edges
    }

    private fun polylineLength(points: List<Point2D>): Double = points.zipWithNext().sumOf { (a, b) ->
        val dx = b.xMeters - a.xMeters; val dy = b.yMeters - a.yMeters
        kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
