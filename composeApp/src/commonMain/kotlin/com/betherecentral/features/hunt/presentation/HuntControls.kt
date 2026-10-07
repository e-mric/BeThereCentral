package com.betherecentral.features.hunt.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.features.building.domain.Checkpoint
import com.betherecentral.features.hunt.domain.CooperativeHunt
import com.betherecentral.features.hunt.domain.HuntResult
import com.betherecentral.core.presentation.*

@Composable
internal fun HuntControls(
    checkpoints: List<Checkpoint>,
    hunt: CooperativeHunt,
    onHuntChange: (CooperativeHunt) -> Unit,
    activePlayer: String,
    onPlayerChange: (String) -> Unit,
) {
    var feedback by remember { mutableStateOf<String?>(null) }
    val stops = hunt.checkpointIds.mapNotNull { id -> checkpoints.firstOrNull { it.id == id } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("COOPERATIVE QR HUNT")
        Text("A trail to explore together", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Use one device to simulate each player's QR scans. Find checkpoints in order; there is no camera or team sync yet.", color = Muted, fontSize = 13.sp)
        Text("Active player", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            hunt.players.forEach { player ->
                FilterChip(selected = activePlayer == player, onClick = { onPlayerChange(player) }, label = { Text(player) })
            }
        }
        Text("${hunt.completedCheckpointCount} / ${stops.size} found", color = AccentText, fontWeight = FontWeight.Bold)
        stops.forEachIndexed { index, stop ->
            val foundBy = hunt.contributions.firstOrNull { it.checkpointId == stop.id }?.playerId
            Surface(color = if (foundBy != null) HighlightPanel else PanelSurface, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Hairline)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (foundBy != null) "✓" else "${index + 1}", color = AccentText, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stop.name, maxLines = 1)
                        if (foundBy != null) Text("Found by $foundBy", color = Muted, fontSize = 10.sp)
                    }
                    TextButton(onClick = {
                        when (val result = hunt.contribute(activePlayer, stop.id)) {
                            is HuntResult.Accepted -> { onHuntChange(result.hunt); feedback = "$activePlayer found ${stop.name}." }
                            is HuntResult.Rejected -> feedback = "Scan rejected: ${result.reason.name.lowercase().replace('_', ' ')}."
                        }
                    }) { Text(if (foundBy != null) "Found" else "Scan") }
                }
            }
        }
        feedback?.let { Text(it, color = AccentText, fontSize = 12.sp) }
        if (hunt.completed) Text("Trail complete!", color = AccentText, fontWeight = FontWeight.Bold)
    }
}
