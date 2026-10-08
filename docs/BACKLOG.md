# Lean backlog

This local roadmap tracks [e-mric/BeThereCentral](https://github.com/e-mric/BeThereCentral). GitHub Issues are the intended public tracker; publishing requires user authorization.

## Next experiment: review five supplied plans

- Inspect every source-derived footprint and courtyard mask against its schematic and rendered full-floor artwork. Correct coarse clipping, labels and approximate anchors where needed; review all five distinct scenes. Preserve the complete ground-floor silhouette while leaving the unassigned grey area without furniture; keep ground grey/orange and second grey unassigned in the directory, first-floor FARI detached, and fourth-floor 66 / The Sky unplaced.
- Confirm source revision, rights and occupant names with the building owner before calling the directory current. Record source provenance and a process for later plan updates.
- Test the Campfire AI → 42 Belgium → The Sky walkthrough with 5–8 consenting people. Measure enjoyment of the furnished worlds, whether they find two placed names, explain the unplaced result, and understand that interior contents are fictional; check pan/zoom/Fit comprehension. Keep anonymous notes only.
- Check labelled controls, search/legend alternative, large text and touch targets with actual accessibility tools. Review Android emulator, iOS simulator, browser demo and physical device separately; do not infer one from another.
- Investigate the iOS simulator accessibility crash observed while dismissing a checkpoint sheet on 8 October 2026 before re-enabling that legacy journey. Root cause remains unconfirmed; see [verification status](testing/STATUS.md).

## Next playable experiment: community game and character Studio

- Prototype one playable room with one avatar, one classmate NPC, a conversation, a short discovery quest and a reward. Keep the current map available during evaluation.
- Evaluate Godot as the game runtime using a browser-Studio content export; verify Android/iOS controls, collision, sprite layering, save/load and performance before committing to a migration.
- Extend Studio with a modular character creator: appearance, display name, portrait, directional idle/walk animation preview, introduction and optional quest role. Validate one approved classmate before scaling to the roster.
- Define versioned room/character/quest content, frame dimensions, foot pivots, asset provenance and licences. Keep real-person source photos private; publish only agreed character assets. Photo-assisted generation remains optional future work.
- Use Sea of Stars and HYKE as art-direction references for atmosphere, depth and animation; build an original coherent palette/asset library. Do not equate a large generated floor image with a fully editable game level.

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
