package com.betherecentral.features.building.presentation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.core.presentation.PixelIcon
import com.betherecentral.core.presentation.PixelIconKind
import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.domain.PlanDirectory
import com.betherecentral.features.building.domain.PlanFloor
import com.betherecentral.features.building.domain.PlanPlace
import com.betherecentral.features.building.domain.PlanPoint
import com.betherecentral.features.building.domain.PlanRegion
import com.betherecentral.features.building.domain.PlanRegionKind
import com.betherecentral.features.building.domain.PlanWorld
import com.betherecentral.features.building.domain.PlanViewport
import com.betherecentral.features.guide.presentation.drawGuideSprite
import com.betherecentral.resources.Res
import com.betherecentral.resources.brand_logo
import com.betherecentral.resources.plan_ground
import com.betherecentral.resources.plan_first
import com.betherecentral.resources.plan_second
import com.betherecentral.resources.plan_third
import com.betherecentral.resources.plan_fourth
import com.betherecentral.resources.founder_atlas
import org.jetbrains.compose.resources.imageResource
import kotlin.math.hypot
import kotlin.math.roundToInt

private val WorldCanvas = Color(0xFF0C1114)
private val WallInk = Color(0xFF33302C)
private val WallLight = Color(0xFFD9C7A6)
private val Ink = Color(0xFFFFF8F4)
private val PlanBlue = Color(0xFF8DAEFF)
private val PlanOrange = Color(0xFFF77A55)
private const val VIEW_LEFT = 80f
private const val VIEW_TOP = 580f
private const val VIEW_WIDTH = 1860f
private const val VIEW_HEIGHT = 700f

/** Pixel-art preview of the supplied schematic plans. It exposes source labels and place anchors only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanWorldScreen(onAppearanceChanged: (Boolean) -> Unit = {}) {
    LaunchedEffect(Unit) { onAppearanceChanged(false) }
    var lightPanels by remember { mutableStateOf(false) }
    val inheritedScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = if (lightPanels) planPreviewLightScheme else inheritedScheme) {
        PlanWorldContent(lightPanels = lightPanels, onPanelModeChange = { lightPanels = it })
    }
}

private val planPreviewLightScheme = lightColorScheme(
    primary = Color(0xFF171978), onPrimary = Color(0xFFFFF8F4),
    secondary = Color(0xFFF74B23), onSecondary = Color(0xFF24130E),
    background = Color(0xFFFFF8F4), onBackground = Color(0xFF202528),
    surface = Color(0xFFFFF8F4), onSurface = Color(0xFF202528),
    surfaceVariant = Color(0xFFF3E8E1), onSurfaceVariant = Color(0xFF4D565B),
    outline = Color(0xFF778187),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanWorldContent(lightPanels: Boolean, onPanelModeChange: (Boolean) -> Unit) {
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val safeStart = safePadding.calculateLeftPadding(LocalLayoutDirection.current)
    val safeEnd = safePadding.calculateRightPadding(LocalLayoutDirection.current)
    val floors = remember { BeCentralPlans.floors }
    var floorIndex by remember { mutableStateOf(0) }
    var searchOpen by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<PlanPlace?>(null) }
    var highlightedId by remember { mutableStateOf<String?>(null) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val floor = floors.getOrNull(floorIndex) ?: return
    val directory = remember(floors) { PlanDirectory(floors) }

    Box(Modifier.fillMaxSize().background(WorldCanvas)) {
        PlanCanvas(
            floor = floor,
            floorArtwork = imageResource(when (floor.id) {
                "ground" -> Res.drawable.plan_ground
                "first" -> Res.drawable.plan_first
                "second" -> Res.drawable.plan_second
                "third" -> Res.drawable.plan_third
                else -> Res.drawable.plan_fourth
            }),
            zoom = zoom,
            pan = pan,
            highlightedId = highlightedId,
            onPanZoom = { nextZoom, nextPan -> zoom = nextZoom; pan = nextPan },
            onPlaceTap = { selected = it; highlightedId = it.id },
            onCanvasSize = { canvasSize = it },
            modifier = Modifier.fillMaxSize().padding(start = safeStart + 8.dp, end = safeEnd + 68.dp, top = safePadding.calculateTopPadding() + 122.dp, bottom = safePadding.calculateBottomPadding() + 96.dp),
        )

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(start = safeStart + 16.dp, end = safeEnd + 16.dp, top = safePadding.calculateTopPadding() + 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(imageResource(Res.drawable.brand_logo), "BeThereCentral logo", Modifier.weight(1f).widthIn(max = 180.dp).height(42.dp), contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
            Surface(shape = RoundedCornerShape(50), color = Color(0xFF353C41), contentColor = Color(0xFFFFD9CE)) {
                Text("PIXEL WORLD", Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Surface(
                modifier = Modifier.size(48.dp).semantics { contentDescription = "More options" }.clickable { moreOpen = true },
                shape = CircleShape, color = Color(0xFF353C41), contentColor = Ink,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { PixelIcon(PixelIconKind.MORE, Modifier.size(18.dp)) }
            }
        }

        Surface(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(start = safeStart + 16.dp, end = safeEnd + 16.dp).padding(top = safePadding.calculateTopPadding() + 66.dp)
                .heightIn(min = 50.dp).semantics { contentDescription = "Find a company or place" }.clickable { searchOpen = true },
            shape = RoundedCornerShape(18.dp), color = Color(0xF0343C41), contentColor = Ink,
            tonalElevation = 2.dp,
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⌕", fontSize = 22.sp, color = PlanBlue)
                Spacer(Modifier.width(10.dp))
                Text("Find a company or place", color = Color(0xFFD2D9DC), fontSize = 14.sp)
            }
        }

        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = safeEnd + 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MapControl("+", "Zoom in") { zoom = (zoom * 1.25f).coerceAtMost(8f) }
            MapControl("−", "Zoom out") { zoom = (zoom / 1.25f).coerceAtLeast(0.7f) }
            MapControl("Fit", "Fit whole floor") { zoom = 1f; pan = Offset.Zero }
        }

        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = safeStart + 16.dp, end = safeEnd + 16.dp, bottom = safePadding.calculateBottomPadding() + 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${floor.name.uppercase()} · FICTIONAL INTERIORS", Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFBBC4C9), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                floors.forEachIndexed { index, item ->
                    val label = when (item.level) { 0 -> "G"; else -> item.level.toString() }
                    val active = index == floorIndex
                    Surface(
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { contentDescription = item.name; this.selected = active }.clickable(role = Role.Tab) {
                            floorIndex = index; zoom = 1f; pan = Offset.Zero; highlightedId = null
                        }, shape = RoundedCornerShape(16.dp),
                        color = if (active) PlanBlue else Color(0xFF343B40),
                        contentColor = if (active) Color(0xFF101724) else Ink,
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                }
            }
        }
    }

    if (moreOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { moreOpen = false }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("About this plan preview", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("The building outlines and courtyards follow five supplied plans. Rooms, furniture and decorative characters are fictional pixel-art interpretations, including areas left unlabelled in the plans. Company names and numbered anchors come from the supplied legend; some entries have no shown position.", fontSize = 14.sp)
                Text("The plans do not establish scale, verified room use, accessible routes, live location or a matching 3D scene.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { onPanelModeChange(true) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (lightPanels) "✓ Light panels" else "Light panels")
                }
                TextButton(onClick = { onPanelModeChange(false) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (!lightPanels) "✓ Dark panels" else "Dark panels")
                }
            }
        }
    }

    if (searchOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val normalized = query.trim().lowercase()
        val results = remember(normalized, directory) { directory.search(normalized) }
        ModalBottomSheet(onDismissRequest = { searchOpen = false; query = "" }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Find a company or place", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search plan labels") })
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(results, key = { it.floor.id + it.place.id }) { (resultFloor, place) ->
                        SearchResult(resultFloor.name, place, onClick = {
                            floorIndex = floors.indexOf(resultFloor).coerceAtLeast(0)
                            selected = place
                            highlightedId = place.id
                            place.anchors.firstOrNull()?.let { anchor ->
                                zoom = 1.55f
                                val point = PlanProjection(canvasSize, 1.55f, Offset.Zero).screen(anchor)
                                pan = Offset(canvasSize.width / 2f - point.x, canvasSize.height / 2f - point.y)
                            } ?: run {
                                zoom = 1f
                                pan = Offset.Zero
                            }
                            searchOpen = false
                            query = ""
                        })
                    }
                    if (results.isEmpty()) item { Text("No matching plan label", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }

    selected?.let { place ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { selected = null }, sheetState = sheetState) {
            PlanPlaceIntroduction(place, floor.name, onClose = { selected = null })
        }
    }
}

@Composable
private fun MapControl(label: String, description: String, onClick: () -> Unit) {
    Surface(Modifier.size(width = if (label == "Fit") 54.dp else 48.dp, height = 48.dp).semantics { contentDescription = description }.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = Color(0xE83A4247), contentColor = Ink) {
        Box(contentAlignment = Alignment.Center) { Text(label, fontSize = if (label == "Fit") 12.sp else 22.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun SearchResult(floorName: String, place: PlanPlace, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().heightIn(min = 58.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            place.number?.let { NumberBadge(it) }
            Column(Modifier.padding(start = if (place.number == null) 0.dp else 12.dp).weight(1f)) {
                Text(place.label, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(floorName + if (place.anchors.isEmpty()) " · position not shown on plan" else "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NumberBadge(number: String) {
    Surface(shape = RoundedCornerShape(9.dp), color = PlanOrange, contentColor = Color(0xFF251712)) {
        Text(number, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PlanPlaceIntroduction(place: PlanPlace, floorName: String, onClose: () -> Unit) {
    val atlas = imageResource(Res.drawable.founder_atlas)
    val name = place.label
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Canvas(Modifier.size(width = 48.dp, height = 56.dp).semantics { contentDescription = "Pixel founder guide" }) {
                drawGuideSprite(atlas, Offset(size.width / 2f, size.height), walkingFrame = -1, facingLeft = false)
            }
            Column {
                Text("Plan guide", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(name, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(place.details.ifBlank { "The supplied plan labels this as ${place.label}." }, fontSize = 14.sp)
        if (place.occupants.isNotEmpty()) {
            Text("Listed occupants", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            place.occupants.forEach { occupant -> Text(occupant, fontSize = 14.sp) }
        }
        Text("Source plan · $floorName${place.number?.let { " · $it" }.orEmpty()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("The illustrated interior is fictional.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (place.anchors.isEmpty()) {
            Text("Position not shown on plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = onClose, Modifier.fillMaxWidth().heightIn(min = 48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171978))) {
            Text("Back to plan")
        }
    }
}

private data class PlanProjection(val size: IntSize, val zoom: Float, val pan: Offset) {
    private val viewport = PlanViewport(
        width = size.width.toDouble(),
        height = size.height.toDouble(),
        zoom = zoom.toDouble(),
        pan = PlanPoint(pan.x.toDouble(), pan.y.toDouble()),
    )
    val scale get() = viewport.scale.toFloat()
    val left get() = viewport.screen(PlanPoint(VIEW_LEFT.toDouble(), VIEW_TOP.toDouble())).x.toFloat()
    val top get() = viewport.screen(PlanPoint(VIEW_LEFT.toDouble(), VIEW_TOP.toDouble())).y.toFloat()
    fun screen(point: PlanPoint): Offset = viewport.screen(point).let { Offset(it.x.toFloat(), it.y.toFloat()) }
    fun plan(point: Offset) = viewport.plan(PlanPoint(point.x.toDouble(), point.y.toDouble()))
}

@Composable
private fun PlanCanvas(
    floor: PlanFloor,
    floorArtwork: ImageBitmap,
    zoom: Float,
    pan: Offset,
    highlightedId: String?,
    onPanZoom: (Float, Offset) -> Unit,
    onPlaceTap: (PlanPlace) -> Unit,
    onCanvasSize: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val textMeasurer = rememberTextMeasurer()
    val currentZoom by rememberUpdatedState(zoom)
    val currentPan by rememberUpdatedState(pan)
    val currentOnPanZoom by rememberUpdatedState(onPanZoom)
    Canvas(modifier.clipToBounds().semantics {
        contentDescription = "Schematic pixel map of ${floor.name}. Search to browse places, or pan and zoom to explore."
    }.onSizeChanged(onCanvasSize).pointerInput(floor, zoom, pan) {
        detectTapGestures { tap ->
            val projection = PlanProjection(IntSize(size.width, size.height), zoom, pan)
            val nearest = floor.places.flatMap { place -> place.anchors.map { place to it } }
                .map { (place, anchor) -> place to hypot(projection.screen(anchor).x - tap.x, projection.screen(anchor).y - tap.y) }
                .filter { it.second <= 28f * density }
                .minByOrNull { it.second }
            nearest?.first?.let(onPlaceTap)
        }
    }.pointerInput(floor) {
        detectTransformGestures { centroid, panChange, zoomChange, _ ->
            val current = PlanViewport(size.width.toDouble(), size.height.toDouble(), currentZoom.toDouble(), PlanPoint(currentPan.x.toDouble(), currentPan.y.toDouble()))
            val next = current.transform(
                factor = zoomChange.toDouble(),
                centroid = PlanPoint(centroid.x.toDouble(), centroid.y.toDouble()),
                translation = PlanPoint(panChange.x.toDouble(), panChange.y.toDouble()),
            )
            currentOnPanZoom(next.zoom.toFloat(), Offset(next.pan.x.toFloat(), next.pan.y.toFloat()))
        }
    }, onDraw = {
        drawRect(WorldCanvas)
        val projection = PlanProjection(IntSize(size.width.roundToInt(), size.height.roundToInt()), zoom, pan)
        val geometry = floor.regions.map { region -> region to Path().apply {
            region.polygon.forEachIndexed { i, point ->
                val p = projection.screen(point)
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            close()
        } }
        var shell = Path()
        val outlinePolygons = floor.footprints.ifEmpty { floor.regions.map { it.polygon } }
        outlinePolygons.forEach { shell = Path.combine(PathOperation.Union, shell, projectedPath(it, projection)) }
        floor.voids.forEach { shell = Path.combine(PathOperation.Difference, shell, projectedPath(it, projection)) }
        // Generated furnishings are fictional. Source-pixel silhouette and courtyards remain canonical.
        clipPath(shell) {
            val topLeft = projection.screen(PlanPoint(VIEW_LEFT.toDouble(), VIEW_TOP.toDouble()))
            drawImage(
                image = floorArtwork,
                dstOffset = androidx.compose.ui.unit.IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
                dstSize = IntSize((VIEW_WIDTH * projection.scale).roundToInt().coerceAtLeast(1),
                    (VIEW_HEIGHT * projection.scale).roundToInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.None,
            )
        }
        drawPath(shell, Color(0xFF4D4944), style = Stroke(width = (2f * projection.scale).coerceAtLeast(.6f)))
        floor.places.forEach { place ->
            place.anchors.forEachIndexed { anchorIndex, anchor ->
                val point = projection.screen(anchor)
                val selected = place.id == highlightedId
                val radius = if (selected) 11f else 4f
                drawCircle(Color(0xFF171717), radius + 3f, point)
                drawCircle(if (selected) PlanBlue else PlanOrange, radius, point)
                drawCircle(Color(0xFFFFF8F4), 2.2f, point)
                if (zoom >= 1.5f || selected) place.number?.let { number ->
                    val layout = textMeasurer.measure(number, TextStyle(color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    val labelW = layout.size.width + 8f
                    val labelH = layout.size.height + 4f
                    val x = point.x - labelW / 2f
                    val y = point.y - labelH - 7f
                    drawRoundRect(Color(0xF51D2327), Offset(x, y), Size(labelW, labelH), androidx.compose.ui.geometry.CornerRadius(5f))
                    drawText(layout, topLeft = Offset(point.x - layout.size.width / 2f, y + 2f))
                }
                if (selected && anchorIndex == 0) {
                    val label = place.label
                    val layout = textMeasurer.measure(label, TextStyle(color = Ink, fontSize = 10.sp, fontWeight = FontWeight.SemiBold))
                    val labelY = point.y + 12f
                    drawRoundRect(Color(0xE51D2327), Offset(point.x - layout.size.width / 2f - 5f, labelY), Size(layout.size.width + 10f, layout.size.height + 5f), androidx.compose.ui.geometry.CornerRadius(5f))
                    drawText(layout, topLeft = Offset(point.x - layout.size.width / 2f, labelY + 2f))
                }
            }
        }
    })
}

private fun projectedPath(points: List<PlanPoint>, projection: PlanProjection) = Path().apply {
    points.forEachIndexed { index, point ->
        val p = projection.screen(point)
        if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
    }
    close()
}
private fun projectedPolyline(points: List<PlanPoint>, projection: PlanProjection) = Path().apply {
    points.forEachIndexed { index, point ->
        val p = projection.screen(point)
        if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
    }
}
