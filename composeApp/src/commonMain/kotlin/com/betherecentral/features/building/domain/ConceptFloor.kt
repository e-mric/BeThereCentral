package com.betherecentral.features.building.domain

/** Registration for the fictional Concept 01 floor illustration, in original image pixels. */
object ConceptFloorRegistration {
    const val sourceLeftPx = 24
    const val sourceTopPx = 48
    const val sourceWidthPx = 1488
    const val sourceHeightPx = 872
    const val sourceBottomPx = 920
    const val pixelsPerMeter = 20.0
    const val floorWidthMeters = 74.4
    const val floorHeightMeters = 43.6

    fun imageToWorld(point: Point2D): Point2D = Point2D(
        xMeters = (point.xMeters - sourceLeftPx) / pixelsPerMeter,
        yMeters = (sourceBottomPx - point.yMeters) / pixelsPerMeter,
    )
}
