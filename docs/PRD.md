# BeThereCentral — Product Requirements

Version 0.11 · 9 October 2026 · Status: five-floor modular asset work active; game/Studio migration on hold

Godot for gameplay and SvelteKit for browser Studio are the selected technical direction. Product scope remains provisional while residents respond to the shared idea. Incorporate their feedback into this PRD and the [implementation plan](plans/GODOT_SVELTEKIT.md), then wait for the user to explicitly resume implementation. The user separately authorized existing-map work on 9 October: convert all five floors to reusable authored asset recipes. This limited map slice may proceed while migration, quests and character/Studio implementation stay on hold.

## Product iteration: map and reusable assets

People should recognize BeCentral’s shape, enjoy exploring a full pixel-art coworking world, and find a named occupant or numbered place without mistaking fictional interiors for surveyed navigation. Five supplied schematic plans cover ground through fourth floor. Their revision, ownership and current accuracy are unconfirmed. The current delivery priority is an accurate-to-source company directory and readable floor maps assembled from reusable assets across all five floors. This is iterative product development, not a disposable prototype.

The app opens on **Plan World**, a five-floor map and directory experience under active product development. Each floor follows its own approximate source-derived outer footprint and relative proportions. Courtyards stay empty dark voids. On ground, retain the complete building silhouette in muted charcoal (#252B2D) with a subtle outline, while confining fictional artwork to known colored regions and leaving the unassigned grey area unfurnished. Other interior walls, desks and furniture are fictional. Search finds source-listed company names and plan numbers; the legend lists The Sky / 66 without a guessed marker. For numbered entries, marker centers match the badge centers in the supplied plans, including repeated badges. These are source-image coordinates, not surveyed room assignments or measured physical positions. FARI's ground-floor position has region-level support only. No route, arrival time, live position or accessibility claim follows from a marker.

Ask 5–8 consenting people to find Campfire AI on the second floor and 42 Belgium on the third, then find The Sky in the fourth-floor legend and explain why it has no marker. Ask whether they understand that the full ground silhouette remains visible while the unassigned grey area has no furniture, and that source colors do not confirm ground or second-floor occupant assignments. Record anonymous task outcomes and confusion, without movement traces. These are proposed checks, not achieved results.

## Map acceptance rules

- Company identities, floor assignments and relative positions must follow the supplied plans. Keep repeated locations and shared-area occupants faithful to the legend; do not invent positions for entries absent from the drawing. Audit corrections against source images before changing anchors.
- The five-floor number/name/anchor review is recorded in the [company placement audit](assets/COMPANY_PLACEMENT_AUDIT.md). Keep badge centers in original source pixels. The bike-parking icon is in a separate room west of the lobby; preserve its continuous divider and place the marker inside that room. Its source icon is not a numbered directory badge.
- Corridor widths may be visually exaggerated where needed for legibility. Keep source-plan coordinates separate from display geometry, preserve company ordering and meaningful adjacency, and record any deliberate display adjustment. A widened decorative corridor does not establish a surveyed route.
- Toilet positions are pending user input. Do not infer them from decorative art or add toilet markers until those positions are supplied.

## Active slice: plan-derived pixel world

- Show ground, first, second, third and fourth floors independently, preserving identity during pan, zoom and Fit. A floor switch does not imply physical movement.
- Keep each floor's source-derived outer footprint, courtyard voids, source numbers and approximate anchors editable as structured data in the original 2048 × 1448 plan-image coordinate space, with Y increasing downward. The active drawing viewport is approximately X 80–1940, Y 580–1280. Pixels are source-art coordinates, not metres. Coarse traced masks preserve relative shape, not exact surveyed walls or dimensions.
- Build each floor’s decoration from authored source-coordinate recipes using reusable materials, wall/window modules, reserved stair motifs and individually placed furniture from the atlas. Use the shared modular renderer, separate recipes from plan geometry, preserve the ground bike-room divider, keep unassigned grey regions plain, and leave courtyards empty. Original full-floor PNGs are historical references, not active art or fallback. This source data is editable in code, not through an in-app floor editor. Treat all decoration as fictional rather than observed room inventory.
- Search source-confirmed names and plan numbers case-insensitively. A repeated number is one legend place with multiple approximate anchors, not several invented tenants. Keep entries without a defensible anchor in the accessible legend and search results, with no guessed marker.
- Make occupants readable in map and list form. Ground-floor grey and orange source areas and second-floor grey source areas have no confirmed occupant assignment. Keep the ground grey area unfurnished within the visible footprint. First-floor FARI is detached without an asserted connection; fourth-floor number 66 / The Sky remains unplaced.
- Keep the canvas charcoal, with a translucent +/−/Fit overlay, one floor selector sheet, and no persistent PIXEL WORLD chip. Preserve large courtyard voids as empty dark areas. On ground, preserve the full silhouette as muted charcoal with a subtle outline, but do not furnish the unassigned grey source area; keep fictional artwork in known colored regions. Explain in More that outer shapes and courtyards follow supplied plans while interiors are fictional.
- Keep company markers attached to their plan positions as zoom changes. Wrap long names at closer zoom, use leader lines where labels move away from anchors, repeat the same company name at repeated source positions, and omit labels for offscreen anchors. Keep map controls along the top-right side.
- Provide labelled floor picker, Fit, zoom, search, results and legend with keyboard, touch and assistive-technology access. Inspect actual rendered screens on each platform claimed; compilation alone is insufficient.
- Attribute the supplied plans and record their unverified revision. Do not upload them, add a backend or collect location data for this slice.

## Limits

Company marker centers follow badge centers in the supplied image where a badge exists, but the plan itself has unknown revision and survey accuracy. Ground FARI has only a region-level display anchor; The Sky / 66 remains unplaced. Neither kind of marker registers an exact physical room. Source silhouettes, schematic fills and generated walls do not prove entrances, navigable connectivity, accessible paths, measured dimensions or floor elevations. Do not derive routes, step-free guidance, QR checkpoints, room-linked 3D alignment or live tracking from them. This is not emergency guidance. A building owner must verify plan revision, rights, occupant register, entrances and accessibility before an on-site pilot.

## Parked product goals and samples

The broader goal remains: **make an unfamiliar building feel familiar—and enjoyable to explore.** The codebase retains a fictional four-floor sample with graph routes, optional guide animation, simulated QR observations, local consent and same-device discovery. These are internal legacy fixtures and tests while the active home uses the five supplied plans. They do not work against those plans. The disconnected loopback sharing server tests grant expiry and revocation but is not connected to the app; opaque payload storage is not end-to-end encryption.

The bundled licensed Gaussian-splat engine-room viewer and `exploration/dist/` browser demo remain a standalone experiment. The captured room is unrelated to BeCentral and has no verified map transform or room hotspots. Immersive exploration remains a long-term direction; the earlier reception doorway and bottom Map/3D journey are historical prototype behavior, not the current five-plan walkthrough. See [ADR 0006](adr/0006-plan-derived-pixel-world.md).

## Active slice: Room Studio prototype

The one-room editor is an exploratory tool for testing a bounded decorative authoring flow, not a tenant-facing production feature. The browser application in `room-studio/` uses PixiJS and the same JSON fixture packaged by Compose. Its single fictional 320 × 224 chamfered room has immutable geometry; users can edit the company sign and add, move, keyboard-nudge, delete, undo and redo up to 24 furniture objects (desk, plant, sofa, rug). The browser saves and reloads on the same device and imports/exports versioned JSON. The app's **More** menu opens a Compose preview where users can paste/import the JSON or reset the preview; it does not persist changes.

The courtyard garden idea remains optional future art. If a future perspective view is introduced, visible garden scale must follow viewpoint and floor height rather than repeating a same-size graphic on every level. No garden or scale simulation is part of the current room editor. The demo room is not a real tenant room and source labels do not prove exact room assignment. Authentication, AI generation, shared persistence, company account access, review workflow and publishing remain future work. Never imply that tenants can currently edit their actual campus rooms.

## Product direction: open-source community game

The intended product is now an open-source game set in a BeCentral-inspired pixel world, with a useful map/directory as one part of the experience. The current delivered iteration is the map and directory; walking, quests and a resident character creator are not implemented in Plan World. Sea of Stars and HYKE:Northern Light(s) are visual references for detailed environments, expressive characters, lighting and atmosphere, not asset sources or promises of equivalent production scope.

Studio should eventually author **rooms, characters and quests**. A resident/classmate character can have a display name, editable appearance, dialogue portrait, directional idle/walk sprites, a short approved introduction and an optional mission. Start with a manual modular character creator and an animation preview; photo-assisted generation is optional later. A generated portrait alone is not a usable animated sprite sheet. Keep faces, proportions, palette, frame dimensions and foot pivots consistent across directions. Let the represented person review the result; keep source photos out of the public repository and track permission/licensing for distributable character assets separately from the code licence.

Proposed first playable experiment: one room, one controllable avatar, one classmate NPC, a conversation, a short discovery task and a visible reward. Rooms, decorative props and characters should export as versioned content with stable IDs and asset references. Preserve the source-plan footprint separately from fictional game collision, interaction points and quest placement. Tenant missions and personal facts must be supplied or approved by their owners. Multiplayer, AI dialogue and a whole-campus RPG are not required for this first experiment.

Godot is selected for the future game runtime and SvelteKit for browser Studio; [ADR 0008](adr/0008-godot-game-sveltekit-studio.md) records the decision and the Compose-for-web alternative. Start with a standalone game experiment only after the feedback gate opens. Test content import, mobile controls, sprite layering/collision, saves and performance before deciding whether to replace or integrate with the current Compose app. The existing one-room JSON is a starting fixture, not a complete character/quest interchange standard.

The intended learning platform serves both players, through educational quests, and contributors, through readable code, original sample assets, exercises and documentation. The first learning outcome and lesson content remain subject to resident input; AI assistance is optional, not a prerequisite for contributing.

References reviewed 8 October 2026: [Sea of Stars listing](https://play.google.com/store/apps/details?id=com.playdigious.seaofstars), [HYKE listing](https://apps.apple.com/in/app/hyke-northern-light-s/id6479881755), [Godot 2D capabilities](https://docs.godotengine.org/en/stable/tutorials/2d/introduction_to_2d.html).

## Future pilot gates

| Goal | Evidence needed before a claim |
| --- | --- |
| Site directory | Approved plan revision and occupant register, stable identifiers and update owner |
| Routes and step-free guidance | Surveyed entrances, connectors, graph and accessibility review; on-site validation |
| QR last-seen observation | Approved checkpoint locations, versioned payloads, camera integration and freshness display |
| Sharing | Identity, TLS, verified recipient keys, audited client encryption, expiry/revocation integration and consent |
| Room-linked 3D | Permitted capture, provenance and surveyed alignment with shared building/version IDs |
| Room Studio production use | Verified room-to-tenant mapping, tenant rights, asset permissions, access control, review, rollback and published versioning |

No location history is kept by default. Analytics and external location upload require explicit consent. Never call a checkpoint observation live tracking or claim E2EE before authenticated key exchange and encryption are implemented and reviewed.

## Architecture and verification

Kotlin Multiplatform shares pure building/search behavior and Compose Multiplatform presentation across Android, iOS and desktop. Keep plan data and search independent of Compose and transport; put behavior tests in matching feature packages. Preserve thin platform launchers and the checked-in offline sample viewer. Update affected docs, changelog and [verification status](testing/STATUS.md) together. Distinguish browser, emulator and physical device evidence.
