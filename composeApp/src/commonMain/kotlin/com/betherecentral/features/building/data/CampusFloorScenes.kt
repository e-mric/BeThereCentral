package com.betherecentral.features.building.data

import com.betherecentral.features.building.domain.FloorScene

/** Every current floor has an independently editable asset recipe. No image fallback. */
object CampusFloorScenes {
    fun forFloor(id: String): FloorScene =
        if (id == GroundFloorScene.scene.floorId) GroundFloorScene.scene
        else requireNotNull(UpperFloorScenes.forFloor(id)) { "No asset recipe for floor $id" }
}
