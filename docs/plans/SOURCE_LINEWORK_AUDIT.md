# Five-floor source linework audit

9 October 2026. The user found that the third-floor Garden tenants appear in one open space. The modular conversion preserved badge positions and silhouettes but omitted most interior source lines. This is a fidelity defect, not permission to invent new partitions.

## Plan before implementation

1. Preserve the five newly supplied schematic PNGs and their hashes as audit references, distinct from generated historical artwork.
2. Audit the full source drawing on every floor: exterior/courtyard boundaries, partitions, visible gaps and stair motifs. Keep badges, labels and the ground bicycle symbol out of wall geometry.
3. Extract editable vector contours at source resolution, retaining holes and gaps. Use the original thin geometry for dense details; do not stretch wall caps across openings. Compare extracted vectors to the source, with explicit missed/extra-pixel diagnostics.
4. Render the source linework independently of approximate color-zone polygons and decorative furniture. Remove conflicting fictional dividers and omit or reposition assets that intersect the source lines; never move a source line to accommodate furniture.
5. Inspect actual rendered maps on all floors, including close views of the third-floor Garden, ground bike room, detached FARI, dense bottom office rows and stairs. Exercise search/guide/Fit in the Android emulator when available. Keep renderer captures distinct from emulator evidence.
6. Record per-floor findings, limitations and tests in the audit and verification status, update product rules, commit and push.

Source fidelity is assessed against the supplied drawings. It does not certify surveyed walls, doors, routes or present-day occupancy. Toilet locations remain pending. Game/Studio migration stays on hold.

## Delivered

All five source drawings are preserved with hashes. Source contours replace the fictional partition screens, retain openings and stair marks, and take priority over furniture and decorative trim. Full-floor Compose renders and source overlays are saved in the [audit report](../assets/SOURCE_LINEWORK_AUDIT.md). Company coordinates are unchanged; no toilet positions were invented. The source-only render comparison passes on every floor within one source pixel.
