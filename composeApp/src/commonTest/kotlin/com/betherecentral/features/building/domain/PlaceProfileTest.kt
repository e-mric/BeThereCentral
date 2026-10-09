package com.betherecentral.features.building.domain

import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.data.PlaceProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlaceProfileTest {
    private fun place(id: String) = BeCentralPlans.floors.flatMap { it.places }.first { it.id == id }

    @Test
    fun classificationsFollowTheSourceDirectory() {
        assertEquals(PlaceKind.MEETING_ROOM, PlaceProfiles.forPlace(place("place-31")).kind)
        assertEquals(PlaceKind.COMPANY, PlaceProfiles.forPlace(place("place-21")).kind)
        assertEquals(PlaceKind.OTHER, PlaceProfiles.forPlace(place("place-19")).kind)
        assertEquals(PlaceKind.OTHER, PlaceProfiles.forPlace(place("place-66")).kind)
        assertEquals(PlaceKind.OTHER, PlaceProfiles.forPlace(place("place-38")).kind)
    }

    @Test
    fun profileFactsRequireAttributionAndLinksMustBeLabelledHttps() {
        assertFailsWith<IllegalArgumentException> { PlaceProfile(purpose = "A supplied purpose") }
        assertFailsWith<IllegalArgumentException> {
            PlaceProfile(purpose = "A supplied purpose", providedBy = "   ")
        }
        assertFailsWith<IllegalArgumentException> { PlaceLink("Website", "http://example.org") }
        assertFailsWith<IllegalArgumentException> { PlaceLink(" ", "https://example.org") }
        assertFailsWith<IllegalArgumentException> { PlaceLink("Website", "https://") }

        val profile = PlaceProfile(
            extraInfo = listOf("A fact supplied by the company"),
            links = listOf(PlaceLink("Website", "https://example.org/about")),
            providedBy = "Company representative",
        )
        assertEquals("Company representative", profile.providedBy)
    }
}
