package com.betherecentral.features.building.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.data.CampusFloorScenes
import com.betherecentral.features.building.domain.FloorProp
import com.betherecentral.features.building.domain.FloorScene
import com.betherecentral.features.building.domain.PlanFloor
import com.betherecentral.features.building.domain.PlanPoint
import com.betherecentral.features.building.domain.PlanRegionKind
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import java.nio.file.Files
import java.nio.file.Path as FilePath
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Opt-in desktop renderer evidence for the five authored floor scenes. Set
 * BTC_RENDER_AUDIT=1 and run the desktop test task to write PNGs and a prop inventory
 * under /tmp/btc-floor-audit. These are Skia desktop renders, not Android or device evidence.
 */
class FloorRenderAuditTest {
    @Test
    fun renderAllFloorScenesForVisualAudit() {
        if (System.getenv("BTC_RENDER_AUDIT") != "1") return

        val root = generateSequence(FilePath.of("").toAbsolutePath()) { it.parent }
            .firstOrNull { Files.exists(it.resolve("composeApp/src/commonMain/composeResources/drawable/campus_props.png")) }
            ?: error("Could not locate the BeThereCentral repository root from the current directory")
        val output = FilePath.of("/tmp/btc-floor-audit")
        Files.createDirectories(output)
        val atlasBytes = Files.readAllBytes(root.resolve("composeApp/src/commonMain/composeResources/drawable/campus_props.png"))
        val atlas = Image.makeFromEncoded(atlasBytes).toComposeImageBitmap()

        BeCentralPlans.floors.forEach { floor ->
            val scene = CampusFloorScenes.forFloor(floor.id)
            renderFloor(floor, atlas, output.resolve("${floor.id}.png"))
            renderLinework(floor.id, scene, output.resolve("${floor.id}-linework.png"))
        }
        writePropInventory(output.resolve("props.json"))
        assertTrue(BeCentralPlans.floors.size == 5, "Expected all five supplied-plan floors")
    }

    private fun renderFloor(floor: PlanFloor, atlas: androidx.compose.ui.graphics.ImageBitmap, destination: FilePath) {
        val scale = 2f
        val width = ((BeCentralPlans.viewportRight - BeCentralPlans.viewportLeft) * scale).toInt()
        val height = ((BeCentralPlans.viewportBottom - BeCentralPlans.viewportTop) * scale).toInt()
        val project: (PlanPoint) -> Offset = { point ->
            Offset(
                ((point.x - BeCentralPlans.viewportLeft) * scale).toFloat(),
                ((point.y - BeCentralPlans.viewportTop) * scale).toFloat(),
            )
        }
        val surface = Surface.makeRasterN32Premul(width, height)
        try {
            val canvas = surface.canvas.asComposeCanvas()
            CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(width.toFloat(), height.toFloat())) {
                drawRect(Color(0xFF0C1114))

                var buildingShell = Path()
                (floor.footprints.ifEmpty { floor.regions.map { it.polygon } }).forEach { polygon ->
                    buildingShell = Path.combine(PathOperation.Union, buildingShell, projectedPath(polygon, project))
                }
                floor.voids.forEach { polygon ->
                    buildingShell = Path.combine(PathOperation.Difference, buildingShell, projectedPath(polygon, project))
                }
                drawPath(buildingShell, Color(0xFF252B2D))

                var assignedShell = Path()
                floor.regions.filter { it.kind != PlanRegionKind.UNMAPPED }.forEach { region ->
                    assignedShell = Path.combine(PathOperation.Union, assignedShell, projectedPath(region.polygon, project))
                }
                assignedShell = Path.combine(PathOperation.Intersect, buildingShell, assignedShell)
                if (floor.id != "ground") {
                    floor.regions.filter { it.kind == PlanRegionKind.UNMAPPED }.forEach { region ->
                        assignedShell = Path.combine(PathOperation.Difference, assignedShell, projectedPath(region.polygon, project))
                    }
                }

                val scene = CampusFloorScenes.forFloor(floor.id)
                drawModularFloor(
                    floor = floor,
                    scene = scene,
                    propsAtlas = atlas,
                    project = project,
                    scale = scale,
                    assignedShell = assignedShell,
                )
                drawPath(buildingShell, Color(0xFF626766), style = Stroke(width = 2f * scale))
                drawCourtyardWalls(floor.voids.map { polygon -> polygon.map(project) }, scale, buildingShell)
                drawSourceLinework(scene, project, scale,
                    unmappedMask = Path.combine(PathOperation.Difference, buildingShell, assignedShell))
            }
            val png = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG, 100)
                ?: error("Skia could not encode ${floor.id} render")
            Files.write(destination, png.bytes)
        } finally {
            surface.close()
        }
    }

    private fun renderLinework(floorId: String, scene: FloorScene, destination: FilePath) {
        val width = BeCentralPlans.sourceWidth
        val height = BeCentralPlans.sourceHeight
        val surface = Surface.makeRasterN32Premul(width, height)
        try {
            CanvasDrawScope().draw(
                Density(1f),
                LayoutDirection.Ltr,
                surface.canvas.asComposeCanvas(),
                Size(width.toFloat(), height.toFloat()),
            ) {
                drawRect(Color.White)
                drawSourceLinework(
                    scene,
                    { point -> Offset(point.x.toFloat(), point.y.toFloat()) },
                    scale = 1f,
                    partitionColor = Color.Black,
                    stairColor = Color.Black,
                )
            }
            val png = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG, 100)
                ?: error("Skia could not encode $floorId source linework")
            Files.write(destination, png.bytes)
        } finally {
            surface.close()
        }
    }

    private fun projectedPath(points: List<PlanPoint>, project: (PlanPoint) -> Offset) = Path().apply {
        points.forEachIndexed { index, point ->
            val screen = project(point)
            if (index == 0) moveTo(screen.x, screen.y) else lineTo(screen.x, screen.y)
        }
        close()
    }

    private fun writePropInventory(destination: FilePath) {
        val entries = BeCentralPlans.floors.flatMap { floor ->
            CampusFloorScenes.forFloor(floor.id).props.map { prop -> inventoryEntry(floor.id, prop) }
        }
        Files.writeString(destination, "[\n${entries.joinToString(",\n")}\n]\n")
    }

    private fun inventoryEntry(floorId: String, prop: FloorProp): String {
        val bounds = prop.bounds.joinToString(", ") { point -> "[${point.x}, ${point.y}]" }
        return "  {\"floor\": \"${floorId.jsonEscape()}\", \"id\": \"${prop.id.jsonEscape()}\", " +
            "\"center\": [${prop.center.x}, ${prop.center.y}], \"bounds\": [$bounds]}"
    }

    private fun String.jsonEscape(): String = replace("\\", "\\\\").replace("\"", "\\\"")
}
