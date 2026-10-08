package com.betherecentral.features.discovery.domain

/** Optional public resource; these URLs are community context, not fictional tenant sites. */
data class CommunityResource(val label: String, val url: String)

/** Fictional orientation text for the sample map. */
data class SampleRoomIntroduction(
    val purpose: String,
    val mission: String,
    val sampleNotice: String = "Sample content · This map and its room details are fictional.",
    val communityResources: List<CommunityResource> = emptyList(),
)
