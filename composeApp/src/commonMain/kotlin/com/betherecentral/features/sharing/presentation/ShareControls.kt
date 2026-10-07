package com.betherecentral.features.sharing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.core.presentation.*
import com.betherecentral.features.building.domain.Checkpoint
import com.betherecentral.features.sharing.domain.ShareDuration
import com.betherecentral.features.sharing.domain.ShareGrantState
import kotlinx.coroutines.delay

@Composable
internal fun ShareControls(lastSeen: Checkpoint?, grant: ShareGrantState?, onGrantChange: (ShareGrantState?) -> Unit) {
    val people = listOf("Alex", "Morgan", "Sam")
    var selected by remember { mutableStateOf(setOf<String>()) }
    var duration by remember { mutableStateOf(ShareDuration.TEN) }
    var now by remember { mutableStateOf(kotlin.time.Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(grant) {
        while (grant?.isActiveAt(now) == true) {
            delay(1_000)
            now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        }
    }
    val active = grant?.isActiveAt(now) == true
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("PEOPLE")
        Text("Share your last-seen point", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Choose recipients and a short duration for a local consent demo. No person receives data.", color = Muted, fontSize = 13.sp)
        Text("Last seen: ${lastSeen?.name ?: "choose a sample QR checkpoint first"}", color = AccentText, fontSize = 12.sp)
        Text("Share with", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            people.forEach { person ->
                FilterChip(selected = if (active) person in grant.recipientIds else person in selected,
                    enabled = !active,
                    onClick = { selected = if (person in selected) selected - person else selected + person }, label = { Text(person) })
            }
        }
        Text("Expires after", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            ShareDuration.entries.forEach { option ->
                FilterChip(selected = if (active) grant.expiresAtEpochMillis - grant.createdAtEpochMillis == option.minutes * 60_000L else duration == option,
                    enabled = !active,
                    onClick = { duration = option }, label = { Text("${option.minutes} min") })
            }
        }
        if (active) {
            Surface(color = HighlightPanel, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Local demo grant active", color = AccentText, fontWeight = FontWeight.Bold)
                    val remaining = ((grant.expiresAtEpochMillis - now + 999) / 1000).coerceAtLeast(0)
                    Text("Selected: ${grant.recipientIds.joinToString()} · ${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')} remaining", color = Ink, fontSize = 12.sp)
                    Text("No recipient can read this demo state.", color = Muted, fontSize = 11.sp)
                }
            }
            OutlinedButton(onClick = { onGrantChange(grant.revoke(kotlin.time.Clock.System.now().toEpochMilliseconds())) }, modifier = Modifier.fillMaxWidth()) { Text("Revoke now") }
            TextButton(onClick = { onGrantChange(grant.copy(expiresAtEpochMillis = now)) }) { Text("Simulate expiry now") }
        } else {
            if (grant != null) Text(if (grant.revokedAtEpochMillis != null) "Demo grant revoked." else "Demo grant expired.", color = AccentText, fontWeight = FontWeight.SemiBold)
            Button(onClick = {
                val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
                now = timestamp
                onGrantChange(ShareGrantState.create("local-$timestamp", selected, timestamp, duration))
            }, enabled = selected.isNotEmpty() && lastSeen != null, modifier = Modifier.fillMaxWidth()) { Text("Start demo share") }
        }
        Text("The app demo has no network sharing. Production requires verified identities, server-enforced expiry and reviewed end-to-end encryption. No history is stored here.", color = Muted, fontSize = 11.sp)
    }
}
