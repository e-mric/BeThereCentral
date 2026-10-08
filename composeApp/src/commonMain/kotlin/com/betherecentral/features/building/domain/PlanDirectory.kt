package com.betherecentral.features.building.domain

data class PlanSearchResult(val floor: PlanFloor, val place: PlanPlace)

/** Search transcribed labels and shared-area occupants without inventing a location. */
class PlanDirectory(floors: List<PlanFloor>) {
    private val entries = floors.flatMap { floor -> floor.places.map { PlanSearchResult(floor, it) } }

    fun search(query: String): List<PlanSearchResult> {
        val term = query.trim()
        return entries.filter { (_, place) ->
            term.isEmpty() || place.label.contains(term, ignoreCase = true) ||
                place.number.orEmpty().contains(term, ignoreCase = true) ||
                place.occupants.any { it.contains(term, ignoreCase = true) }
        }
    }
}
