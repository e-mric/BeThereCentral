package com.betherecentral.features.hunt.domain


/** Same-device sample hunt state; contributions are idempotent per player and checkpoint. */
data class CooperativeHunt(
    val checkpointIds: List<String> = listOf("cp-l1-west", "cp-l1-lobby", "cp-l2-lobby", "cp-l3-lobby", "cp-l4-lobby"),
    val players: List<String> = listOf("Alex", "Sam"),
    val contributions: Set<HuntContribution> = emptySet(),
) {
    val currentCheckpointId: String?
        get() = checkpointIds.firstOrNull { id -> contributions.none { it.checkpointId == id } }

    val completed: Boolean get() = currentCheckpointId == null
    val completedCheckpointCount: Int
        get() = checkpointIds.count { id -> contributions.any { it.checkpointId == id } }

    fun contribute(playerId: String, checkpointId: String): HuntResult {
        if (playerId !in players) return HuntResult.Rejected(HuntRejection.UNKNOWN_PLAYER)
        if (checkpointId !in checkpointIds) return HuntResult.Rejected(HuntRejection.UNKNOWN_CHECKPOINT)
        if (contributions.any { it.checkpointId == checkpointId }) return HuntResult.Rejected(HuntRejection.ALREADY_FOUND)
        if (checkpointId != currentCheckpointId) return HuntResult.Rejected(HuntRejection.OUT_OF_ORDER)
        return HuntResult.Accepted(copy(contributions = contributions + HuntContribution(playerId, checkpointId)))
    }
}

data class HuntContribution(val playerId: String, val checkpointId: String)
enum class HuntRejection { UNKNOWN_PLAYER, UNKNOWN_CHECKPOINT, ALREADY_FOUND, OUT_OF_ORDER }
sealed interface HuntResult {
    data class Accepted(val hunt: CooperativeHunt) : HuntResult
    data class Rejected(val reason: HuntRejection) : HuntResult
}
