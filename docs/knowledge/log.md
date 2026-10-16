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
