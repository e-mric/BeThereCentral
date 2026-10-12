# Decorative partition wall finish

12 October 2026. User problem: enlarged interior source marks looked like thin cracks against the pixel-art floor. The improvement is a bounded slate finish around the same source-coordinate marks, with a shaded face and restrained upper-left edge.

`SourcePlanLinework.kt` and the original schematics remain unchanged. `scripts/style_plan_partitions.py` consumes the reproducible extracted masks and generates `StyledPlanPartitions.kt`. Ordinary app builds consume checked-in vectors and need no Python/OpenCV. The decorative recipe is separate from surveyed geometry and future game collision.

## Geometry and paint rules

- Added pixels are at most three source pixels from the original partition ink and inside the diagnostic plan shell. Original partition ink is retained, including tiny marks.
- A 7×7 closing operation is used only to identify forbidden growth near narrow gaps. Its closed output is never exported as wall geometry. Sequential foreground-8/background-4 simple-point additions preserve connected marks and enclosed openings. A precomputed local-neighbour lookup accelerates generation.
- Stair pixels block expansion and remain at their original display width. Isolated small components remain source-sized. Full contour hierarchy and one/two-point contours survive export; ignoring hierarchy would incorrectly fill rooms.
- Diagnostic shell occupancy uses contour-by-contour parity fill because filtered shell metadata can retain stale hierarchy indices. Only the complete source partition/stair hierarchies use the original round-trip reconstruction.
- The renderer clips all cap/face shading inside the skin. No external drop shadow expands the protected silhouette. Per-floor source-space bevel paths are cached and transformed for pan/zoom.
- Final courtyard/perimeter caps cover partition tips at facade junctions, keeping glass clean. This is decorative occlusion of the ends, not a moved source line. Company anchors and the continuous separate bike-room divider remain source-based.

## Evidence

The [mask audit](partition-finish/mask-audit.json) records all five floors: unchanged connected-component and hole counts, zero original ink loss, zero added pixels in stairs/outside shell, maximum added distance three pixels, and exact contour round-trip. Six synthetic tests cover one/two/five-pixel gaps, returns/holes, stairs, specks and contour export. The shared suite checks complete furniture bounds against the skin plus raster-edge clearance.

The raw source-only renderer remains `stylePartitions=false`; its one-source-pixel comparison tolerance has not been widened. [Raw renderer comparison](partition-finish/raw-render-comparison.json) is independent of the colored wall finish. The preceding [source audit](SOURCE_LINEWORK_AUDIT.md) remains a historical account of source restoration.

| Floor | Actual Compose desktop render |
| --- | --- |
| Ground | [Full floor](partition-finish/ground.png) · [Enlarged detail](partition-finish/ground-detail.png) |
| First | [Full floor](partition-finish/first.png) |
| Second | [Full floor](partition-finish/second.png) |
| Third | [Full floor](partition-finish/third.png) |
| Fourth | [Full floor](partition-finish/fourth.png) |

Actual platform commands and results are recorded in [verification status](../testing/STATUS.md). These drawings still have unconfirmed revision and no measured scale. Styling can narrow a displayed gap; it does not assert doors, routes or wall dimensions. Very small schematic marks and raster-stepped curves remain conservative rather than being replaced by guessed straight walls. Outer/courtyard masks remain approximate.

## Reproduce

With local Pillow, NumPy and OpenCV dependencies:

```sh
python scripts/trace_plan_linework.py --out /tmp/btc-linework
python -m unittest scripts.tests.test_style_plan_partitions -v
python scripts/style_plan_partitions.py --input /tmp/btc-linework --audit /tmp/btc-partition-audit.json
BTC_RENDER_AUDIT=1 ./gradlew :composeApp:desktopTest :androidApp:assembleDebug
python scripts/audit_plan_renders.py
```

Review all five colored floors and the zoomed ground region, not just zero-difference metrics.
