# Working on BeThereCentral

Read `README.md`, `docs/PRD.md`, `GLOSSARY.md` and relevant `docs/adr/` entries first.

For interface work, also read `docs/EXPERIENCE.md`. Review the complete demonstration journey and actual rendered screens. A build or renderer-ready log alone cannot establish visual success. Keep emulator, browser and physical-device evidence distinct.

## Delivery rules

- Work in small, runnable vertical slices. State the user problem and experiment before adding scope.
- Organize by feature; keep pure domain logic independent of Compose, operating systems and transport. Add layers only when they have a purpose.
- Keep tests in the matching feature package under the platform's test source set. Test observable behavior, especially routes, accessibility filtering, checkpoint validation, consent, expiry and revocation.
- Update affected docs and `CHANGELOG.md` in the same change. Record commands actually run and their outcome in `docs/testing/STATUS.md`; never imply an unrun platform passed.
- Always distinguish sample maps, simulated scanning/sharing/co-op, last-seen observations and real integrations. Never label last seen as live tracking.
- No location history by default. No analytics or external location upload without explicit consent. Do not claim E2EE until authenticated key exchange and encryption are implemented and reviewed.
- Do not commit credentials, local SDK paths, signing assets or ordinary build output. The documented exception is the reproducible `exploration/dist/` runtime bundle: mobile builds consume it without requiring Node.js. Commit its source, attribution and regenerated bundle together.

## Physical-device checks

- Do not install or run instrumentation inspection services on the user’s phone; the user has declined them because of battery cost. Use ordinary ADB installation, launch and narrowly scoped crash diagnostics. Let the user verify the visible experience unless they authorize another inspection method.

## Agent workflow

Use Astra (`gpt-6-astra`) for planning and review, Sol (`gpt-6-sol`) for orchestration, and Luna (`gpt-6-luna`) for bounded execution tasks. Delegate with explicit file ownership and acceptance tests. Parent reviews and integrates. If these models are unavailable, disclose the fallback. Keep model names out of application behavior.

## Agent skills

Selected Matt Pocock skills are vendored under `.agents/skills/`; provenance is in `docs/agents/SKILLS.md`. Read a skill before using it. The repository's explicit requirements and user instructions take precedence over skill defaults. Existing authorization to implement features with tests and maintain docs covers routine interfaces, test locations and document edits; avoid redundant approval gates.

### Issue tracker

The repository is `https://github.com/e-mric/BeThereCentral`. Keep `docs/BACKLOG.md` as the working roadmap; GitHub Issues is the intended public tracker. See `docs/agents/issue-tracker.md`. Do not publish issues, comments or messages without user authorization.

### Domain docs

Single context: root `GLOSSARY.md`, decisions in `docs/adr/`. See `docs/agents/domain.md`.
