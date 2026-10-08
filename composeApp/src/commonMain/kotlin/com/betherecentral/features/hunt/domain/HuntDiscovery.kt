package com.betherecentral.features.hunt.domain

/** Fictional, sample-only orientation content shown at each cooperative hunt stop. */
data class HuntDiscovery(
    val checkpointId: String,
    val floorName: String,
    val area: String,
    val introduction: String,
    val spotlightRoomId: String,
    val spotlightText: String,
    val nearbyRoomIds: List<String>,
)
