# BeThereCentral — Product Requirements

Version 0.15 · 12 October 2026 · Status: Plan World iteration active; community implementation Stage 1 underway

Godot for gameplay and SvelteKit for browser Studio are the selected technical direction. On 12 October 2026, the user explicitly resumed implementation, starting with the Stage 1 fictional room handoff contract and its two consumers. No resident feedback has been collected yet; it can inform later scope, but it is not a gate for this bounded fictional sample. Do not use real tenant facts, resident likenesses or personal introductions without their separate approval. The Compose Plan World remains the current map implementation, and its separately authorized map and place-guide work remains distinct from the Godot game.

## Product iteration: map and reusable assets

People should recognize BeCentral’s shape, enjoy exploring a full pixel-art coworking world, and find a named occupant or numbered place without mistaking fictional interiors for surveyed navigation. Five supplied schematic plans cover ground through fourth floor. Their revision, ownership and current accuracy are unconfirmed. The current delivery priority is an accurate-to-source company directory and readable floor maps assembled from reusable assets across all five floors. This is iterative product development, not a disposable prototype.

The app opens on **Plan World**, a five-floor map and directory experience under active product development. Each floor follows its own approximate source-derived outer footprint and relative proportions. Courtyards stay empty dark voids. On ground, retain the complete building silhouette in muted charcoal (#252B2D) with a subtle outline, while confining fictional artwork to known colored regions and leaving the unassigned grey area unfurnished. Interior lines follow the supplied schematics; desks, furniture and window styling remain fictional. Search finds source-listed company names and plan numbers; the legend lists The Sky / 66 without a guessed marker. For numbered entries, marker centers match the badge centers in the supplied plans, including repeated badges. These are source-image coordinates, not surveyed room assignments or measured physical positions. FARI's ground-floor position has region-level support only. No route, arrival time, live position or accessibility claim follows from a marker.

Ask 5–8 consenting people to find Campfire AI on the second floor and 42 Belgium on the third, then find The Sky in the fourth-floor legend and explain why it has no marker. Ask whether they understand that the full ground silhouette remains visible while the unassigned grey area has no furniture, and that source colors do not confirm ground or second-floor occupant assignments. Record anonymous task outcomes and confusion, without movement traces. These are proposed checks, not achieved results.

## Map acceptance rules

- Company identities, floor assignments and relative positions must follow the supplied plans. Keep repeated locations and shared-area occupants faithful to the legend; do not invent positions for entries absent from the drawing. Audit corrections against source images before changing anchors.
- The five-floor number/name/anchor review is recorded in the [company placement audit](assets/COMPANY_PLACEMENT_AUDIT.md). Keep badge centers in original source pixels. The bike-parking icon is in a separate room west of the lobby; preserve its continuous divider and place the marker inside that room. Its source icon is not a numbered directory badge.
- Corridor widths may be visually exaggerated where needed for legibility. Keep source-plan coordinates separate from display geometry, preserve company ordering and meaningful adjacency, and record any deliberate display adjustment. A widened decorative corridor does not establish a surveyed route.
- Toilet positions are pending user input. Do not infer them from decorative art or add toilet markers until those positions are supplied.

## Active slice: plan-derived pixel world

- Show ground, first, second, third and fourth floors independently, preserving identity during pan, zoom and Fit. A floor switch does not imply physical movement.
- Keep each floor's source-derived outer footprint, courtyard voids, source numbers and approximate anchors editable as structured data in the original 2048 × 1448 plan-image coordinate space, with Y increasing downward. The active drawing viewport is approximately X 80–1940, Y 580–1280. Pixels are source-art coordinates, not metres. Coarse traced masks preserve relative shape, not exact surveyed walls or dimensions.
- Preserve visible source partition lines, small returns, curves, gaps and stair marks as immutable source-coordinate linework, independently from decoration. Store reproducible vectors and original schematic references. A bounded decorative wall-finish mask may expand partition pixels by at most three source pixels for pixel-art readability; derive it from the immutable vectors and retain its separate provenance. Use conservative 7×7 closing only as a forbidden-growth guard, and preserve additions with foreground-8/background-4 topology checks. Decorative widths may narrow a visible gap, but may not create a doorway, route or new connection. Keep stairs as their original fine marks and preserve their positions, gaps and company anchors. Remove conflicting invented dividers and omit furniture whose full bounds intersects the finished wall. Never move source lines to fit props or infer doors from gaps.
- Build each floor’s decoration from authored source-coordinate recipes using reusable materials, wall/window modules, reserved stair motifs and individually placed furniture from the atlas. Use the shared modular renderer, separate recipes from plan geometry, preserve the ground bike-room divider, keep unassigned grey regions plain, and leave courtyards empty. Original full-floor PNGs are historical references, not active art or fallback. This source data is editable in code, not through an in-app floor editor. Treat all decoration as fictional rather than observed room inventory.
- Search source-confirmed names and plan numbers case-insensitively. A repeated number is one legend place with multiple approximate anchors, not several invented tenants. Keep entries without a defensible anchor in the accessible legend and search results, with no guessed marker.
- Selecting a map marker/sign or directory entry opens a compact pixel-character place guide and animates the map to zoom 2.6 above the selected source anchor. A tapped repeated marker selects that exact anchor; search and directory selection use the first known anchor. An unplaced entry stays at Fit with no invented focus point.
- Offer Directions with the known floor and source plan position only; state that entrances and walking routes are unconfirmed. Do not show an origin, path, distance, or accessibility claim. For company, meeting-room 31, and other place profiles, show only source-supported facts and attributed optional content; use clear empty states when mission or purpose is absent. Optional extra information and HTTPS links are structured for future supplied content, but there is no resident editor or publishing flow.
- Make occupants readable in map and list form. Ground-floor grey and orange source areas and second-floor grey source areas have no confirmed occupant assignment. Keep the ground grey area unfurnished within the visible footprint. First-floor FARI is detached without an asserted connection; fourth-floor number 66 / The Sky remains unplaced.
- Keep the canvas charcoal, with a translucent +/−/Fit overlay, one floor selector sheet, and no persistent PIXEL WORLD chip. Preserve large courtyard voids as empty dark areas. On ground, preserve the full silhouette as muted charcoal with a subtle outline, but do not furnish the unassigned grey source area; keep fictional artwork in known colored regions. Explain in More that outer shapes, courtyard openings and interior lines follow supplied plans while furnishings and window styling are fictional.
- Keep company markers attached to their plan positions as zoom changes. Wrap long names at closer zoom, use leader lines where labels move away from anchors, repeat the same company name at repeated source positions, and omit labels for offscreen anchors. Keep map controls along the top-right side.
- Provide labelled floor picker, Fit, zoom, search, results and legend with keyboard, touch and assistive-technology access. Inspect actual rendered screens on each platform claimed; compilation alone is insufficient.
- Attribute the supplied plans and record their unverified revision. Do not upload them, add a backend or collect location data for this slice.

The source-line audit remains a raw comparison with `stylePartitions=false` and its existing one-source-pixel tolerance. Do not rewrite those historical results to match the decorative finish. Evaluate the styled mask, rendered pixels and furniture clearance separately; the mask is illustration, not a replacement source trace or room/door interpretation.

## Limits

Company marker centers follow badge centers in the supplied image where a badge exists, but the plan itself has unknown revision and survey accuracy. Ground FARI has only a region-level display anchor; The Sky / 66 remains unplaced. Neither kind of marker registers an exact physical room. Source silhouettes, schematic fills and generated walls do not prove entrances, navigable connectivity, accessible paths, measured dimensions or floor elevations. Do not derive routes, step-free guidance, QR checkpoints, room-linked 3D alignment or live tracking from them. This is not emergency guidance. A building owner must verify plan revision, rights, occupant register, entrances and accessibility before an on-site pilot.

## Parked product goals and samples

The broader goal remains: **make an unfamiliar building feel familiar—and enjoyable to explore.** The codebase retains a fictional four-floor sample with graph routes, optional guide animation, simulated QR observations, local consent and same-device discovery. These are internal legacy fixtures and tests while the active home uses the five supplied plans. They do not work against those plans. The disconnected loopback sharing server tests grant expiry and revocation but is not connected to the app; opaque payload storage is not end-to-end encryption.

The bundled licensed Gaussian-splat engine-room viewer is now accessible through the mobile Experimental menu; `exploration/dist/` remains separately runnable for browser demonstrations. The captured room is unrelated to BeCentral and has no verified map transform or room hotspots. Immersive exploration remains a long-term direction; the earlier reception doorway and bottom Map/3D journey are historical prototype behavior, not the current five-plan walkthrough. See [ADR 0006](adr/0006-plan-derived-pixel-world.md).

## Active slice: Room Studio prototype

The original one-room editor remains an exploratory tool, not tenant-facing production. The browser application in `room-studio/` uses PixiJS and the JSON fixture shared with its Compose preview. The resumed Community Studio is a separate SvelteKit project that authors `rooms-v1` packages for the standalone Godot desktop preview. Both the old fixture and the new community sample use the same fictional 320 × 224 chamfered room idea, but the Community Studio package has its own validator and import/export boundary. No editor is mapped to a real tenant room.

The courtyard garden idea remains optional future art. If a future perspective view is introduced, visible garden scale must follow viewpoint and floor height rather than repeating a same-size graphic on every level. No garden or scale simulation is part of the current room editor. The demo room is not a real tenant room and source labels do not prove exact room assignment. Authentication, AI generation, shared persistence, company account access, review workflow and publishing remain future work. Never imply that tenants can currently edit their actual campus rooms.

## Product direction: open-source community game

The intended product is now an open-source game set in a BeCentral-inspired pixel world, with a useful map/directory as one part of the experience. The current delivered iteration is the map and directory; walking, quests and a resident character creator are not implemented in Plan World. Sea of Stars and HYKE:Northern Light(s) are visual references for detailed environments, expressive characters, lighting and atmosphere, not asset sources or promises of equivalent production scope.

The active implementation begins with a standalone fictional room handoff: the shared contract lives in `content/community/v1/`, SvelteKit Studio authors a room package, and Godot independently validates and previews it. Pin the initial consumers to SvelteKit 3.0.1 and Godot 4.7.2. This work does not replace Compose Plan World, integrate the unrelated Experimental 3D sample, or implement gameplay, characters, quests, accounts or publishing. See the [Stage 1 plan](plans/COMMUNITY_GAME.md#stage-1--contract-first-room-handoff) and [technical plan](plans/GODOT_SVELTEKIT.md).

Resident feedback remains welcome. Since none has been collected, the default next playable experiment is a fictional open-source contribution task that teaches a small edit/review/export workflow using sample content. It does not depend on a real resident, company project, or tenant story. Feedback can redirect later stages before any real-person or building-specific material is introduced.

Use the [Mobbin references](design/MOBBIN_REFERENCES.md) for original interaction patterns and character/editor inspiration; do not copy their screenshots or third-party art into the project's assets.

Studio should eventually author **rooms, characters and quests**. A resident/classmate character can have a display name, editable appearance, dialogue portrait, directional idle/walk sprites, a short approved introduction and an optional mission. Start with a manual modular character creator and an animation preview; photo-assisted generation is optional later. A generated portrait alone is not a usable animated sprite sheet. Keep faces, proportions, palette, frame dimensions and foot pivots consistent across directions. Let the represented person review the result; keep source photos out of the public repository and track permission/licensing for distributable character assets separately from the code licence.

Godot is selected for the game runtime and SvelteKit for browser Studio; [ADR 0008](adr/0008-godot-game-sveltekit-studio.md) records the decision and the Compose-for-web alternative. Stage 1 is active with SvelteKit 3.0.1, Godot 4.7.2 and the contract in `content/community/v1/`. Test the standalone room handoff first. Mobile controls, sprite layering/collision, saves and performance need later target-specific evidence before any replacement or integration decision. `rooms-v1` is only a room package; it does not yet define characters or quests.

The intended learning platform serves players through educational challenges and contributors through readable code, original sample assets, exercises and documentation. No resident feedback has been collected. The first default challenge teaches a fictional open-source contribution; optional resident input may refine later learning topics. AI assistance is optional and never required to contribute.

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

## Experimental 3D sample — 12 October 2026

The user authorized exposing the existing licensed engine-room viewer in the current mobile app through **More → Experimental → 3D exploration · sample**. Its visible labels distinguish the unrelated Tugboat Bat sample from BeCentral. Keep a native Back to map action available during loading, rendering and errors; returning retains the selected floor and map view. The menu scrolls on short screens. Preserve the offline bundle and its attribution; dispose viewer resources after leaving. Android system Back returns to the map while the viewer is open. Unsupported desktop hosts do not offer an inactive sample action.

This is a demonstration of a possible future capability. Actual building capture/development requires building permission, agreed permitted areas and a privacy review. It remains separate from pixel gameplay and surveyed navigation. The sample menu integration and phone installation are separate from the resumed Stage 1 Godot/SvelteKit work. The [community roadmap](plans/COMMUNITY_GAME.md) records implementation status and planned stages.

## Community v2 development slice — 12 October

The bounded fictional desktop slice adds manual character parts and trusted original Founder/Patch presets and approved Ninon/Morgane/The Architect portrayals, independent Studio/Godot package validation, 24 original props, one two-clue/three-answer learning activity, local progress/reward/reset and a hidden white Construct. Source-floor map data stays independent. A preset appearance is exactly a trusted preset ID; a custom appearance uses exactly the seven documented part/palette fields. Mixed appearances and caller resources are rejected. Preserve the frozen v1 room consumer and explicit lossless migration limits.

Actual acceptance includes browser author/edit/export/reload/rejected-import checks, matching game appearance/placement, movement/collision, clue rereading, retry/completion, single reward, saved progress and secret return. Builds and shared RGBA hashes alone are insufficient. Chrome authoring, save/reload, portrait selection and download→Godot handoff have been exercised; browser file-import interaction remains unverified. Record Godot desktop evidence separately. The user reported public portrayal permission for Ninon/Morgane and requested their own character as The Architect; original photographs remain private and introductions empty. Further portraits, factual project introductions, publishing and mobile game delivery require later scope.

## Android offline Studio — 13 October 2026

For a phone-only BeCentral demonstration, Android includes Community Studio’s static production bundle behind More → Community Studio · offline. Users can author a fictional room, select approved character presets and edit the bounded learning activity without network access or a laptop. Native Back returns to the retained map view; fold/window-size/density changes retain the open editor. Save/Load is explicit phone-local draft storage. Native import/export uses user-selected JSON documents, retaining package validation and the65,536-byte boundary. No publishing/account, tenant-room mapping, external generation or Android Godot gameplay is added. Desktop/iOS do not expose this offline-host menu entry.
