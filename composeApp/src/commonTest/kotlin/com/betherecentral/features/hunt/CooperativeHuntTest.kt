package com.betherecentral.features.hunt

import com.betherecentral.features.hunt.domain.CooperativeHunt
import com.betherecentral.features.hunt.domain.HuntRejection
import com.betherecentral.features.hunt.domain.HuntResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CooperativeHuntTest {
    @Test
    fun acceptsOnlyNextCheckpointAndContributionsAreIdempotent() {
        val initial = CooperativeHunt()
        assertEquals(HuntRejection.OUT_OF_ORDER, (initial.contribute("Alex", "cp-l2-lobby") as HuntResult.Rejected).reason)
        val first = (initial.contribute("Alex", "cp-l1-west") as HuntResult.Accepted).hunt
        assertEquals(HuntRejection.ALREADY_FOUND, (first.contribute("Sam", "cp-l1-west") as HuntResult.Rejected).reason)
        val second = (first.contribute("Sam", "cp-l1-lobby") as HuntResult.Accepted).hunt
        assertEquals(2, second.completedCheckpointCount)
        assertTrue(second.contributions.any { it.playerId == "Sam" && it.checkpointId == "cp-l1-lobby" })
    }
}
