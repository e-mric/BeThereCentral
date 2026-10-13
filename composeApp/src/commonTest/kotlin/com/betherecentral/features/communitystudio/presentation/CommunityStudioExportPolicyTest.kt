package com.betherecentral.features.communitystudio.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommunityStudioExportPolicyTest {
    @Test
    fun acceptsOnlyBoundedJsonObjects() {
        assertTrue(isValidCommunityStudioExportPayload("{\"room\":{}}"))
        assertFalse(isValidCommunityStudioExportPayload("[]"))
        assertFalse(isValidCommunityStudioExportPayload("{\"room\":}"))
        assertFalse(isValidCommunityStudioExportPayload("{} {}"))
    }

    @Test
    fun measuresUtf8BytesAndAcceptsTheExactLimit() {
        val exactLimit = "{\"x\":\"${"a".repeat(COMMUNITY_STUDIO_MAX_EXPORT_BYTES - 8)}\"}"
        assertTrue(isValidCommunityStudioExportPayload(exactLimit))
        assertFalse(isValidCommunityStudioExportPayload(exactLimit + " "))
        assertFalse(isValidCommunityStudioExportPayload("{\"x\":\"${"€".repeat(21_845)}\"}"))
    }
}
