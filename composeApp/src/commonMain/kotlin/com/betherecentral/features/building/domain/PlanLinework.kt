package com.betherecentral.features.building.domain

/** Filled source-pixel contours. Nested contours are holes (even-odd fill).
 * These are observed schematic marks, not a wall/door or navigation graph.
 */
data class PlanLinework(
    val partitions: List<List<PlanPoint>>,
    val stairs: List<List<PlanPoint>>,
) {
    /** Full furniture rectangle plus a small visual clearance must avoid source ink. */
    fun intersects(prop: FloorProp, clearance: Double = 3.0): Boolean {
        val left = prop.center.x - prop.width / 2 - clearance
        val right = prop.center.x + prop.width / 2 + clearance
        val top = prop.center.y - prop.height / 2 - clearance
        val bottom = prop.center.y + prop.height / 2 + clearance
        val all = partitions + stairs
        if (all.count { PlanWorld.contains(it, PlanPoint(left, top)) } % 2 == 1) return true
        return all.any { polygon ->
            polygon.indices.any { index ->
                val a = polygon[index]
                val b = polygon[(index + 1) % polygon.size]
                var enter = 0.0
                var leave = 1.0
                fun clip(p: Double, q: Double): Boolean {
                    if (p == 0.0) return q >= 0.0
                    val t = q / p
                    if (p < 0) enter = maxOf(enter, t) else leave = minOf(leave, t)
                    return enter <= leave
                }
                val dx = b.x - a.x
                val dy = b.y - a.y
                clip(-dx, a.x - left) && clip(dx, right - a.x) &&
                    clip(-dy, a.y - top) && clip(dy, bottom - a.y)
            }
        }
    }
}
