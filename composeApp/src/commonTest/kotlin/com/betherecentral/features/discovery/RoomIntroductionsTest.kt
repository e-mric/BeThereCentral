package com.betherecentral.features.discovery

import com.betherecentral.features.building.data.DemoBuilding
import com.betherecentral.features.discovery.data.RoomIntroductions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomIntroductionsTest {
    @Test
    fun everySampleRoomHasPurposefulFloorConsistentCopy() {
        DemoBuilding.rooms.forEach { room ->
            val intro = RoomIntroductions.forRoom(room)
            assertTrue(intro.purpose.isNotBlank(), room.id)
            assertTrue(intro.mission.isNotBlank(), room.id)
            assertTrue(intro.sampleNotice.contains("Sample", ignoreCase = true), room.id)
        }
        assertEquals(48, DemoBuilding.rooms.size)
        val sameKinds = DemoBuilding.rooms.groupBy { it.id.replace(Regex("^room-l[0-9]+-"), "") }
        sameKinds.values.forEach { floors ->
            assertEquals(4, floors.size)
            assertEquals(1, floors.map { RoomIntroductions.forRoom(it) }.distinct().size)
        }
        val companyMissions = mapOf(
            "spark" to "Spark turns early ideas into small prototypes.",
            "orbit" to "Orbit explores ways to make everyday work simpler.",
            "moss" to "Moss makes tools for greener city living.",
            "north" to "North studies clearer ways to share information.",
        )
        companyMissions.forEach { (slug, mission) ->
            DemoBuilding.rooms.filter { it.id.endsWith("-$slug") }.forEach { room ->
                assertEquals("Workspace", room.category)
                assertEquals(mission, RoomIntroductions.forRoom(room).mission)
            }
        }
    }

    @Test
    fun onlyAllowlistedCommunityResourcesAreOffered() {
        val allowed = setOf("https://www.becentral.org/", "https://www.becentral.org/programs/we-are-founders")
        DemoBuilding.rooms.flatMap { RoomIntroductions.forRoom(it).communityResources }.forEach { resource ->
            assertTrue(resource.url.startsWith("https://"))
            assertTrue(resource.url in allowed, resource.url)
            assertTrue("community" in resource.label.lowercase() || "program" in resource.label.lowercase())
        }
        assertFalse(DemoBuilding.rooms.flatMap { RoomIntroductions.forRoom(it).communityResources }.any { "orbit" in it.url || "spark" in it.url || "moss" in it.url || "north" in it.url })
        assertEquals(2, RoomIntroductions.forRoom(DemoBuilding.rooms.first { it.id.endsWith("reception") }).communityResources.size)
    }
}
