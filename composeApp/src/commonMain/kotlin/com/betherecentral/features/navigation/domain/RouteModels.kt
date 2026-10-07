package com.betherecentral.features.navigation.domain

import com.betherecentral.features.building.domain.Point2D

enum class RouteMode { WALK, STAIRS, LIFT }
enum class RoutePreference { DEFAULT, LIFT_ORIENTED, STEP_FREE }

data class RouteLeg(
    val fromNodeId: String,
    val toNodeId: String,
    val floorId: String,
    val mode: RouteMode,
    val distanceMeters: Double,
    val points: List<Point2D>,
)

data class Route(
    val fromCheckpointId: String,
    val destinationRoomId: String,
    val legs: List<RouteLeg>,
    val distanceMeters: Double,
    val durationSeconds: Int,
    val stepFree: Boolean,
    val preference: RoutePreference = if (stepFree) RoutePreference.STEP_FREE else RoutePreference.DEFAULT,
)

data class NavigationEdge(
    val fromNodeId: String,
    val toNodeId: String,
    val floorId: String,
    val mode: RouteMode,
    val distanceMeters: Double,
    val routingCost: Double,
    val points: List<Point2D>,
    val isStepFree: Boolean = true,
) {
    init {
        require(distanceMeters.isFinite() && distanceMeters >= 0.0) { "distanceMeters must be finite and nonnegative" }
        require(routingCost.isFinite() && routingCost >= 0.0) { "routingCost must be finite and nonnegative" }
    }
}
