package com.betherecentral.core.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val Ink: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
internal val AccentText: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
internal val HighlightPanel: Color
    @Composable get() = MaterialTheme.colorScheme.secondaryContainer
internal val PanelSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface
internal val Muted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
internal val Hairline: Color
    @Composable get() = MaterialTheme.colorScheme.outline

@Composable
internal fun SectionLabel(text: String) {
    Text(text, color = AccentText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
}
