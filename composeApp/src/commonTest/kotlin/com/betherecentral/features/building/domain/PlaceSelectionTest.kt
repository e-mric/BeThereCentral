package com.betherecentral.features.building.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaceSelectionTest {
    private val first = PlanPoint(1250.0, 850.0)
    private val second = PlanPoint(1480.0, 1010.0)
    private val place = PlanPlace("company", "Company", anchors = listOf(first, second))

    @Test fun repeatedBadgeSelectionKeepsTheClickedPositionAboveTheGuide() {
        val selection = PlaceSelection.resolve(place, second)
        assertEquals(second, selection.anchor)
        val view = PlanViewport(1080.0, 1700.0).focusOn(selection)
        assertEquals(2.6, view.zoom)
        assertEquals(540.0, view.screen(second).x, .001)
        assertEquals(476.0, view.screen(second).y, .001)
    }

    @Test fun searchAndForeignAnchorsUseTheFirstKnownPosition() {
        assertEquals(first, PlaceSelection.resolve(place).anchor)
        assertEquals(first, PlaceSelection.resolve(place, PlanPoint(0.0, 0.0)).anchor)
    }

    @Test fun unplacedEntryNeverInheritsAnotherPlacesFocus() {
        val selection = PlaceSelection.resolve(PlanPlace("sky", "The Sky"), second)
        assertNull(selection.anchor)
        val view = PlanViewport(1080.0, 1700.0, 4.0, PlanPoint(40.0, 90.0)).focusOn(selection)
        assertEquals(1.0, view.zoom)
        assertEquals(PlanPoint(0.0, 0.0), view.pan)
    }
}
