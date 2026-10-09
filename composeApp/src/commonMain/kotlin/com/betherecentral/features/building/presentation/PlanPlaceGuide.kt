package com.betherecentral.features.building.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.core.presentation.PixelIcon
import com.betherecentral.core.presentation.PixelIconKind
import com.betherecentral.features.building.data.PlaceProfiles
import com.betherecentral.features.building.domain.PlaceKind
import com.betherecentral.features.building.domain.PlanPlace
import com.betherecentral.features.guide.presentation.drawGuideSprite
import com.betherecentral.resources.Res
import com.betherecentral.resources.founder_atlas
import org.jetbrains.compose.resources.imageResource

private enum class GuidePage { WELCOME, DIRECTIONS, ABOUT }

/** Place facts and resident content stay separate from the illustrative host. */
@Composable
internal fun PlanPlaceGuide(place: PlanPlace, floorName: String, onClose: () -> Unit) {
    var page by remember(place.id) { mutableStateOf(GuidePage.WELCOME) }
    val profile = remember(place) { PlaceProfiles.forPlace(place) }
    val atlas = imageResource(Res.drawable.founder_atlas)
    val uriHandler = LocalUriHandler.current
    val aboutLabel = when (profile.kind) {
        PlaceKind.COMPANY -> "About the company"
        PlaceKind.MEETING_ROOM -> "Room purpose"
        PlaceKind.OTHER -> "About this place"
    }
    Column(
        Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Canvas(Modifier.size(44.dp, 56.dp).semantics { contentDescription = "Pixel character guide" }) {
                val feet = Offset(size.width / 2, size.height)
                withTransform({ scale(1.6f, 1.6f, pivot = feet) }) {
                    drawGuideSprite(atlas, feet, walkingFrame = -1, facingLeft = false)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(place.label, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(floorName + (place.number?.let { " · Plan $it" } ?: ""),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when (page) {
            GuidePage.WELCOME -> {
                Text(when (profile.kind) {
                    PlaceKind.COMPANY -> "Hi! Would you like directions or to learn about this company?"
                    PlaceKind.MEETING_ROOM -> "Hi! Would you like directions or to learn what this meeting room is for?"
                    PlaceKind.OTHER -> "Hi! Would you like directions or more information about this place?"
                })
                GuideButton("Directions", PixelIconKind.DIRECTIONS) { page = GuidePage.DIRECTIONS }
                GuideButton(aboutLabel, PixelIconKind.ROOMS) { page = GuidePage.ABOUT }
            }
            GuidePage.DIRECTIONS -> {
                Text("Floor and plan position", fontWeight = FontWeight.Bold)
                Text(if (place.anchors.isEmpty()) "This place is listed on $floorName, but its position is not shown on the supplied plan."
                    else "The map is focused on this place’s plan marker on $floorName.")
                Text("Entrance positions and walking routes have not been confirmed yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            GuidePage.ABOUT -> {
                Text(aboutLabel, fontWeight = FontWeight.Bold)
                Text(when (profile.kind) {
                    PlaceKind.COMPANY -> profile.mission ?: "This company hasn’t supplied its introduction or mission yet."
                    PlaceKind.MEETING_ROOM -> profile.purpose ?: "The plan identifies this as a meeting room. Its purpose and facilities haven’t been supplied yet."
                    PlaceKind.OTHER -> profile.purpose ?: place.details.ifBlank { "More information hasn’t been supplied yet." }
                })
                if (place.occupants.isNotEmpty()) {
                    Text("Listed here: ${place.occupants.joinToString(", ")}")
                }
                profile.extraInfo.forEach { Text(it) }
                profile.links.forEach { link ->
                    TextButton(onClick = { uriHandler.openUri(link.url) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        PixelIcon(PixelIconKind.LINK, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(link.label)
                    }
                }
                profile.providedBy?.let { Text("Information provided by $it", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        if (page != GuidePage.WELCOME) {
            TextButton(onClick = { page = GuidePage.WELCOME }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Back to choices")
            }
        }
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to map") }
    }
}

@Composable
private fun GuideButton(label: String, icon: PixelIconKind, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171978), contentColor = Color(0xFFFFF8F4)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        PixelIcon(icon, Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label)
    }
}
