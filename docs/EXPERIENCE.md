# Experience standards

## Point of view

**Make an unfamiliar building feel familiar—and enjoyable to explore.** The current product iteration is a rich pixel-art coworking world shaped by five supplied BeCentral schematic plans, starting with reusable ground-floor assets. Each floor should invite exploration while clearly separating source-supported occupants and outer shapes from fictional furnished interiors. Plan revision and survey accuracy remain unverified.

## Current demonstration journey

| Moment | Intended experience | Observable acceptance |
| --- | --- | --- |
| Open | Recognize an explorable ground-floor world | Full building silhouette remains visible in muted charcoal with a subtle outline; fictional art stays in known colored regions, unassigned grey has no furniture, and courtyard openings remain empty dark voids |
| Choose a floor | Explore five visually distinct floors | Ground through fourth each has its own illustration and approximate outline; one floor selector sheet keeps the canvas uncluttered |
| Explore | Read the space at different scales | Pan, zoom and Fit work; labels and source numbers remain understandable |
| Search | Find a known company or plan number | Case-insensitive results show floor and known source label; selecting a placed result reveals an approximate anchor |
| Read a place | Learn only what the plan supports | Compact pixel host states the known occupant/number without invented mission, room use or route |
| Check uncertainty | Understand an unresolved source area or name | The ground grey area remains visible but unfurnished; ground grey/orange and second grey source areas have no confirmed occupant assignment; FARI is detached; The Sky / fourth-floor 66 is searchable and listed without a guessed marker |
| Use an alternative | Access the same information without reading tiny pixels | Search/legend and floor controls expose readable text and assistive labels |

A fitting short walkthrough: search **Campfire AI** on the second floor, search **42 Belgium** on the third, then find **The Sky** on the fourth. The last entry must explain that number 66 is listed but not placed. Do not say that any anchor is an entrance or that the map calculates a route.

## Appearance and content

Keep a charcoal canvas, restrained BeCentral-inspired blue/orange/peach accents and clear text contrast. The full product name is **BeThereCentral**. The palette takes inspiration from public website styles; no approved brand kit or official endorsement has been established. The ground floor uses reusable surface, wall/window and furniture assets; four generated upper-floor illustrations remain. All are clipped to approximate source-derived outlines. Ground furniture should form deliberate groups, retain consistent scale and stay clear of stair motifs and grey areas, with no clipped sprite fragments. Place company markers at their supplied-plan badge centers and preserve repeated badges; ground FARI has a region-level display anchor only. Keep names linked to their markers at every zoom: wrap long labels when needed, use leader lines when a label moves away from its marker, repeat company names at repeated positions, and hide labels for offscreen markers. Keep map controls at the top right. The bike-parking marker belongs west of the lobby divider, inside its separate room; keep the supplied divider continuous without an invented doorway gap. Leave courtyard voids empty dark. Frame their building-facing edges with thick slate pixel walls, bevelled caps, framed blue-grey window bands and corner piers. Scale this decoration with the map and keep it outside the courtyard opening; the windows are fictional facade detail. On ground, retain the full silhouette in muted charcoal (#252B2D) with a subtle outline; confine furnished art to known colored regions and leave the unassigned grey source area without furniture. Interior partitions and furniture are fictional; they cannot define doors, accessible routes, real room inventory or tenant facts. Marker centers match plan image coordinates but do not establish surveyed room assignments. Use the legend for discoverability.

More explains what came from supplied plans and what was imagined. Keep the map controls translucent and compact so the full horizontal canvas remains visible. The floor name opens one selector sheet; +, − and Fit remain directly available. Use a white label on the blue action button for contrast.

## Room Studio prototype journey

Open Room Studio from More. The browser experiment presents one fictional chamfered room, with immutable outline and editable company sign/furniture. Drag or nudge props, add or delete them, and use undo/redo. Save writes to that browser's local storage; export/import moves a versioned JSON scene between devices. Compose offers the matching preview through More: paste/import JSON or reset to the shared fixture. This native preview is transient and does not save or publish edits. Keep the fictional-room notice visible. Do not call the fixture a mapped company room or claim that a tenant has an account, AI generation, or publishing access.

The user's garden suggestion is a possible future courtyard treatment. The current courtyards stay empty and dark; any later multi-floor perspective should render a garden smaller from higher floors, with size tied to apparent perspective. Do not imply that this illustration is an observed BeCentral courtyard.

The supplied plans use a 2048 × 1448 pixel coordinate space with Y down and an approximate viewport of X 80–1940, Y 580–1280. The generated full-floor illustrations are display assets clipped into that source space. These are drawing coordinates, not measured dimensions. A repeated source number belongs to one legend place even when the drawing shows several anchors. Source revision is unknown, so avoid “current tenant” and “verified location” language.

## What counts as verified

A build proves compilation and packaging; it does not prove readable pixels or usable controls. Inspect the actual opening and every floor on each claimed target, including the three searches above, an unplaced result, Fit/zoom and the accessible list. Record the exact browser, emulator or physical device in [verification status](testing/STATUS.md). Do not describe an emulator result as a phone pass. Screenshot review does not establish VoiceOver or TalkBack usability.

The user's phone must not receive instrumentation inspection services. Use ordinary ADB installation, launch and narrow crash diagnostics; let the user verify visible phone experience unless they authorize another method.

## Historical sample journey

The former fictional four-floor prototype opened a repeated coworking footprint with a reception **3D SAMPLE** doorway to a separately licensed engine-room capture. It offered sample routes, a pixel guide, simulated QR checkpoints, local sharing and a same-device hunt. That interaction remains in legacy fixtures/tests and historical screenshots; it is not the Plan World demonstration. The engine-room viewer remains separately runnable in a browser and is unrelated to BeCentral. See [ADR 0005](adr/0005-reception-sample-doorway.md) for its historical design and [ADR 0006](adr/0006-plan-derived-pixel-world.md) for the active change.

Future surveyed navigation and room-linked captures require building permission, verified geometry and accessibility review. No schematic map should be used for emergency guidance, live position or step-free directions.

## Small study

With 5–8 consenting participants, ask for Campfire AI, 42 Belgium and The Sky without coaching. Observe which labels and controls they use, whether they understand the unplaced state and whether the world makes the building more approachable. Collect anonymous task notes, no movement history. Review accessibility with users and assistive technology before claiming it works.

## Interface references

[Material 3 Expressive](https://m3.material.io/get-started) informs compact controls; [Android inset guidance](https://developer.android.com/develop/ui/compose/system/insets-ui) informs edge-to-edge layout. Platform behavior must be checked on each host.

## Map-first product iteration — 9 October 2026

All 64 numbered entries were checked against the supplied plan badges; the [placement audit](assets/COMPANY_PLACEMENT_AUDIT.md) records the source centers, repeated positions and shared-area occupants. Company names, floors and relative placements follow the supplied plans; decorative assets adapt around them. Corridor widths may be exaggerated for readability, with source coordinates retained separately from display geometry. Do not move a company to fit the artwork or infer toilet positions; the user will supply those later. Keep The Sky / 66 visible in search and the legend without a marker.
