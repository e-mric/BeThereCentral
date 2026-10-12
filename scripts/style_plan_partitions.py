#!/usr/bin/env python3
"""Generate bounded decorative wall masks from extracted source partitions.

This offline asset step consumes trace_plan_linework.py JSON. Runtime Kotlin has
no Python or OpenCV dependency. Source strokes remain immutable; the output is a
separate decorative mask with at most three source pixels of added ink.
"""
from __future__ import annotations

import argparse
import importlib.util
import hashlib
import json
from pathlib import Path

import cv2
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
FLOORS = ("ground", "first", "second", "third", "fourth")
X0, Y0, X1, Y1 = 80, 580, 1940, 1280
SHAPE = (Y1 - Y0, X1 - X0)


def contours_mask(contours: list[dict]) -> np.ndarray:
    """Rasterize extracted contour JSON with the tracer's round-trip helper."""
    spec = importlib.util.spec_from_file_location("trace_plan_linework", ROOT / "scripts/trace_plan_linework.py")
    tracer = importlib.util.module_from_spec(spec)
    assert spec and spec.loader
    spec.loader.exec_module(tracer)
    return tracer.rasterize_contours(contours, SHAPE, X0, Y0)


def shell_occupancy_mask(contours: list[dict]) -> np.ndarray:
    """Rebuild filtered shell contours with parity fill, ignoring stale hierarchy links."""
    mask = np.zeros(SHAPE, dtype=np.uint8)
    for item in contours:
        points = np.asarray([[x - X0, y - Y0] for x, y in item["points"]], dtype=np.int32)
        if len(points) < 3:
            continue
        layer = np.zeros_like(mask)
        cv2.fillPoly(layer, [points.reshape(-1, 1, 2)], 255)
        mask ^= layer
    return mask


def _component_sizes(mask: np.ndarray, connectivity: int = 8) -> tuple[int, list[int]]:
    count, _, stats, _ = cv2.connectedComponentsWithStats(mask.astype(np.uint8), connectivity=connectivity)
    return count - 1, [int(stats[i, cv2.CC_STAT_AREA]) for i in range(1, count)]


_RING = ((-1, -1), (-1, 0), (-1, 1), (0, -1), (0, 1), (1, -1), (1, 0), (1, 1))


def _simple_lut() -> tuple[bool, ...]:
    """Precompute all 256 local FG8/BG4 simple-point configurations."""
    values = []
    cardinal = ((0, 1), (1, 0), (1, 2), (2, 1))
    for code in range(256):
        patch = np.zeros((3, 3), dtype=np.uint8)
        for bit, (dy, dx) in enumerate(_RING):
            patch[dy + 1, dx + 1] = (code >> bit) & 1
        _, fg_labels = cv2.connectedComponents(patch, connectivity=8)
        foreground_groups = {int(fg_labels[dy + 1, dx + 1]) for dy, dx in _RING if patch[dy + 1, dx + 1]}
        bg = (patch == 0).astype(np.uint8)
        bg[1, 1] = 0  # The candidate center becomes foreground.
        _, bg_labels = cv2.connectedComponents(bg, connectivity=4)
        background_groups = {int(bg_labels[y, x]) for y, x in cardinal if bg[y, x]}
        values.append(len(foreground_groups) == 1 and len(background_groups) == 1)
    return tuple(values)


_SIMPLE_LUT = _simple_lut()


def grow_partition_mask(partitions: np.ndarray, stairs: np.ndarray | None = None,
                        occupancy: np.ndarray | None = None,
                        max_growth: int = 3, min_component_area: int = 12) -> tuple[np.ndarray, dict]:
    """Add only topology-safe pixels within distance three of raw partition ink."""
    raw = partitions > 0
    if raw.shape[0] < 3 or raw.shape[1] < 3:
        raise ValueError("mask must be at least 3x3")
    stair = np.zeros_like(raw) if stairs is None else stairs > 0
    allowed = np.ones_like(raw) if occupancy is None else occupancy > 0
    count, labels, stats, _ = cv2.connectedComponentsWithStats(raw.astype(np.uint8), connectivity=8)
    eligible = np.zeros_like(raw)
    tiny = np.zeros_like(raw)
    for label in range(1, count):
        component = labels == label
        if int(stats[label, cv2.CC_STAT_AREA]) < min_component_area:
            tiny |= component
        else:
            eligible |= component
    # The 7x7 close is solely a gap guard. Its filled pixels are never exported.
    guarded_source = cv2.bitwise_or(partitions, (stair.astype(np.uint8) * 255))
    closed = cv2.morphologyEx(guarded_source, cv2.MORPH_CLOSE, np.ones((7, 7), np.uint8)) > 0
    forbidden = closed & ~((guarded_source) > 0)
    distance = cv2.distanceTransform((~eligible).astype(np.uint8), cv2.DIST_L2, 5)
    tiny_guard = cv2.dilate(tiny.astype(np.uint8), np.ones((3, 3), np.uint8)) > 0
    candidates = (distance > 0) & (distance <= max_growth + 0.01) & ~forbidden & ~stair & ~tiny_guard & allowed
    # Encode each candidate's 8-neighborhood once, then update just the eight
    # neighboring codes after an accepted addition.
    ys, xs = np.where(candidates)
    result = raw.copy()
    codes = np.zeros(raw.shape, dtype=np.uint8)
    height, width = raw.shape
    for bit, (dy, dx) in enumerate(_RING):
        cy0, cy1 = max(1, 1 - dy), min(height - 1, height - 1 - dy)
        cx0, cx1 = max(1, 1 - dx), min(width - 1, width - 1 - dx)
        codes[cy0:cy1, cx0:cx1] |= (
            result[cy0 + dy:cy1 + dy, cx0 + dx:cx1 + dx].astype(np.uint8) << bit)
    added = np.zeros_like(raw)
    for y0, x0 in zip(ys, xs):
        y, x = int(y0), int(x0)
        if _SIMPLE_LUT[int(codes[y, x])]:
            result[y, x] = True
            added[y, x] = True
            for bit, (dy, dx) in enumerate(_RING):
                yy, xx = y - dy, x - dx
                if 0 < yy < height - 1 and 0 < xx < width - 1:
                    codes[yy, xx] |= 1 << bit
    # Original strokes, including tiny specks, were present throughout growth.
    metrics = {
        "raw_pixels": int(raw.sum()), "decorative_pixels": int(result.sum()),
        "growth_pixels": int(added.sum()), "tiny_components_preserved_raw_only": int(tiny.sum()),
        "gap_guard_forbidden_pixels": int(forbidden.sum()), "max_growth_px": max_growth,
        "raw_components": _component_sizes(raw)[0], "decorative_components": _component_sizes(result)[0],
        "raw_holes": _hole_count(raw), "decorative_holes": _hole_count(result),
        "raw_sha256": hashlib.sha256(raw.astype(np.uint8).tobytes()).hexdigest(),
        "decorative_sha256": hashlib.sha256(result.astype(np.uint8).tobytes()).hexdigest(),
        "raw_pixels_lost": int(np.count_nonzero(raw & ~result)),
        "growth_outside_occupancy": int(np.count_nonzero(added & ~allowed)),
        "growth_in_stairs": int(np.count_nonzero(added & stair)),
        "max_growth_distance_px": float(distanceTransformToInk(added, raw)) if np.any(added) else 0.0,
    }
    return result.astype(np.uint8) * 255, metrics


def _hole_count(mask: np.ndarray) -> int:
    contours, hierarchy = cv2.findContours(mask.astype(np.uint8), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    if hierarchy is None:
        return 0
    holes = 0
    for index in range(len(contours)):
        parent = int(hierarchy[0][index][3])
        depth = 0
        while parent >= 0:
            depth += 1
            parent = int(hierarchy[0][parent][3])
        holes += depth % 2
    return holes


def distanceTransformToInk(added: np.ndarray, raw: np.ndarray) -> float:
    distance = cv2.distanceTransform((~raw).astype(np.uint8), cv2.DIST_L2, 5)
    return round(float(distance[added].max()), 3)


def mask_contours(mask: np.ndarray, x0: int = X0, y0: int = Y0) -> list[dict]:
    """Return every body and hole contour, including degenerate speck contours."""
    contours, hierarchy = cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    if hierarchy is None:
        return []
    out = []
    for index, contour in enumerate(contours):
        points = cv2.approxPolyDP(contour, 0.0, True).reshape(-1, 2)
        out.append({
            "points": [[int(x + x0), int(y + y0)] for x, y in points],
            "hierarchy": [int(v) for v in hierarchy[0][index]],
        })
    return out


def rasterize_mask_contours(contours: list[dict], shape: tuple[int, int] = SHAPE,
                            x0: int = X0, y0: int = Y0) -> np.ndarray:
    """Rasterize contour tree with even-odd filling for export-fidelity audit."""
    if not contours:
        return np.zeros(shape, np.uint8)
    vectors = [np.asarray([[x - x0, y - y0] for x, y in item["points"]], dtype=np.int32).reshape(-1, 1, 2)
               for item in contours]
    hierarchy = np.asarray([item["hierarchy"] for item in contours], dtype=np.int32).reshape(1, -1, 4)
    rebuilt = np.zeros(shape, np.uint8)
    cv2.drawContours(rebuilt, vectors, -1, 255, cv2.FILLED, hierarchy=hierarchy)
    return rebuilt


def kotlin_source(payloads: dict[str, list[dict]]) -> str:
    def expr(contours: list[dict]) -> str:
        encoded = "|".join(" ".join(f"{x},{y}" for x, y in item["points"]) for item in contours)
        chunks = [encoded[i:i + 3500] for i in range(0, len(encoded), 3500)] or [""]
        return "decodeContours(listOf(\n" + ",\n".join('            "' + c + '"' for c in chunks) + "\n        ))"
    lines = [
        "package com.betherecentral.features.building.data", "",
        "import com.betherecentral.features.building.domain.PlanPoint", "",
        "/** Generated decorative mask from traced plan partitions; fictional styling only. */",
        "object StyledPlanPartitions {",
        "    private val cached: Map<String, List<List<PlanPoint>>> by lazy {",
        "        mapOf(" + ", ".join(f'\"{f}\" to {f}()' for f in FLOORS) + ")",
        "    }",
        '    fun forFloor(id: String): List<List<PlanPoint>> = requireNotNull(cached[id]) { "Unknown floor: $id" }',
        "    private fun decodeContours(chunks: List<String>): List<List<PlanPoint>> =",
        '        chunks.joinToString("").split(\'|\').filter { it.isNotBlank() }.map { contour ->',
        "            contour.trim().split(' ').map { token ->",
        "                val (x, y) = token.split(',')",
        "                PlanPoint(x.toDouble(), y.toDouble())",
        "            }", "        }",
    ]
    for floor in FLOORS:
        lines += ["", f"    private fun {floor}() = {expr(payloads[floor])}"]
    lines += ["}", ""]
    return "\n".join(lines)


def generate(input_dir: Path, kotlin_out: Path) -> dict:
    audit = {}
    payloads = {}
    for floor in FLOORS:
        print(f"Styling {floor} floor...", flush=True)
        data = json.loads((input_dir / f"{floor}.json").read_text())
        partitions = contours_mask(data["linework"]["partition"])
        stairs = contours_mask(data["linework"]["stair_tread"])
        occupancy = shell_occupancy_mask(data["shell"])
        print(f"  traced masks for {floor}", flush=True)
        styled, metrics = grow_partition_mask(partitions, stairs, occupancy)
        print(f"  grew {floor}: {metrics['growth_pixels']} additions", flush=True)
        metrics["source_masks_sha256"] = hashlib.sha256(
            partitions.tobytes() + stairs.tobytes() + occupancy.tobytes()).hexdigest()
        payloads[floor] = mask_contours(styled)
        rebuilt = rasterize_mask_contours(payloads[floor])
        metrics["vector_roundtrip_lost_pixels"] = int(np.count_nonzero((styled > 0) & (rebuilt == 0)))
        metrics["vector_roundtrip_added_pixels"] = int(np.count_nonzero((rebuilt > 0) & (styled == 0)))
        if metrics["raw_pixels_lost"] or metrics["growth_outside_occupancy"] or metrics["growth_in_stairs"]:
            raise ValueError(f"{floor}: source ink or growth constraint failed: {metrics}")
        if metrics["raw_components"] != metrics["decorative_components"] or metrics["raw_holes"] != metrics["decorative_holes"]:
            raise ValueError(f"{floor}: topology changed: {metrics}")
        if metrics["max_growth_distance_px"] > 3.001:
            raise ValueError(f"{floor}: growth exceeded 3 source pixels: {metrics}")
        if metrics["vector_roundtrip_lost_pixels"] or metrics["vector_roundtrip_added_pixels"]:
            raise ValueError(f"{floor}: contour export changed mask pixels: {metrics}")
        audit[floor] = {**metrics, "contours": len(payloads[floor])}
    kotlin_out.parent.mkdir(parents=True, exist_ok=True)
    kotlin_out.write_text(kotlin_source(payloads))
    return audit


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, default=Path("/tmp/btc-linework"))
    parser.add_argument("--kotlin-out", type=Path, default=ROOT / "composeApp/src/commonMain/kotlin/com/betherecentral/features/building/data/StyledPlanPartitions.kt")
    parser.add_argument("--audit", type=Path)
    args = parser.parse_args()
    report = generate(args.input, args.kotlin_out)
    if args.audit:
        args.audit.write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
