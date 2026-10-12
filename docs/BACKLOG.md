# Lean backlog

This local roadmap tracks [e-mric/BeThereCentral](https://github.com/e-mric/BeThereCentral). GitHub Issues are the intended public tracker; publishing requires user authorization.

## Community game and Studio implementation

The user explicitly resumed Stage 1 on 12 October, beginning with the fictional `rooms-v1` contract and SvelteKit/Godot consumers. No resident feedback has been collected; it may shape later scope but is not a gate for this bounded work. The [community game plan](plans/COMMUNITY_GAME.md), [technical plan](plans/GODOT_SVELTEKIT.md) and [ADR 0008](adr/0008-godot-game-sveltekit-studio.md) record the active slice and acceptance checks. The existing Compose map, experimental 3D sample and Vite/PixiJS Room Studio remain separate. Do not imply Godot mobile support, real tenant content or a production publishing service.

## Current iteration: five-floor map and reusable assets

- Completed source review: all 64 numbered entries, repeated badge centers and shared-area occupants are recorded in the [company placement audit](assets/COMPANY_PLACEMENT_AUDIT.md), and corrected source centers are in the floor data. The ground-floor bike icon and continuous divider are also traced as a separate west-side room. Keep toilet positions pending user input.
- Review all five rendered maps and authored modular scenes against the source silhouettes, courtyard masks and audited markers. Keep company positions tied to source badges while adjusting decorative assets and readable corridor width; keep source and display geometry separate. Preserve the complete ground silhouette with grey unfurnished, ground orange and second grey unassigned, first-floor FARI detached, and fourth-floor 66 / The Sky unplaced.
- Retain wrapped company labels, marker leader lines, repeated names and offscreen-label filtering across zoom levels. Keep map controls on the top-right side and verify the complete search/legend journey on rendered screens.
- Confirm source revision, rights and occupant names with the building owner before calling the directory current. Record source provenance and a process for later plan updates.
- Test the Campfire AI → 42 Belgium → The Sky walkthrough with 5–8 consenting people. Measure enjoyment of the furnished worlds, whether they find two placed names, explain the unplaced result, and understand that interior contents are fictional; check pan/zoom/Fit comprehension. Keep anonymous notes only.
- Check labelled controls, search/legend alternative, large text and touch targets with actual accessibility tools. Review Android emulator, iOS simulator, browser demo and physical device separately; do not infer one from another.
- Investigate the iOS simulator accessibility crash observed while dismissing a checkpoint sheet on 8 October 2026 before re-enabling that legacy journey. Root cause remains unconfirmed; see [verification status](testing/STATUS.md).

## Next playable experiment: community game and character Studio

The [12 October plan](plans/COMMUNITY_GAME.md) defines dependencies and exit evidence. Stage 1 is active; later stages remain planned.

- **Stage 0 — optional discovery:** record resident needs to inform later scope; no feedback has been collected. It is not a gate for the fictional Stage 1 handoff.
- **Stage 1 — contract-first room handoff:** the v1 shared contract and common fixtures are in `content/community/v1/`; SvelteKit 3.0.1 authors packages, and Godot 4.7.2 previews them on desktop. Finish independent consumer validation, browser/Godot screen review and beginner decorate/export lesson. Preserve Compose/PixiJS and keep the sample fictional.
- **Stage 2 — default fictional open-source contribution task:** if the user gives no different preference, teach a small sample-room/package edit or review with hint, retry, explanation and reward. Keep it independent of resident or tenant content. Assess desktop and mobile target behavior separately.
- **Stage 3 — manual character and quest authoring:** create appearance, display name, portrait, coherent directional idle/walk sprites and an approved introduction; author a supported challenge template and preview it in the game. Provide keyboard/text alternatives and design/pixel-art lessons. Photo-assisted generation remains optional later work with explicit consent.
- **Stage 4 — reviewed community pack:** add a small approved set of rooms, characters, project introductions and challenges after participant evidence. Validate cross-references, updates, progress compatibility, rollback and content withdrawal from future versions. Keep source geometry separate from fictional collision and verify any real room association before claiming it.
- Build coding, design, pixel-art, storytelling and optional AI-assisted lessons alongside the capability they teach. Each has a small visible result, fixture, check and review path; contributors need no AI subscription. Keep private approval records and source photos out of public packages.
- Defer hosted accounts/publishing, multiplayer, AI dialogue and campus-wide expansion to separate evidence-led scope decisions. Use visual references for atmosphere and animation, not as asset sources or promises of production scope.

## Room Studio: next experiment steps

- Use the current one-room fictional prototype to test whether a simple furniture editor and JSON handoff are understandable. Record usability evidence separately for browser and Compose preview; a local editor is not tenant production access.
- Keep the demo room outline immutable. Before using a real place, confirm the approved plan version and exact room-to-company assignment. Source-label anchors alone are insufficient.
- Keep courtyard planting as a design option for later. If a multi-floor perspective is chosen, test apparent scale by level so the garden reads smaller from higher floors; the current courtyard illustration stays empty charcoal.
- Before shared use, define tenant identity and authorization, asset rights, review, versioning, rollback, security and a publishing owner. AI generation and public publishing are not in the current prototype.

## Before a real building pilot

- Confirm sponsor, approved brand kit, source-plan licence, current occupant register and update owner.
- Survey entrances, connectors and accessibility before adding routes or step-free guidance; do not infer connectivity from plan lines.
- Validate any QR location on site, add camera integration and versioned payloads, and keep last-seen wording. No location history by default.
- Add platform UI tests for floor selection, search, Fit, legend and unplaced states after visual checks.
- Prepare permitted and surveyed captures before attempting room-linked 3D.

## Parked prototype follow-up

- Keep the fictional four-floor route/guide/checkpoint/sharing/hunt fixtures and tests accurate but clearly separate from Plan World.
- Run an accessibility and performance study of the standalone licensed splat viewer on named devices before selecting a production renderer. The browser demo remains independent.
- For real sharing, implement identity, TLS, reviewed client encryption and verified keys, then integrate expiry/revocation. For real co-op, add authorized sessions and conflict-safe synchronization.
- Revisit navigation comprehension once a verified route graph exists; no schematic-plan route should be advertised as real guidance.

## Publishing

Enable and review a hosted CI run before claiming it passed. Choose platform accounts before signing/distribution. Keep [verification status](testing/STATUS.md) tied to commands actually run.

### Active community asset/game slice — 12 October

- [x] Exercise browser editing, save/reload and actual export → Godot import handoff for Stage 1.
- [ ] Finish browser file-import UI verification and assistive-technology checks.
- [x] Ship original coworking and Easter egg catalog with provenance and shared crop/scale/collision rules.
- [x] Add v2 independently validated manual characters and two-clue learning quest, preserving v1 room imports.
- [x] Verify desktop movement, retry/reward/local progress, hidden bookshelf → white Construct → safe return.
- [ ] Verify responsive three-pane Studio, categorized thumbnails and numeric/keyboard authoring alternatives.
- [ ] Review fictional content before expansion; collect optional resident feedback and separate portrayal/content approvals.

Beginner path: [first community contribution](learning/FIRST_CONTRIBUTION.md). Accounts, publishing, real room editing and mobile Godot delivery remain separately scoped.
