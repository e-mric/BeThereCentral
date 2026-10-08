package com.betherecentral.features.discovery.data

import com.betherecentral.features.building.domain.Room
import com.betherecentral.features.discovery.domain.CommunityResource
import com.betherecentral.features.discovery.domain.SampleRoomIntroduction

/** Sample-only room copy for the fictional four-floor coworking map. */
object RoomIntroductions {
    private val communityResources = listOf(
        CommunityResource("BeCentral community resource", "https://www.becentral.org/"),
        CommunityResource("WeAreFounders program resource", "https://www.becentral.org/programs/we-are-founders"),
    )

    fun forRoom(room: Room): SampleRoomIntroduction {
        // IDs contain multi-part slugs, so recover the part after the floor prefix.
        val slug = room.id.replace(Regex("^room-l[0-9]+-"), "")
        val details = when (slug) {
            "spark" -> SampleRoomIntroduction("A fictional studio workspace for trying early ideas.", "Spark turns early ideas into small prototypes.")
            "orbit" -> SampleRoomIntroduction("A fictional team workspace for thoughtful experiments.", "Orbit explores ways to make everyday work simpler.")
            "moss" -> SampleRoomIntroduction("A fictional design workspace for city-focused projects.", "Moss makes tools for greener city living.")
            "north" -> SampleRoomIntroduction("A fictional research workspace for clear communication.", "North studies clearer ways to share information.")
            "meeting-01", "meeting-02" -> SampleRoomIntroduction("A shared meeting room for sample team conversations.", "Bring people together to compare ideas and decide what to try next.")
            "cafe" -> SampleRoomIntroduction("A shared café for a pause, a coffee or an informal chat.", "Good ideas can start around a shared table.")
            "lounge" -> SampleRoomIntroduction("A shared lounge for informal conversations and breaks.", "Take a moment to connect with people across the sample building.")
            "quiet" -> SampleRoomIntroduction("A quiet room for focused work or a short reset.", "Make space for concentration in a busy shared workplace.")
            "phone-booth-1", "phone-booth-2" -> SampleRoomIntroduction("A small phone booth for a private call or focused conversation.", "Find a little privacy without leaving the shared workspace.")
            "reception" -> SampleRoomIntroduction(
                "The sample building's welcome point, beside the shared spaces.",
                "Start here to get oriented. Community links provide real-world context; the mapped rooms and tenants remain fictional.",
                communityResources = communityResources,
            )
            else -> SampleRoomIntroduction("A room in the fictional sample building.", "Explore the map to learn how its shared spaces fit together.")
        }
        // Floor-qualified room names are shown by the UI; content deliberately stays consistent across reused floors.
        return details
    }
}
