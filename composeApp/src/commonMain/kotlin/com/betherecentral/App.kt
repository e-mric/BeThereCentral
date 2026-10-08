package com.betherecentral

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import com.betherecentral.features.building.presentation.PlanWorldScreen
import com.betherecentral.resources.Res
import com.betherecentral.resources.brand_logo
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betherecentral.core.presentation.PixelIcon
import com.betherecentral.core.presentation.PixelIconKind
import com.betherecentral.features.discovery.presentation.RoomIntroduction
import com.betherecentral.features.exploration.data.SampleDoorway
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

private enum class PanelKind { QR, HUNT, PEOPLE, ABOUT, MORE, SEARCH, FLOOR, JOURNEY, ROOM }
private enum class MainScene { EXPLORE, MAP }
private enum class MapEnvironment { DARK, LIGHT }

@Composable
fun App(onAppearanceChanged: (Boolean) -> Unit = {}) {
    MaterialTheme(colorScheme = darkScheme) {
        PlanWorldScreen(onAppearanceChanged)
    }
}

/** Kept as a historical fictional navigation experiment; never used for the supplied plans. */
@Composable
private fun LegacySampleApp(onAppearanceChanged: (Boolean) -> Unit = {}) {
    val floors = DemoBuilding.floors
    val rooms = DemoBuilding.rooms
    val checkpoints = DemoBuilding.checkpoints
    val tracker = remember { CheckpointTracker(DemoBuilding.id, checkpoints) }
    var floorId by remember { mutableStateOf(floors.first().id) }
    var checkpointId by remember { mutableStateOf<String?>(null) }
    var checkpointRevision by remember { mutableIntStateOf(0) }
    var observedAt by remember { mutableStateOf<String?>(null) }
    var inspectedRoomId by remember { mutableStateOf<String?>(null) }
    var destinationId by remember { mutableStateOf<String?>(null) }
    var preference by remember { mutableStateOf(RoutePreference.DEFAULT) }
    var query by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var panel by remember { mutableStateOf<PanelKind?>(null) }
    var zoomCommand by remember { mutableIntStateOf(0) }
    var overviewCommand by remember { mutableIntStateOf(0) }
    var fitFloorCommand by remember { mutableIntStateOf(0) }
    var shareGrant by remember { mutableStateOf<ShareGrantState?>(null) }
    var hunt by remember { mutableStateOf(CooperativeHunt()) }
    var huntPlayer by remember { mutableStateOf(hunt.players.first()) }
    var mainScene by remember { mutableStateOf(MainScene.MAP) }
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
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val windowFocused = LocalWindowInfo.current.isWindowFocused

    // The map surrounds and the Explore scene stay charcoal in both interface modes.
    LaunchedEffect(Unit) { onAppearanceChanged(false) }

    fun acceptPayload(payload: String): Boolean {
        val now = kotlin.time.Clock.System.now()
        val observation = tracker.acceptPayload(payload, now.toEpochMilliseconds()) ?: return false
        mainScene = MainScene.MAP
        checkpointId = observation.checkpoint.id
        checkpointRevision++
        floorId = observation.checkpoint.floorId
        observedAt = now.toString().take(19).replace('T', ' ') + " UTC"
        overviewCommand++
        return true
    }
    fun selectRoom(room: Room) {
        mainScene = MainScene.MAP
        destinationId = room.id
        query = room.name
        floorId = current?.floorId ?: room.floorId
        searchOpen = false
        panel = null
        focusManager.clearFocus()
        keyboard?.hide()
        overviewCommand++
    }
    fun inspectRoom(room: Room) {
        inspectedRoomId = room.id
        guideProgress = guideProgress?.withPaused(true)
        searchOpen = false
        focusManager.clearFocus()
        keyboard?.hide()
        panel = PanelKind.ROOM
    }
    fun openSampleScene() {
        guideProgress = guideProgress?.withPaused(true)
        panel = null
        searchOpen = false
        focusManager.clearFocus()
        keyboard?.hide()
        mainScene = MainScene.EXPLORE
    }
    fun openSearch() {
        guideProgress = guideProgress?.withPaused(true)
        searchOpen = true
        panel = PanelKind.SEARCH
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
    LaunchedEffect(guideTimeline, mainScene, panel, searchOpen, windowFocused, guideProgress?.paused, guideFrame?.phase) {
        val timeline = guideTimeline ?: return@LaunchedEffect
        if (!windowFocused || mainScene != MainScene.MAP || panel != null || searchOpen ||
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

    fun playGuide(replay: Boolean = false) {
        val timeline = guideTimeline ?: return
        val next = if (replay) timeline.initial() else guideProgress?.withPaused(false) ?: timeline.initial()
        guideProgress = next
        floorId = timeline.frame(next).floorId
        panel = null
        searchOpen = false
        overviewCommand++
    }
    fun continueGuide() {
        val timeline = guideTimeline ?: return
        val progress = guideProgress ?: return
        val next = timeline.continueFloor(progress).withPaused(false)
        guideProgress = next
        floorId = timeline.frame(next).floorId
        panel = null
        overviewCommand++
    }

    MaterialTheme(colorScheme = if (lightMapVisible) lightScheme else darkScheme) {
      CompositionLocalProvider(LocalAppPalette provides if (lightMapVisible) lightPalette else darkPalette) {
        BoxWithConstraints(Modifier.fillMaxSize().background(DarkInk)) {
            val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
            val safeTop = safeInsets.calculateTopPadding()
            val safeBottom = safeInsets.calculateBottomPadding()
            val side = 16.dp
            val navHeight = 72.dp
            val floor = floors.first { it.id == floorId }
            val sheetWidth = minOf(maxWidth - 40.dp, 560.dp)

            // Keep viewport state alive while the separate sample viewer is open.
            FloorMap(
                    floor = floor, rooms = rooms.filter { it.floorId == floorId },
                    checkpoints = checkpoints.filter { it.floorId == floorId }, lastSeen = current,
                    destination = destination, route = route, guide = guideFrame,
                    guideWalkingFrame = if (guideFrame?.phase == GuidePhase.MOVING && guideProgress?.paused != true)
                        (((guideProgress?.distanceOnLegMeters ?: 0.0) * 1.5).toInt() and 1) else -1,
                    onRoomClick = ::inspectRoom, onMapTap = { focusManager.clearFocus() },
                    zoomCommand = zoomCommand, overviewCommand = overviewCommand, fitFloorCommand = fitFloorCommand,
                    topInset = safeTop + 112.dp, bottomInset = safeBottom + navHeight + 98.dp,
                    rightInset = 0.dp, mapSurroundColor = DarkInk,
                    visible = mainScene == MainScene.MAP,
                    sceneDoorway = if (supportsExploreScene) SampleDoorway.positionOn(floorId, rooms) else null,
                    onSceneDoorwayClick = ::openSampleScene,
                )
            if (mainScene == MainScene.EXPLORE && supportsExploreScene) {
                ExploreScene(Modifier.fillMaxSize().padding(bottom = safeBottom + navHeight))
            } else {
                Row(Modifier.align(Alignment.TopCenter).padding(start = side, end = side, top = safeTop + 60.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = ::openSearch,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        color = Panel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Edge)) {
                        Row(Modifier.padding(horizontal = 16.dp).heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("⌕", color = AccentText, fontSize = 24.sp)
                            Text("Find a room", color = Bright, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Surface(onClick = { showPanel(PanelKind.FLOOR) },
                        modifier = Modifier.heightIn(min = 48.dp),
                        color = Panel, shape = RoundedCornerShape(24.dp)) {
                        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompositionLocalProvider(LocalContentColor provides Bright) { NavSymbol(3) }
                            Text(floor.name, color = Bright, fontSize = 13.sp)
                        }
                    }
                }
                Surface(onClick = {
                    when {
                        current == null -> showPanel(PanelKind.QR)
                        else -> showPanel(PanelKind.JOURNEY)
                    }
                },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(start = side, end = side,
                        bottom = safeBottom + navHeight + 12.dp).widthIn(max = 560.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp), color = Panel) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp).heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(destination?.name ?: if (current == null) "Plan a route" else "Where to?",
                                color = Bright, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (current != null) "Last seen · ${current.name}" else "Choose where your demo route begins",
                                color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (guideFrame != null) Text(when(guideFrame.phase) {
                                GuidePhase.MOVING -> if (guideProgress?.paused == true) "Guide preview paused" else "Guide preview walking"
                                GuidePhase.WAITING_FOR_FLOOR -> "${if (guideFrame.transitionMode == RouteMode.LIFT) "Lift" else "Stairs"} to ${floors.firstOrNull { it.id == guideFrame.targetFloorId }?.name ?: "next floor"} · Preview"
                                GuidePhase.ARRIVED -> "Guide preview · Arrived"
                            }, color = AccentText, fontSize = 10.sp)
                        }
                        if (guideFrame?.phase == GuidePhase.WAITING_FOR_FLOOR) {
                            TextButton(onClick = ::continueGuide) { Text("Continue") }
                        } else if (guideFrame?.phase == GuidePhase.MOVING && guideProgress?.paused == false) {
                            TextButton(onClick = { guideProgress = guideProgress?.withPaused(true) }) { Text("Pause") }
                        } else if (current != null && destination != null) Text("Details", color = AccentText, fontSize = 12.sp)
                    }
                }
            }
            Row(Modifier.align(Alignment.TopStart).padding(start = side, end = side, top = safeTop + 4.dp)
                .fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(Res.drawable.brand_logo), "BeThereCentral",
                    modifier = Modifier.weight(1f).height(40.dp), contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart)
                Surface(color = DarkInk, shape = RoundedCornerShape(16.dp)) {
                    Text(if (mainScene == MainScene.EXPLORE) "ENGINE ROOM\n3D SAMPLE" else "SAMPLE · 2D",
                        Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = Peach,
                        fontSize = 9.sp, lineHeight = 12.sp, maxLines = 2, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(onClick = { showPanel(PanelKind.MORE) }, modifier = Modifier.size(48.dp)
                    .semantics { contentDescription = "More options" },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = BrandBlue, contentColor = WarmWhite)) {
                    NavSymbol(2)
                }
            }
            Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), color = DarkInk) {
                NavigationBar(Modifier.padding(bottom = safeBottom).height(navHeight), containerColor = DarkInk,
                    windowInsets = WindowInsets(0, 0, 0, 0)) {
                    (listOf("Map") + if (supportsExploreScene) listOf("3D") else emptyList()).forEach { label ->
                        NavigationBarItem(selected = (label == "Map" && mainScene == MainScene.MAP) ||
                                (label == "3D" && mainScene == MainScene.EXPLORE),
                            onClick = {
                                guideProgress = guideProgress?.withPaused(true)
                                when(label) {
                                    "Map" -> {
                                        panel = null; searchOpen = false; mainScene = MainScene.MAP
                                        focusManager.clearFocus(); keyboard?.hide()
                                    }
                                    "3D" -> openSampleScene()
                                }
                            }, icon = { NavSymbol(if (label == "Map") 1 else 4) }, label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = WarmWhite,
                                unselectedTextColor = WarmWhite))
                    }
                }
            }
            panel?.let { selected ->
                DetailSheet(title = when(selected) {
                    PanelKind.MORE -> "More"; PanelKind.SEARCH -> "Find a room"; PanelKind.FLOOR -> "Floor & view"
                    PanelKind.JOURNEY -> "Your route"; PanelKind.QR -> "Where does your route begin?"; PanelKind.HUNT -> "Discover the building"
                    PanelKind.PEOPLE -> "People"; PanelKind.ABOUT -> "About the sample"
                    PanelKind.ROOM -> rooms.firstOrNull { it.id == inspectedRoomId }?.name ?: "Meet your host"
                }, onDismiss = { panel = null; searchOpen = false; focusManager.clearFocus(); keyboard?.hide() }) {
                    when(selected) {
                        PanelKind.SEARCH -> {
                            SearchBox(sheetWidth, query, { query = it }, focusRequester, {},
                                { focusManager.clearFocus(); keyboard?.hide() }, { query = "" })
                            val term = query.trim()
                            val matches = rooms.filter { term.isEmpty() || it.name.contains(term, true) || it.id.contains(term, true) }.take(16)
                            SearchResults(sheetWidth, matches, ::inspectRoom)
                        }
                        PanelKind.ROOM -> rooms.firstOrNull { it.id == inspectedRoomId }?.let { room ->
                            RoomIntroduction(room = room, onDirections = {
                                selectRoom(room)
                                if (current == null) panel = PanelKind.QR
                            })
                        }
                        PanelKind.FLOOR -> {
                            floors.forEach { candidate ->
                                SheetAction(if (candidate.id == floorId) "✓ ${candidate.name}" else candidate.name) {
                                    floorId = candidate.id; overviewCommand++; panel = null
                                }
                            }
                            HorizontalDivider(color = Edge)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Control("Zoom in", "+") { zoomCommand++ }
                                Control("Zoom out", "−") { zoomCommand-- }
                                TextButton(onClick = { if (route == null) fitFloorCommand++ else overviewCommand++; panel = null }) { Text("Fit view") }
                            }
                        }
                        PanelKind.MORE -> {
                            SheetAction("Discover the building") { showPanel(PanelKind.HUNT) }
                            SheetAction("People sharing demo") { showPanel(PanelKind.PEOPLE) }
                            SheetAction("About this sample") { showPanel(PanelKind.ABOUT) }
                            Text("Map panels", color = Muted, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MapEnvironment.entries.forEach { choice ->
                                    FilterChip(selected = mapEnvironment == choice, onClick = { mapEnvironment = choice },
                                        label = { Text(if (choice == MapEnvironment.DARK) "Dark" else "Light") })
                                }
                            }
                        }
                        PanelKind.JOURNEY -> {
                          if (supportsExploreScene && destinationId == SampleDoorway.roomId) {
                            Text("Reception has a doorway to an unrelated engine-room sample.", color = Muted, fontSize = 13.sp)
                            SheetAction("Enter 3D sample") { openSampleScene() }
                          }
                          JourneyCard(
                            Modifier.fillMaxWidth(), current, observedAt, destination, route,
                            guideFrame, guideProgress?.paused == true, floors, preference,
                            { preference = it; overviewCommand++ }, { showPanel(PanelKind.QR) }, ::openSearch,
                            { destinationId = null; query = ""; overviewCommand++; panel = null },
                            { playGuide() }, { guideProgress = guideProgress?.withPaused(true) }, ::continueGuide,
                            { playGuide(true) }, { guideProgress = null },
                        )
                        }
                        PanelKind.QR -> QrPanel(checkpoints, current, observedAt, ::acceptPayload, { panel = null })
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

@Composable
private fun SheetAction(label: String, action: () -> Unit) {
    TextButton(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)) {
        Text(label, Modifier.weight(1f), color = Bright, fontSize = 16.sp)
    }
}

@Composable
private fun NavSymbol(index: Int) {
    PixelIcon(when (index) {
        0 -> PixelIconKind.LOCATION
        1 -> PixelIconKind.ROOMS
        3 -> PixelIconKind.FLOORS
        4 -> PixelIconKind.SCENE
        else -> PixelIconKind.MORE
    })
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
                    TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) { Text("Choose a start point", color = AccentText) }
                }
                current == null -> {
                    Text(destination!!.name, color = Bright, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Choose where your demo route begins to see directions.", color = Muted, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) { Text("Choose a start point", color = AccentText) }
                        TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) { Text("Clear", color = Muted) }
                    }
                }
                destination == null -> {
                    Text("Last seen · ${current.name}", color = Bright, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Sample QR · ${observedAt ?: "just now"}", color = Muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onFind, contentPadding = PaddingValues(0.dp)) { Text("Find a room", color = AccentText) }
                        TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) { Text("Change start point", color = Muted) }
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
                    TextButton(onClick = onSetStart, contentPadding = PaddingValues(0.dp)) {
                        Text("Change start point", color = AccentText)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PanelSolid, contentColor = Bright) {
        Column(Modifier.fillMaxWidth().heightIn(max = 640.dp).imePadding().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), color = Bright, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) {
                    Text("×", fontSize = 26.sp, color = Muted)
                }
            }
            content()
        }
    }
}

@Composable
private fun QrPanel(checkpoints: List<Checkpoint>, current: Checkpoint?, observedAt: String?, onPayload: (String) -> Boolean, onDone: () -> Unit) {
    var payload by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    var testCode by remember { mutableStateOf(false) }
    Text("Choose a place on this fictional map. We'll calculate directions from there to the room you select.", color = Bright, fontSize = 14.sp)
    Text("Demo only: choosing a place simulates a QR checkpoint. It doesn't detect your location or open the camera.", color = Muted, fontSize = 12.sp)
    if (current != null) Text("Last seen: ${current.name} · ${observedAt ?: "just now"}", color = AccentText, fontSize = 12.sp)
    checkpoints.forEach { checkpoint ->
        Surface(onClick = {
            if (onPayload("btcentral://${DemoBuilding.id}/checkpoint/${checkpoint.id}")) onDone()
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(20.dp),
            color = QrItem, border = BorderStroke(1.dp, if (checkpoint.id == current?.id) AccentText else Edge)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(checkpoint.name, Modifier.weight(1f), color = Bright, fontSize = 14.sp)
                if (checkpoint.id == current?.id) Text("Selected", color = AccentText, fontSize = 11.sp)
            }
        }
    }
    TextButton(onClick = { testCode = !testCode }, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(if (testCode) "Hide QR code test" else "Test a QR code", color = Muted, fontSize = 12.sp)
    }
    if (testCode) {
        OutlinedTextField(value = payload, onValueChange = { payload = it; invalid = false }, label = { Text("Sample QR code text") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (invalid) Text("This code isn't a checkpoint on the sample map. Your start point hasn't changed.", color = Amber, fontSize = 12.sp)
        Button(onClick = { if (onPayload(payload)) onDone() else invalid = true }, enabled = payload.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Use this sample code") }
    }
}

@Composable
private fun AboutPanel() {
    Text("${DemoBuilding.name} reuses one illustrated coworking floor across four fictional demo levels. Its company names, ${DemoBuilding.rooms.size} room entries, QR checkpoints and metre distances are illustrative sample data.", color = Bright, fontSize = 14.sp)
    Text("The Explore engine room is a separate example space. This map is a fictional 2D floor plan. Last seen means the latest accepted sample QR checkpoint, not live tracking. Lift and step-free routes demonstrate route preferences; the sample plan is not an accessibility or emergency guide.", color = Muted, fontSize = 13.sp)
    Text("People sharing and building discovery with a cooperative hunt run locally in this demo. No location is sent to another person.", color = Muted, fontSize = 13.sp)
}
