package com.betherecentral.features.studio

import com.betherecentral.features.studio.domain.FurnitureKind
import com.betherecentral.features.studio.domain.RoomSceneCodec
import com.betherecentral.resources.Res
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class BundledRoomSceneTest {
    @Test fun bundledFixtureIsValidAndHasFourKinds() = runBlocking {
        val fixture = Res.readBytes("files/studio/demo-room.json").decodeToString()
        val decoded = RoomSceneCodec.parse(fixture)
        assertEquals("Your company", decoded.companyName)
        assertEquals(FurnitureKind.entries.toSet(), decoded.objects.map { it.kind }.toSet())
    }
}
