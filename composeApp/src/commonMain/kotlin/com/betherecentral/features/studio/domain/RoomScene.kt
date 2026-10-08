package com.betherecentral.features.studio.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Fictional demo-room drawing coordinates. They are unrelated to BeCentral's supplied plans. */
data class StudioPoint(val x: Double, val y: Double)

enum class FurnitureKind(val wireName: String) {
    DESK("desk"), PLANT("plant"), SOFA("sofa"), RUG("rug");

    companion object {
        fun fromWireName(value: String): FurnitureKind = entries.firstOrNull { it.wireName == value }
            ?: invalid("Unknown furniture kind: $value")
    }
}

data class StudioObject(
    val id: String,
    val kind: FurnitureKind,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

data class RoomScene(val companyName: String, val objects: List<StudioObject>)

object RoomSceneCodec {
    const val schemaVersion = 1
    const val roomId = "studio-demo"
    const val roomWidth = 320.0
    const val roomHeight = 224.0
    const val maxObjects = 24
    val polygon = listOf(
        StudioPoint(24.0, 0.0), StudioPoint(296.0, 0.0),
        StudioPoint(320.0, 24.0), StudioPoint(320.0, 200.0),
        StudioPoint(296.0, 224.0), StudioPoint(24.0, 224.0),
        StudioPoint(0.0, 200.0), StudioPoint(0.0, 24.0),
    )

    fun parse(source: String): RoomScene {
        require(source.length <= 65_536) { "Scene JSON is too large." }
        val root = Json.parseToJsonElement(source).objectAt("scene")
        root.exactKeys("scene", setOf("schemaVersion", "companyName", "room", "objects"))
        require(root.required("schemaVersion").numberAt("schemaVersion") == schemaVersion.toDouble()) {
            "Unsupported scene schema version."
        }
        val room = root.required("room").objectAt("room")
        room.exactKeys("room", setOf("id", "width", "height", "polygon"))
        require(room.required("id").stringAt("room.id") == roomId) { "Room identity cannot be changed." }
        require(room.required("width").numberAt("room.width") == roomWidth &&
            room.required("height").numberAt("room.height") == roomHeight) { "Room dimensions cannot be changed." }
        val points = room.required("polygon").arrayAt("room.polygon")
        require(points.size == polygon.size) { "Room outline cannot be changed." }
        points.forEachIndexed { index, item ->
            val coordinate = item.arrayAt("room.polygon[$index]")
            require(coordinate.size == 2 &&
                coordinate[0].numberAt("room.polygon[$index].x") == polygon[index].x &&
                coordinate[1].numberAt("room.polygon[$index].y") == polygon[index].y) {
                "Room outline cannot be changed."
            }
        }
        val objects = root.required("objects").arrayAt("objects")
        require(objects.size <= maxObjects) { "A scene can contain at most $maxObjects objects." }
        val parsedObjects = objects.mapIndexed { index, item ->
            val entry = item.objectAt("objects[$index]")
            entry.exactKeys("objects[$index]", setOf("id", "kind", "x", "y", "width", "height"))
            StudioObject(
                id = entry.required("id").stringAt("objects[$index].id"),
                kind = FurnitureKind.fromWireName(entry.required("kind").stringAt("objects[$index].kind")),
                x = entry.required("x").numberAt("objects[$index].x"),
                y = entry.required("y").numberAt("objects[$index].y"),
                width = entry.required("width").numberAt("objects[$index].width"),
                height = entry.required("height").numberAt("objects[$index].height"),
            )
        }
        return RoomScene(root.required("companyName").stringAt("companyName"), parsedObjects).also(::validate)
    }

    fun encode(scene: RoomScene): String {
        validate(scene)
        return buildJsonObject {
            put("schemaVersion", schemaVersion)
            put("companyName", scene.companyName)
            put("room", buildJsonObject {
                put("id", roomId)
                put("width", roomWidth)
                put("height", roomHeight)
                put("polygon", buildJsonArray {
                    polygon.forEach { point -> add(buildJsonArray { add(JsonPrimitive(point.x)); add(JsonPrimitive(point.y)) }) }
                })
            })
            put("objects", buildJsonArray {
                scene.objects.forEach { furniture ->
                    add(buildJsonObject {
                        put("id", furniture.id)
                        put("kind", furniture.kind.wireName)
                        put("x", furniture.x)
                        put("y", furniture.y)
                        put("width", furniture.width)
                        put("height", furniture.height)
                    })
                }
            })
        }.toString()
    }

    fun validate(scene: RoomScene) {
        require(scene.companyName == scene.companyName.trim() && scene.companyName.length in 1..60 &&
            scene.companyName.none { it.code < 32 || it.code in 127..159 }) {
            "Company name must be 1–60 trimmed characters without control characters."
        }
        require(scene.objects.size <= maxObjects) { "A scene can contain at most $maxObjects objects." }
        val ids = mutableSetOf<String>()
        scene.objects.forEach { item ->
            require(item.id.length in 1..40 && item.id.all {
                it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '-' || it == '_'
            }) {
                "Object IDs must be 1–40 letters, digits, dashes or underscores."
            }
            require(ids.add(item.id)) { "Object IDs must be unique." }
            require(listOf(item.x, item.y, item.width, item.height).all { it.isFinite() }) {
                "Object coordinates must be finite."
            }
            require(item.width >= 16.0 && item.height >= 16.0 && item.width <= roomWidth && item.height <= roomHeight) {
                "Object dimensions are outside the allowed range."
            }
            require(item.x >= 0 && item.y >= 0 && item.x + item.width <= roomWidth && item.y + item.height <= roomHeight &&
                listOf(
                    StudioPoint(item.x, item.y), StudioPoint(item.x + item.width, item.y),
                    StudioPoint(item.x + item.width, item.y + item.height), StudioPoint(item.x, item.y + item.height),
                ).all(::insidePolygon)) { "Object ${item.id} extends outside the room outline." }
        }
    }

    private fun insidePolygon(point: StudioPoint): Boolean {
        // The trusted fixture polygon is convex; every rectangle corner inside it implies its whole footprint is inside.
        for (index in polygon.indices) {
            val a = polygon[index]
            val b = polygon[(index + 1) % polygon.size]
            val cross = (b.x - a.x) * (point.y - a.y) - (b.y - a.y) * (point.x - a.x)
            if (cross < -1e-9) return false
        }
        return true
    }
}

private fun JsonElement.objectAt(path: String): JsonObject = this as? JsonObject ?: invalid("$path must be an object.")
private fun JsonElement.arrayAt(path: String): JsonArray = this as? JsonArray ?: invalid("$path must be an array.")
private fun JsonElement.stringAt(path: String): String {
    val value = this as? JsonPrimitive ?: invalid("$path must be a string.")
    require(value.isString) { "$path must be a string." }
    return value.content
}
private fun JsonElement.numberAt(path: String): Double {
    val value = this as? JsonPrimitive ?: invalid("$path must be a number.")
    require(!value.isString && value.content != "true" && value.content != "false") { "$path must be a number." }
    return value.content.toDoubleOrNull()?.takeIf { it.isFinite() } ?: invalid("$path must be a finite number.")
}
private fun JsonObject.required(key: String): JsonElement = this[key] ?: invalid("Missing $key.")
private fun JsonObject.exactKeys(path: String, keys: Set<String>) {
    require(this.keys == keys) { "$path has missing or unsupported fields." }
}
private fun invalid(message: String): Nothing = throw IllegalArgumentException(message)
