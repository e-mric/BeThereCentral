package com.betherecentral.features.discovery.presentation

import com.betherecentral.core.presentation.PixelIcon
import com.betherecentral.core.presentation.PixelIconKind
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import com.betherecentral.features.building.domain.Room
import com.betherecentral.features.discovery.data.RoomIntroductions
import com.betherecentral.features.guide.presentation.drawGuideSprite
import com.betherecentral.resources.Res
import com.betherecentral.resources.founder_atlas
import org.jetbrains.compose.resources.imageResource

/** Compact NPC introduction intended to sit inside the existing bottom sheet. */
@Composable
fun RoomIntroduction(room: Room, onDirections: () -> Unit) {
    val intro = RoomIntroductions.forRoom(room)
    val atlas = imageResource(Res.drawable.founder_atlas)
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Canvas(Modifier.size(width = 44.dp, height = 56.dp).semantics {
                contentDescription = "Sample founder guide with an orange backpack"
            }) {
                val feet = Offset(size.width / 2f, size.height)
                withTransform({ scale(1.6f, 1.6f, pivot = feet) }) {
                    drawGuideSprite(atlas, feet, walkingFrame = -1, facingLeft = false)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Your sample host", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("Hi! Welcome to ${room.name.substringBefore(" ·")}.", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(intro.purpose, fontSize = 14.sp)
        Text(intro.mission, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(intro.sampleNotice, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        intro.communityResources.forEach { resource ->
            TextButton(onClick = { uriHandler.openUri(resource.url) }, modifier = Modifier.heightIn(min = 48.dp)) {
                PixelIcon(PixelIconKind.LINK, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(resource.label, fontSize = 12.sp)
            }
        }
        Button(onClick = onDirections, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171978), contentColor = Color(0xFFFFF8F4)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            PixelIcon(PixelIconKind.DIRECTIONS, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Get directions")
        }
    }
}
