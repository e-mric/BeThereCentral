package com.betherecentral.features.exploration.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Full-screen wrapper for the unrelated, licensed exploration sample. */
@Composable
fun ExperimentalExploreScreen(onBack: () -> Unit) {
    ExploreBackHandler(onBack)
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0C1114)).padding(
            start = safe.calculateLeftPadding(layoutDirection) + 8.dp,
            end = safe.calculateRightPadding(layoutDirection) + 8.dp,
            top = safe.calculateTopPadding() + 4.dp,
            bottom = safe.calculateBottomPadding() + 4.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF7F4EF),
                    contentColor = Color(0xFF171978),
                ),
            ) {
                Text("‹ Back to map", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Experimental · 3D sample", color = Color.White, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                Text("Sample scene · not BeCentral", color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
        ExploreScene(Modifier.weight(1f).fillMaxWidth())
    }
}
