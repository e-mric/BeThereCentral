package com.betherecentral.features.building.domain

/** A selected source badge, never an inferred doorway or a person's position. */
data class PlaceSelection(val place: PlanPlace, val anchor: PlanPoint?) {
    companion object {
        fun resolve(place: PlanPlace, tappedAnchor: PlanPoint? = null) = PlaceSelection(
            place, tappedAnchor?.takeIf { it in place.anchors } ?: place.anchors.firstOrNull(),
        )
    }
}

/** Keep the selected badge above the guide, with no fabricated focus for unplaced entries. */
fun PlanViewport.focusOn(selection: PlaceSelection): PlanViewport {
    val anchor = selection.anchor ?: return fit()
    val focused = copy(zoom = 2.6, pan = PlanPoint(0.0, 0.0))
    val point = focused.screen(anchor)
    return focused.copy(pan = PlanPoint(width * .5 - point.x, height * .28 - point.y))
}
