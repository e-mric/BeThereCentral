package com.betherecentral.features.exploration

import com.betherecentral.features.building.data.DemoBuilding
import com.betherecentral.features.exploration.data.SampleDoorway
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SampleDoorwayTest {
    @Test fun doorwayIsInsideGroundReception() {
        val room = DemoBuilding.rooms.single { it.id == SampleDoorway.roomId }
        val point = assertNotNull(SampleDoorway.positionOn(room.floorId, DemoBuilding.rooms))
        assertTrue(point.xMeters in (room.center.xMeters - room.widthMeters / 2)..(room.center.xMeters + room.widthMeters / 2))
        assertTrue(point.yMeters in (room.center.yMeters - room.heightMeters / 2)..(room.center.yMeters + room.heightMeters / 2))
    }

    @Test fun repeatedArtworkDoesNotCreateDoorwaysOnOtherFloors() {
        DemoBuilding.floors.drop(1).forEach { assertNull(SampleDoorway.positionOn(it.id, DemoBuilding.rooms)) }
        assertNull(SampleDoorway.positionOn("floor-1", emptyList()))
    }
}
