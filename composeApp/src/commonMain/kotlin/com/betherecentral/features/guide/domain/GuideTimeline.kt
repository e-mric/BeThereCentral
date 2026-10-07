package com.betherecentral.features.guide.domain

import com.betherecentral.features.building.domain.Point2D
import com.betherecentral.features.navigation.domain.Route
import com.betherecentral.features.navigation.domain.RouteLeg
import com.betherecentral.features.navigation.domain.RouteMode

const val SAMPLE_GUIDE_METERS_PER_SECOND: Double = 4.0

enum class GuidePhase { MOVING, WAITING_FOR_FLOOR, ARRIVED }

enum class GuideHeading { LEFT, RIGHT }

data class GuideProgress(
    val legIndex: Int,
    val distanceOnLegMeters: Double,
    val paused: Boolean,
) {
    fun withPaused(paused: Boolean): GuideProgress = copy(paused = paused)
}

data class GuideFrame(
    val floorId: String,
    val point: Point2D,
    val phase: GuidePhase,
    val targetFloorId: String? = null,
    val transitionMode: RouteMode? = null,
    val heading: GuideHeading = GuideHeading.RIGHT,
)

/** A deterministic, clock-free playback of a sample route. */
class GuideTimeline private constructor(private val route: Route) {
    private val legs = route.legs

    fun initial(): GuideProgress = GuideProgress(0, 0.0, paused = false)

    fun advance(progress: GuideProgress, seconds: Double): GuideProgress {
        if (progress.paused || progress.legIndex >= legs.size || seconds <= 0.0) return normalize(progress)
        require(seconds.isFinite()) { "seconds must be finite" }
        var index = progress.legIndex.coerceAtLeast(0)
        var offset = progress.distanceOnLegMeters.coerceAtLeast(0.0)
        var remaining = seconds * SAMPLE_GUIDE_METERS_PER_SECOND
        while (index < legs.size) {
            val leg = legs[index]
            if (leg.mode != RouteMode.WALK) return GuideProgress(index, 0.0, progress.paused)
            val length = polylineLength(leg.points)
            if (length <= 0.0) {
                index++
                offset = 0.0
                continue
            }
            val left = (length - offset).coerceAtLeast(0.0)
            if (remaining < left) return GuideProgress(index, offset + remaining, progress.paused)
            remaining -= left
            index++
            offset = 0.0
        }
        return GuideProgress(legs.size, 0.0, progress.paused)
    }

    /** Confirms a floor change after a stairs or lift transition. */
    fun continueFloor(progress: GuideProgress): GuideProgress {
        val current = legs.getOrNull(progress.legIndex) ?: return normalize(progress)
        if (current.mode == RouteMode.WALK) return normalize(progress)
        return normalize(progress.copy(legIndex = progress.legIndex + 1, distanceOnLegMeters = 0.0))
    }

    fun frame(progress: GuideProgress): GuideFrame {
        val normalized = normalize(progress)
        val leg = legs.getOrNull(normalized.legIndex)
        val heading = headingAt(normalized.legIndex, normalized.distanceOnLegMeters)
        if (leg == null) {
            val last = legs.lastOrNull()
            val point = last?.points?.lastOrNull() ?: Point2D(0.0, 0.0)
            return GuideFrame(last?.floorId.orEmpty(), point, GuidePhase.ARRIVED, heading = heading)
        }
        if (leg.mode != RouteMode.WALK) {
            val targetFloor = legs.drop(normalized.legIndex + 1).firstOrNull()?.floorId
            return GuideFrame(
                floorId = leg.floorId,
                point = leg.points.lastOrNull() ?: leg.points.firstOrNull() ?: previousPoint(normalized.legIndex),
                phase = GuidePhase.WAITING_FOR_FLOOR,
                targetFloorId = targetFloor,
                transitionMode = leg.mode,
                heading = heading,
            )
        }
        return GuideFrame(leg.floorId, pointOnPolyline(leg.points, normalized.distanceOnLegMeters), GuidePhase.MOVING, heading = heading)
    }

    /** Uses the latest horizontal route tangent; vertical and stationary segments retain it. */
    private fun headingAt(legIndex: Int, distanceOnLeg: Double): GuideHeading {
        var heading = GuideHeading.RIGHT
        for ((index, leg) in legs.withIndex()) {
            if (index > legIndex) break
            if (leg.mode != RouteMode.WALK) continue
            var distanceBeforeSegment = 0.0
            for ((a, b) in leg.points.zipWithNext()) {
                val segmentLength = distance(a, b)
                if (index < legIndex || distanceBeforeSegment <= distanceOnLeg) {
                    val dx = b.xMeters - a.xMeters
                    if (dx < 0.0) heading = GuideHeading.LEFT
                    else if (dx > 0.0) heading = GuideHeading.RIGHT
                }
                distanceBeforeSegment += segmentLength
                if (index == legIndex && distanceBeforeSegment > distanceOnLeg) break
            }
        }
        return heading
    }

    private fun normalize(progress: GuideProgress): GuideProgress {
        val index = progress.legIndex.coerceIn(0, legs.size)
        if (index >= legs.size) return progress.copy(legIndex = legs.size, distanceOnLegMeters = 0.0)
        val leg = legs[index]
        if (leg.mode != RouteMode.WALK) return progress.copy(legIndex = index, distanceOnLegMeters = 0.0)
        return progress.copy(legIndex = index, distanceOnLegMeters = progress.distanceOnLegMeters.coerceIn(0.0, polylineLength(leg.points)))
    }

    private fun previousPoint(index: Int): Point2D = legs.take(index).asReversed().firstNotNullOfOrNull { it.points.lastOrNull() } ?: Point2D(0.0, 0.0)

    companion object {
        fun fromRoute(route: Route): GuideTimeline = GuideTimeline(route)
    }
}

private fun polylineLength(points: List<Point2D>): Double = points.zipWithNext().sumOf { (a, b) -> distance(a, b) }

private fun pointOnPolyline(points: List<Point2D>, distanceMeters: Double): Point2D {
    if (points.isEmpty()) return Point2D(0.0, 0.0)
    var remaining = distanceMeters.coerceAtLeast(0.0)
    for ((a, b) in points.zipWithNext()) {
        val segment = distance(a, b)
        if (segment == 0.0) continue
        if (remaining <= segment) {
            val t = remaining / segment
            return Point2D(a.xMeters + (b.xMeters - a.xMeters) * t, a.yMeters + (b.yMeters - a.yMeters) * t)
        }
        remaining -= segment
    }
    return points.last()
}

private fun distance(a: Point2D, b: Point2D): Double {
    val dx = b.xMeters - a.xMeters
    val dy = b.yMeters - a.yMeters
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
