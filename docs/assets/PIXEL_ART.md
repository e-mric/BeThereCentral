# Current plan-derived pixel world

The creator supplied five BeCentral schematic plans on 8 October 2026, then explicitly approved fictional interiors in the detailed coworking pixel-art style. The current app uses a modular ground-floor scene and four distinct, fully furnished GPT upper-floor illustrations. The original ground illustration remains a reference asset. Source-derived outer footprints and courtyard masks preserve approximate relative plan proportions; internal rooms, partitions, furniture and decorative characters are fictional. These images are not a record of the building's actual furnishings or navigable entrances. See [ADR 0006](../adr/0006-plan-derived-pixel-world.md).

## Modular ground assets — 9 October 2026

`GroundFloorScene.kt` is an explicitly authored decorative recipe in source pixels. `FloorScene.kt` defines the reusable material/prop references; `ModularFloorRenderer.kt` renders wood/tile patterns, wall/window modules and individual atlas sprites. Ground no longer renders `plan_ground.png`. The existing `campus_props.png` is reused unchanged, with nearest-neighbour sampling and separate placements; no new image generation or external asset service is used in this slice. Its checksum and origin remain below.

Scene data is separate from source geometry and directory facts. Paint is clipped to the canonical shell minus courtyards and to assigned colored regions; stairs are reserved, and the grey silhouette stays unfurnished. Materials and furniture are fictional decorative choices. This is part of the active map and directory product iteration; it does not change company badge positions or supply a portable Godot content package.

## Retained full-floor artwork

The five original PNG outputs are saved unchanged in `composeApp/src/commonMain/composeResources/drawable/`. All are 2048×768 RGBA. The ground image is retained as a reference; only the first through fourth-floor images currently render as full-floor art. No image-generation service is needed at runtime. Upper-floor images register to source plan X 80–1940, Y 580–1280 and clip to independently traced footprint and courtyard masks. Generated alpha includes soft background shading; the canonical clipping masks keep courtyard shapes clear. Interior walls and props are fictional rather than authoritative geometry. Company labels and source badge centers render from structured data; generated artwork has no company text baked into it.

| Asset | SHA-256 |
| --- | --- |
| `plan_ground.png` | `346211bc1cf926a074f0d746d28ca42247568608ba3d8b2219df406624089311` |
| `plan_first.png` | `a32f23a35162775d0978be148eb2a89b600a232c4dc96d968a516b48955b0e28` |
| `plan_second.png` | `72d0f75db437417459c4e0b385fed9140ed8e25d13e0bf20841e24e38a3f163a` |
| `plan_third.png` | `2b01c32e6616c7701e95218a149e58f1c5e34dd146d21cd958f425bf2d63344f` |
| `plan_fourth.png` | `a24ded84b8d296945014cd74a4934a6aa9ebd0711dfbc23f225f796b81e5a8f2` |

The [generation prompts](../design/plan-world-generation-prompts.md) identify the floor-specific stencil and approved coworking style references. Stencils are layout guides created from vectors, not edits of the supplied source bitmap. Generation is nondeterministic: the checked-in output and its checksum are the source of truth. Future art revisions must preserve registration or explicitly update it and review every floor and anchor.

## Plan structure and directory

`BeCentralPlans.kt` owns transcribed numbers, known names/occupants, plan badge centers, region fills and source wall hints. All 64 numbered entries, repeated anchors and shared-area names were checked against the source in the [company placement audit](COMPANY_PLACEMENT_AUDIT.md). Ground bike parking is a separate unnumbered source icon in the west-side lobby room; its continuous divider is traced independently. `TracedPlanGeometry.kt` owns coarse exterior and courtyard vectors extracted from the supplied images on a six-source-pixel grid, simplified within eight source pixels. `scripts/trace_plan_geometry.py` reads five local source files and emits Kotlin vectors only; it never edits or embeds a bitmap. Regeneration needs Pillow and NumPy. Ordinary app builds consume the checked-in vectors and need neither. Plan coordinates are source pixels, Y down, with no measured scale or elevations; any corridor display widening remains separate from these positions.

Repeated source badges share one directory entry with several anchors; each source position is rendered with its company name. Long labels wrap or use a leader line as zoom changes, and offscreen labels are omitted. Unplaced number 66 / The Sky remains searchable and visible in the legend without a guessed marker. Ground and second-floor grey and ground orange areas have no confirmed occupant assignment. The guide sheet lists all supplied occupants for shared areas and explains that the interior design is illustrative. Decorative figures are fictional and cannot represent real location sharing. Toilet positions remain pending user input.

## Source input identity

All input images are 2048×1448, supplied in this chat on 8 October 2026; revision and survey accuracy are unknown. Source rasters have not been copied into the public repository. Their original rights and official marks are not granted the repository's MIT license by this work.

| Source level | SHA-256 |
| --- | --- |
| ground | `e650986b84242ffde65df0158ac2aaf98b2075161c2d6e66d66465f33e4677c1` |
| first | `cdd8d4b30a5e90a30e5ac42b7137e4e31d24283becaa4a48003e4539743d3416` |
| second | `41d1520f58b9ff8cdb26bf4d5e969ec5b84d9762f64c22d61732d576278d340b` |
| third | `5682e43e25bccfe3ef9b183f836d0cff0245b6aa43934221b8d776e05f313290` |
| fourth | `42804ffb11f27e756d4b5801a6b73570fd1055ab3ef6e1e7f13cadbde390280b` |

Receiving references and permission to develop the map do not establish building-team approval of a real visitor service. Review plan revision, directory facts and asset distribution before a real campus pilot. Generated illustrations are offered under the project's asset terms to the extent applicable, without relicensing third-party plans, names or marks.

## Retained props study

`campus_props.png` is an original 1254×1254 transparent 4×4 atlas generated in the same style. SHA-256: `4aad722c140dab17cfffd79d9ce80a7ea9178ab2c271fb0dcce9a56686e6a138`. It was first used in an intermediate procedural-world study and is now reused for independently placed ground-floor furniture. Upper floors still use their cohesive full-floor illustrations. Its prompt is retained in [generation prompts](../design/generation-prompts.md). The historical founder atlas remains the fictional guide-sheet character.

## Historical fictional sample assets


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
