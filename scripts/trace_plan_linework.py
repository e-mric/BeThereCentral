#!/usr/bin/env python3
"""Extract source-pixel floor masks and white plan linework for review.

This is an analysis utility, not app runtime code. It deliberately does no gap
closing or short-stroke filtering: source discontinuities remain visible. Output
coordinates are absolute pixels in the original 2048 x 1448 source image.

Requires Pillow, NumPy and OpenCV (tested with the bundled Python runtime).
"""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

import cv2
import numpy as np
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "docs/assets/source-plans"
DEFAULT_OUT = Path("/tmp/btc-linework")
FLOORS = ("ground", "first", "second", "third", "fourth")
CROP = (80, 580, 1940, 1280)


def badge_centers() -> dict[str, list[tuple[float, float]]]:
    """Read numbered marker anchors from the reviewed source-data declaration."""
    source = (ROOT / "composeApp/src/commonMain/kotlin/com/betherecentral/features/building/data/BeCentralPlans.kt").read_text()
    found: dict[str, list[tuple[float, float]]] = {floor: [] for floor in FLOORS}
    # Keep each PlanFloor block, then read only place(...) calls from its places list.
    blocks = re.findall(r'PlanFloor\(\s*id = "([a-z]+)"(.*?)(?=\n\s*PlanFloor\(|\n\s*\),\s*\n\s*\)\s*\n\s*\))', source, re.S)
    # Regex structure in the source is intentionally simple; fallback to per-floor
    # ranges makes this robust to nested calls and formatting changes.
    starts = list(re.finditer(r'PlanFloor\(\s*id = "([a-z]+)"', source))
    for index, match in enumerate(starts):
        floor = match.group(1)
        if floor not in found:
            continue
        end = starts[index + 1].start() if index + 1 < len(starts) else len(source)
        block = source[match.start():end]
        place_section = re.search(r'places\s*=\s*listOf\((.*?)(?:\n\s*\)\s*,?\s*\n\s*notes\s*=)', block, re.S)
        if not place_section:
            continue
        for call in re.finditer(r'\bplace\(\s*\d+\s*,\s*"[^"\n]*"\s*,\s*"([^"]*)"', place_section.group(1)):
            for coord in re.finditer(r'(\d+(?:\.\d+)?),(\d+(?:\.\d+)?)', call.group(1)):
                found[floor].append((float(coord.group(1)), float(coord.group(2))))
    return found


def special_ground_symbols() -> list[tuple[float, float]]:
    """Non-numbered circular info and entrance badges on the ground plan."""
    return [(1273.0, 1126.0), (1380.0, 1198.0)]


def stair_polygons() -> dict[str, list[list[tuple[float, float]]]]:
    """Read the named stair polygons from the reviewed source-plan declarations."""
    source = (ROOT / "composeApp/src/commonMain/kotlin/com/betherecentral/features/building/data/BeCentralPlans.kt").read_text()
    found: dict[str, list[list[tuple[float, float]]]] = {floor: [] for floor in FLOORS}
    starts = list(re.finditer(r'PlanFloor\(\s*id = "([a-z]+)"', source))
    for index, match in enumerate(starts):
        floor = match.group(1)
        if floor not in found:
            continue
        end = starts[index + 1].start() if index + 1 < len(starts) else len(source)
        block = source[match.start():end]
        for region in re.finditer(
                r'region\(\s*"([^"]+)"\s*,\s*"[^"]*"\s*,\s*NAVY\s*,\s*"([^"]+)"\s*,\s*PlanRegionKind\.STAIRS\s*\)',
                block, re.S):
            points = [(float(x), float(y)) for x, y in
                      re.findall(r'(\d+(?:\.\d+)?),(\d+(?:\.\d+)?)', region.group(2))]
            if len(points) >= 3:
                found[floor].append(points)
    return found


STAIRS = stair_polygons()


def raster_polygons(mask: np.ndarray, x0: int, y0: int, epsilon: float = 0.5) -> list[dict]:
    contours, hierarchy = cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    if hierarchy is None:
        return []
    result = []
    for i, contour in enumerate(contours):
        poly = cv2.approxPolyDP(contour, epsilon, True).reshape(-1, 2)
        if len(poly) == 0:
            continue
        parent = int(hierarchy[0][i][3])
        depth = 0
        ancestor = parent
        while ancestor >= 0:
            depth += 1
            ancestor = int(hierarchy[0][ancestor][3])
        result.append({
            "hole": depth % 2 == 1,
            "depth": depth,
            "parent": parent,
            "hierarchy": [int(v) for v in hierarchy[0][i]],
            "area_px2": round(abs(float(cv2.contourArea(contour))), 1),
            "stroke_width_px": 1.0 if len(poly) < 3 else None,
            "points": [[int(x + x0), int(y + y0)] for x, y in poly],
        })
    return result


def exclude_discs(mask: np.ndarray, centers: list[tuple[float, float]], radius: int) -> None:
    ox, oy = CROP[:2]
    for x, y in centers:
        cx, cy = round(x - ox), round(y - oy)
        if 0 <= cx < mask.shape[1] and 0 <= cy < mask.shape[0]:
            cv2.circle(mask, (cx, cy), radius, 0, thickness=-1)


def measured_badge_radius(rgb: np.ndarray, center: tuple[float, float]) -> int:
    """Measure the white badge extent by sampling bright pixels around its center."""
    x, y = center
    values = []
    angles = np.arange(0, 360, 5) * np.pi / 180
    for radius in range(10, 19):
        xx = np.rint(x + radius * np.cos(angles)).astype(int)
        yy = np.rint(y + radius * np.sin(angles)).astype(int)
        inside = (xx >= 0) & (yy >= 0) & (xx < rgb.shape[1]) & (yy < rgb.shape[0])
        pixels = rgb[yy[inside], xx[inside]]
        bright = ((pixels.min(axis=1) > 200) & (pixels.max(axis=1) > 235) &
                  ((pixels.max(axis=1) - pixels.min(axis=1)) < 60))
        values.append((radius, float(np.mean(bright))))
    eligible = [r for r, fraction in values if fraction >= 0.20]
    return min(18, max(16, max(eligible, default=16)))


def contour_lines(mask: np.ndarray, x0: int, y0: int) -> list[dict]:
    """Represent observed white stroke pixels as thin filled polygon contours."""
    # No simplification here: a subpixel simplification can drop a narrow source
    # stroke or move its boundary enough to lose several observed pixels.
    return raster_polygons(mask, x0, y0, epsilon=0.0)


def rasterize_contours(contours: list[dict], shape: tuple[int, int], x0: int, y0: int) -> np.ndarray:
    """Reconstruct the vector representation to measure pixel retention."""
    rebuilt = np.zeros(shape, dtype=np.uint8)
    if not contours:
        return rebuilt
    vector_contours = [np.asarray([[x - x0, y - y0] for x, y in item["points"]], dtype=np.int32).reshape(-1, 1, 2)
                       for item in contours]
    vector_hierarchy = np.asarray([item["hierarchy"] for item in contours], dtype=np.int32).reshape(1, -1, 4)
    cv2.drawContours(rebuilt, vector_contours, -1, 255, cv2.FILLED, hierarchy=vector_hierarchy)
    return rebuilt


def process(floor: str, source: Path, out_dir: Path, centers: list[tuple[float, float]]) -> dict:
    image = np.asarray(Image.open(source).convert("RGB"))
    if image.shape[:2] != (1448, 2048):
        raise ValueError(f"{source}: expected 2048 x 1448, got {image.shape[1]} x {image.shape[0]}")
    x0, y0, x1, y1 = CROP
    rgb = image[y0:y1, x0:x1]
    hsv = cv2.cvtColor(rgb, cv2.COLOR_RGB2HSV)

    # Separate floor ink from the pale page. Neutral grey is part of the ground
    # floor's unassigned footprint. Closing is only used for occupancy support
    # and shell topology; exported linework always comes from unchanged pixels.
    saturated = (hsv[:, :, 1] >= 22).astype(np.uint8) * 255
    neutral_structure = ((rgb.max(axis=2) >= 190) & (rgb.max(axis=2) < 244) &
                         ((rgb.max(axis=2) - rgb.min(axis=2)) < 15)).astype(np.uint8) * 255
    neutral_in_footprint = neutral_structure if floor != "first" else np.zeros_like(neutral_structure)
    shell_source = cv2.bitwise_or(saturated, neutral_in_footprint)
    shell_support = cv2.morphologyEx(shell_source, cv2.MORPH_CLOSE, np.ones((7, 7), np.uint8))

    # Exclude headings, floor numbers, and isolated plan annotations by retaining
    # only large connected occupancy pieces. Detached wings remain separate when
    # they have building-scale area. Badge-sized holes are filtered below.
    component_count, labels, stats, _ = cv2.connectedComponentsWithStats((shell_support > 0).astype(np.uint8), 8)
    shell_occupancy = np.zeros_like(shell_support)
    for component in range(1, component_count):
        if int(stats[component, cv2.CC_STAT_AREA]) > 10_000:
            shell_occupancy[labels == component] = 255
    raw_shell_polys = raster_polygons(shell_occupancy, x0, y0, epsilon=0.5)
    shell_polys = [p for p in raw_shell_polys if (not p["hole"] and p["area_px2"] >= 10_000) or
                   (p["hole"] and p["area_px2"] >= 3000)]

    # Source white strokes include antialiased pixels whose weakest channel can
    # be as low as 228. A local occupancy mask rejects broad courtyards/page
    # white while retaining those observed pixels. This support close does not
    # alter the source-pixel mask or bridge gaps in exported linework.
    near_white = ((rgb.min(axis=2) > 200) & (rgb.max(axis=2) > 235) &
                  ((rgb.max(axis=2) - rgb.min(axis=2)) < 60)).astype(np.uint8)
    support_source = cv2.bitwise_or(saturated, neutral_in_footprint)
    line_support = (cv2.morphologyEx(support_source, cv2.MORPH_CLOSE, np.ones((9, 9), np.uint8)) > 0)
    line_support &= shell_occupancy > 0
    white_strokes = ((near_white > 0) & line_support).astype(np.uint8) * 255
    # Remove each isolated badge's observed bright component, including its
    # antialiased edge. A centre-radius alone leaves crescent artifacts.
    _, bright_labels, bright_stats, _ = cv2.connectedComponentsWithStats(near_white, 8)
    numbered_badge_mask = np.zeros_like(white_strokes)
    for x, y in centers:
        cx, cy = round(x - x0), round(y - y0)
        local = bright_labels[max(0, cy-12):cy+13, max(0, cx-12):cx+13]
        for label in np.unique(local):
            if label == 0:
                continue
            bx, by, width, height, area = bright_stats[label]
            if width <= 40 and height <= 40 and area <= 1600:
                numbered_badge_mask[bright_labels == label] = 255
    exclusions = list(centers) + (special_ground_symbols() if floor == "ground" else [])
    line_pixels_before_badges = int(np.count_nonzero(white_strokes))
    badge_pixels_excluded = int(np.count_nonzero((white_strokes > 0) & (numbered_badge_mask > 0)))
    white_strokes[numbered_badge_mask > 0] = 0
    badge_radii = []
    for center in exclusions:
        radius = measured_badge_radius(image, center)
        badge_radii.append(radius)
        before = int(np.count_nonzero(white_strokes))
        exclude_discs(white_strokes, [center], radius)
        badge_pixels_excluded += before - int(np.count_nonzero(white_strokes))

    # Classify treads using the source's explicitly named stair polygons. This
    # works for olive, purple, curved, and navy stairs alike.
    stair_region = np.zeros(white_strokes.shape, dtype=np.uint8)
    for polygon in STAIRS[floor]:
        points = np.asarray([[(round(x - x0), round(y - y0)) for x, y in polygon]], dtype=np.int32)
        cv2.fillPoly(stair_region, points, 255)
    stair_near = cv2.dilate(stair_region, np.ones((3, 3), np.uint8)) > 0
    stair_lines = ((white_strokes > 0) & stair_near).astype(np.uint8) * 255
    partition_lines = ((white_strokes > 0) & ~stair_near).astype(np.uint8) * 255

    line_polys = {
        "partition": contour_lines(partition_lines, x0, y0),
        "stair_tread": contour_lines(stair_lines, x0, y0),
    }
    reconstructed_lines = np.zeros(white_strokes.shape, dtype=np.uint8)
    for contours in line_polys.values():
        reconstructed_lines = cv2.bitwise_or(
            reconstructed_lines, rasterize_contours(contours, white_strokes.shape, x0, y0))
    source_line_pixels = int(np.count_nonzero(white_strokes))
    retained_line_pixels = int(np.count_nonzero((white_strokes > 0) & (reconstructed_lines > 0)))
    payload = {
        "schema": "betherecentral.plan-linework.v1",
        "floor": floor,
        "source": str(source),
        "source_size": [2048, 1448],
        "crop": list(CROP),
        "coordinate_space": "absolute source-image pixels; x right, y down",
        "method": {
            "shell_mask": "saturated plan pigment plus neutral gray footprint closed with a 7x7 kernel for shell topology only; retain connected occupancy pieces >10000 px2 and large holes >=3000 px2",
            "white_line_mask": "source pixels with min(R,G,B)>200, max(R,G,B)>235, chroma<60, inside a 9x9-closed saturated-or-neutral plan-ink support mask; isolated bright badge components and measured badge discs radius 16..18 removed; source mask itself is never closed",
            "line_geometry": "filled observed-pixel polygons and one-pixel polylines for degenerate contours; no line simplification (epsilon 0.0 px)",
            "limitations": "White source icons/marks touching saturated plan fills can survive. Pixel coverage measures contour round-trip fidelity only; it does not prove every visible wall was identified. This is raster tracing, not verified wall geometry.",
        },
        "shell": shell_polys,
        "linework": line_polys,
        "counts": {
            "shell_contours": len(shell_polys),
            "shell_holes": sum(p["hole"] for p in shell_polys),
            "partition_strokes": len(line_polys["partition"]),
            "stair_tread_strokes": len(line_polys["stair_tread"]),
            "white_source_pixels": source_line_pixels,
            "line_candidate_pixels_before_badges": line_pixels_before_badges,
            "badge_pixels_excluded": badge_pixels_excluded,
            "badge_exclusion_radii_px": badge_radii,
            "line_pixels_retained_after_vectorization": retained_line_pixels,
            "line_pixel_coverage_percent": round(100 * retained_line_pixels / max(source_line_pixels, 1), 2),
            "line_pixels_lost_after_vectorization": source_line_pixels - retained_line_pixels,
            "line_pixels_added_by_vectorization": int(np.count_nonzero((reconstructed_lines > 0) & (white_strokes == 0))),
            "badge_discs_removed": len(exclusions),
            "stair_regions_used": len(STAIRS[floor]),
        },
    }
    out_dir.mkdir(parents=True, exist_ok=True)
    json_path = out_dir / f"{floor}.json"
    json_path.write_text(json.dumps(payload, separators=(",", ":")))

    # Audit image: translucent shell outlines and contrasting, thin line polygons
    # over the untouched original pixels. This keeps provenance easy to inspect.
    overlay = Image.open(source).convert("RGBA")
    draw = ImageDraw.Draw(overlay, "RGBA")
    for item in shell_polys:
        if item["hole"]:
            continue
        pts = [tuple(p) for p in item["points"]]
        if len(pts) >= 3:
            draw.line(pts + [pts[0]], fill=(50, 240, 255, 185), width=2)
    line_layer = np.zeros((image.shape[0], image.shape[1], 4), dtype=np.uint8)
    line_layer[y0:y1, x0:x1][partition_lines > 0] = (255, 30, 190, 235)
    line_layer[y0:y1, x0:x1][stair_lines > 0] = (30, 250, 90, 255)
    overlay = Image.alpha_composite(overlay, Image.fromarray(line_layer, mode="RGBA"))
    overlay.save(out_dir / f"{floor}-overlay.png")
    return payload


def kotlin_source(payloads: dict[str, dict]) -> str:
    """Emit a cached Kotlin SourcePlanLinework object from traced contours."""
    def chunks(contours: list[dict], maximum: int = 3500) -> list[str]:
        encoded = "|".join(" ".join(f"{x},{y}" for x, y in item["points"]) for item in contours)
        return [encoded[i:i + maximum] for i in range(0, len(encoded), maximum)] or [""]

    def list_expr(contours: list[dict]) -> str:
        values = chunks(contours)
        return "decodeContours(listOf(\n" + ",\n".join('            "' + part + '"' for part in values) + "\n        ))"

    lines = [
        "package com.betherecentral.features.building.data",
        "",
        "import com.betherecentral.features.building.domain.PlanLinework",
        "import com.betherecentral.features.building.domain.PlanPoint",
        "",
        "/** Generated from docs/assets/source-plans with scripts/trace_plan_linework.py. */",
        "object SourcePlanLinework {",
        "    private val cached: Map<String, PlanLinework> by lazy {",
        "        mapOf(",
        "            \"ground\" to ground(), \"first\" to first(), \"second\" to second(),",
        "            \"third\" to third(), \"fourth\" to fourth(),",
        "        )",
        "    }",
        "",
        '    fun forFloor(id: String): PlanLinework = requireNotNull(cached[id]) { "Unknown floor: $id" }',
        "",
        "    private fun decodeContours(chunks: List<String>): List<List<PlanPoint>> =",
        "        chunks.joinToString(\"\").split('|').filter { it.isNotBlank() }.map { contour ->",
        "            contour.trim().split(' ').map { token ->",
        "                val (x, y) = token.split(',')",
        "                PlanPoint(x.toDouble(), y.toDouble())",
        "            }",
        "        }",
    ]
    for floor in FLOORS:
        payload = payloads[floor]
        lines.extend([
            "",
            f"    private fun {floor}() = PlanLinework(",
            "        partitions = " + list_expr(payload["linework"]["partition"]) + ",",
            "        stairs = " + list_expr(payload["linework"]["stair_tread"]),
            "    )",
        ])
    lines.extend(["}", ""])
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, default=SOURCE_DIR)
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    parser.add_argument("--floors", nargs="+", choices=FLOORS, default=list(FLOORS))
    parser.add_argument("--kotlin-out", type=Path, help="also emit a cached SourcePlanLinework.kt file")
    args = parser.parse_args()
    if args.kotlin_out and set(args.floors) != set(FLOORS):
        parser.error("--kotlin-out requires all five floors")
    centers = badge_centers()
    payloads = {}
    summary = []
    for floor in args.floors:
        source = args.source_dir / f"{floor}.png"
        if not source.exists():
            raise SystemExit(f"Missing source image: {source}")
        item = process(floor, source, args.out, centers[floor])
        payloads[floor] = item
        summary.append({"floor": floor, **item["counts"]})
    (args.out / "manifest.json").write_text(json.dumps({
        "schema": "betherecentral.plan-linework.v1",
        "source_pixel_coordinates": True,
        "floors": summary,
    }, indent=2) + "\n")
    if args.kotlin_out:
        args.kotlin_out.parent.mkdir(parents=True, exist_ok=True)
        args.kotlin_out.write_text(kotlin_source(payloads))
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
