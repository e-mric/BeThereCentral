package com.betherecentral.features.guide.domain

import com.betherecentral.features.building.domain.Point2D
import com.betherecentral.features.building.data.DemoBuilding
import com.betherecentral.features.navigation.domain.Route
import com.betherecentral.features.navigation.domain.RouteLeg
import com.betherecentral.features.navigation.domain.RouteMode
import com.betherecentral.features.navigation.domain.RoutePreference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GuideTimelineTest {
    @Test
    fun leadingWestboundSegmentFacesLeftFromTheStartThroughPauseAndArrival() {
        val timeline = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(10, 0), p(0, 0))))
        val initial = timeline.initial()
        assertEquals(GuideHeading.LEFT, timeline.frame(initial).heading)

        val progress = timeline.advance(initial, 1.0)
        assertEquals(GuideHeading.LEFT, timeline.frame(progress).heading)
        val paused = progress.withPaused(true)
        assertEquals(GuideHeading.LEFT, timeline.frame(paused).heading)
        val arrived = timeline.advance(progress, 10.0)
        assertEquals(GuidePhase.ARRIVED, timeline.frame(arrived).phase)
        assertEquals(GuideHeading.LEFT, timeline.frame(arrived).heading)
    }

    @Test
    fun eastboundAndWestboundHeadingsPersistAcrossVerticalSegments() {
        val eastbound = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(0, 0), p(5, 0), p(5, 8))))
        assertEquals(GuideHeading.RIGHT, eastbound.frame(eastbound.initial()).heading)
        assertEquals(GuideHeading.RIGHT, eastbound.frame(eastbound.advance(eastbound.initial(), 1.5)).heading)
        assertEquals(GuideHeading.RIGHT, eastbound.frame(eastbound.advance(eastbound.initial(), 2.5)).heading)

        val westbound = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(8, 0), p(3, 0), p(3, 8))))
        assertEquals(GuideHeading.LEFT, westbound.frame(westbound.initial()).heading)
        assertEquals(GuideHeading.LEFT, westbound.frame(westbound.advance(westbound.initial(), 2.0)).heading)
        assertEquals(GuideHeading.LEFT, westbound.frame(timelineArrival(westbound)).heading)
    }

    @Test
    fun headingChangesAtAnExactHorizontalCornerAndSurvivesFloorChangeUntilMotionTurns() {
        val corner = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(0, 0), p(4, 0), p(0, 0))))
        assertEquals(GuideHeading.LEFT, corner.frame(corner.advance(corner.initial(), 1.0)).heading)

        val acrossFloors = GuideTimeline.fromRoute(route(
            leg("L1", RouteMode.WALK, p(10, 0), p(4, 0)),
            leg("L1", RouteMode.LIFT, p(4, 0)),
            leg("L2", RouteMode.WALK, p(4, 0), p(4, 3), p(10, 3)),
        ))
        val waiting = acrossFloors.advance(acrossFloors.initial(), 10.0)
        assertEquals(GuidePhase.WAITING_FOR_FLOOR, acrossFloors.frame(waiting).phase)
        assertEquals(GuideHeading.LEFT, acrossFloors.frame(waiting).heading)
        val continued = acrossFloors.continueFloor(waiting)
        assertEquals(GuideHeading.LEFT, acrossFloors.frame(continued).heading)
        assertEquals(GuideHeading.RIGHT, acrossFloors.frame(acrossFloors.advance(continued, 1.0)).heading)
    }

    @Test
    fun stationaryRouteUsesDefaultHeadingAndDoesNotEraseTheLastMovingHeading() {
        val still = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(4, 4), p(4, 4))))
        assertEquals(GuideHeading.RIGHT, still.frame(still.initial()).heading)
        assertEquals(GuideHeading.RIGHT, still.frame(still.advance(still.initial(), 1.0)).heading)

        val thenStill = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(0, 0), p(3, 0), p(3, 0))))
        assertEquals(GuideHeading.RIGHT, thenStill.frame(timelineArrival(thenStill)).heading)
    }

    @Test
    fun irregularPolylineUsesArcLengthAndSplitsAtCorners() {
        val timeline = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(0, 0), p(3, 0), p(3, 4))))
        val afterOneSecond = timeline.advance(timeline.initial(), 1.0)
        assertEquals(p(3, 1), timeline.frame(afterOneSecond).point)
        val afterCorner = timeline.advance(afterOneSecond, 0.75)
        assertEquals(p(3, 4), timeline.frame(afterCorner).point)
        assertEquals(GuidePhase.ARRIVED, timeline.frame(timeline.advance(afterCorner, 10.0)).phase)
    }

    @Test
    fun zeroLengthSegmentsAreSkippedAndSinglePointWalkCompletes() {
        val timeline = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(0, 0), p(0, 0), p(4, 0), p(4, 0))))
        val frame = timeline.frame(timeline.advance(timeline.initial(), 0.5))
        assertEquals(p(2, 0), frame.point)
        val done = timeline.advance(timeline.initial(), 1.0)
        assertEquals(GuidePhase.ARRIVED, timeline.frame(done).phase)

        val single = GuideTimeline.fromRoute(route(leg("L1", RouteMode.WALK, p(7, 2))))
        assertEquals(GuidePhase.ARRIVED, single.frame(single.advance(single.initial(), 1.0)).phase)
    }

    @Test
    fun stairsAndLiftWaitAtConnectorAndContinueToNextLegFloor() {
        for (mode in listOf(RouteMode.STAIRS, RouteMode.LIFT)) {
            val timeline = GuideTimeline.fromRoute(route(
                leg("L1", RouteMode.WALK, p(0, 0), p(4, 0)),
                leg("L1", mode, p(4, 0)),
                leg("L2", RouteMode.WALK, p(4, 0), p(8, 0)),
            ))
            val waiting = timeline.advance(timeline.initial(), 1.0)
            val frame = timeline.frame(waiting)
            assertEquals(GuidePhase.WAITING_FOR_FLOOR, frame.phase)
            assertEquals("L1", frame.floorId)
            assertEquals("L2", frame.targetFloorId)
            assertEquals(mode, frame.transitionMode)
            assertEquals(p(4, 0), frame.point)
            assertEquals(waiting, timeline.advance(waiting, 20.0))
            val resumed = timeline.continueFloor(waiting)
            assertEquals("L2", timeline.frame(resumed).floorId)
            assertEquals(p(4, 0), timeline.frame(resumed).point)
        }
    }

    @Test
    fun downwardAndChainedTransitionsUseEachNextLegFloor() {
        val timeline = GuideTimeline.fromRoute(route(
            leg("L3", RouteMode.WALK, p(0, 0), p(4, 0)),
            leg("L3", RouteMode.LIFT, p(4, 0)),
            leg("L2", RouteMode.WALK, p(4, 0), p(8, 0)),
            leg("L2", RouteMode.STAIRS, p(8, 0)),
            leg("L1", RouteMode.WALK, p(8, 0), p(12, 0)),
        ))

        val descendingInLift = timeline.advance(timeline.initial(), 1.0)
        assertEquals("L3", timeline.frame(descendingInLift).floorId)
        assertEquals("L2", timeline.frame(descendingInLift).targetFloorId)
        assertEquals(RouteMode.LIFT, timeline.frame(descendingInLift).transitionMode)

        val atDownStairs = timeline.advance(timeline.continueFloor(descendingInLift), 1.0)
        assertEquals(GuidePhase.WAITING_FOR_FLOOR, timeline.frame(atDownStairs).phase)
        assertEquals("L2", timeline.frame(atDownStairs).floorId)
        assertEquals("L1", timeline.frame(atDownStairs).targetFloorId)
        assertEquals(RouteMode.STAIRS, timeline.frame(atDownStairs).transitionMode)

        val destinationFloor = timeline.continueFloor(atDownStairs)
        assertEquals("L1", timeline.frame(destinationFloor).floorId)
        assertEquals(p(8, 0), timeline.frame(destinationFloor).point)
    }

    @Test
    fun pauseStopsProgressAndResumeContinues() {
        val timeline = GuideTimeline.fromRoute(route(leg("a", RouteMode.WALK, p(0, 0), p(12, 0))))
        val progress = timeline.advance(timeline.initial(), 1.0)
        val paused = progress.withPaused(true)
        assertEquals(paused, timeline.advance(paused, 50.0))
        assertEquals(progress, paused.withPaused(false))
        assertEquals(p(8, 0), timeline.frame(timeline.advance(paused.withPaused(false), 1.0)).point)
    }

    @Test
    fun completionClampsAtFinalRouteEntranceAndEmptyRouteArrivesSafely() {
        val timeline = GuideTimeline.fromRoute(route(leg("a", RouteMode.WALK, p(0, 0), p(3, 4))))
        val done = timeline.advance(timeline.initial(), 100.0)
        assertEquals(GuideFrame("a", p(3, 4), GuidePhase.ARRIVED), timeline.frame(done))
        assertEquals(done, timeline.advance(done, 100.0))

        val empty = GuideTimeline.fromRoute(route())
        assertEquals(GuidePhase.ARRIVED, empty.frame(empty.initial()).phase)
        assertEquals(empty.initial(), empty.advance(empty.initial(), 2.0))
    }

    @Test
    fun demoBuildingRoutesPlayUpAndDownThroughEveryFloorAndReachRoomEntrance() {
        fun walkthrough(checkpointId: String, roomId: String) {
            val plannedRoute = assertNotNull(DemoBuilding.findRoute(checkpointId, roomId, RoutePreference.DEFAULT))
            val timeline = GuideTimeline.fromRoute(plannedRoute)
            var progress = timeline.initial()
            val observedTransitions = mutableListOf<String>()
            var iterations = 0

            while (timeline.frame(progress).phase != GuidePhase.ARRIVED && iterations++ < 100) {
                progress = timeline.advance(progress, 1_000.0)
                val frame = timeline.frame(progress)
                if (frame.phase == GuidePhase.WAITING_FOR_FLOOR) {
                    observedTransitions += "${frame.transitionMode}:${frame.floorId}->${frame.targetFloorId}"
                    progress = timeline.continueFloor(progress)
                }
            }

            val expectedTransitions = plannedRoute.legs.mapIndexedNotNull { index, leg ->
                if (leg.mode == RouteMode.WALK) null
                else "${leg.mode}:${leg.floorId}->${plannedRoute.legs.getOrNull(index + 1)?.floorId}"
            }
            assertEquals(expectedTransitions, observedTransitions)
            assertTrue(expectedTransitions.isNotEmpty())
            val destination = DemoBuilding.rooms.single { it.id == roomId }
            assertEquals(GuidePhase.ARRIVED, timeline.frame(progress).phase)
            assertEquals(destination.floorId, timeline.frame(progress).floorId)
            assertEquals(destination.entrance, timeline.frame(progress).point)
        }

        walkthrough("cp-l1-lobby", "room-l4-orbit")
        walkthrough("cp-l4-lobby", "room-l1-cafe")

        val replacement = GuideTimeline.fromRoute(route(leg("replacement", RouteMode.WALK, p(5, 6), p(9, 6))))
        assertEquals(p(5, 6), replacement.frame(replacement.initial()).point)
        val cleared = GuideTimeline.fromRoute(route())
        assertEquals(GuidePhase.ARRIVED, cleared.frame(cleared.initial()).phase)
        assertFalse(cleared.initial().paused)
    }

    private fun p(x: Number, y: Number) = Point2D(x.toDouble(), y.toDouble())
    private fun timelineArrival(timeline: GuideTimeline) = timeline.advance(timeline.initial(), 1_000.0)
    private fun leg(floor: String, mode: RouteMode, vararg points: Point2D) = RouteLeg("from", "to", floor, mode, 0.0, points.toList())
    private fun route(vararg legs: RouteLeg) = Route("checkpoint", "room", legs.toList(), 0.0, 0, false, RoutePreference.DEFAULT)
}
