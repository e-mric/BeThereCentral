# Five-floor source-line audit

9 October 2026. References: the five schematic images supplied by the user, preserved in [source-plans](source-plans/) with a [hash manifest](source-plans/manifest.json). These are distinct from historical generated floor illustrations. Coordinates below refer to the original 2048 × 1448 source pixels.

## Finding and correction

The asset conversion retained the building silhouettes, courtyards and company badge positions but omitted most interior partitions. Its small set of fictional screens did not represent the source room divisions. This affected every floor, not only the third-floor Garden wing.

The active renderer now draws source-derived interior contours independently of coarse decorative region polygons. It retains small returns, curves, visible openings and stair marks, including one-pixel/degenerate details. Arbitrary divider screens are no longer rendered. Furniture whose complete rectangle plus a three-pixel clearance crosses a source line is omitted; selected furnishings are refitted inside the remaining space. The layer draws after decorative trim so trim cannot erase a junction.

## Floor-by-floor inspection

| Floor | Source features checked and restored | Preserved constraints |
| --- | --- | --- |
| Ground | Orange-room returns and curved marks; continuous angled/vertical bike-room divider; lobby curved stair treads; detached FARI divisions and stairs | Bike parking stays in its own west-side room; grey silhouette stays unfurnished; no doorway invented in the continuous bike divider |
| First | Youth Campus subdivisions around the west courtyard; Sunset diagonal corridor and small returns; Angels Hub rooms; Red-zone corridor and repeated rooms; detached FARI partitions | Detached FARI remains separate; repeated badge anchors remain unchanged; visible source gaps stay open |
| Second | Junction Area curved corridor and rooms 32–36; Central Perk lower rooms; Beta Zone diagonal corridor, Campfire AI divisions and lower 24–27 rooms; Field Open Space boundary | Grey upper-left area remains unfurnished; shared/repeated occupants retain source positions; five stair regions retain their visible marks |
| Third | Garden rooms 48–55, especially Microstart, NOX Energy/51, Valkuren and Ecas/Optiniti; Galaxy rooms 44–47; 42 Belgium divisions; Atlantis 40–43 row | West wing no longer appears as a single undivided room; the supplied curved corners and corridor breaks survive; courtyards remain empty |
| Fourth | Proximus west/north and bottom subdivisions; BeAngels boundary; repeated FARI room divisions; right wing rooms 62–65; central grey connector’s transverse divider | The Sky/66 remains unplaced; grey connector stays unfurnished; no connection or room assignment inferred from its lines |

Original plans and all five diagnostic overlays were visually compared. Actual Compose/Skia floor renders were then inspected, including the third-floor west wing and lower office rows, the ground bike/lobby area, and first-floor detached FARI. Full-floor desktop captures are linked below; they are renderer evidence, not phone captures.

| Floor | Original source | Extraction overlay | Actual desktop render |
| --- | --- | --- | --- |
| Ground | [Source](source-plans/ground.png) | [Overlay](linework-audit/ground-overlay.png) | [Render](linework-audit/ground.png) |
| First | [Source](source-plans/first.png) | [Overlay](linework-audit/first-overlay.png) | [Render](linework-audit/first.png) |
| Second | [Source](source-plans/second.png) | [Overlay](linework-audit/second-overlay.png) | [Render](linework-audit/second.png) |
| Third | [Source](source-plans/third.png) | [Overlay](linework-audit/third-overlay.png) | [Render](linework-audit/third.png) |
| Fourth | [Source](source-plans/fourth.png) | [Overlay](linework-audit/fourth-overlay.png) | [Render](linework-audit/fourth.png) |

Overlay colours: magenta = source partition marks, green = marks within the source stair regions, cyan = diagnostic silhouette. The diagnostic silhouette is not used to replace the app’s established approximate masks. Stair masks can split a source motif; this changes its shade, not its coordinates or gaps.

## Extraction and rendering checks

- `scripts/trace_plan_linework.py` detects near-white source pixels within plan-ink support. Morphological closing builds support only; it never closes gaps in exported line pixels. Dense line geometry is not simplified.
- Numbered badge bright components and bounded discs are excluded. Ground information/entrance annotations are excluded; the bike symbol is not interpreted as a wall. Legends and page headings are outside the retained building occupancy.
- Palette and support corrections restored antialiased third-floor lines and the fourth-floor grey connector divider. Stair classification uses named stair regions, avoiding the earlier mistaken classification of whole green/purple tenant zones.
- Exported contours round-trip to their candidate masks with zero missing or added pixels on all five floors. This is a vectorization check, **not a claim that every source pixel is a wall**.
- `FloorRenderAuditTest` produces actual Compose/Skia source-only renders at source resolution. `scripts/audit_plan_renders.py` compares these with the exported candidates. All five retain the candidate marks with **zero missing or extra pixels outside a one-source-pixel envelope**, allowing raster-edge antialiasing. [Measured results](linework-audit/render-comparison.json).
- Regression tests protect line crossings, holes, real gaps, isolated pixels, stair collisions, selected Garden/grey-connector source landmarks and removal of badge artifacts. Every displayed prop must clear source linework.

## Practical limits

The supplied drawings are schematics with unconfirmed revision and no measured scale. The audit supports source-drawing fidelity, not surveyed wall thickness, door semantics or a route graph. Interior marks include architectural symbols as drawn; the app does not infer their meaning. The outer and courtyard masks remain approximate, and their window/cap styling remains fictional. Minor antialiasing speckle can remain at those trim edges. Fine lines require zoom on a phone; the overview is not a substitute for a detailed floor view.

Company coordinates are unchanged. Toilet locations remain pending. No game/Studio migration, route generation or new tenant facts were added.

## Reproduce

With Python dependencies `Pillow`, `numpy`, `opencv-python-headless` installed in a local environment:

```sh
python scripts/trace_plan_linework.py --out /tmp/btc-linework --kotlin-out composeApp/src/commonMain/kotlin/com/betherecentral/features/building/data/SourcePlanLinework.kt
BTC_RENDER_AUDIT=1 ./gradlew :composeApp:desktopTest :androidApp:assembleDebug
python scripts/audit_plan_renders.py
```

The first command regenerates vectors and source overlays. The opt-in desktop test writes full-floor and source-only captures under `/tmp/btc-floor-audit`. Review the pictures as well as the comparison results. See [verification status](../testing/STATUS.md) for actual platform checks.
