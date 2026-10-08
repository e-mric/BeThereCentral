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
import com.betherecentral.features.hunt.data.HuntDiscoveries
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
        SectionLabel("SAMPLE DISCOVERY TRAIL")
        Text("Discover the building together", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("SAMPLE · Fictional building and company details. QR scanning is simulated; teammates take turns on this device.", color = Muted, fontSize = 13.sp)
        Text("Active player", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            hunt.players.forEach { player ->
                FilterChip(selected = activePlayer == player, onClick = { onPlayerChange(player) }, label = { Text(player) })
            }
        }
        Text("${hunt.completedCheckpointCount} / ${stops.size} found", color = AccentText, fontWeight = FontWeight.Bold)
        stops.forEachIndexed { index, stop ->
            val foundBy = hunt.contributions.firstOrNull { it.checkpointId == stop.id }?.playerId
            val discovery = HuntDiscoveries.forCheckpoint(stop.id)
            Surface(color = if (foundBy != null) HighlightPanel else PanelSurface, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Hairline)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (foundBy != null) "✓" else "${index + 1}", color = AccentText, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stop.name, maxLines = 1)
                        discovery?.let { card ->
                            Text("${card.floorName} · ${card.area} · SAMPLE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(card.introduction, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            val spotlightRoom = HuntDiscoveries.spotlightRoom(card)
                            Text("SAMPLE COMPANY · ${spotlightRoom?.name?.substringBefore(" ·") ?: "Fictional workspace"}", color = AccentText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(card.spotlightText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            val nearby = HuntDiscoveries.nearbyRooms(card).joinToString(" · ") { it.name.substringBefore(" ·") }
                            Text("Nearby: $nearby", color = AccentText, fontSize = 10.sp)
                        }
                        if (foundBy != null) Text("Found by $foundBy", color = Muted, fontSize = 10.sp)
                    }
                    TextButton(onClick = {
                        when (val result = hunt.contribute(activePlayer, stop.id)) {
                            is HuntResult.Accepted -> { onHuntChange(result.hunt); feedback = "$activePlayer found ${stop.name}." }
                            is HuntResult.Rejected -> feedback = "Scan rejected: ${result.reason.name.lowercase().replace('_', ' ')}."
                        }
                    }) { Text(if (foundBy != null) "Found" else "Demo scan") }
                }
            }
        }
        feedback?.let { Text(it, color = AccentText, fontSize = 12.sp) }
        if (hunt.completed) Text("Trail complete!", color = AccentText, fontWeight = FontWeight.Bold)
    }
}
