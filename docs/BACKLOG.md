# Lean backlog

This local roadmap tracks [e-mric/BeThereCentral](https://github.com/e-mric/BeThereCentral). GitHub Issues are the intended public tracker; publishing requires user authorization.

## Best-practices audit follow-up — 16 October 2026

The [project-wide audit and prioritized plan](plans/PROJECT_KNOWLEDGE.md#project-wide-best-practices-audit--16-october-2026) records evidence, ownership and completion criteria for A1–A8. Start with canonical scope/status reconciliation and missing package/Godot CI coverage, then ID-based Studio roles and durable progress-write feedback. Later slices cover recoverable draft loading, specific interaction/accessibility checks, clean-checkout setup and learner outcomes. A1 documentation reconciliation and A2 package/Godot CI coverage are complete. A3 (ID-based Studio roles) is next; A3–A8 remain open; already evidenced browser import is not unfinished.

The original audit [Verify run](https://github.com/e-mric/BeThereCentral/actions/runs/38886767636) passed its five jobs for `68476e5` before Godot/package coverage existed. A2 adds a sixth job: the [content gate](https://github.com/e-mric/BeThereCentral/actions/runs/38902652586/job/116978412553) passed for `bb80231`, checking independent v1/v2 acceptance and normalized content, asset synchronization and Godot import/runtime. These checks do not prove visible UI behavior. Full dated evidence and remaining platform checks are in [STATUS](testing/STATUS.md#a2-independent-content-ci-gate--16-october-2026).

## Community game and Studio implementation

The frozen fictional `rooms-v1` handoff remains supported. Separate v2 room, character and single-activity authoring is implemented in SvelteKit Studio and consumed by standalone Godot desktop gameplay. Android hosts Studio for offline authoring; it does not run Godot. The 13 October canvas-first workshop is delivered. Browser valid/rejected file-import interaction is evidenced. No resident or learner study has been conducted. Native Android invalid-file import, accessibility review and the final all-four-directions animation walkthrough remain open; dated evidence and the plans are linked from the [community roadmap](plans/COMMUNITY_GAME.md), [technical plan](plans/GODOT_SVELTEKIT.md), [workshop delivery](plans/STUDIO_WORKSHOP.md) and [ADR 0008](adr/0008-godot-game-sveltekit-studio.md). Compose map, Experimental 3D sample and Vite/PixiJS Room Studio remain separate. Do not imply Godot mobile support, real tenant content or a publishing service.

## Current iteration: five-floor map and reusable assets

- Completed source review: all 64 numbered entries, repeated badge centers and shared-area occupants are recorded in the [company placement audit](assets/COMPANY_PLACEMENT_AUDIT.md), and corrected source centers are in the floor data. The ground-floor bike icon and continuous divider are also traced as a separate west-side room. Keep toilet positions pending user input.
- Review all five rendered maps and authored modular scenes against the source silhouettes, courtyard masks and audited markers. Keep company positions tied to source badges while adjusting decorative assets and readable corridor width; keep source and display geometry separate. Preserve the complete ground silhouette with grey unfurnished, ground orange and second grey unassigned, first-floor FARI detached, and fourth-floor 66 / The Sky unplaced.
- Retain wrapped company labels, marker leader lines, repeated names and offscreen-label filtering across zoom levels. Keep map controls on the top-right side and verify the complete search/legend journey on rendered screens.
- Confirm source revision, rights and occupant names with the building owner before calling the directory current. Record source provenance and a process for later plan updates.
- Test the Campfire AI → 42 Belgium → The Sky walkthrough with 5–8 consenting people. Measure enjoyment of the furnished worlds, whether they find two placed names, explain the unplaced result, and understand that interior contents are fictional; check pan/zoom/Fit comprehension. Keep anonymous notes only.
- Check labelled controls, search/legend alternative, large text and touch targets with actual accessibility tools. Review Android emulator, iOS simulator, browser demo and physical device separately; do not infer one from another.
- Investigate the iOS simulator accessibility crash observed while dismissing a checkpoint sheet on 8 October 2026 before re-enabling that legacy journey. Root cause remains unconfirmed; see [verification status](testing/STATUS.md).

## Community game: implemented capability and remaining work

The [community plan](plans/COMMUNITY_GAME.md) defines the original stage rationale and current evidence boundary. Capabilities can span the plan's stages; no entire learner-facing stage is complete merely because its code exists.

- **Stage 0 — optional discovery:** record resident needs to inform later scope; no feedback has been collected. It is not a gate for the fictional Stage 1 handoff.
- **Stage 1 — v1 contract-first handoff:** the contract is frozen in `content/community/v1/` and retained by both consumers. A2 supplies clean-checkout CI coverage of package and Godot checks; its independent consumers agree on the pinned corpus. Browser/Godot visual evidence remains recorded by target.
- **Delivered v2 capability:** room editing, manual and trusted-preset characters, 24 props, one activity, desktop gameplay and local progress are implemented. The 13 October workshop is delivered; Android hosts Studio offline. Native rejected-file import and accessibility evidence remain open, and Godot gameplay is not mobile.
- **Learning evidence:** Contribution Club is implemented as one fictional activity. A voluntary learner walkthrough to see whether a novice can explain and complete the intended contribution remains future work; no resident/learner study has been conducted.
- **Future reviewed community pack:** add approved rooms, characters, project introductions and challenges only after participant evidence. Validate cross-references, updates, progress compatibility, rollback and withdrawal from future versions. Keep source geometry separate from fictional collision and verify any real room association before claiming it.
- Build coding, design, pixel-art, storytelling and optional AI-assisted lessons alongside the capability they teach. Each has a small visible result, fixture, check and review path; contributors need no AI subscription. Keep private approval records and source photos out of public packages.
- Defer hosted accounts/publishing, multiplayer, AI dialogue and campus-wide expansion to separate evidence-led scope decisions. Use visual references for atmosphere and animation, not as asset sources or promises of production scope.

## Original Room Studio prototype

- Keep the Vite/PixiJS and Compose prototype distinct from Community Studio. Its local-room behavior is not tenant production access; the later SvelteKit workshop delivery is documented in [STUDIO_WORKSHOP.md](plans/STUDIO_WORKSHOP.md).
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

### Delivered community asset/game and workshop capabilities — 13 October

- [x] Exercise browser editing, save/reload and actual export → Godot import handoff for Stage 1.
- [x] Exercise checked and rejected browser file imports in the workshop; invalid native Android import and assistive-technology checks remain open.
- [x] Ship original coworking and Easter egg catalog with provenance and shared crop/scale/collision rules.
- [x] Add v2 independently validated manual characters and two-clue learning quest, preserving v1 room imports.
- [x] Verify desktop movement, retry/reward/local progress, hidden bookshelf → white Construct → safe return.
- [x] Verify the canvas-first Studio workshop, categorized thumbnails and numeric/text authoring alternatives on browser and Android emulator; keep physical phone confirmation separate.
- [ ] Complete the final visual Godot walkthrough for all four movement directions.
- [ ] Review fictional content before expansion; collect optional resident feedback and separate portrayal/content approvals.

Beginner path: [first community contribution](learning/FIRST_CONTRIBUTION.md). Accounts, publishing, real room editing and mobile Godot delivery remain separately scoped.
