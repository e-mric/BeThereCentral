package com.betherecentral.features.communitystudio.presentation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

const val COMMUNITY_STUDIO_MAX_EXPORT_BYTES: Int = 65_536

/** Accepts only one bounded JSON object for the native, user-directed export flow. */
fun isValidCommunityStudioExportPayload(payload: String): Boolean {
    if (payload.encodeToByteArray().size > COMMUNITY_STUDIO_MAX_EXPORT_BYTES) return false
    return runCatching { Json.parseToJsonElement(payload) is JsonObject }.getOrDefault(false)
}
