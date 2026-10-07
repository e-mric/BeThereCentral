package com.betherecentral.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.betherecentral.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "BeThereCentral",
        state = rememberWindowState(size = DpSize(1280.dp, 850.dp)),
    ) {
        App()
    }
}
