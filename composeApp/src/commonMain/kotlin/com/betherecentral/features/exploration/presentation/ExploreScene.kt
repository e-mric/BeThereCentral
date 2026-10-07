package com.betherecentral.features.exploration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Whether this target has a locally bundled Explore viewer. */
expect val supportsExploreScene: Boolean

/** Hosts the offline, separately identified sample scene on supported mobile targets. */
@Composable
expect fun ExploreScene(modifier: Modifier = Modifier)
