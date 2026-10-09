package com.betherecentral.features.building.presentation

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.betherecentral.features.building.domain.PlaceSelection
import com.betherecentral.features.building.domain.focusOn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.core.presentation.PixelIcon
import com.betherecentral.core.presentation.PixelIconKind
import com.betherecentral.features.studio.presentation.RoomStudioPreview
import com.betherecentral.features.building.data.CampusFloorScenes
import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.domain.PlanDirectory
import com.betherecentral.features.building.domain.PlanFloor
import com.betherecentral.features.building.domain.PlanPlace
import com.betherecentral.features.building.domain.PlanPoint
import com.betherecentral.features.building.domain.PlanRegionKind
import com.betherecentral.features.building.domain.PlanWorld
import com.betherecentral.features.building.domain.PlanViewport
import com.betherecentral.resources.Res
import com.betherecentral.resources.brand_logo
import com.betherecentral.resources.campus_props
import org.jetbrains.compose.resources.imageResource
import kotlin.math.hypot
import kotlin.math.roundToInt

private val WorldCanvas = Color(0xFF0C1114)
private val Ink = Color(0xFFFFF8F4)
private val PlanBlue = Color(0xFF8DAEFF)
private val PlanOrange = Color(0xFFF77A55)

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
    var floorsOpen by remember { mutableStateOf(false) }
    var studioOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<PlaceSelection?>(null) }
    var highlightedId by remember { mutableStateOf<String?>(null) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val floor = floors.getOrNull(floorIndex) ?: return
    val directory = remember(floors) { PlanDirectory(floors) }
    val scope = rememberCoroutineScope()
    var focusJob by remember { mutableStateOf<Job?>(null) }

    fun choosePlace(resultFloor: PlanFloor, place: PlanPlace, anchor: PlanPoint? = null) {
        focusJob?.cancel()
        val selection = PlaceSelection.resolve(place, anchor)
        val changingFloor = resultFloor.id != floor.id
        floorIndex = floors.indexOf(resultFloor).coerceAtLeast(0)
        selected = selection
        highlightedId = place.id
        val target = PlanViewport(canvasSize.width.toDouble(), canvasSize.height.toDouble()).focusOn(selection)
        val startZoom = if (changingFloor) 1f else zoom
        val startPan = if (changingFloor) Offset.Zero else pan
        focusJob = scope.launch {
            animate(0f, 1f, animationSpec = tween(380)) { fraction, _ ->
                zoom = startZoom + (target.zoom.toFloat() - startZoom) * fraction
                pan = startPan + (Offset(target.pan.x.toFloat(), target.pan.y.toFloat()) - startPan) * fraction
            }
        }
    }

    if (studioOpen) {
        RoomStudioPreview(onClose = { studioOpen = false })
        return
    }

    Box(Modifier.fillMaxSize().background(WorldCanvas)) {
        PlanCanvas(
            floor = floor,
            propsAtlas = imageResource(Res.drawable.campus_props),
            zoom = zoom,
            pan = pan,
            highlightedId = highlightedId,
            onPanZoom = { nextZoom, nextPan -> focusJob?.cancel(); zoom = nextZoom; pan = nextPan },
            onPlaceTap = { place, anchor -> choosePlace(floor, place, anchor) },
            onCanvasSize = { canvasSize = it },
            modifier = Modifier.fillMaxSize().padding(top = safePadding.calculateTopPadding() + 122.dp, bottom = safePadding.calculateBottomPadding() + 38.dp),
        )

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(start = safeStart + 16.dp, end = safeEnd + 16.dp, top = safePadding.calculateTopPadding() + 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(imageResource(Res.drawable.brand_logo), "BeThereCentral logo", Modifier.weight(1f).widthIn(max = 180.dp).height(42.dp), contentScale = ContentScale.Fit, alignment = Alignment.CenterStart)
            Surface(
                modifier = Modifier.size(48.dp).semantics { contentDescription = "More options" }.clickable { moreOpen = true },
                shape = CircleShape, color = Color(0xFF353C41), contentColor = Ink,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { PixelIcon(PixelIconKind.MORE, Modifier.size(18.dp)) }
            }
        }

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .padding(start = safeStart + 12.dp, end = safeEnd + 12.dp, top = safePadding.calculateTopPadding() + 66.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                Modifier.weight(1f).heightIn(min = 50.dp).semantics { contentDescription = "Find a company or place" }.clickable { searchOpen = true },
                shape = RoundedCornerShape(24.dp), color = Color(0xED242D32), contentColor = Ink,
                border = BorderStroke(1.dp, Color(0xFF68747B)),
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⌕", fontSize = 22.sp, color = PlanOrange)
                    Spacer(Modifier.width(8.dp))
                    Text("Find a room", fontSize = 14.sp, maxLines = 1)
                }
            }
            Surface(
                Modifier.heightIn(min = 50.dp).semantics { contentDescription = "Choose floor. ${floor.name}" }.clickable { floorsOpen = true },
                shape = RoundedCornerShape(24.dp), color = Color(0xED242D32), contentColor = Ink,
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    PixelIcon(PixelIconKind.FLOORS, Modifier.size(16.dp))
                    Text(floor.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Column(
            Modifier.align(Alignment.TopEnd).padding(end = safeEnd + 10.dp, top = safePadding.calculateTopPadding() + 128.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MapControl("+", "Zoom in") { focusJob?.cancel(); zoom = (zoom * 1.25f).coerceAtMost(8f) }
            MapControl("−", "Zoom out") { focusJob?.cancel(); zoom = (zoom / 1.25f).coerceAtLeast(0.7f) }
            MapControl("Fit", "Fit whole floor") { focusJob?.cancel(); zoom = 1f; pan = Offset.Zero }
        }

        Text("PLAN-BASED LOCATIONS · FICTIONAL DECOR",
            Modifier.align(Alignment.BottomCenter).padding(bottom = safePadding.calculateBottomPadding() + 12.dp),
            color = Color(0xFFBBC4C9), fontSize = 9.sp, letterSpacing = 1.sp)
    }

    if (floorsOpen) {
        ModalBottomSheet(onDismissRequest = { floorsOpen = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose a floor", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                floors.forEachIndexed { index, item ->
                    val active = index == floorIndex
                    Surface(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics { this.selected = active }.clickable(role = Role.RadioButton) {
                            focusJob?.cancel(); selected = null; floorIndex = index; zoom = 1f; pan = Offset.Zero; highlightedId = null; floorsOpen = false
                        }, shape = RoundedCornerShape(18.dp),
                        color = if (active) Color(0xFF171978) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) Ink else MaterialTheme.colorScheme.onSurfaceVariant,
                    ) { Text(item.name + if (active) " · Selected" else "", Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }

    if (moreOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { moreOpen = false }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("About the map", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Button(onClick = { moreOpen = false; studioOpen = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171978), contentColor = Ink), modifier = Modifier.fillMaxWidth()) {
                    Text("Room Studio · preview")
                }
                Text("The building outlines and courtyards follow five supplied plans. Rooms, furniture and decorative characters are fictional pixel-art interpretations. The grey ground-floor area is left empty. Company names and numbered anchors come from the supplied legend; some entries have no shown position.", fontSize = 14.sp)
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
                            choosePlace(resultFloor, place)
                            searchOpen = false
                            query = ""
                        })
                    }
                    if (results.isEmpty()) item { Text("No matching plan label", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }

    selected?.let { selection ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { selected = null }, sheetState = sheetState) {
            PlanPlaceGuide(selection.place, floor.name, onClose = { selected = null })
        }
    }
}

@Composable
private fun MapControl(label: String, description: String, onClick: () -> Unit) {
    Surface(Modifier.size(width = if (label == "Fit") 54.dp else 48.dp, height = 48.dp).semantics { contentDescription = description }.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = Color(0xB31C252A), contentColor = Ink, border = BorderStroke(1.dp, Color(0xAA8C999F))) {
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

private data class PlanProjection(val size: IntSize, val zoom: Float, val pan: Offset) {
    private val viewport = PlanViewport(
        width = size.width.toDouble(),
        height = size.height.toDouble(),
        zoom = zoom.toDouble(),
        pan = PlanPoint(pan.x.toDouble(), pan.y.toDouble()),
    )
    val scale get() = viewport.scale.toFloat()
    fun screen(point: PlanPoint): Offset = viewport.screen(point).let { Offset(it.x.toFloat(), it.y.toFloat()) }
    fun plan(point: Offset) = viewport.plan(PlanPoint(point.x.toDouble(), point.y.toDouble()))
}

@Composable
private fun PlanCanvas(
    floor: PlanFloor,
    propsAtlas: ImageBitmap,
    zoom: Float,
    pan: Offset,
    highlightedId: String?,
    onPanZoom: (Float, Offset) -> Unit,
    onPlaceTap: (PlanPlace, PlanPoint) -> Unit,
    onCanvasSize: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val textMeasurer = rememberTextMeasurer()
    val signTargets = remember { mutableListOf<Triple<PlanPlace, PlanPoint, androidx.compose.ui.geometry.Rect>>() }
    val currentZoom by rememberUpdatedState(zoom)
    val currentPan by rememberUpdatedState(pan)
    val currentOnPanZoom by rememberUpdatedState(onPanZoom)
    Canvas(modifier.clipToBounds().semantics {
        contentDescription = "Schematic pixel map of ${floor.name}. Search to browse places, or pan and zoom to explore."
    }.onSizeChanged(onCanvasSize).pointerInput(floor, zoom, pan) {
        detectTapGestures { tap ->
            val sign = signTargets.firstOrNull { it.third.contains(tap) }
            if (sign != null) {
                onPlaceTap(sign.first, sign.second)
                return@detectTapGestures
            }
            val projection = PlanProjection(IntSize(size.width, size.height), zoom, pan)
            val nearest = floor.places.flatMap { place -> place.anchors.map { place to it } }
                .map { (place, anchor) -> Triple(place, anchor, hypot(projection.screen(anchor).x - tap.x, projection.screen(anchor).y - tap.y)) }
                .filter { it.third <= 28f * density }
                .minByOrNull { it.third }
            nearest?.let { onPlaceTap(it.first, it.second) }
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
        var shell = Path()
        val outlinePolygons = floor.footprints.ifEmpty { floor.regions.map { it.polygon } }
        outlinePolygons.forEach { shell = Path.combine(PathOperation.Union, shell, projectedPath(it, projection)) }
        floor.voids.forEach { shell = Path.combine(PathOperation.Difference, shell, projectedPath(it, projection)) }
        val buildingShell = shell
        // The complete silhouette stays visible even where the source has no assigned interior.
        drawPath(buildingShell, Color(0xFF252B2D))
        var assignedFootprint = Path()
        floor.regions.filter { it.kind != PlanRegionKind.UNMAPPED }.forEach {
            assignedFootprint = Path.combine(PathOperation.Union, assignedFootprint, projectedPath(it.polygon, projection))
        }
        shell = Path.combine(PathOperation.Intersect, shell, assignedFootprint)
        // Ground's grey polygon describes the coarse whole shell. On upper floors,
        // grey polygons are actual unassigned areas and must also exclude surfaces.
        if (floor.id != "ground") floor.regions.filter { it.kind == PlanRegionKind.UNMAPPED }.forEach {
            shell = Path.combine(PathOperation.Difference, shell, projectedPath(it.polygon, projection))
        }
        drawModularFloor(floor, CampusFloorScenes.forFloor(floor.id), propsAtlas, projection::screen, projection.scale, shell)
        drawPath(buildingShell, Color(0xFF626766), style = Stroke(width = (2f * projection.scale).coerceAtLeast(.6f)))
        drawCourtyardWalls(floor.voids.map { polygon -> polygon.map(projection::screen) }, projection.scale, buildingShell)
        signTargets.clear()
        val occupied = mutableListOf<androidx.compose.ui.geometry.Rect>()
        val paintSigns = mutableListOf<() -> Unit>()
        // Markers and leaders stay beneath every sign, including signs laid out earlier.
        floor.places.forEach { place ->
            place.anchors.forEach { anchor ->
                drawCircle(if (place.id == highlightedId) PlanBlue else PlanOrange, 2f * density, projection.screen(anchor))
            }
        }
        floor.places.sortedBy { if (it.id == highlightedId) 0 else 1 }.forEach { place ->
            place.anchors.forEachIndexed { anchorIndex, anchor ->
                val point = projection.screen(anchor)
                if (point.x !in 0f..size.width || point.y !in 0f..size.height) return@forEachIndexed
                val selected = place.id == highlightedId
                if ((anchorIndex == 0 || zoom >= 1.5f) && (selected || !place.label.contains("name not supplied"))) {
                    val layout = textMeasurer.measure(
                        place.label,
                        TextStyle(color = Color(0xFF201E19), fontSize = if (selected || zoom >= 1.5f) 11.sp else 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        maxLines = if (selected || zoom >= 1.5f) 2 else 1, overflow = TextOverflow.Ellipsis,
                        constraints = androidx.compose.ui.unit.Constraints(maxWidth = ((if (selected || zoom >= 1.5f) 160f else 110f) * density).roundToInt()),
                    )
                    val padding = 3f * density
                    val width = layout.size.width + padding * 2
                    val height = layout.size.height + padding * 2
                    val labelX = (point.x - width / 2).coerceIn(2f * density, (size.width - width - 2f * density).coerceAtLeast(2f * density))
                    // A short leader retains the source location when labels need space.
                    val candidates = listOf(
                        point.y - height - 5f * density,
                        point.y + 6f * density,
                        point.y - 2 * height - 10f * density,
                        point.y - 3 * height - 15f * density,
                        point.y + height + 11f * density,
                    ).map { top -> androidx.compose.ui.geometry.Rect(labelX, top, labelX + width, top + height) }
                    val visibleCandidates = candidates.filter { it.top >= 0 && it.bottom <= size.height }
                    val rect = visibleCandidates.firstOrNull { candidate ->
                        occupied.none { it.overlaps(candidate.inflate(3f * density)) }
                    } ?: if (selected) visibleCandidates.firstOrNull() else null
                    if (rect != null) {
                        val labelPoint = Offset(point.x.coerceIn(rect.left, rect.right), point.y.coerceIn(rect.top, rect.bottom))
                        drawLine(Color(0xCCF2DFC0), point, labelPoint, density)
                        paintSigns.add {
                            drawRect(Color(0xFF33291E), rect.topLeft - Offset(2f * density, 2f * density), Size(rect.width + 4f * density, rect.height + 4f * density))
                            drawRect(if (selected) Color(0xFFE5EFFF) else Color(0xFFF2DFC0), rect.topLeft, rect.size)
                            drawText(layout, topLeft = rect.topLeft + Offset(padding, padding))
                        }
                        occupied.add(rect)
                        signTargets.add(Triple(place, anchor, rect))
                    }
                }
            }
        }
        paintSigns.forEach { it() }
    })
}

private fun projectedPath(points: List<PlanPoint>, projection: PlanProjection) = Path().apply {
    points.forEachIndexed { index, point ->
        val p = projection.screen(point)
        if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
    }
    close()
}
