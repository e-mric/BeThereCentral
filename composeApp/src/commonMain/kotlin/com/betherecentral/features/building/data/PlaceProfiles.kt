package com.betherecentral.features.building.data

import com.betherecentral.features.building.domain.PlaceKind
import com.betherecentral.features.building.domain.PlaceProfile
import com.betherecentral.features.building.domain.PlanPlace

/** Source-grounded place categories. Profiles intentionally add no unsourced tenant claims. */
object PlaceProfiles {
    private val companyIds = setOf(
        3, 9, 10, 11, 12, 13, 14, 15, 18, 20, 21, 23, 27, 33, 35, 36, 37,
        39, 43, 46, 47, 48, 49, 50, 53, 54, 55, 56, 57, 58, 59, 61, 62, 63, 64,
    ).mapTo(mutableSetOf()) { "place-$it" }

    fun forPlace(place: PlanPlace): PlaceProfile = when (place.id) {
        "place-31" -> PlaceProfile(kind = PlaceKind.MEETING_ROOM)
        "place-19", "place-65", "place-66" -> PlaceProfile(kind = PlaceKind.OTHER)
        in companyIds -> PlaceProfile(kind = PlaceKind.COMPANY)
        else -> PlaceProfile()
    }
}
