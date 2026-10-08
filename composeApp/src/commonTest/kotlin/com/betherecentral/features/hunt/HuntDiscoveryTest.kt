package com.betherecentral.features.hunt

import com.betherecentral.features.hunt.domain.CooperativeHunt
import com.betherecentral.features.hunt.data.HuntDiscoveries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HuntDiscoveryTest {
    @Test
    fun everyOrderedHuntStopHasSampleContentWithExistingNearbyRooms() {
        val hunt = CooperativeHunt()
        val discoveries = hunt.checkpointIds.map { id -> assertNotNull(HuntDiscoveries.forCheckpoint(id)) }

        assertEquals(hunt.checkpointIds, discoveries.map { it.checkpointId })
        discoveries.forEach { card ->
            val nearbyRooms = HuntDiscoveries.nearbyRooms(card)
            assertTrue(nearbyRooms.isNotEmpty(), "${card.checkpointId} should name nearby sample rooms")
            assertEquals(card.nearbyRoomIds.size, nearbyRooms.size, "All room references should resolve")
            assertTrue(nearbyRooms.all { it.id.startsWith("room-l") })
            val spotlightRoom = assertNotNull(HuntDiscoveries.spotlightRoom(card))
            assertTrue(spotlightRoom in nearbyRooms, "Spotlight company should be one of the nearby rooms")
            assertTrue(spotlightRoom.name.endsWith(card.floorName), "Spotlight must belong to ${card.floorName}")
            assertTrue(card.spotlightText.isNotBlank(), "${card.checkpointId} should explain its fictional company")
            assertTrue(card.spotlightRoomId.startsWith("room-l"))
        }
        assertEquals("room-l1-spark", discoveries.first().spotlightRoomId)
        assertEquals("room-l4-orbit", discoveries.last().spotlightRoomId)
    }
}
