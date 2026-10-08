package com.betherecentral.features.building

import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.domain.PlanPoint
import com.betherecentral.features.building.domain.PlanRegionKind
import com.betherecentral.features.building.domain.PlanWorld
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BeCentralPlansTest {
    @Test
    fun fiveSourceFloorsHaveStableDistinctGeometryAndLegendIds() {
        assertEquals(listOf("ground", "first", "second", "third", "fourth"),
            BeCentralPlans.floors.map { it.id })
        assertEquals(listOf(0, 1, 2, 3, 4), BeCentralPlans.floors.map { it.level })
        for (floor in BeCentralPlans.floors) {
            assertTrue(floor.regions.isNotEmpty(), floor.id)
            assertTrue(floor.walls.isNotEmpty(), floor.id)
            assertEquals(floor.regions.size, floor.regions.map { it.id }.toSet().size, floor.id)
            assertEquals(floor.places.size, floor.places.map { it.id }.toSet().size, floor.id)
            assertTrue(floor.places.all { place ->
                place.regionId == null || floor.regions.any { it.id == place.regionId }
            }, floor.id)
            assertTrue(floor.places.flatMap { it.anchors }.all {
                it.x in 0.0..BeCentralPlans.sourceWidth.toDouble() &&
                    it.y in 0.0..BeCentralPlans.sourceHeight.toDouble()
            }, floor.id)
        }
        val numbers = BeCentralPlans.floors.flatMap { floor -> floor.places.mapNotNull { it.number } }
        assertEquals((3..66).map(Int::toString), numbers)
    }

    @Test
    fun traceKeepsCourtyardsUnmappedSpaceAndDetachedWing() {
        val ground = BeCentralPlans.floor("ground")!!
        assertEquals(2, ground.footprints.size)
        assertEquals(1, ground.voids.size)
        assertTrue(ground.regions.any { it.kind == PlanRegionKind.UNMAPPED })
        assertTrue(PlanWorld.contains(ground, PlanPoint(1273.0, 1126.0)))
        assertFalse(PlanWorld.contains(ground, PlanPoint(850.0, 850.0)))

        val first = BeCentralPlans.floor("first")!!
        assertEquals(3, first.voids.size)
        assertFalse(PlanWorld.contains(first, PlanPoint(700.0, 960.0)),
            "the broad left arm of the central courtyard is open")
        val fari = first.regions.single { it.id == "first-fari" }
        assertTrue(PlanWorld.contains(fari.polygon, PlanPoint(1747.0, 1044.0)))
        assertFalse(PlanWorld.contains(first, PlanPoint(1480.0, 1080.0)))
        assertTrue(first.regions.any { it.kind == PlanRegionKind.UNMAPPED })

        val second = BeCentralPlans.floor("second")!!
        assertEquals(3, second.voids.size)
        assertTrue(second.regions.any { it.id == "second-unmapped" })
        assertFalse(PlanWorld.contains(second, PlanPoint(930.0, 820.0)))
        assertFalse(PlanWorld.contains(second, PlanPoint(700.0, 950.0)),
            "the courtyard extends west beneath the grey upper footprint")
        assertEquals("Central Perk", second.regions.single { it.id == "second-field" }.label)
        assertTrue(second.regions.any { it.id == "second-center-stair" && it.kind == PlanRegionKind.STAIRS })
        assertTrue(BeCentralPlans.floors.all { floor ->
            floor.regions.count { it.kind == PlanRegionKind.STAIRS } >= 2
        })
    }

    @Test
    fun numberedLegendEntriesRetainRepeatedAnchorsAndUnknownLocations() {
        val first = BeCentralPlans.floor("first")!!
        assertEquals(3, first.places.single { it.number == "6" }.anchors.size)
        assertEquals("MakePlan", first.places.single { it.number == "9" }.label)
        val second = BeCentralPlans.floor("second")!!
        assertEquals(3, second.places.single { it.number == "21" }.anchors.size)
        assertEquals(listOf("Besecure", "Curewiki", "EAIF", "European Startup Network",
            "Sandora VR", "Startup Factory", "WeTechCare"),
            second.places.single { it.number == "19" }.occupants)
        val third = BeCentralPlans.floor("third")!!
        assertEquals(2, third.places.single { it.number == "37" }.anchors.size)
        val fourth = BeCentralPlans.floor("fourth")!!
        assertEquals(6, fourth.places.single { it.number == "61" }.anchors.size)
        val unplaced = fourth.places.single { it.number == "66" }
        assertTrue(unplaced.anchors.isEmpty())
        assertEquals(listOf("Agence Digitale Solidaire", "SkillsFactory", "Moon 9",
            "Alliance4Europe", "I.CY"), unplaced.occupants)
    }

    @Test
    fun correctedCompanyAnchorsFollowSourceBadgesAcrossFloors() {
        // Independently checked original-image badge centres; see COMPANY_PLACEMENT_AUDIT.md.
        val expected = listOf(
            Triple("first", "3", PlanPoint(1747.0, 1072.0)),
            Triple("second", "36", PlanPoint(378.0, 944.0)),
            Triple("third", "37", PlanPoint(1124.0, 840.0)),
            Triple("fourth", "57", PlanPoint(618.0, 800.0)),
        )
        expected.forEach { (floor, number, anchor) ->
            assertTrue(anchor in BeCentralPlans.floor(floor)!!.places.single { it.number == number }.anchors)
        }
        val ground = BeCentralPlans.floor("ground")!!
        assertEquals(listOf(PlanPoint(1166.0, 1089.0)), ground.places.single { it.id == "bike-parking" }.anchors)
        assertTrue(ground.places.none { it.label.contains("toilet", ignoreCase = true) })
    }

    @Test
    fun everyPlottedPlaceAnchorFallsOnSelectablePlanFootprint() {
        val exceptions = setOf("entrance-cantersteen-12")
        val missed = BeCentralPlans.floors.flatMap { floor ->
            floor.places.filterNot { it.id in exceptions }.flatMap { place ->
                place.anchors.filterNot { PlanWorld.contains(floor, it) }
                    .map { "${floor.id}/${place.id}@${it.x},${it.y}" }
            }
        }
        assertTrue(missed.isEmpty(), "Unselectable source badges: ${missed.joinToString()}")
    }
}
