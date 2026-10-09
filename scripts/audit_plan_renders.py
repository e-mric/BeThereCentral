#!/usr/bin/env python3
"""Compare extracted candidate pixels with the actual Compose source-only renders.

Run trace_plan_linework.py and the opt-in FloorRenderAuditTest first. Requires
Pillow, NumPy and OpenCV. A one-source-pixel envelope accommodates Skia edge
antialiasing; this verifies rendering retention, not semantic wall completeness.
"""
import argparse
import json
from pathlib import Path

import cv2
import numpy as np
from PIL import Image
from trace_plan_linework import FLOORS, rasterize_contours


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--vectors', type=Path, default=Path('/tmp/btc-linework'))
    parser.add_argument('--renders', type=Path, default=Path('/tmp/btc-floor-audit'))
    parser.add_argument('--out', type=Path, default=Path('/tmp/btc-floor-audit/render-comparison.json'))
    args = parser.parse_args()
    rows = []
    for floor in FLOORS:
        vectors = json.loads((args.vectors / f'{floor}.json').read_text())
        expected = np.zeros((1448, 2048), dtype=np.uint8)
        for contours in vectors['linework'].values():
            expected |= rasterize_contours(contours, expected.shape, 0, 0)
        image = np.asarray(Image.open(args.renders / f'{floor}-linework.png').convert('RGB'))
        if image.shape[:2] != expected.shape:
            raise ValueError(f'{floor}: source-only render must be 2048x1448')
        observed = (image.min(axis=2) < 250).astype(np.uint8)
        near_observed = cv2.dilate(observed, np.ones((3, 3), dtype=np.uint8))
        near_expected = cv2.dilate((expected > 0).astype(np.uint8), np.ones((3, 3), dtype=np.uint8))
        rows.append(dict(
            floor=floor,
            candidate_pixels=int(np.count_nonzero(expected)),
            rendered_pixels=int(np.count_nonzero(observed)),
            missing_outside_one_pixel=int(np.count_nonzero((expected > 0) & (near_observed == 0))),
            extra_outside_one_pixel=int(np.count_nonzero((observed > 0) & (near_expected == 0))),
        ))
    report = dict(
        target='Actual Compose/Skia source-only desktop render, not emulator or physical device',
        tolerance_source_pixels=1,
        limitation='Candidate retention only; does not prove all source marks are walls, or that every wall was identified.',
        floors=rows,
    )
    args.out.write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(rows, indent=2))
    if any(r['missing_outside_one_pixel'] or r['extra_outside_one_pixel'] for r in rows):
        raise SystemExit('Rendered geometry differs beyond the one-source-pixel envelope')


if __name__ == '__main__':
    main()
