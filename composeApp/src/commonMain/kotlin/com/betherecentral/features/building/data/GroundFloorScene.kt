package com.betherecentral.features.building.data

import com.betherecentral.features.building.domain.FloorMaterial
import com.betherecentral.features.building.domain.FloorMaterialPatch
import com.betherecentral.features.building.domain.FloorProp
import com.betherecentral.features.building.domain.FloorScene
import com.betherecentral.features.building.domain.CampusPropAsset
import com.betherecentral.features.building.domain.FloorWallSegment
import com.betherecentral.features.building.domain.PlanPoint

/** Authored decorative recipe in supplied-plan pixels; it contains no tenant or route claims. */
object GroundFloorScene {
    val scene = FloorScene(
        floorId = "ground",
        materials = listOf(
            FloorMaterialPatch("orange-oak", "ground-orange", FloorMaterial.WOOD),
            FloorMaterialPatch("lobby-stone", "ground-lobby", FloorMaterial.TILE),
            FloorMaterialPatch("bike-stone", "ground-bike-room", FloorMaterial.TILE),
            FloorMaterialPatch("fari-oak", "ground-fari", FloorMaterial.WOOD),
        ),
        walls = listOf(
            FloorWallSegment("orange-glazed-divider", "ground-orange", PlanPoint(800.0, 1017.0), PlanPoint(876.0, 995.0), windows = true),
            FloorWallSegment("bike-partition-angle", "ground-lobby", PlanPoint(1199.0, 1058.0), PlanPoint(1177.0, 1138.0)),
            FloorWallSegment("bike-partition-straight", "ground-lobby", PlanPoint(1177.0, 1138.0), PlanPoint(1177.0, 1185.0)),
            FloorWallSegment("lobby-slate-return", "ground-lobby", PlanPoint(1360.0, 1055.0), PlanPoint(1380.0, 1072.0)),
        ),
        props = listOf(
            // Orange area: an open shared-work lounge, with its west stair left clear.
            prop("orange-rug", "ground-orange", 861.0, 1114.0, 206.0, 118.0, CampusPropAsset.RUG, -1),
            prop("orange-worktable", "ground-orange", 964.0, 1031.0, 56.0, 40.0, CampusPropAsset.SHARED_TABLE),
            prop("orange-sofa", "ground-orange", 955.0, 1113.0, 132.0, 78.0, CampusPropAsset.SOFA),
            prop("orange-plant", "ground-orange", 704.0, 1153.0, 24.0, 28.0, CampusPropAsset.PLANT),
            prop("orange-round-table", "ground-orange", 995.0, 1085.0, 66.0, 56.0, CampusPropAsset.ROUND_TABLE),
            // Lobby: furniture stays clear of stairs; bike rack follows the source icon.
            prop("lobby-bike-parking", "ground-bike-room", 1170.0, 1090.0, 22.0, 20.0, CampusPropAsset.BIKE_RACK),
            prop("lobby-rug", "ground-lobby", 1310.0, 1160.0, 40.0, 36.0, CampusPropAsset.RUG, -1),
            prop("lobby-desk", "ground-lobby", 1272.0, 1128.0, 40.0, 32.0, CampusPropAsset.RECEPTION_DESK),
            prop("lobby-plant", "ground-lobby", 1339.0, 1112.0, 24.0, 24.0, CampusPropAsset.PLANT),
            // Detached FARI area: a compact visitor lounge; east stair is kept clear.
            prop("fari-rug", "ground-fari", 1690.0, 1090.0, 110.0, 70.0, CampusPropAsset.RUG, -1),
            prop("fari-table", "ground-fari", 1714.0, 1152.0, 48.0, 36.0, CampusPropAsset.SHARED_TABLE),
            prop("fari-sofa", "ground-fari", 1710.0, 1105.0, 80.0, 54.0, CampusPropAsset.SOFA),
            prop("fari-plant", "ground-fari", 1602.0, 1052.0, 24.0, 28.0, CampusPropAsset.PLANT),
            prop("fari-shelf", "ground-fari", 1802.0, 1142.0, 50.0, 66.0, CampusPropAsset.BOOKCASE),
        ),
    )

    private fun prop(
        id: String, region: String, x: Double, y: Double, width: Double, height: Double,
        asset: CampusPropAsset, layer: Int = 0,
    ) = FloorProp(id, region, PlanPoint(x, y), width, height, asset, layer)
}
