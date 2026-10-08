package com.betherecentral.features.hunt.data

import com.betherecentral.features.building.data.DemoBuilding
import com.betherecentral.features.hunt.domain.HuntDiscovery

/** Fictional discovery cards backed by the building's sample floors and rooms. */
object HuntDiscoveries {
    private fun card(
        checkpointId: String,
        floorId: String,
        area: String,
        introduction: String,
        spotlightRoomId: String,
        spotlightText: String,
        vararg nearbyRoomIds: String,
    ) = HuntDiscovery(
        checkpointId = checkpointId,
        floorName = DemoBuilding.floors.first { it.id == floorId }.name,
        area = area,
        introduction = introduction,
        spotlightRoomId = spotlightRoomId,
        spotlightText = spotlightText,
        nearbyRoomIds = nearbyRoomIds.toList(),
    )

    private val byCheckpointId = listOf(
        card("cp-l1-west", "floor-1", "West entrance", "Start at the west side of the sample coworking floor.", "room-l1-spark", "Spark is a fictional studio turning early ideas into small prototypes.", "room-l1-spark", "room-l1-orbit"),
        card("cp-l1-lobby", "floor-1", "Lobby", "Find your bearings near reception and the shared spaces.", "room-l1-orbit", "Orbit is a fictional team exploring ways to make everyday work simpler.", "room-l1-reception", "room-l1-cafe", "room-l1-lounge", "room-l1-orbit"),
        card("cp-l2-lobby", "floor-2", "Lobby", "Explore the first-floor workspaces and meeting rooms.", "room-l2-moss", "Moss is a fictional design group making tools for greener city living.", "room-l2-orbit", "room-l2-meeting-01", "room-l2-moss"),
        card("cp-l3-lobby", "floor-3", "Lobby", "Look around the second-floor suites and quiet spaces.", "room-l3-north", "North is a fictional research team studying clearer ways to share information.", "room-l3-spark", "room-l3-quiet", "room-l3-north"),
        card("cp-l4-lobby", "floor-4", "Lobby", "Finish among the upper-floor sample workspaces.", "room-l4-orbit", "Orbit is a fictional team exploring ways to make everyday work simpler.", "room-l4-moss", "room-l4-meeting-02", "room-l4-north", "room-l4-orbit"),
    ).associateBy(HuntDiscovery::checkpointId)

    fun forCheckpoint(checkpointId: String): HuntDiscovery? = byCheckpointId[checkpointId]

    fun nearbyRooms(discovery: HuntDiscovery) = discovery.nearbyRoomIds.mapNotNull { id ->
        DemoBuilding.rooms.firstOrNull { it.id == id }
    }

    fun spotlightRoom(discovery: HuntDiscovery) = DemoBuilding.rooms.firstOrNull { it.id == discovery.spotlightRoomId }
}
