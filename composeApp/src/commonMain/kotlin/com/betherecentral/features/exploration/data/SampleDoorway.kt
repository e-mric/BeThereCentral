package com.betherecentral.features.exploration.data

import com.betherecentral.features.building.domain.Point2D
import com.betherecentral.features.building.domain.Room

/** A demonstration doorway, not a surveyed transform into the unrelated engine-room capture. */
object SampleDoorway {
    const val roomId = "room-l1-reception"

    fun positionOn(floorId: String, rooms: List<Room>): Point2D? =
        rooms.firstOrNull { it.id == roomId && it.floorId == floorId }?.let { room ->
            // Just inside reception's entrance, using the same world projection as the map.
            Point2D(room.entrance.xMeters, room.entrance.yMeters - 2.0)
        }
}
