package com.betherecentral.features.building.domain

data class Point2D(val xMeters: Double, val yMeters: Double)

data class Bounds2D(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double,
)

data class Floor(
    val id: String,
    val name: String,
    val elevationMeters: Double,
    val widthMeters: Double = 80.0,
    val heightMeters: Double = 50.0,
)

data class Room(
    val id: String,
    val floorId: String,
    val name: String,
    val category: String,
    val center: Point2D,
    val widthMeters: Double = 5.0,
    val heightMeters: Double = 6.0,
    val entrance: Point2D = center,
)

data class Checkpoint(
    val id: String,
    val floorId: String,
    val name: String,
    val position: Point2D,
)
