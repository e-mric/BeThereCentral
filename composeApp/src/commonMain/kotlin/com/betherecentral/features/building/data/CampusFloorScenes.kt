package com.betherecentral.features.building.data

import com.betherecentral.features.building.domain.FloorScene

/** Current display recipes retain authored surfaces and props while source ink owns walls and stairs. */
object CampusFloorScenes {
    private val scenes: Map<String, FloorScene> = BeCentralPlans.floors.associate { floor ->
        val authored = if (floor.id == GroundFloorScene.scene.floorId) GroundFloorScene.scene
        else requireNotNull(UpperFloorScenes.forFloor(floor.id)) { "No asset recipe for floor ${floor.id}" }
        val linework = SourcePlanLinework.forFloor(floor.id)
        floor.id to authored.copy(
            walls = emptyList(),
            props = authored.props.filterNot(linework::intersects),
            sourceLinework = linework,
        )
    }

    fun forFloor(id: String): FloorScene =
        requireNotNull(scenes[id]) { "No asset recipe for floor $id" }
}
