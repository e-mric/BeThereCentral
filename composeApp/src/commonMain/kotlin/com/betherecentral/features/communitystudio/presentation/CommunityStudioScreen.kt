package com.betherecentral.features.communitystudio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
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

expect val supportsCommunityStudio: Boolean

@Composable
expect fun CommunityStudioBackHandler(onBack: () -> Unit)

@Composable
expect fun CommunityStudioHost(modifier: Modifier)

/** Native shell for the offline fictional-room authoring experience. */
@Composable
fun CommunityStudioScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    CommunityStudioBackHandler(onBack)
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val direction = androidx.compose.ui.platform.LocalLayoutDirection.current
    Column(
        modifier.fillMaxSize().background(Color(0xFF0C1114)).padding(
            start = safe.calculateLeftPadding(direction) + 8.dp,
            end = safe.calculateRightPadding(direction) + 8.dp,
            top = safe.calculateTopPadding() + 4.dp,
            bottom = safe.calculateBottomPadding() + 4.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        androidx.compose.foundation.layout.Row(
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
            Text(
                "Community Studio · fictional room",
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
        CommunityStudioHost(Modifier.weight(1f).fillMaxWidth())
    }
}
