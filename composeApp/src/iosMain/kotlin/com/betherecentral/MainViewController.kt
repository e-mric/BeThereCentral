package com.betherecentral

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(onAppearanceChanged: (Boolean) -> Unit) =
    ComposeUIViewController { App(onAppearanceChanged = onAppearanceChanged) }
