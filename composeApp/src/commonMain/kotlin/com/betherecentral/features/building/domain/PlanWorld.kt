package com.betherecentral.features.building.domain

/** Positions are original 2048 × 1448 plan-image pixels, with y increasing downward. */
data class PlanPoint(val x: Double, val y: Double)

enum class PlanRegionKind { WORKSPACE, SHARED, STAIRS, UNMAPPED }

data class PlanRegion(
    val id: String,
    val label: String,
    val polygon: List<PlanPoint>,
    val colorHex: Long,
    val kind: PlanRegionKind = PlanRegionKind.WORKSPACE,
)

/** A number denotes a legend entry; several anchors can depict the same entry. */
data class PlanPlace(
    val id: String,
    val label: String,
    val number: String? = null,
    val anchors: List<PlanPoint> = emptyList(),
    val regionId: String? = null,
    val occupants: List<String> = emptyList(),
    val details: String = "",
)

data class PlanFloor(
    val id: String,
    val name: String,
    val level: Int,
    val regions: List<PlanRegion>,
    /** Open courtyards or absent footprint, separate from explicitly grey unmapped regions. */
    val voids: List<List<PlanPoint>>,
    /** Visible internal plan lines; these do not establish entrances or navigable routes. */
    val walls: List<List<PlanPoint>>,
    val places: List<PlanPlace>,
    val notes: String,
    /** Outer silhouettes traced from the source pixels, independent of colored zone guesses. */
    val footprints: List<List<PlanPoint>> = emptyList(),
)

/** Geometric selection only. It says nothing about access, routes, distance or survey accuracy. */
object PlanWorld {
    fun contains(floor: PlanFloor, point: PlanPoint): Boolean =
        (if (floor.footprints.isEmpty()) floor.regions.map { it.polygon } else floor.footprints)
            .any { contains(it, point) } &&
            floor.voids.none { contains(it, point) }

    fun contains(polygon: List<PlanPoint>, point: PlanPoint): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var previous = polygon.last()
        for (current in polygon) {
            val crosses = (current.y > point.y) != (previous.y > point.y)
            if (crosses) {
                val xAtY = (previous.x - current.x) * (point.y - current.y) /
                    (previous.y - current.y) + current.x
                if (point.x < xAtY) inside = !inside
            }
            previous = current
        }
        return inside
    }
}
