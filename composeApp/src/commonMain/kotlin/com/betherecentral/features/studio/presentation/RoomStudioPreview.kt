package com.betherecentral.features.studio.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.features.studio.domain.FurnitureKind
import com.betherecentral.features.studio.domain.RoomScene
import com.betherecentral.features.studio.domain.RoomSceneCodec
import com.betherecentral.features.studio.domain.StudioObject
import com.betherecentral.resources.Res

private val Outside = Color(0xFF162025)
private val Floor = Color(0xFFE8D7BA)
private val Wall = Color(0xFF604D46)
private val Ink = Color(0xFF343034)

/** Preview-only consumer of the same versioned JSON export used by the browser Studio. */
@Composable
fun RoomStudioPreview(onClose: () -> Unit) {
    var fixtureText by remember { mutableStateOf<String?>(null) }
    var scene by remember { mutableStateOf<RoomScene?>(null) }
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            val source = Res.readBytes("files/studio/demo-room.json").decodeToString()
            val initial = RoomSceneCodec.parse(source)
            fixtureText = source
            scene = initial
            draft = source
            status = "Demo scene ready."
        } catch (failure: Exception) {
            error = "Demo scene could not load: ${failure.message ?: "Unknown error"}"
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFFFC5A7), onPrimary = Color(0xFF29201F),
        surface = Outside, onSurface = Color.White,
        background = Outside, onBackground = Color.White,
    )) {
    Column(
        Modifier.fillMaxSize().background(Outside).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ROOM STUDIO", color = Color(0xFFFFE2CD), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TextButton(onClick = onClose) { Text("Close", color = Color(0xFFFFC5A7)) }
        }
        Text("FICTIONAL PROTOTYPE · Shared demo room", color = Color(0xFFFFBD92), fontSize = 12.sp)
        Text(
            "Paste a Room Studio JSON export to preview its decorations. This room is fictional and is not aligned with a BeCentral plan.",
            color = Color.White,
            fontSize = 14.sp,
        )
        scene?.let { current ->
            Text(current.companyName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Text("${current.objects.size} objects · 320 × 224 drawing units", color = Color(0xFFD2C6BC), fontSize = 12.sp)
            StudioRoomCanvas(current, Modifier.fillMaxWidth().height(240.dp))
            Text("Objects in the preview", color = Color.White, fontWeight = FontWeight.Bold)
            if (current.objects.isEmpty()) Text("No furniture objects.", color = Color.White)
            current.objects.forEach { item ->
                Text("${item.kind.wireName} · ${item.id} · x ${item.x.formatSceneNumber()}, y ${item.y.formatSceneNumber()} · ${item.width.formatSceneNumber()} × ${item.height.formatSceneNumber()}", color = Color.White, fontSize = 13.sp)
            }
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it; error = null; status = null },
            label = { Text("Scene JSON") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            minLines = 7,
            maxLines = 12,
            supportingText = { Text("Import only changes this preview. It does not publish a room.") },
        )
        error?.let { Text(it, color = Color(0xFFFFB4AB), fontSize = 13.sp) }
        status?.let { Text(it, color = Color(0xFFBCE4C9), fontSize = 13.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                try {
                    scene = RoomSceneCodec.parse(draft)
                    error = null
                    status = "Preview updated from JSON."
                } catch (failure: Exception) {
                    error = failure.message ?: "Invalid scene JSON."
                    status = null
                }
            }) { Text("Apply preview") }
            TextButton(onClick = {
                fixtureText?.let { source ->
                    draft = source
                    scene = RoomSceneCodec.parse(source)
                    error = null
                    status = "Demo scene restored."
                }
            }, enabled = fixtureText != null) { Text("Reset demo", color = Color(0xFFFFC5A7)) }
        }
    }
    }
}

private fun Double.formatSceneNumber(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

@Composable
private fun StudioRoomCanvas(scene: RoomScene, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Outside)) {
        val scale = minOf(size.width / RoomSceneCodec.roomWidth.toFloat(), size.height / RoomSceneCodec.roomHeight.toFloat())
        val origin = Offset(
            (size.width - RoomSceneCodec.roomWidth.toFloat() * scale) / 2f,
            (size.height - RoomSceneCodec.roomHeight.toFloat() * scale) / 2f,
        )
        fun at(x: Double, y: Double) = Offset(origin.x + x.toFloat() * scale, origin.y + y.toFloat() * scale)
        fun box(x: Double, y: Double, width: Double, height: Double, color: Color) {
            drawRect(color, at(x, y), Size(width.toFloat() * scale, height.toFloat() * scale))
        }
        val outline = Path().apply {
            RoomSceneCodec.polygon.forEachIndexed { index, point ->
                val offset = at(point.x, point.y)
                if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
            }
            close()
        }
        drawPath(outline, Floor)
        fun furniture(item: StudioObject) {
            val x = item.x
            val y = item.y
            val w = item.width
            val h = item.height
            when (item.kind) {
                FurnitureKind.DESK -> {
                    box(x, y, w, h, Ink)
                    box(x + 2, y + 2, w - 4, h - 4, Color(0xFFB57957))
                    box(x + 4, y + 4, w - 8, h - 8, Color(0xFFD49A6E))
                    box(x + 8, y + h - 11, w / 3, 2.0, Ink)
                    box(x + w * 0.57, y + h - 11, w / 3, 2.0, Ink)
                }
                FurnitureKind.SOFA -> {
                    box(x, y, w, h, Ink)
                    box(x + 2, y + 2, w - 4, h - 4, Color(0xFF547D82))
                    box(x + 5, y + 5, w - 10, h - 10, Color(0xFF75A3A7))
                    box(x + w / 2 - 1, y + 7, 2.0, h - 14, Ink)
                }
                FurnitureKind.RUG -> {
                    box(x, y, w, h, Ink)
                    box(x + 2, y + 2, w - 4, h - 4, Color(0xFFCB8168))
                    box(x + 5, y + 5, w - 10, h - 10, Color(0xFFE6AE84))
                    box(x + 7, y + 7, 8.0, 2.0, Ink)
                    box(x + w - 15, y + 7, 8.0, 2.0, Ink)
                    box(x + 7, y + h - 9, 8.0, 2.0, Ink)
                    box(x + w - 15, y + h - 9, 8.0, 2.0, Ink)
                }
                FurnitureKind.PLANT -> {
                    box(x + w * 0.2, y + h * 0.60, w * 0.6, h * 0.4, Ink)
                    box(x + w * 0.27, y + h * 0.63, w * 0.46, h * 0.3, Color(0xFF80624C))
                    box(x + w * 0.27, y, w * 0.46, h * 0.55, Color(0xFF5E936B))
                    box(x, y + h * 0.22, w, h * 0.2, Color(0xFF5E936B))
                }
            }
        }
        scene.objects.filter { it.kind == FurnitureKind.RUG }.forEach(::furniture)
        scene.objects.filter { it.kind != FurnitureKind.RUG }.forEach(::furniture)
        drawPath(outline, Wall, style = Stroke(width = 5f * scale))
    }
}
