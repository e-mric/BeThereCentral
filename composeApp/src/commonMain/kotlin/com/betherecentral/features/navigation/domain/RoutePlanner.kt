package com.betherecentral.features.navigation.domain

/** Finds the least-cost path through caller-supplied, directed building graph edges. */
object RoutePlanner {
    fun findRoute(
        edges: List<NavigationEdge>,
        startNodeId: String,
        destinationNodeId: String,
        fromCheckpointId: String,
        destinationRoomId: String,
        preference: RoutePreference = RoutePreference.DEFAULT,
    ): Route? {
        val stepFree = preference == RoutePreference.STEP_FREE
        val adjacency = edges.groupBy { it.fromNodeId }
        val distances = mutableMapOf(startNodeId to 0.0)
        val previous = mutableMapOf<String, NavigationEdge>()
        val pending = mutableSetOf(startNodeId)
        val visited = mutableSetOf<String>()

        while (pending.isNotEmpty()) {
            val current = pending.minByOrNull { distances[it] ?: Double.POSITIVE_INFINITY } ?: break
            pending.remove(current)
            if (!visited.add(current)) continue
            if (current == destinationNodeId) break
            for (edge in adjacency[current].orEmpty()) {
                if (stepFree && (!edge.isStepFree || edge.mode == RouteMode.STAIRS)) continue
                val preferenceCost = when (preference) {
                    RoutePreference.DEFAULT, RoutePreference.STEP_FREE -> edge.routingCost
                    RoutePreference.LIFT_ORIENTED -> edge.routingCost + if (edge.mode == RouteMode.STAIRS) 8.0 else 0.0
                }
                val candidate = (distances[current] ?: Double.POSITIVE_INFINITY) + preferenceCost
                if (candidate < (distances[edge.toNodeId] ?: Double.POSITIVE_INFINITY)) {
                    distances[edge.toNodeId] = candidate
                    previous[edge.toNodeId] = edge
                    pending += edge.toNodeId
                }
            }
        }

        if (destinationNodeId !in distances) return null
        val reversedEdges = mutableListOf<NavigationEdge>()
        var cursor = destinationNodeId
        while (cursor != startNodeId) {
            val edge = previous[cursor] ?: return null
            reversedEdges += edge
            cursor = edge.fromNodeId
        }
        val legs = reversedEdges.asReversed().map { edge ->
            RouteLeg(edge.fromNodeId, edge.toNodeId, edge.floorId, edge.mode, edge.distanceMeters, edge.points)
        }
        val distance = legs.sumOf { it.distanceMeters }
        val duration = legs.sumOf { leg ->
            when (leg.mode) {
                RouteMode.WALK -> leg.distanceMeters / 1.2
                RouteMode.STAIRS -> leg.distanceMeters / 0.6
                RouteMode.LIFT -> 12.0
            }
        }.toInt()
        return Route(fromCheckpointId, destinationRoomId, legs, distance, duration, stepFree, preference)
    }
}
