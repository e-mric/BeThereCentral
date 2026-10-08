# Lean backlog

These local work items track the roadmap for [e-mric/BeThereCentral](https://github.com/e-mric/BeThereCentral). Immersive exploration is a core product direction alongside navigation.

## Known verification issue

- Investigate the iOS simulator accessibility crash observed while dismissing a checkpoint sheet on 8 October 2026. The stack faults in Compose accessibility properties during a delayed activation callback; root cause is unconfirmed. Reproduce with touch and VoiceOver, then verify a targeted fix before claiming iOS accessibility stability. See [verification status](testing/STATUS.md).

## Next experiment

- Test the focused Explore → Top recovery → separate Map demo journey with 5–8 participants using [experience standards](EXPERIENCE.md). Record unaided recovery, recognition of the separate places, route comprehension and enjoyment; use anonymous observations rather than movement traces.
- Test the sample discovery trail with participants: can they name an area and nearby fictional company after reading a checkpoint card, and do they understand that scan and team progress are simulated? Use anonymous task observations, not movement traces.
- Test real devices with VoiceOver/TalkBack, large text, color-vision differences and reduced dexterity. Confirm map alternatives remain usable.

## Next milestone — Room Studio

Finish and publish the sample app first. Then validate a separate tenant editor with one fictional room before adding a broader publishing workflow.

- Import a tenant-owned logo and preview a pixel-style version.
- Place furniture and decorations inside the assigned room; preserve room IDs, doors, corridors and navigation geometry.
- Export a versioned room decoration manifest using the shared coordinate contract.
- Define tenant permissions, asset licensing, building review and rollback before real contributions are published.

## Before a real building pilot

- Confirm the BeCentral pilot sponsor and approved brand kit; replace website-inspired typography only with fonts/assets licensed for app and open-source redistribution.
- Obtain permission and surveyed plans/room register; validate every entrance and accessible connector. Keep licensed source and graph provenance.
- Add camera QR scanning on Android/iOS with permission denial, malformed-code and lifecycle tests. Manual entry remains a fallback.
- Audit checkpoint placement and tamper risk; define freshness presentation and invalidated map versions.
- Replace fictional discovery descriptions only after a building owner approves accurate area and company information, its reuse rights and a process for updates.
- Add independently reviewed platform UI tests for selection, route fit, floor transitions and scan results.

## Before real sharing or multiplayer

- Implement identity/TLS and audited client-side E2EE/key verification; connect mobile adapters to a reviewed server.
- Test expiry/revoke across offline/background/restart and simultaneous reads; confirm no logs/backups contain positions.
- Build opt-in real team sessions, conflict-safe progress and accessibility-equivalent checkpoint challenges.

## Immersive Explore — prototype follow-up

- Run device-level accessibility checks for the bundled Explore viewer and its Map fallback; confirm app state survives switching scenes.
- Measure Android/iOS loading, first-frame and memory use on named hardware; set budgets from the licensed sample. Study enjoyment and orientation in the sample space.
- Review the web-surface renderer against device measurements before choosing a production rendering path.

## Later building-linked exploration

- Survey and obtain permission for building captures; map room-linked scenes to verified building/version, floor and room IDs and meter-based coordinates.
- Build the separate PlayCanvas/WebXR experience using the same coordinate and identity contract.
- Trial indoor-positioning hardware without relabelling QR observations as live data.
- Evaluate camera transitions, independent visual layers and non-personal room-view links for future clients. Keep any personal position out of shareable-view URLs.

## Navigation improvements informed by evidence

- Run the PRD's 5–8 person navigation study. Record anonymous task outcomes and last-seen comprehension, not traces.
- Use findings to improve maps, transitions and route comprehension. The study does not gate the immersive Explore slice.

## Publishing

- Repository: `e-mric/BeThereCentral`. Configure private vulnerability reporting and a maintainer contact before a real pilot.
- Enable CI and review its first hosted run. Add release signing/distribution only after choosing platform accounts.
