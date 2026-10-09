package com.betherecentral.features.building.domain

/** Decorative material used by an explicitly authored floor patch. */
enum class FloorMaterial { WOOD, TILE }

/** Named cell in the supplied 4 × 4 campus props atlas. */
enum class CampusPropAsset(val column: Int, val row: Int) {
    DESK(0, 0), TWIN_DESK(1, 0),
    SHARED_TABLE(2, 0), ROUND_TABLE(3, 0), BOOKCASE(1, 1), PLANT(2, 1),
    LOUNGE(0, 1), CAFE_COUNTER(3, 1), PHONE_BOOTH(1, 2), PRINTER(2, 2),
    WATER_COOLER(3, 2), WHITEBOARD(0, 3),
    BIKE_RACK(0, 2), RUG(1, 3), RECEPTION_DESK(2, 3), SOFA(3, 3),
}

/** Rendered wall/window band; its appearance is decorative, with source-supported placement where known. */
data class FloorWallSegment(
    val id: String,
    val regionId: String,
    val start: PlanPoint,
    val end: PlanPoint,
    val windows: Boolean = false,
)

/** A source-coordinate rectangle for a reusable atlas prop. */
data class FloorProp(
    val id: String,
    val regionId: String,
    val center: PlanPoint,
    val width: Double,
    val height: Double,
    val asset: CampusPropAsset,
    val layer: Int = 0,
) {
    val bounds: List<PlanPoint> get() = listOf(
        PlanPoint(center.x - width / 2, center.y - height / 2),
        PlanPoint(center.x + width / 2, center.y - height / 2),
        PlanPoint(center.x + width / 2, center.y + height / 2),
        PlanPoint(center.x - width / 2, center.y + height / 2),
    )
}

data class FloorMaterialPatch(val id: String, val regionId: String, val material: FloorMaterial)

data class FloorScene(
    val floorId: String,
    val materials: List<FloorMaterialPatch>,
    val walls: List<FloorWallSegment>,
    val props: List<FloorProp>,
    val sourceLinework: PlanLinework? = null,
)
