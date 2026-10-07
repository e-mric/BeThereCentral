package com.betherecentral.features.sharing.domain

enum class ShareDuration(val minutes: Int) { FIVE(5), TEN(10), FIFTEEN(15) }

data class ShareGrantState(
    val id: String,
    val recipientIds: Set<String>,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val revokedAtEpochMillis: Long? = null,
) {
    fun isActiveAt(nowEpochMillis: Long): Boolean =
        revokedAtEpochMillis == null && nowEpochMillis < expiresAtEpochMillis

    fun revoke(atEpochMillis: Long): ShareGrantState =
        if (revokedAtEpochMillis != null) this else copy(revokedAtEpochMillis = atEpochMillis)

    companion object {
        fun create(id: String, recipientIds: Set<String>, createdAtEpochMillis: Long, duration: ShareDuration): ShareGrantState =
            ShareGrantState(
                id = id,
                recipientIds = recipientIds.toSet(),
                createdAtEpochMillis = createdAtEpochMillis,
                expiresAtEpochMillis = createdAtEpochMillis + duration.minutes * 60_000L,
            )
    }
}
