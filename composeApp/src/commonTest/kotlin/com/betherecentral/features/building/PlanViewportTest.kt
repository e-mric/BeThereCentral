package com.betherecentral.features.building

import com.betherecentral.features.building.domain.PlanPoint
import com.betherecentral.features.building.domain.PlanViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanViewportTest {
    @Test fun sourcePointsRoundTripAcrossViewportShapes() {
        for ((width, height) in listOf(360.0 to 600.0, 900.0 to 320.0, 1440.0 to 900.0)) {
            val viewport = PlanViewport(width, height, 2.4, PlanPoint(54.0, -82.0))
            val source = PlanPoint(1273.0, 1126.0)
            val restored = viewport.plan(viewport.screen(source))
            assertEquals(source.x, restored.x, 0.000001)
            assertEquals(source.y, restored.y, 0.000001)
        }
    }

    @Test fun pinchKeepsTheSourcePointUnderTheFingersAndPanMovesIt() {
        val viewport = PlanViewport(412.0, 720.0, 1.6, PlanPoint(43.0, -90.0))
        val fingers = PlanPoint(87.0, 280.0)
        val source = viewport.plan(fingers)
        val next = viewport.transform(1.4, fingers, PlanPoint(18.0, -7.0))
        val result = next.screen(source)
        assertEquals(fingers.x + 18, result.x, 0.000001)
        assertEquals(fingers.y - 7, result.y, 0.000001)
    }

    @Test fun fitRecoversTheWholeRegisteredPlanAndInvalidGesturesDoNothing() {
        val viewport = PlanViewport(412.0, 720.0, 4.0, PlanPoint(-370.0, 240.0))
        val fit = viewport.fit()
        for (source in listOf(PlanPoint(80.0, 580.0), PlanPoint(1940.0, 1280.0))) {
            val screen = fit.screen(source)
            assertTrue(screen.x >= -0.000001 && screen.x <= fit.width + 0.000001)
            assertTrue(screen.y >= -0.000001 && screen.y <= fit.height + 0.000001)
        }
        assertEquals(viewport, viewport.transform(Double.NaN, PlanPoint(0.0, 0.0), PlanPoint(0.0, 0.0)))
        assertEquals(8.0, viewport.transform(20.0, PlanPoint(0.0, 0.0), PlanPoint(0.0, 0.0)).zoom)
    }
}
