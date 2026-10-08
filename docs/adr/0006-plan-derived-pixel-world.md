# ADR 0006: Plan-derived pixel world as the active home

Status: accepted · 8 October 2026

## Context

Five BeCentral schematic plans, ground through fourth floor, were supplied after the fictional four-floor prototype. The creator wants a rich pixel-art coworking world that follows each plan's outer shape and relative proportions. They explicitly accept fictional interiors. A repeated footprint, invented occupant directory or route graph would misrepresent the source. Plan revision, survey accuracy, accessibility and building approval remain unverified.

## Decision

Make a structured five-floor Plan World the app's active home. Coarsely derive each source plan's outer footprint and courtyard voids in original 2048 × 1448 image pixels, Y down, retaining approximate relative proportions. Create a distinct, richly furnished full-floor pixel illustration for each level, fit it to the source viewport and clip it to those masks. Interior partitions, furniture and characters are fictional. Use pan, zoom, Fit, source-supported search and an accessible occupant/number legend. A compact pixel host may explain a known place but must not invent missions or uses. Editable structured data owns source geometry and directory labels; the generated images own only illustration.

Keep ground grey/orange and second grey source areas without confirmed occupant assignments. The ground grey area remains within the silhouette but unfurnished. Do not infer a connection from the detached first-floor FARI region. Keep fourth-floor number 66 / The Sky searchable and listed but unplaced. A repeated source number is one place with multiple approximate anchors. No anchor registers the fictional room art to a real room, entrance or route endpoint.

For the ground floor, preserve the whole outer building silhouette. Render its unassigned grey source area as muted charcoal (#252B2D) with a subtle outline and no furniture; keep fictional artwork within known colored regions. This retains the plan's footprint without suggesting occupancy.

Park the fictional four-floor routes, QR, sharing, hunt and guide journey as internal legacy fixtures/tests. The licensed engine-room splat and browser viewer remain standalone experiments with no plan alignment. Do not expose a 3D or route handoff implying that the supplied plans support it. This decision supersedes ADR 0005's opening/entry behavior and updates ADRs 0003–0004 for the current experiment; their rationale and historical prototype remain documented.

Courtyard edges receive a renderer-only decorative wall treatment: slate caps, framed window sections and piers, clipped to the building side of the existing void boundary. This replaces the bare mask line without changing source geometry or filling courtyards. These facade details are fictional.

## Consequences

The first study can test visual recognition, enjoyment, search and source uncertainty without inventing physical guidance. Coarse source masks and generated art cannot supply measured dimensions, floor elevations, door connectivity or step-free status. Before a real pilot, confirm plan rights/revision, occupant register and accessibility with the building owner and survey the needed connections. Future Room Studio decorations may attach to reviewed regions without changing source geometry. Keep browser, emulator and physical-device evidence distinct in verification records.

## Amendment

ADR 0007 accepts a local, fictional one-room Studio experiment as a separate authoring prototype. It does not change this decision: the Studio room is not bound to a plan anchor, tenant or real room, and its editable decoration model cannot modify the supplied-plan geometry. Courtyard openings remain empty dark voids in the current experience; a garden and floor-height perspective scaling remain future art options.

## Modular ground-floor amendment — 9 October 2026

Replace only the ground illustration with a structured decorative recipe: reusable wood/tile materials, slate wall/window segments, stair motifs and individually positioned props from the existing campus atlas. Preserve source footprints, courtyard voids, directory anchors and grey silhouette exactly; clip decoration to the assigned source regions and keep stair areas free of furniture. Upper floors retain their illustrations pending review of this first slice. The original ground image remains as a reference. This is map asset work, not game-engine migration or Studio authoring.

The user clarified that this is iterative product development. Company placements must be faithful to the supplied plans, while corridor width may be exaggerated for legibility. Source coordinates remain separate from display adjustments. Toilet positions are pending the user’s later input and must not be guessed.
