package com.betherecentral.features.sharing

import com.betherecentral.features.sharing.domain.ShareDuration
import com.betherecentral.features.sharing.domain.ShareGrantState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShareExpiryTest {
    @Test
    fun expiresAtDeadlineAndRevocationIsImmediateAndIdempotent() {
        val grant = ShareGrantState.create("grant-1", setOf("friend-1"), 1_000L, ShareDuration.FIVE)
        assertTrue(grant.isActiveAt(1_000L))
        assertFalse(grant.isActiveAt(grant.expiresAtEpochMillis))
        val revoked = grant.revoke(2_000L)
        assertFalse(revoked.isActiveAt(2_000L))
        assertTrue(revoked.revoke(3_000L) == revoked)
    }
}
