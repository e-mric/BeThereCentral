package com.betherecentral.features.checkpoints.domain

import com.betherecentral.features.building.domain.Checkpoint

data class CheckpointObservation(val checkpoint: Checkpoint, val observedAtEpochMillis: Long)

enum class CheckpointPayloadError { MALFORMED, WRONG_BUILDING, UNKNOWN_CHECKPOINT }
sealed interface CheckpointValidation {
    data class Valid(val checkpoint: Checkpoint) : CheckpointValidation
    data class Invalid(val error: CheckpointPayloadError) : CheckpointValidation
}

/** Local sample validation and latest-observation state. This does not scan a camera. */
class CheckpointTracker(
    private val buildingId: String,
    private val checkpoints: List<Checkpoint>,
) {
    var latestObservation: CheckpointObservation? = null
        private set

    fun validatePayload(payload: String): CheckpointValidation {
        if (!payload.startsWith("btcentral://")) {
            return CheckpointValidation.Invalid(CheckpointPayloadError.MALFORMED)
        }
        val parts = payload.removePrefix("btcentral://").split('/')
        if (parts.size != 3 || parts.any { it.isBlank() } || parts[1] != "checkpoint") {
            return CheckpointValidation.Invalid(CheckpointPayloadError.MALFORMED)
        }
        if (parts[0] != buildingId) return CheckpointValidation.Invalid(CheckpointPayloadError.WRONG_BUILDING)
        val match = checkpoints.firstOrNull { it.id == parts[2] }
            ?: return CheckpointValidation.Invalid(CheckpointPayloadError.UNKNOWN_CHECKPOINT)
        return CheckpointValidation.Valid(match)
    }

    fun acceptPayload(payload: String, observedAtEpochMillis: Long): CheckpointObservation? {
        val validation = validatePayload(payload)
        if (validation !is CheckpointValidation.Valid) return null
        return CheckpointObservation(validation.checkpoint, observedAtEpochMillis).also { latestObservation = it }
    }
}
