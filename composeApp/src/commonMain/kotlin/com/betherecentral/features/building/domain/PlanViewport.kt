package com.betherecentral.features.building.domain

import kotlin.math.min

/** View state only: source-image pixels are not building measurements. */
data class PlanViewport(
    val width: Double,
    val height: Double,
    val zoom: Double = 1.0,
    val pan: PlanPoint = PlanPoint(0.0, 0.0),
) {
    private val sourceLeft = 80.0
    private val sourceTop = 580.0
    private val sourceWidth = 1860.0
    private val sourceHeight = 700.0
    val scale = min(width / sourceWidth, height / sourceHeight).coerceAtLeast(0.001) * zoom
    private val left = (width - sourceWidth * scale) / 2 + pan.x
    private val top = (height - sourceHeight * scale) / 2 + pan.y

    fun screen(point: PlanPoint) = PlanPoint(
        left + (point.x - sourceLeft) * scale,
        top + (point.y - sourceTop) * scale,
    )

    fun plan(point: PlanPoint) = PlanPoint(
        (point.x - left) / scale + sourceLeft,
        (point.y - top) / scale + sourceTop,
    )

    fun transform(factor: Double, centroid: PlanPoint, translation: PlanPoint): PlanViewport {
        if (!factor.isFinite() || factor <= 0 ||
            !centroid.x.isFinite() || !centroid.y.isFinite() ||
            !translation.x.isFinite() || !translation.y.isFinite()) return this
        val nextZoom = (zoom * factor).coerceIn(0.7, 8.0)
        val ratio = nextZoom / zoom
        return copy(zoom = nextZoom, pan = PlanPoint(
            pan.x * ratio + (centroid.x - width / 2) * (1 - ratio) + translation.x,
            pan.y * ratio + (centroid.y - height / 2) * (1 - ratio) + translation.y,
        ))
    }

    fun fit() = copy(zoom = 1.0, pan = PlanPoint(0.0, 0.0))
}
