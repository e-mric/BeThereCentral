# Lean backlog

This local roadmap tracks [e-mric/BeThereCentral](https://github.com/e-mric/BeThereCentral). GitHub Issues are the intended public tracker; publishing requires user authorization.

## Next experiment: review five supplied plans

- Inspect every source-derived footprint and courtyard mask against its schematic and rendered full-floor artwork. Correct coarse clipping, labels and approximate anchors where needed; review all five distinct scenes. Confirm ground grey/orange and second grey remain unassigned in the directory despite fictional furnishing, first-floor FARI appears detached, and fourth-floor 66 / The Sky remains unplaced.
- Confirm source revision, rights and occupant names with the building owner before calling the directory current. Record source provenance and a process for later plan updates.
- Test the Campfire AI → 42 Belgium → The Sky walkthrough with 5–8 consenting people. Measure enjoyment of the furnished worlds, whether they find two placed names, explain the unplaced result, and understand that interior contents are fictional; check pan/zoom/Fit comprehension. Keep anonymous notes only.
- Check labelled controls, search/legend alternative, large text and touch targets with actual accessibility tools. Review Android emulator, iOS simulator, browser demo and physical device separately; do not infer one from another.
- Investigate the iOS simulator accessibility crash observed while dismissing a checkpoint sheet on 8 October 2026 before re-enabling that legacy journey. Root cause remains unconfirmed; see [verification status](testing/STATUS.md).

## After plan review: Room Studio proposal

Validate one bounded tenant decoration flow against a reviewed plan. Import a tenant-owned logo, preview a pixel treatment and place decorative props within an assigned region. Export a versioned appearance manifest without modifying plan geometry. Define tenant permission, asset rights, review and rollback before publication. This is planned, not implemented.

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
