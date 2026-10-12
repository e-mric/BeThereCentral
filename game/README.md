# BeThereCentral community room

This Godot 4.7.2 desktop development slice opens the shared fictional learning-room sample with the recognizable Founder and Patch characters. Walk with WASD or the arrow keys, find both clues, then speak with the guide and choose an answer. The activity saves locally on this computer; **Reset progress** clears it. The secret bookshelf opens a small White Construct scene, and the duck has a playful debugging interaction. Press Escape or Return to go back.

Use **Open room…** to load a room file, **Restore sample** to return to the sample, and **Browse props** to see the bundled community collection. Room imports are checked completely before the current room is replaced. The runtime independently validates both the frozen v1 room format and v2 room/play format, including exact fields, UTF-8 size limits, text lengths, trusted catalog metadata, room bounds, collision clearance, and reachable activity points. Imported JSON provides declarative data only: its paths are never opened as scripts or resources.

## Run

Open this folder in Godot 4.7.2, or start it from the repository root:

```sh
godot --path game
godot --path game -- --package=/absolute/path/to/package.json
godot --path game -- --catalog
godot --path game -- --package=../content/community/v2/package-ninon-v2.json
godot --path game -- --package=../content/community/v2/package-morgane-v2.json
godot --path game -- --package=../content/community/v2/package-architect-v2.json
```

The optional package argument loads a room at startup. The catalog argument opens the actual trusted prop gallery. The Ninon, Morgane and The Architect packages use approved names, no invented biographies, and trusted pixel presets. To choose a room while the game is open, use **Open room…**.

Run the independent contract fixtures and Godot project parse/import check with:

```sh
godot --headless --path game --script res://tests/run_contract.gd
godot --headless --path game --editor --quit
```

The trusted catalog artwork, original character-parts definition, and five trusted direction atlases are copied reproducibly from the repository community asset collection with `node game/tools/sync_catalog_assets.mjs`; their provenance and license details are recorded in [`ASSET_LICENSE.md`](ASSET_LICENSE.md). Prop crops use the shared atlas coordinates. Both manual characters and trusted presets render to 64 × 96 pixels, display at 32 × 48 world size, and use nearest-neighbor filtering.
