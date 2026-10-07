# Pixel-world sample assets

Original images were generated with OpenAI's built-in GPT image tool on 7 October 2026 at the creator's request. The creator approved [Concept 01](../design/cowork-concept-approved.png); its warm interiors remain the map design, while BeCentral blue appears in interface controls. This is an interpretation of the references in [experience standards](../EXPERIENCE.md), not an official brand kit or actual floor plan.

## Runtime assets

Both files are in `composeApp/src/commonMain/composeResources/drawable/`, bundled offline through Compose resources. No image-generation service or API key is required at runtime.

| Asset | Contents | SHA-256 |
| --- | --- | --- |
| `founder_atlas.png` | 1254 × 1254 transparent PNG; four fictional founder poses with cream shirt, navy trousers and orange backpack | `a57faca5ffcb706792244b541cd04bff9f21bd3da8aeac4e4df8145ec1cac820` |
| `cowork_floor.png` | 1536 × 1024 opaque PNG; furnished coworking floor without people or footer | `45e0d82aade5e4216f1d775a52a34bdd6f8c01eda49b480a2c54483c2db056fb` |

The floor background was edited with the same built-in image tool to remove all baked-in people, clarify the lift's north approach and widen the passage below the café. The app draws its optional guide independently; decorative people cannot be confused with real location sharing.

Runtime rendering crops the image at `x=24, y=48, width=1488, height=872`. Its illustrative scale is 20 source pixels per metre. Coordinates are `worldX=(imageX-24)/20`, `worldY=(920-imageY)/20`; the floor is 74.4 × 43.6 sample metres, with world Y pointing upward. This scale is not measured. Four sample floors reuse the same footprint. Room data, corridor waypoints and the guide use this registration; raster pixels never establish accessibility or route policy.

Founder frames use source rectangles and nearest-neighbour sampling. The original PNGs are preserved. The earlier furniture atlas is now a design study at `docs/design/campus-props-study.png` (SHA-256 `d904732ecaeaae3602c71b10aa5f27e422b9152e4de9b2024b0c2b3f801c79a3`), and is not shipped in the app.

## Generation and rights

The [prompt record](../design/generation-prompts.md) contains the approved concept and background preparation prompts. The founder specification was an original crisp 16-bit-style four-pose atlas, with a consistent orange backpack, cream shirt, navy hair/trousers, transparent background, no scenery, logos or real-person likeness.

Generation is not deterministic. The checked-in images and checksums are the source of truth; regeneration is an art revision, not a reproducible build step. Recheck image registration and routes whenever the floor artwork changes.

The project distributes these generated sample assets under its [MIT license](../../LICENSE) to the extent applicable. Orbit, Moss, Spark and North are fictional company labels. No BeCentral approval or actual room contents are claimed. The separate Gaussian-splat sample retains its own CC BY 4.0 provenance.
