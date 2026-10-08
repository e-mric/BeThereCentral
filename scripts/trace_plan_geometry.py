#!/usr/bin/env python3
"""Trace plan silhouettes from the five supplied 2048x1448 schematic PNGs.

Usage:
  python3 scripts/trace_plan_geometry.py ground.png first.png second.png third.png fourth.png \
    --output composeApp/src/commonMain/kotlin/com/betherecentral/features/building/data/TracedPlanGeometry.kt

Only a coarse vector trace is emitted. Source rasters are read locally and never embedded.
The 6-pixel grid, classification, component sizes and simplification are fixed for
reproducible output; the result remains a visual trace, not a measured floor plan.
"""

from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
from PIL import Image

FLOORS = ("ground", "first", "second", "third", "fourth")
LEFT, TOP, RIGHT, BOTTOM = 80, 580, 1940, 1280
STEP = 6
GRID_WIDTH = (RIGHT - LEFT) // STEP
GRID_HEIGHT = ((BOTTOM - TOP) + STEP - 1) // STEP


def components(mask: np.ndarray) -> list[list[tuple[int, int]]]:
    seen = np.zeros(mask.shape, dtype=bool)
    groups: list[list[tuple[int, int]]] = []
    for start_y, start_x in zip(*np.nonzero(mask)):
        if seen[start_y, start_x]:
            continue
        group = [(int(start_y), int(start_x))]
        seen[start_y, start_x] = True
        for y, x in group:
            for yy, xx in ((y - 1, x), (y + 1, x), (y, x - 1), (y, x + 1)):
                if (
                    0 <= yy < mask.shape[0]
                    and 0 <= xx < mask.shape[1]
                    and mask[yy, xx]
                    and not seen[yy, xx]
                ):
                    seen[yy, xx] = True
                    group.append((yy, xx))
        groups.append(group)
    return sorted(groups, key=len, reverse=True)


def classified_mask(source: Path) -> np.ndarray:
    image = Image.open(source).convert("RGB")
    if image.size != (2048, 1448):
        raise ValueError(f"{source}: expected 2048x1448, got {image.size}")
    pixels = np.asarray(image)[TOP:BOTTOM, LEFT:RIGHT]
    pixels = np.pad(pixels, ((0, GRID_HEIGHT * STEP - pixels.shape[0]), (0, 0), (0, 0)),
                    constant_values=255)
    cells = pixels.reshape(GRID_HEIGHT, STEP, GRID_WIDTH, STEP, 3)
    # Colored plan or grey plan area, separated from the near-white paper.
    fraction = (cells.min(axis=4) < 240).mean(axis=(1, 3))
    mask = fraction > 0.15
    result = np.zeros_like(mask)
    for group in components(mask):
        if len(group) >= 1000:  # suppress stray title/legend marks
            for y, x in group:
                result[y, x] = True
    for group in components(~result):
        touches_crop = any(
            y in (0, GRID_HEIGHT - 1) or x in (0, GRID_WIDTH - 1)
            for y, x in group
        )
        if not touches_crop and len(group) < 100:  # badges and thin white plan lines
            for y, x in group:
                result[y, x] = True
    return result


def traced_edges(mask: np.ndarray) -> list[list[tuple[int, int]]]:
    edges: dict[tuple[int, int], list[tuple[int, int]]] = {}

    def edge(start: tuple[int, int], end: tuple[int, int]) -> None:
        edges.setdefault(start, []).append(end)

    height, width = mask.shape
    for y, x in zip(*np.nonzero(mask)):
        y, x = int(y), int(x)
        if y == 0 or not mask[y - 1, x]:
            edge((x, y), (x + 1, y))
        if x == width - 1 or not mask[y, x + 1]:
            edge((x + 1, y), (x + 1, y + 1))
        if y == height - 1 or not mask[y + 1, x]:
            edge((x + 1, y + 1), (x, y + 1))
        if x == 0 or not mask[y, x - 1]:
            edge((x, y + 1), (x, y))

    direction = {(1, 0): 0, (0, 1): 1, (-1, 0): 2, (0, -1): 3}
    loops = []
    while edges:
        start = min(edges)
        current = start
        previous_direction = None
        loop = [start]
        while True:
            options = edges[current]
            if previous_direction is None:
                next_point = options[0]
            else:
                next_point = min(
                    options,
                    key=lambda p: (
                        (direction[(p[0] - current[0], p[1] - current[1])]
                         - previous_direction - 1) % 4,
                        p,
                    ),
                )
            options.remove(next_point)
            if not options:
                del edges[current]
            previous_direction = direction[(next_point[0] - current[0],
                                            next_point[1] - current[1])]
            current = next_point
            if current == start:
                break
            loop.append(current)
        loops.append([(LEFT + x * STEP, TOP + y * STEP) for x, y in loop])
    return loops


def area(points: list[tuple[int, int]]) -> float:
    return sum(x * yy - xx * y for (x, y), (xx, yy) in
               zip(points, points[1:] + points[:1])) / 2


def segment_distance(point, start, end) -> float:
    px, py = point
    ax, ay = start
    bx, by = end
    dx, dy = bx - ax, by - ay
    if dx == dy == 0:
        return ((px - ax) ** 2 + (py - ay) ** 2) ** 0.5
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)))
    return ((px - ax - t * dx) ** 2 + (py - ay - t * dy) ** 2) ** 0.5


def simplify_open(points: list[tuple[int, int]], tolerance: float) -> list[tuple[int, int]]:
    if len(points) <= 2:
        return points
    distances = [segment_distance(p, points[0], points[-1]) for p in points[1:-1]]
    furthest = max(range(len(distances)), key=distances.__getitem__)
    if distances[furthest] <= tolerance:
        return [points[0], points[-1]]
    pivot = furthest + 1
    return (simplify_open(points[:pivot + 1], tolerance)[:-1]
            + simplify_open(points[pivot:], tolerance))


def simplify_loop(points: list[tuple[int, int]]) -> list[tuple[int, int]]:
    first = min(range(len(points)), key=lambda i: (points[i][1], points[i][0]))
    rotated = points[first:] + points[:first]
    split = max(range(1, len(rotated)), key=lambda i:
                (rotated[i][0] - rotated[0][0]) ** 2
                + (rotated[i][1] - rotated[0][1]) ** 2)
    return (simplify_open(rotated[:split + 1], 8)[:-1]
            + simplify_open(rotated[split:] + [rotated[0]], 8)[:-1])


def kotlin_paths(paths: list[list[tuple[int, int]]]) -> str:
    return "listOf(\n" + "".join(
        '            path("' + " ".join(f"{x},{y}" for x, y in p) + '"),\n'
        for p in paths
    ) + "        )"


def generate(sources: list[Path]) -> str:
    values = {}
    for floor, source in zip(FLOORS, sources):
        loops = [simplify_loop(loop) for loop in traced_edges(classified_mask(source))]
        footprints = sorted((p for p in loops if area(p) > 0), key=lambda p: -area(p))
        voids = sorted((p for p in loops if area(p) < 0 and abs(area(p)) >= 100 * STEP * STEP),
                       key=lambda p: area(p))
        values[floor] = (footprints, voids)
        print(f"{floor}: {len(footprints)} footprints, {len(voids)} voids, "
              f"{sum(len(p) for p in footprints + voids)} points")
    lines = [
        "package com.betherecentral.features.building.data",
        "",
        "import com.betherecentral.features.building.domain.PlanPoint",
        "",
        "/** Coarse source-pixel silhouette trace. Regenerate with scripts/trace_plan_geometry.py. */",
        "internal object TracedPlanGeometry {",
        "    private fun path(points: String): List<PlanPoint> = points.split(\" \").map {",
        "        val (x, y) = it.split(\",\")",
        "        PlanPoint(x.toDouble(), y.toDouble())",
        "    }",
        "    val footprints: Map<String, List<List<PlanPoint>>> = mapOf(",
    ]
    for floor in FLOORS:
        lines += [f'        "{floor}" to {kotlin_paths(values[floor][0])},']
    lines += ["    )", "    val voids: Map<String, List<List<PlanPoint>>> = mapOf("]
    for floor in FLOORS:
        lines += [f'        "{floor}" to {kotlin_paths(values[floor][1])},']
    lines += ["    )", "}", ""]
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("sources", nargs=5, type=Path, metavar="PNG")
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    args.output.write_text(generate(args.sources))


if __name__ == "__main__":
    main()
