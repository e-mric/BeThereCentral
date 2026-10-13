package com.betherecentral.features.communitystudio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

actual val supportsCommunityStudio: Boolean = false

@Composable
actual fun CommunityStudioBackHandler(onBack: () -> Unit) = Unit

@Composable
actual fun CommunityStudioHost(modifier: Modifier) {
    Box(modifier.fillMaxSize().background(Color(0xFF0C1114)), contentAlignment = Alignment.Center) {
        Text(
            "Community Studio is available in the Android app.",
            color = Color(0xFFD3CEE9),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
