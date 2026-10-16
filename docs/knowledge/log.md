# Knowledge maintenance log

Append entries below; preserve previous records. Dates identify actual maintenance, not promises of future work.

## 2026-10-16 · ingest

- Read the official best-practices guide and user-supplied LLM Wiki attachment. Added source notes and provenance records, retaining the attachment outside Git.
- Added the index, maintenance schema and dated delivery snapshot. The existing PRD, backlog, glossary, ADRs and test status remain canonical.
- Planning review found stale implementation-deferral and partition/viewer statements in the architecture overview. Corrected them from accepted ADRs and current delivery records.
- Open questions remain plan permissions/revisions, toilet positions, resident input and later community scope. Source ingestion resolves none of these.

## 2026-10-16 · lint and reader exercise

- Offline provenance/link/index validation and optional private-attachment digest validation passed. The checker fixture suite passed 18 tests; see [verification status](../testing/STATUS.md) for actual commands and limits.
- Corrected a nonexistent capture-plan link during lint. Review found and fixed registry symlink containment and a metadata-validation bypass; synthetic regressions cover both.
- Reader exercise found phone Studio scope, canonical requirements, applicable checks and dated evidence through the index. No new resident input or platform verification was inferred.

## 2026-10-16 · project-wide audit

- Compared the best-practices guide with repository instructions, canonical plans, CI and representative code boundaries. Added A1–A8 with priorities, file ownership and observable completion criteria to [the existing plan](../plans/PROJECT_KNOWLEDGE.md); linked it from the working backlog.
- Found scope/status contradictions and obsolete holds beyond the structural checker's remit. Kept those corrections as explicit A1 work, without marking learning or platform evidence complete.
- Confirmed the hosted Verify run for `68476e5` passed all five configured jobs; package/Godot coverage remains missing. Confirmed a valid reversed-character package exposes index-based Studio labels. Source review identified non-recoverable saved-draft loading and unchecked progress-write failures.
- Detailed actual commands, review limits and unresolved evidence are in [STATUS](../testing/STATUS.md). No product fixes, device checks, outreach or schedules were performed for this audit.

## 2026-10-16 · A1 canonical reconciliation

- Reconciled requirements, glossary, backlog and community/technical/workshop plans against existing delivery commits and dated evidence. Marked older game/Studio holds superseded by ADR 0008's resumption.
- Kept v1 frozen, v2 content distinct from local progress, Android authoring distinct from desktop Godot, and browser/emulator/phone evidence separate. Corrected mixed walking-pose wording during review.
- A2 CI coverage is next; native invalid-import, accessibility, final animation and learner outcomes remain open. No app/device result is newly claimed.

## 2026-10-16 · A2 content gate

- After A1 commit `c68c42e`, added the independent v1/v2 package and Godot content gate with full shared-corpus reports, normalized-value comparison and isolated test storage. CI pins the official engine/archive digest.
- Source review and seven negative/semantic regression checks pass; fresh-checkout and hosted evidence will be recorded in STATUS before A2 is marked complete. A3–A8 remain separate tasks.

## 2026-10-16 · A2 verification and handoff

- Parent clean-clone execution passed the full content gate; changing only one Godot normalized value in that clone made the gate fail as expected. Production fixtures and player progress were untouched.
- Pushed A1 `c68c42e` and A2 `bb80231`; the [exact-commit hosted content job](https://github.com/e-mric/BeThereCentral/actions/runs/38902652586/job/116978412553) passed. Updated the plan, backlog and delivery snapshot to close A2 and leave A3–A8 open. This supplies contract/runtime evidence, with no new visual or mobile gameplay claim.
