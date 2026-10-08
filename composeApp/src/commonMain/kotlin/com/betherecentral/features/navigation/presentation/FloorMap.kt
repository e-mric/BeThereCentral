package com.betherecentral.features.navigation.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.betherecentral.features.building.domain.Checkpoint
import com.betherecentral.features.building.domain.ConceptFloorRegistration
import com.betherecentral.features.building.domain.Floor
import com.betherecentral.features.building.domain.Point2D
import com.betherecentral.features.building.domain.Room
import com.betherecentral.features.guide.domain.GuideFrame
import com.betherecentral.features.guide.domain.GuideHeading
import com.betherecentral.features.guide.presentation.drawGuideSprite
import com.betherecentral.resources.Res
import com.betherecentral.resources.founder_atlas
import com.betherecentral.resources.cowork_floor
import com.betherecentral.resources.scene_sign
import org.jetbrains.compose.resources.imageResource
import com.betherecentral.features.navigation.domain.Route
import com.betherecentral.features.navigation.domain.RouteMode
import kotlin.math.min
import kotlin.math.roundToInt

private val CanvasInk = Color(0xFF0C1114)
private val RouteAccent = Color(0xFFF74B23)
private val RouteOutline = Color(0xFF0C1114)
private val CurrentMarker = Color(0xFF9CC1FF)
private val DestinationMarker = Color(0xFFFFF8F4)
private val MutedLabel = Color(0xFF586367)

private data class MapProjection(
    val size: IntSize,
    val floor: Floor,
    val zoom: Float,
    val pan: Offset,
    val topInset: Float = 0f,
    val bottomInset: Float = 0f,
    val rightInset: Float = 0f,
) {
    private val usableWidth = (size.width - rightInset - 36f).coerceAtLeast(1f)
    private val usableHeight = (size.height - topInset - bottomInset - 36f).coerceAtLeast(1f)
    val base = min(usableWidth / floor.widthMeters.toFloat(), usableHeight / floor.heightMeters.toFloat())
        .coerceAtLeast(0.01f)
    val scale = base * zoom
    val left = (size.width - rightInset - floor.widthMeters.toFloat() * scale) / 2f + pan.x
    val top = topInset + (size.height - topInset - bottomInset - floor.heightMeters.toFloat() * scale) / 2f + pan.y

    fun screen(point: Point2D): Offset = Offset(
        left + point.xMeters.toFloat() * scale,
        top + (floor.heightMeters.toFloat() - point.yMeters.toFloat()) * scale,
    )

    fun world(screen: Offset): Point2D = Point2D(
        xMeters = ((screen.x - left) / scale).toDouble(),
        yMeters = floor.heightMeters - ((screen.y - top) / scale).toDouble(),
    )
}

/**
 * A fictional illustrated floor. Viewport commands are edge-triggered: changing zoomCommand applies one
 * step (positive zooms in, negative zooms out); changing overviewCommand fits the current route.
 */
@Composable
fun FloorMap(
    floor: Floor,
    rooms: List<Room>,
    checkpoints: List<Checkpoint>,
    lastSeen: Checkpoint?,
    destination: Room?,
    route: Route?,
    guide: GuideFrame? = null,
    guideWalkingFrame: Int = 0,
    mapSurroundColor: Color = CanvasInk,
    onRoomClick: (Room) -> Unit,
    onMapTap: () -> Unit = {},
    zoomCommand: Int = 0,
    overviewCommand: Int = 0,
    fitFloorCommand: Int = 0,
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp,
    rightInset: Dp = 0.dp,
    visible: Boolean = true,
    sceneDoorway: Point2D? = null,
    onSceneDoorwayClick: () -> Unit = {},
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val founderAtlas = imageResource(Res.drawable.founder_atlas)
    val floorArtwork = imageResource(Res.drawable.cowork_floor)
    val density = LocalDensity.current
    val topInsetPx = with(density) { topInset.toPx() }
    val bottomInsetPx = with(density) { bottomInset.toPx() }
    val rightInsetPx = with(density) { rightInset.toPx() }
    var previousZoomCommand by remember { mutableIntStateOf(zoomCommand) }
    var previousFitFloorCommand by remember { mutableIntStateOf(fitFloorCommand) }
    var previousFloorId by remember { mutableStateOf(floor.id) }
    var previousRoute by remember { mutableStateOf(route) }
    var previousOverviewCommand by remember { mutableIntStateOf(overviewCommand) }
    var previousMapSize by remember { mutableStateOf(IntSize.Zero) }
    var fullFloorFitFloorId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(floor.id, route, size, overviewCommand, fitFloorCommand, topInsetPx, bottomInsetPx, rightInsetPx) {
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        val firstMeasuredSize = previousMapSize == IntSize.Zero
        val floorChanged = previousFloorId != floor.id
        val routeChanged = previousRoute != route
        val overviewChanged = previousOverviewCommand != overviewCommand
        val fitFloorRequested = previousFitFloorCommand != fitFloorCommand

        if (route != null) {
            fullFloorFitFloorId = null
            val routePoints = route.legs.filter { it.floorId == floor.id }.flatMap { it.points }
            if (routePoints.isEmpty()) {
                zoom = 1f
                pan = Offset.Zero
            } else {
                val xMin = routePoints.minOf { it.xMeters }.toFloat()
                val xMax = routePoints.maxOf { it.xMeters }.toFloat()
                val yMin = routePoints.minOf { it.yMeters }.toFloat()
                val yMax = routePoints.maxOf { it.yMeters }.toFloat()
                val width = (xMax - xMin + 18f).coerceAtLeast(24f)
                val height = (yMax - yMin + 18f).coerceAtLeast(24f)
                val baseProjection = MapProjection(size, floor, 1f, Offset.Zero, topInsetPx, bottomInsetPx, rightInsetPx)
                zoom = (min((size.width - rightInsetPx - 52f).coerceAtLeast(1f) / width,
                    (size.height - topInsetPx - bottomInsetPx - 52f).coerceAtLeast(1f) / height) / baseProjection.base).coerceIn(1f, 3f)
                val center = MapProjection(size, floor, zoom, Offset.Zero, topInsetPx, bottomInsetPx, rightInsetPx).screen(
                    Point2D(((xMin + xMax) / 2).toDouble(), ((yMin + yMax) / 2).toDouble()),
                )
                pan = Offset((size.width - rightInsetPx) / 2f - center.x,
                    topInsetPx + (size.height - topInsetPx - bottomInsetPx) / 2f - center.y)
            }
        } else {
            if (floorChanged || routeChanged) fullFloorFitFloorId = null
            if (fitFloorRequested) {
                zoom = 1f
                pan = Offset.Zero
                fullFloorFitFloorId = floor.id
            } else if (fullFloorFitFloorId != floor.id && (firstMeasuredSize || floorChanged || routeChanged || overviewChanged)) {
                val compactPortrait = size.height > size.width && size.width / density.density < 600f
                zoom = if (compactPortrait) 1.25f else 1f
                pan = Offset.Zero
            }
        }
        previousFitFloorCommand = fitFloorCommand
        previousFloorId = floor.id
        previousRoute = route
        previousOverviewCommand = overviewCommand
        previousMapSize = size
    }

    LaunchedEffect(zoomCommand) {
        val difference = zoomCommand - previousZoomCommand
        if (difference != 0) {
            fullFloorFitFloorId = null
            zoom = if (difference > 0) (zoom * 1.2f).coerceAtMost(4f) else (zoom / 1.2f).coerceAtLeast(0.7f)
        }
        previousZoomCommand = zoomCommand
    }

    // Retain the viewport but remove drawing and gesture handlers while the scene covers it.
    if (!visible) return

    Box(Modifier.fillMaxSize().background(mapSurroundColor).clipToBounds()) {
        Canvas(
            Modifier.fillMaxSize().onSizeChanged { size = it }
                .pointerInput(floor.id, rooms, zoom, pan, size) {
                    detectTapGestures { tap ->
                        onMapTap()
                        val world = MapProjection(size, floor, zoom, pan, topInsetPx, bottomInsetPx, rightInsetPx).world(tap)
                        rooms.firstOrNull {
                            world.xMeters in (it.center.xMeters - it.widthMeters / 2)..(it.center.xMeters + it.widthMeters / 2) &&
                                world.yMeters in (it.center.yMeters - it.heightMeters / 2)..(it.center.yMeters + it.heightMeters / 2)
                        }?.let(onRoomClick)
                    }
                }
                .pointerInput(floor.id) {
                    detectTransformGestures { _, gesturePan, gestureZoom, _ ->
                        fullFloorFitFloorId = null
                        zoom = (zoom * gestureZoom).coerceIn(0.7f, 4f)
                        pan += gesturePan
                    }
                },
        ) {
            val projection = MapProjection(size, floor, zoom, pan, topInsetPx, bottomInsetPx, rightInsetPx)
            val left = projection.left
            val top = projection.top
            val width = floor.widthMeters.toFloat() * projection.scale
            val height = floor.heightMeters.toFloat() * projection.scale

            // One raster shares the exact world projection used by taps, routes, checkpoints and guide feet.
            drawImage(
                image = floorArtwork,
                srcOffset = IntOffset(ConceptFloorRegistration.sourceLeftPx, ConceptFloorRegistration.sourceTopPx),
                srcSize = IntSize(ConceptFloorRegistration.sourceWidthPx, ConceptFloorRegistration.sourceHeightPx),
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.None,
            )
            if (destination?.floorId == floor.id) {
                val center = projection.screen(destination.center)
                val roomWidth = destination.widthMeters.toFloat() * projection.scale
                val roomHeight = destination.heightMeters.toFloat() * projection.scale
                drawRect(
                    RouteAccent.copy(alpha = 0.9f),
                    Offset(center.x - roomWidth / 2f, center.y - roomHeight / 2f),
                    Size(roomWidth, roomHeight),
                    style = Stroke(width = 2.5f),
                )
            }

            route?.legs?.filter { it.floorId == floor.id }?.forEach { leg ->
                if (leg.points.size >= 2) {
                    val path = Path()
                    val first = projection.screen(leg.points.first())
                    path.moveTo(first.x, first.y)
                    leg.points.drop(1).forEach { point ->
                        val p = projection.screen(point)
                        path.lineTo(p.x, p.y)
                    }
                    drawPath(path, RouteOutline, style = Stroke(width = 8f, cap = StrokeCap.Round))
                    drawPath(path, RouteAccent, style = Stroke(width = 4f, cap = StrokeCap.Round))
                }
            }

            // Connectors are marked on every floor they touch, with a clear stair or lift glyph.
            route?.legs?.mapIndexedNotNull { index, leg ->
                val arrivalFloor = route.legs.getOrNull(index + 1)?.floorId
                if (leg.mode != RouteMode.WALK && (leg.floorId == floor.id || arrivalFloor == floor.id)) leg else null
            }?.forEach { leg ->
                val position = leg.points.firstOrNull() ?: return@forEach
                val p = projection.screen(position)
                drawCircle(CanvasInk, radius = 13f, center = p)
                drawCircle(DestinationMarker, radius = 10f, center = p, style = Stroke(1.5f))
                val glyph = if (leg.mode == RouteMode.LIFT) "L" else "S"
                val measured = textMeasurer.measure(glyph, TextStyle(color = DestinationMarker, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                drawText(measured, topLeft = Offset(p.x - measured.size.width / 2f, p.y - measured.size.height / 2f))
            }

            checkpoints.filter { it.floorId == floor.id }.forEach { checkpoint ->
                val p = projection.screen(checkpoint.position)
                if (checkpoint.id == lastSeen?.id) {
                    drawCircle(CurrentMarker.copy(alpha = 0.2f), radius = 14f, center = p)
                    drawCircle(CurrentMarker, radius = 5f, center = p)
                    drawCircle(CanvasInk, radius = 2f, center = p)
                } else {
                    drawCircle(MutedLabel, radius = 2.5f, center = p)
                }
            }
            if (destination?.floorId == floor.id) {
                val p = projection.screen(destination.entrance)
                drawCircle(Color(0x33FEE7E1), radius = 14f, center = p)
                drawCircle(DestinationMarker, radius = 5f, center = p)
                drawCircle(CanvasInk, radius = 2f, center = p)
            }
            if (guide?.floorId == floor.id) drawGuideSprite(
                founderAtlas, projection.screen(guide.point), guideWalkingFrame,
                facingLeft = guide.heading == GuideHeading.LEFT,
            )
        }
        sceneDoorway?.let { point ->
            val projection = MapProjection(size, floor, zoom, pan, topInsetPx, bottomInsetPx, rightInsetPx)
            val anchor = projection.screen(point)
            // Artwork scales with the world; the invisible hit area stays accessible when zoomed out.
            val boardWidth = with(density) { (projection.scale * 7f).toDp() }.coerceIn(64.dp, 100.dp)
            val boardHeight = boardWidth * .75f
            val hitWidth = boardWidth.coerceAtLeast(48.dp)
            val hitHeight = boardHeight.coerceAtLeast(48.dp)
            val halfWidth = with(density) { hitWidth.toPx() / 2f }
            val halfHeight = with(density) { hitHeight.toPx() / 2f }
            Box(Modifier.offset { IntOffset((anchor.x - halfWidth).roundToInt(), (anchor.y - halfHeight).roundToInt()) }
                .size(hitWidth, hitHeight)
                .clickable(role = Role.Button, onClickLabel = "Open sample scene", onClick = onSceneDoorwayClick)
                .semantics { contentDescription = "Reception sign: explore the unrelated 3D sample" },
                contentAlignment = Alignment.Center) {
                Image(imageResource(Res.drawable.scene_sign), contentDescription = null,
                    modifier = Modifier.size(boardWidth, boardHeight), filterQuality = FilterQuality.None)
            }
        }
    }
}
