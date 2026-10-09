# Reusable assets on every floor

9 October 2026. User problem: only ground currently uses independent assets; the other floors still load complete illustrations. Extend the existing map iteration to all five floors without starting the Godot or Community Studio migration.

## Implementation plan

1. Author a distinct structured recipe for each upper floor: material patches, individual atlas furniture and deliberate decorative wall segments. Reuse the existing campus atlas. Keep original company anchors, source polygons and courtyard shapes unchanged.
2. Generalize the ground renderer to accept a floor recipe. All five floors use the same surface, wall/window, stair and sprite rendering. Remove full-floor image loading from Plan World. Historical images remain references only.
3. Preserve the ground bike-room divider, empty unassigned areas, first-floor FARI separation and unplaced Sky entry. Do not infer toilets, real doorways, routes or tenant interiors. Avoid drawing automatic walls around overlapping colored regions.
4. Validate complete furniture bounds against assigned areas, canonical shells, voids and stairs. Review interior wall thickness against furniture and source markers. Keep narrow areas sparse enough to read rather than shrinking assets to fragments.
5. Build and run shared tests; inspect all five floors at Fit and closer zoom on the Android emulator, and complete Campfire AI, 42 Belgium and unplaced Sky searches. Save actual evidence, update affected docs, commit and push the completed slice.

The visual check is whether the independently assembled floors remain recognizable and useful at overview and detail scales. Source company positions are fixed; decorative interiors can differ from the old illustrations. No engine, accounts, publishing, quests or new Studio tools are included.

## Delivery

Completed all five scene recipes through the shared renderer. Original images moved unchanged to documentation; the clean Android APK contains none of them. Source geometry and company anchors are unchanged. Full shared suite: 52 passing tests. All five floors and the three-search walkthrough were visually checked on the dedicated Android emulator; see [evidence](../testing/all-floor-assets-smoke.md). iOS/browser/physical phone were not rechecked.
