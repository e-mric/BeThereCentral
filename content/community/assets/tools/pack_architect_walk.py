#!/usr/bin/env python3
"""Rebuild the derived Architect atlas from pinned originals (Pillow required).

This only extracts/places exact RGBA pixels. No recolouring, background removal,
per-pose fitting, or generated intermediate poses are performed.
"""
import hashlib
import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RECIPE = ROOT / "architect-walk-recipe-v1.json"


def digest(data):
    return hashlib.sha256(data).hexdigest()


def source(entry):
    path = ROOT / entry["image"]
    assert digest(path.read_bytes()) == entry["sha256"], path
    return Image.open(path).convert("RGBA")


def sample(image, frame, scale, mirror=False):
    import math
    output = Image.new("RGBA", (64, 96))
    cx, cy, width, height = frame["crop"]
    px, py = frame["sourcePivot"]
    for y in range(96):
        sy = math.floor((y - 88) / scale + py)
        for x in range(64):
            sx = math.floor((x - 32) / scale + px)
            if mirror:
                sx = int(2 * px - sx)
            if 0 <= sx < width and 0 <= sy < height:
                output.putpixel((x, y), image.getpixel((cx + sx, cy + sy)))
    return output


def build():
    recipe = json.loads(RECIPE.read_text())
    legacy = recipe["legacy"]
    original = source(legacy)
    catalog_path = ROOT / "catalog-v2.json"
    catalog = json.loads(catalog_path.read_text())
    preset = catalog["avatarPresets"]["architect-v1"]
    atlas = Image.new("RGBA", (9 * 64, 4 * 96))
    frames, hashes = {}, {}
    for row, direction in enumerate(["front", "back", "left", "right"]):
        frames[direction], hashes[direction] = {}, {}
        idle = sample(original, legacy["frames"][direction]["idle"], legacy["sourceScale"])
        assert digest(idle.tobytes()) == recipe["idleHashes"][direction]
        cells = [idle]
        walking = recipe["walking"].get(direction)
        if walking:
            image = source(walking)
            assert image.size == (512, 256)
            assert image.getchannel("A").getextrema() == (0, 255)
            for index in range(8):
                frame = {"crop": [(index % 4) * 128, (index // 4) * 128, 128, 128], "sourcePivot": walking["sourcePivot"]}
                cx, cy, width, height = frame["crop"]
                bounds = image.crop((cx, cy, cx + width, cy + height)).getbbox()
                px, py = frame["sourcePivot"]
                left = px - (31 if walking.get("mirror", False) else 32)
                top = py - 88
                assert bounds and left <= bounds[0] and top <= bounds[1] and bounds[2] <= left + 64 and bounds[3] <= top + 96, "Walking pixels would be clipped"
                cells.append(sample(image, frame, 1, walking.get("mirror", False)))
        else:
            for index in range(1, 5):
                cells.append(sample(original, legacy["frames"][direction][f"walk{index}"], legacy["sourceScale"]))
        for column, cell in enumerate(cells):
            name = "idle" if column == 0 else f"walk{column}"
            atlas.paste(cell, (column * 64, row * 96))
            frames[direction][name] = {"crop": [column * 64, row * 96, 64, 96], "sourcePivot": [32, 88]}
            hashes[direction][name] = digest(cell.tobytes())
    output_path = ROOT / preset["image"]
    atlas.save(output_path, compress_level=9)
    preset.update(sha256=digest(output_path.read_bytes()), sourceScale=1, frameDurationMs=80, frames=frames,
                  attribution="Original approved Architect portrayal; RetroDiffusion front/left walking, mirrored right and preserved original back/idle; deterministic RGBA packing. MIT License.")
    catalog_path.write_text(json.dumps(catalog, indent=2, ensure_ascii=False) + "\n")
    conformance = {"presetId": "architect-v1", "format": "row-major RGBA8, floor sampling; SHA-256 of64*96*4bytes", "dimensions": [64, 96], "hashes": hashes}
    (ROOT / "architect-conformance-v1.json").write_text(json.dumps(conformance, indent=2) + "\n")
    print(f"Packed {sum(len(value) for value in frames.values())} frames; original idle hashes preserved.")


if __name__ == "__main__":
    build()
