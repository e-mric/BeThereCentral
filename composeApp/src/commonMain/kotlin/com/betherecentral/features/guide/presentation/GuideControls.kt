package com.betherecentral.features.guide.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.core.presentation.AccentText
import com.betherecentral.core.presentation.Hairline
import com.betherecentral.core.presentation.Ink
import com.betherecentral.core.presentation.Muted
import com.betherecentral.core.presentation.SectionLabel
import com.betherecentral.features.building.domain.Floor
import com.betherecentral.features.building.domain.Room
import com.betherecentral.features.guide.domain.GuideFrame
import com.betherecentral.features.guide.domain.GuidePhase
import com.betherecentral.features.navigation.domain.RouteMode

/** Controls for a simulated guide; its position never changes the last-seen checkpoint. */
@Composable
fun GuideControls(
    frame: GuideFrame?,
    paused: Boolean,
    destination: Room,
    floors: List<Floor>,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onContinueFloor: () -> Unit,
    onReplay: () -> Unit,
    onClose: () -> Unit,
) {
    HorizontalDivider(color = Hairline)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SectionLabel("SAMPLE GUIDE PREVIEW")
            val message = when (frame?.phase) {
                null -> "Tiny founder with an orange backpack"
                GuidePhase.MOVING -> if (paused) "Paused · ${floorName(floors, frame.floorId)}" else "Walking to ${destination.name}"
                GuidePhase.WAITING_FOR_FLOOR -> {
                    val connector = if (frame.transitionMode == RouteMode.LIFT) "Lift" else "Stairs"
                    "$connector to ${floorName(floors, frame.targetFloorId)}"
                }
                GuidePhase.ARRIVED -> "Arrived · ${destination.name} · ${destination.category}"
            }
            Text(message, color = if (frame?.phase == GuidePhase.ARRIVED) Ink else Muted,
                fontSize = 12.sp, fontWeight = if (frame?.phase == GuidePhase.ARRIVED) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        val action = when (frame?.phase) {
            null -> "Play guide"
            GuidePhase.MOVING -> if (paused) "Resume" else "Pause"
            GuidePhase.WAITING_FOR_FLOOR -> "Continue floor"
            GuidePhase.ARRIVED -> "Replay"
        }
        TextButton(onClick = when (frame?.phase) {
            null -> onPlay
            GuidePhase.MOVING -> if (paused) onPlay else onPause
            GuidePhase.WAITING_FOR_FLOOR -> onContinueFloor
            GuidePhase.ARRIVED -> onReplay
        }, modifier = Modifier.heightIn(min = 48.dp)) { Text(action, color = AccentText, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        if (frame != null) {
            TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)) {
                Text("Close", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

private fun floorName(floors: List<Floor>, id: String?): String =
    floors.firstOrNull { it.id == id }?.name ?: "next floor"
