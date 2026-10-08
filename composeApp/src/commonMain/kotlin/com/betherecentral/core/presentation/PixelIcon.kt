package com.betherecentral.core.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal enum class PixelIconKind { ROOMS, LOCATION, SCENE, MORE, FLOORS, DIRECTIONS, LINK }

private val iconPixels = mapOf(
    PixelIconKind.ROOMS to listOf(
        "............", ".##########.", ".#....#...#.", ".#....#...#.",
        ".###..#...#.", ".#....##.##.", ".#........#.", ".##.##....#.",
        ".#...#....#.", ".#...#....#.", ".##########.", "............",
    ),
    PixelIconKind.LOCATION to listOf(
        "....####....", "..########..", ".##########.", ".####..####.",
        ".####..####.", "..########..", "...######...", "....####....",
        ".....##.....", ".....##.....", "......#.....", "............",
    ),
    PixelIconKind.SCENE to listOf(
        ".....##.....", "...##..##...", ".##......##.", "##........##",
        "#.##....##.#", "#...####...#", "#....##....#", "#....##....#",
        "##...##...##", ".##..##..##.", "...######...", ".....##.....",
    ),
    PixelIconKind.MORE to listOf(
        "............", "............", "............", "............",
        "............", "##...##...##", "##...##...##", "............",
        "............", "............", "............", "............",
    ),
    PixelIconKind.FLOORS to listOf(
        "............", "....####....", "..##....##..", "##........##",
        "..##....##..", "....####....", "##........##", "..##....##..",
        "....####....", "##........##", "..##....##..", "....####....",
    ),
    PixelIconKind.DIRECTIONS to listOf(
        ".....##.....", "....####....", "...######...", "..########..",
        ".##########.", "############", ".....##.....", ".....##.....",
        ".....##.....", ".....##.....", ".....##.....", "............",
    ),
    PixelIconKind.LINK to listOf(
        "............", "...#####....", "..##...##...", ".##.....##..",
        ".##.....##..", "..##...##...", "...#####....", "....#####...",
        "...##...##..", "..##.....##.", "..##.....##.", "...##...##..",
    ),
)

@Composable
internal fun PixelIcon(
    kind: PixelIconKind,
    modifier: Modifier = Modifier.size(24.dp),
    contentDescription: String? = null,
) {
    val iconModifier = if (contentDescription == null) modifier else modifier.semantics {
        this.contentDescription = contentDescription
    }
    val color = LocalContentColor.current

    Canvas(iconModifier) {
        drawPixelMask(iconPixels.getValue(kind), color)
    }
}

private fun DrawScope.drawPixelMask(rows: List<String>, color: Color) {
    val cellWidth = size.width / rows.first().length
    val cellHeight = size.height / rows.size
    rows.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { columnIndex, pixel ->
            if (pixel == '#') {
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(columnIndex * cellWidth, rowIndex * cellHeight),
                    size = androidx.compose.ui.geometry.Size(cellWidth, cellHeight),
                )
            }
        }
    }
}
