package com.betherecentral.features.checkpoints

import com.betherecentral.features.checkpoints.domain.CheckpointPayloadError
import com.betherecentral.features.checkpoints.domain.CheckpointTracker
import com.betherecentral.features.checkpoints.domain.CheckpointValidation
import com.betherecentral.features.building.data.DemoBuilding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckpointTrackerTest {
    @Test
    fun validatesSamplePayloadAndRejectsMalformedForeignAndUnknownPayloads() {
        val tracker = CheckpointTracker(DemoBuilding.id, DemoBuilding.checkpoints)
        val accepted = tracker.validatePayload("btcentral://sample-building/checkpoint/cp-l1-lobby")
        assertEquals("cp-l1-lobby", (accepted as CheckpointValidation.Valid).checkpoint.id)
        assertEquals(CheckpointPayloadError.MALFORMED, (tracker.validatePayload("https://sample-building/checkpoint/cp-l1-lobby") as CheckpointValidation.Invalid).error)
        assertEquals(CheckpointPayloadError.WRONG_BUILDING, (tracker.validatePayload("btcentral://other-building/checkpoint/cp-l1-lobby") as CheckpointValidation.Invalid).error)
        assertEquals(CheckpointPayloadError.UNKNOWN_CHECKPOINT, (tracker.validatePayload("btcentral://sample-building/checkpoint/unknown") as CheckpointValidation.Invalid).error)
        assertEquals(CheckpointPayloadError.MALFORMED, (tracker.validatePayload("btcentral://sample-building/checkpoint/cp-l1-lobby/extra") as CheckpointValidation.Invalid).error)
    }

    @Test
    fun storesOnlyTheLatestValidObservation() {
        val tracker = CheckpointTracker(DemoBuilding.id, DemoBuilding.checkpoints)
        assertEquals("cp-l1-lobby", tracker.acceptPayload("btcentral://sample-building/checkpoint/cp-l1-lobby", 100L)?.checkpoint?.id)
        assertNull(tracker.acceptPayload("btcentral://other/checkpoint/cp-l2-lobby", 200L))
        val latest = tracker.acceptPayload("btcentral://sample-building/checkpoint/cp-l2-lobby", 300L)
        assertEquals("cp-l2-lobby", tracker.latestObservation?.checkpoint?.id)
        assertEquals(300L, latest?.observedAtEpochMillis)
    }
}
