package com.betherecentral.features.building

import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.domain.PlanDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanDirectoryTest {
    private val directory = PlanDirectory(BeCentralPlans.floors)

    @Test fun knownCompanyReturnsItsFloorAndRepeatedSourceAnchors() {
        val result = directory.search("  cAmPfIrE aI ").single()
        assertEquals("second", result.floor.id)
        assertEquals("21", result.place.number)
        assertEquals(3, result.place.anchors.size)
    }

    @Test fun sharedAreaOccupantsAndUnknownPositionsAreSearchable() {
        val shared = directory.search("WeTechCare").single()
        assertEquals("19", shared.place.number)
        assertEquals("second", shared.floor.id)
        val sky = directory.search("SkillsFactory").single { it.floor.id == "fourth" }
        assertEquals("66", sky.place.number)
        assertTrue(sky.place.anchors.isEmpty())
        assertEquals(sky, directory.search("66").single())
    }

    @Test fun anEmptySearchListsAllPlacesAndAnUnknownNameHasNoResult() {
        assertEquals(BeCentralPlans.floors.sumOf { it.places.size }, directory.search("  ").size)
        assertTrue(directory.search("a tenant not supplied").isEmpty())
    }
}
