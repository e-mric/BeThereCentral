package com.betherecentral

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.betherecentral.features.building.data.DemoBuilding
import com.betherecentral.features.building.domain.Checkpoint
import com.betherecentral.features.building.domain.Floor
import com.betherecentral.features.building.domain.Room
import com.betherecentral.features.checkpoints.domain.CheckpointTracker
import com.betherecentral.features.hunt.domain.CooperativeHunt
import com.betherecentral.features.hunt.presentation.HuntControls
import com.betherecentral.features.exploration.presentation.ExploreScene
import com.betherecentral.features.exploration.presentation.supportsExploreScene
import com.betherecentral.features.guide.domain.GuidePhase
import com.betherecentral.features.guide.domain.GuideFrame
import com.betherecentral.features.guide.domain.GuideProgress
import com.betherecentral.features.guide.domain.GuideTimeline
import com.betherecentral.features.guide.presentation.GuideControls
import com.betherecentral.features.navigation.domain.Route
import com.betherecentral.features.navigation.domain.RouteMode
import com.betherecentral.features.navigation.domain.RoutePreference
import com.betherecentral.features.navigation.presentation.FloorMap
import com.betherecentral.features.sharing.domain.ShareGrantState
import com.betherecentral.features.sharing.presentation.ShareControls

private val DarkInk = Color(0xFF0C1114)
private val WarmWhite = Color(0xFFFFF8F4)
private val BrandBlue = Color(0xFF171978)
private val Orange = Color(0xFFF74B23)
private val Peach = Color(0xFFFEE7E1)
private val darkScheme = darkColorScheme(
    primary = Orange, onPrimary = DarkInk, primaryContainer = Color(0xFF39302B),
    onPrimaryContainer = Peach, secondary = Peach, onSecondary = DarkInk,
    secondaryContainer = Color(0xFF303A3E), onSecondaryContainer = Peach,
    background = DarkInk, onBackground = WarmWhite, surface = Color(0xFF1B2227),
    onSurface = WarmWhite, surfaceVariant = Color(0xFF1B2227), onSurfaceVariant = Color(0xFFD2D0CC),
    outline = Color(0xFF485158),
)
private val lightScheme = lightColorScheme(
    primary = BrandBlue, onPrimary = WarmWhite, primaryContainer = Peach, onPrimaryContainer = BrandBlue,
    secondary = BrandBlue, onSecondary = WarmWhite,
    secondaryContainer = Peach, onSecondaryContainer = BrandBlue,
    background = WarmWhite, onBackground = BrandBlue, surface = Color(0xFFFFF1EB),
    onSurface = BrandBlue, surfaceVariant = Peach, onSurfaceVariant = Color(0xFF4B4A54),
    outline = Color(0xFFB4A6A3),
)
private data class AppPalette(
    val panel: Color, val panelSolid: Color, val edge: Color,
    val bright: Color, val muted: Color, val accentText: Color, val amber: Color,
    val qrItem: Color,
)
private val darkPalette = AppPalette(Color(0xF21B2227), Color(0xFF1B2227),
    Color(0xFF485158), WarmWhite, Color(0xFFD2D0CC), Color(0xFFFF8A68), Color(0xFFFFD1A6), Color(0xFF303A3E))
private val lightPalette = AppPalette(Color(0xFFFFF1EB), Color(0xFFFFF1EB),
    Color(0xFFB4A6A3), BrandBlue, Color(0xFF4B4A54), BrandBlue, Color(0xFF9D351A), Peach)
private val LocalAppPalette = staticCompositionLocalOf { darkPalette }
private val Panel: Color @Composable get() = LocalAppPalette.current.panel
private val PanelSolid: Color @Composable get() = LocalAppPalette.current.panelSolid
private val Edge: Color @Composable get() = LocalAppPalette.current.edge
private val Bright: Color @Composable get() = LocalAppPalette.current.bright
private val Muted: Color @Composable get() = LocalAppPalette.current.muted
private val AccentText: Color @Composable get() = LocalAppPalette.current.accentText
private val Amber: Color @Composable get() = LocalAppPalette.current.amber
private val QrItem: Color @Composable get() = LocalAppPalette.current.qrItem
private val Shape = RoundedCornerShape(24.dp)

private enum class PanelKind { QR, HUNT, PEOPLE, ABOUT }
private enum class MainScene { EXPLORE, MAP }
private enum class MapEnvironment { DARK, LIGHT }

@Composable
fun App(onAppearanceChanged: (Boolean) -> Unit = {}) {
    val floors = DemoBuilding.floors
    val rooms = DemoBuilding.rooms
    val checkpoints = DemoBuilding.checkpoints
    val tracker = remember { CheckpointTracker(DemoBuilding.id, checkpoints) }
    var floorId by remember { mutableStateOf(floors.first().id) }
    var checkpointId by remember { mutableStateOf<String?>(null) }
    var checkpointRevision by remember { mutableIntStateOf(0) }
    var observedAt by remember { mutableStateOf<String?>(null) }
    var destinationId by remember { mutableStateOf<String?>(null) }
    var preference by remember { mutableStateOf(RoutePreference.DEFAULT) }
    var query by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var panel by remember { mutableStateOf<PanelKind?>(null) }
    var zoomCommand by remember { mutableIntStateOf(0) }
    var overviewCommand by remember { mutableIntStateOf(0) }
    var fitFloorCommand by remember { mutableIntStateOf(0) }
    var shareGrant by remember { mutableStateOf<ShareGrantState?>(null) }
    var hunt by remember { mutableStateOf(CooperativeHunt()) }
    var huntPlayer by remember { mutableStateOf(hunt.players.first()) }
    var mainScene by remember { mutableStateOf(if (supportsExploreScene) MainScene.EXPLORE else MainScene.MAP) }
    var mapEnvironment by remember { mutableStateOf(MapEnvironment.DARK) }
    val lightMapVisible = mainScene == MainScene.MAP && mapEnvironment == MapEnvironment.LIGHT
    val current = checkpoints.firstOrNull { it.id == checkpointId }
    val destination = rooms.firstOrNull { it.id == destinationId }
    val route = remember(checkpointId, destinationId, preference) {
        checkpointId?.let { start -> destinationId?.let { DemoBuilding.findRoute(start, it, preference) } }
    }
    val guideTimeline = remember(route) { route?.let(GuideTimeline::fromRoute) }
    var guideProgress by remember(route, checkpointRevision) { mutableStateOf<GuideProgress?>(null) }
    val guideFrame = guideProgress?.let { guideTimeline?.frame(it) }
    var journeyCardHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    val layoutDirection = LocalLayoutDirection.current

    // The map surrounds and the Explore scene stay charcoal in both interface modes.
    LaunchedEffect(Unit) { onAppearanceChanged(false) }

    fun acceptPayload(payload: String): Boolean {
        val now = kotlin.time.Clock.System.now()
        val observation = tracker.acceptPayload(payload, now.toEpochMilliseconds()) ?: return false
        checkpointId = observation.checkpoint.id
        checkpointRevision++
        floorId = observation.checkpoint.floorId
        observedAt = now.toString().take(19).replace('T', ' ') + " UTC"
        overviewCommand++
        return true
    }
    fun selectRoom(room: Room) {
        destinationId = room.id
        query = room.name
        floorId = current?.floorId ?: room.floorId
        searchOpen = false
        focusManager.clearFocus()
        keyboard?.hide()
        overviewCommand++
    }
    fun openSearch() {
        guideProgress = guideProgress?.withPaused(true)
        searchOpen = true
        focusRequester.requestFocus()
        keyboard?.show()
    }
    fun showPanel(kind: PanelKind) {
        guideProgress = guideProgress?.withPaused(true)
        searchOpen = false
        focusManager.clearFocus()
        keyboard?.hide()
        panel = kind
    }

    LaunchedEffect(windowFocused) {
        if (!windowFocused) guideProgress = guideProgress?.withPaused(true)
    }
    // Frame time is capped, so a resumed preview never catches up while hidden.
    LaunchedEffect(guideTimeline, mainScene, panel, searchOpen, menuOpen, windowFocused, guideProgress?.paused, guideFrame?.phase) {
        val timeline = guideTimeline ?: return@LaunchedEffect
        if (!windowFocused || mainScene != MainScene.MAP || panel != null || searchOpen || menuOpen ||
            guideProgress == null || guideProgress?.paused == true || guideFrame?.phase != GuidePhase.MOVING) return@LaunchedEffect
        var previousFrame = withFrameNanos { it }
        var elapsed = 0.0
        while (true) {
            val frameTime = withFrameNanos { it }
            elapsed += ((frameTime - previousFrame) / 1_000_000_000.0).coerceIn(0.0, 0.05)
            previousFrame = frameTime
            if (elapsed >= 1.0 / 30.0) {
                guideProgress = guideProgress?.let { timeline.advance(it, elapsed) }
                elapsed = 0.0
            }
        }
    }

    MaterialTheme(colorScheme = if (lightMapVisible) lightScheme else darkScheme) {
      CompositionLocalProvider(LocalAppPalette provides if (lightMapVisible) lightPalette else darkPalette) {
        BoxWithConstraints(Modifier.fillMaxSize().background(DarkInk).semantics {
            contentDescription = if (mainScene == MainScene.EXPLORE && supportsExploreScene) "Separate sample Explore scene" else "Central House sample 2D floor map"
        }) {
            val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
            val safeTop = safeInsets.calculateTopPadding()
            val safeBottom = safeInsets.calculateBottomPadding()
            val startInset = safeInsets.calculateStartPadding(layoutDirection) + if (maxWidth < 600.dp) 14.dp else 24.dp
            val endInset = safeInsets.calculateEndPadding(layoutDirection) + if (maxWidth < 600.dp) 14.dp else 24.dp
            val compact = maxWidth < 600.dp
            val searchWidth = if (compact) maxWidth - startInset - endInset - 58.dp else 370.dp
            val cardWidth = if (compact) maxWidth - startInset - endInset else 430.dp
            val floor = floors.first { it.id == floorId }
            val cardHeight = with(density) { journeyCardHeightPx.toDp() }
            val reservedBottom = if (compact) maxOf(160.dp, cardHeight + 24.dp) else 20.dp

            if (mainScene == MainScene.EXPLORE && supportsExploreScene) {
                ExploreScene(Modifier.fillMaxSize())
                SceneTabs(selected = mainScene, onSelect = { selected ->
                    if (selected != MainScene.MAP) guideProgress = guideProgress?.withPaused(true)
                    mainScene = selected
                }, modifier = Modifier.align(Alignment.TopStart).padding(start = startInset, top = safeTop + 8.dp))
                Surface(modifier = Modifier.align(Alignment.TopStart).padding(start = startInset, top = safeTop + 66.dp),
                    shape = RoundedCornerShape(20.dp), color = Panel, border = BorderStroke(1.dp, Edge)) {
                    Text("BeThereCentral · SAMPLE SCENE", Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = Peach, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
                }
            } else {

            FloorMap(
                floor = floor,
                rooms = rooms.filter { it.floorId == floorId },
                checkpoints = checkpoints.filter { it.floorId == floorId },
                lastSeen = current,
                destination = destination,
                route = route,
                guide = guideFrame,
                guideWalkingFrame = if (guideFrame?.phase == GuidePhase.MOVING && guideProgress?.paused != true)
                    (((guideProgress?.distanceOnLegMeters ?: 0.0) * 1.5).toInt() and 1) else -1,
                onRoomClick = ::selectRoom,
                onMapTap = { searchOpen = false; focusManager.clearFocus(); keyboard?.hide() },
                zoomCommand = zoomCommand,
                overviewCommand = overviewCommand,
                fitFloorCommand = fitFloorCommand,
                topInset = safeTop + if (compact) 184.dp else 155.dp,
                bottomInset = safeBottom + reservedBottom,
                rightInset = endInset + if (compact) 54.dp else 40.dp,
                mapSurroundColor = DarkInk,
            )

            Column(
                Modifier.align(Alignment.TopStart).padding(start = startInset, top = safeTop + if (compact) 14.dp else 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("BeThereCentral", color = WarmWhite, fontWeight = FontWeight.Black, letterSpacing = 0.2.sp, fontSize = 13.sp)
                    Surface(modifier = Modifier.clickable { showPanel(PanelKind.ABOUT) }, shape = RoundedCornerShape(50), color = Panel, border = BorderStroke(1.dp, Edge)) {
                        Text("SAMPLE · 2D", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = AccentText, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
                    }
                }
                SceneTabs(selected = mainScene, onSelect = { selected ->
                    if (selected != MainScene.MAP) guideProgress = guideProgress?.withPaused(true)
                    mainScene = selected
                })
                SearchBox(
                    width = searchWidth, query = query, onQuery = { guideProgress = guideProgress?.withPaused(true); query = it; searchOpen = true },
                    focusRequester = focusRequester,
                    onFocus = { guideProgress = guideProgress?.withPaused(true); searchOpen = true },
                    onDone = { searchOpen = false; focusManager.clearFocus(); keyboard?.hide() },
                    onClear = { query = ""; destinationId = null; searchOpen = false; focusManager.clearFocus(); keyboard?.hide(); overviewCommand++ },
                )
                if (searchOpen && query.isNotBlank()) {
                    val term = query.trim()
                    val matches = rooms.filter { it.name.contains(term, ignoreCase = true) || it.id.contains(term, ignoreCase = true) }.take(8)
                    SearchResults(searchWidth, matches, onSelect = ::selectRoom)
                }
            }

            Box(Modifier.align(Alignment.TopEnd).padding(end = endInset, top = safeTop + if (compact) 36.dp else 43.dp)) {
                Control("More options", "⋯") { guideProgress = guideProgress?.withPaused(true); menuOpen = true }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = PanelSolid) {
                    DropdownMenuItem(text = { Text("Map panels", fontWeight = FontWeight.Bold, color = Bright) },
                        onClick = {}, enabled = false)
                    listOf(MapEnvironment.DARK to "Dark panels", MapEnvironment.LIGHT to "Light panels").forEach { (choice, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = Bright) },
                            leadingIcon = { Text(if (mapEnvironment == choice) "●" else "○", color = if (mapEnvironment == choice) Orange else Muted) },
                            onClick = { mapEnvironment = choice; menuOpen = false },
                        )
                    }
                    HorizontalDivider(color = Edge)
                    listOf(
                        PanelKind.QR to "Set QR start",
                        PanelKind.HUNT to "QR hunt",
                        PanelKind.PEOPLE to "People sharing demo",
                        PanelKind.ABOUT to "About this map",
                    ).forEach { (kind, title) ->
                        DropdownMenuItem(text = { Text(title) }, onClick = { menuOpen = false; showPanel(kind) })
                    }
                }
            }

            if (!searchOpen) {
                Column(
                    Modifier.align(Alignment.CenterEnd).padding(end = endInset),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    floors.forEachIndexed { index, candidate ->
                        Control("Show ${candidate.name}", if (index == 0) "G" else "$index", active = floorId == candidate.id) {
                            guideProgress = guideProgress?.withPaused(true)
                            floorId = candidate.id
                            overviewCommand++
                        }
                    }
                }

                if (compact) {
                    Row(Modifier.align(Alignment.BottomStart).padding(start = startInset, bottom = safeBottom + maxOf(225.dp, cardHeight + 64.dp)), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Control("Zoom in", "+") { zoomCommand++ }
                        Control("Zoom out", "−") { zoomCommand-- }
                        Control("Fit route and floor", "⌗") { if (route == null) fitFloorCommand++ else overviewCommand++ }
                    }
                } else {
                    Column(Modifier.align(Alignment.BottomEnd).padding(end = endInset, bottom = safeBottom + 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Control("Zoom in", "+") { zoomCommand++ }
                        Control("Zoom out", "−") { zoomCommand-- }
                        Control("Fit route and floor", "⌗") { if (route == null) fitFloorCommand++ else overviewCommand++ }
                    }
                }
            }

            JourneyCard(
                modifier = Modifier.align(Alignment.BottomStart).padding(start = startInset, bottom = safeBottom + if (compact) 14.dp else 24.dp).width(cardWidth)
                    .onSizeChanged { journeyCardHeightPx = it.height },
                current = current, observedAt = observedAt, destination = destination, route = route,
                guideFrame = guideFrame, guidePaused = guideProgress?.paused == true, floors = floors,
                preference = preference, onPreference = { preference = it; overviewCommand++ },
                onSetStart = { showPanel(PanelKind.QR) }, onFind = ::openSearch,
                onClear = { destinationId = null; query = ""; overviewCommand++ },
                onGuidePlay = {
                    val timeline = guideTimeline
                    if (timeline != null) {
                        val next = guideProgress?.withPaused(false) ?: timeline.initial()
                        guideProgress = next
                        floorId = timeline.frame(next).floorId
                        overviewCommand++
                    }
                },
                onGuidePause = { guideProgress = guideProgress?.withPaused(true) },
                onGuideContinueFloor = {
                    val timeline = guideTimeline
                    val progress = guideProgress
                    if (timeline != null && progress != null) {
                        val next = timeline.continueFloor(progress).withPaused(false)
                        guideProgress = next
                        floorId = timeline.frame(next).floorId
                        overviewCommand++
                    }
                },
                onGuideReplay = {
                    val timeline = guideTimeline
                    if (timeline != null) {
                        val next = timeline.initial()
                        guideProgress = next
                        floorId = timeline.frame(next).floorId
                        overviewCommand++
                    }
                },
                onGuideClose = { guideProgress = null },
            )

            panel?.let { selected ->
                DetailDialog(title = when (selected) {
                    PanelKind.QR -> "Set your start"
                    PanelKind.HUNT -> "QR hunt"
                    PanelKind.PEOPLE -> "People"
                    PanelKind.ABOUT -> "About the map"
                }, onDismiss = { panel = null }) {
                    when (selected) {
                        PanelKind.QR -> QrPanel(checkpoints, current, observedAt, onPayload = ::acceptPayload, onDone = { panel = null })
                        PanelKind.HUNT -> HuntControls(checkpoints, hunt, { hunt = it }, huntPlayer, { huntPlayer = it })
                        PanelKind.PEOPLE -> ShareControls(current, shareGrant) { shareGrant = it }
                        PanelKind.ABOUT -> AboutPanel()
                    }
                }
            }
            }
        }
      }
    }
}

@Composable
private fun SearchBox(
    width: Dp, query: String, onQuery: (String) -> Unit, focusRequester: FocusRequester,
    onFocus: () -> Unit, onDone: () -> Unit, onClear: () -> Unit,
) {
    TextField(
        value = query, onValueChange = onQuery, singleLine = true,
        placeholder = { Text("Find a room", color = Muted, fontSize = 15.sp) },
        leadingIcon = { Text("⌕", fontSize = 27.sp, color = AccentText) },
        trailingIcon = if (query.isNotEmpty()) {{
            TextButton(onClick = onClear, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) {
                Text("×", color = Muted, fontSize = 25.sp)
            }
        }} else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = Shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PanelSolid, unfocusedContainerColor = PanelSolid,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.width(width).heightIn(min = 56.dp).border(1.dp, Edge, Shape)
            .focusRequester(focusRequester).onFocusChanged { if (it.isFocused) onFocus() }
            .semantics { contentDescription = "Find a room" },
    )
}

@Composable
private fun SearchResults(width: Dp, matches: List<Room>, onSelect: (Room) -> Unit) {
    Surface(modifier = Modifier.width(width), shape = Shape, color = PanelSolid, border = BorderStroke(1.dp, Edge), shadowElevation = 14.dp) {
        Column(Modifier.heightIn(max = 285.dp).verticalScroll(rememberScrollState())) {
            if (matches.isEmpty()) Text("No rooms found", Modifier.padding(17.dp), color = Muted, fontSize = 14.sp)
            matches.forEach { room ->
                Row(Modifier.fillMaxWidth().heightIn(min = 54.dp).clickable { onSelect(room) }.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(room.name, color = Bright, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(DemoBuilding.floors.first { it.id == room.floorId }.name, color = Muted, fontSize = 11.sp)
                    }
                    Text("↗", color = AccentText, fontSize = 19.sp)
                }
            }
        }
    }
}

@Composable
private fun Control(label: String, glyph: String, active: Boolean = false, onClick: () -> Unit) {
    val shape = if (active) RoundedCornerShape(17.dp) else CircleShape
    val modifier = Modifier.size(48.dp).border(1.dp, if (active) Orange else Edge, shape)
        .semantics { contentDescription = label }
    val content: @Composable () -> Unit = {
        Text(glyph, fontSize = if (glyph == "⋯") 27.sp else 20.sp, fontWeight = FontWeight.Bold)
    }
    if (active) {
        FilledIconButton(onClick = onClick, modifier = modifier, shape = shape,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Orange, contentColor = DarkInk), content = content)
    } else {
        FilledTonalIconButton(onClick = onClick, modifier = modifier, shape = shape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = BrandBlue, contentColor = WarmWhite), content = content)
    }
}

@Composable
private fun SceneTabs(selected: MainScene, onSelect: (MainScene) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(30.dp), color = Panel,
        border = BorderStroke(1.dp, Edge), shadowElevation = 8.dp) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(MainScene.EXPLORE to "Explore", MainScene.MAP to "Map demo").forEach { (scene, label) ->
                val enabled = scene != MainScene.EXPLORE || supportsExploreScene
                val caption = if (scene == MainScene.EXPLORE && !supportsExploreScene) "Explore · mobile" else label
                val tabModifier = Modifier.heightIn(min = 48.dp)
                if (selected == scene) {
                    Button(onClick = { onSelect(scene) }, enabled = enabled, modifier = tabModifier,
                        shape = RoundedCornerShape(24.dp), contentPadding = PaddingValues(horizontal = 17.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = DarkInk)) {
                        Text(caption, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    FilledTonalButton(onClick = { onSelect(scene) }, enabled = enabled, modifier = tabModifier,
                        shape = RoundedCornerShape(24.dp), contentPadding = PaddingValues(horizontal = 17.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = BrandBlue, contentColor = WarmWhite,
                            disabledContainerColor = Color.Transparent, disabledContentColor = Muted)) {
                        Text(caption, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyCard(
    modifier: Modifier, current: Checkpoint?, observedAt: String?, destination: Room?, route: Route?,
    guideFrame: GuideFrame?, guidePaused: Boolean, floors: List<Floor>,
    preference: RoutePreference, onPreference: (RoutePreference) -> Unit,
    onSetStart: () -> Unit, onFind: () -> Unit, onClear: () -> Unit,
    onGuidePlay: () -> Unit, onGuidePause: () -> Unit, onGuideContinueFloor: () -> Unit,
    onGuideReplay: () -> Unit, onGuideClose: () -> Unit,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = Panel,
        border = BorderStroke(1.dp, Edge), shadowElevation = 16.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                current == null && destination == null -> {
                    Text("Fictional map · Four floors", color = Bright, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("One shared layout, separate from Explore.", color = Muted, fontSize = 12.sp)
                    TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) { Text("Set sample start  ↗", color = AccentText) }
                }
                current == null -> {
                    Text(destination!!.name, color = Bright, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Set a sample QR point to see the route.", color = Muted, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) { Text("Set start", color = AccentText) }
                        TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) { Text("Clear", color = Muted) }
                    }
                }
                destination == null -> {
                    Text("Last seen · ${current.name}", color = Bright, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Sample QR · ${observedAt ?: "just now"}", color = Muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onFind, contentPadding = PaddingValues(0.dp)) { Text("Find a room  ↗", color = AccentText) }
                        TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) { Text("Change start", color = Muted) }
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(destination.name, color = Bright, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val originFloor = DemoBuilding.floors.first { it.id == current.floorId }.name
                            val destinationFloor = DemoBuilding.floors.first { it.id == destination.floorId }.name
                            Text("Last seen: ${current.name} · ${observedAt ?: "just now"}", color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (originFloor == destinationFloor) originFloor else "$originFloor  →  $destinationFloor", color = Muted, fontSize = 11.sp, maxLines = 1)
                        }
                        TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) { Text("×", color = Muted, fontSize = 22.sp) }
                    }
                    if (route == null) {
                        Text("No route for this preference. Try another option.", color = Amber, fontSize = 13.sp)
                    } else {
                        val vertical = route.legs.map { it.mode }.filter { it != RouteMode.WALK }.distinct()
                        val connector = when {
                            RouteMode.LIFT in vertical -> "Lift"
                            RouteMode.STAIRS in vertical -> "Stairs"
                            else -> "Same floor"
                        }
                        Text("${route.distanceMeters.toInt()} m  ·  ~${(route.durationSeconds + 59) / 60} min  ·  $connector", color = AccentText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(RoutePreference.DEFAULT to "Default", RoutePreference.LIFT_ORIENTED to "Lifts", RoutePreference.STEP_FREE to "Step-free").forEach { (option, label) ->
                            FilterChip(selected = preference == option, onClick = { onPreference(option) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = PanelSolid, labelColor = Bright,
                                    selectedContainerColor = Orange, selectedLabelColor = DarkInk,
                                ), border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = preference == option, borderColor = Edge,
                                    selectedBorderColor = Orange,
                                ))
                        }
                    }
                    if (route != null) GuideControls(
                        frame = guideFrame, paused = guidePaused, destination = destination, floors = floors,
                        onPlay = onGuidePlay, onPause = onGuidePause, onContinueFloor = onGuideContinueFloor,
                        onReplay = onGuideReplay, onClose = onGuideClose,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.imePadding(), shape = RoundedCornerShape(24.dp), color = PanelSolid, border = BorderStroke(1.dp, Edge)) {
            Column(Modifier.fillMaxWidth().heightIn(max = 690.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, Modifier.weight(1f), color = Bright, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) { Text("×", fontSize = 26.sp, color = Muted) }
                }
                content()
            }
        }
    }
}

@Composable
private fun QrPanel(checkpoints: List<Checkpoint>, current: Checkpoint?, observedAt: String?, onPayload: (String) -> Boolean, onDone: () -> Unit) {
    var payload by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    Text("Choose a simulated scan to set your last-seen point. This sample has no camera access.", color = Muted, fontSize = 13.sp)
    if (current != null) Text("Last seen: ${current.name} · ${observedAt ?: "just now"}", color = AccentText, fontSize = 12.sp)
    checkpoints.forEach { checkpoint ->
        Surface(Modifier.fillMaxWidth().clickable {
            if (onPayload("btcentral://${DemoBuilding.id}/checkpoint/${checkpoint.id}")) onDone()
        }, shape = RoundedCornerShape(20.dp), color = QrItem, border = BorderStroke(1.dp, Edge)) {
            Text(checkpoint.name, Modifier.padding(14.dp), color = Bright, fontSize = 14.sp)
        }
    }
    OutlinedTextField(value = payload, onValueChange = { payload = it; invalid = false }, label = { Text("Sample QR payload") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    if (invalid) Text("That QR payload is not one of this building’s checkpoints.", color = Amber, fontSize = 12.sp)
    Button(onClick = { if (onPayload(payload)) onDone() else invalid = true }, enabled = payload.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Use QR payload") }
}

@Composable
private fun AboutPanel() {
    Text("${DemoBuilding.name} reuses one illustrated coworking floor across four fictional demo levels. Its company names, ${DemoBuilding.rooms.size} room entries, QR checkpoints and metre distances are illustrative sample data.", color = Bright, fontSize = 14.sp)
    Text("The Explore engine room is a separate example space. This map is a fictional 2D floor plan. Last seen means the latest accepted sample QR checkpoint, not live tracking. Lift and step-free routes demonstrate route preferences; the sample plan is not an accessibility or emergency guide.", color = Muted, fontSize = 13.sp)
    Text("People sharing and the cooperative QR hunt run locally in this demo. No location is sent to another person.", color = Muted, fontSize = 13.sp)
}
