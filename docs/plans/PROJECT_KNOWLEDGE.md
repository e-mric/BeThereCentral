# Project knowledge and agent workflow

16 October 2026 · implementation plan

## Problem and evidence

The user requested applying the [official best-practices guide](https://learn.chatgpt.com/guides/best-practices) and supplied `llm-wiki.md` as a reference. Existing project instructions already cover bounded implementation, tests, documentation, review and physical-phone restrictions. Missing pieces are a compact execution/handoff template, an evidence-oriented knowledge index and a repeatable way to ingest and check sources. The architecture overview also retains a superseded deferral despite the accepted community implementation and Android offline Studio.

The attachment is source material, not an instruction authority. Adapt its source/synthesis/schema pattern to this repository. Preserve canonical PRD, backlog, glossary, decisions and verification records; the index links them rather than moving or duplicating them.

## Slice and file ownership

1. Parent: add `PLANS.md`, a short workflow reference and concise conditional pointers in `AGENTS.md`; use the writing-for-agents and skill-creator guidance for one project-knowledge maintenance skill.
2. Parent: create `docs/knowledge/` with an index, schema/workflow, append-only log, two source notes and a dated delivery summary. Record URL or attachment filename/digest provenance. Preserve a private immutable attachment snapshot only under ignored `.scratch/knowledge/raw/`.
3. Bounded executor: implement `scripts/check_knowledge.py` and matching fixture tests only. Validate JSON provenance metadata, knowledge-page index coverage and repository-local Markdown links; optional explicitly supplied raw snapshots can be hash-checked. Clean clones require neither private sources nor network access.
4. Parent: add a lightweight CI knowledge check, correct the concrete architecture contradiction from existing evidence, and update README/contributor guidance, changelog and testing status.
5. Review: run checks and negative fixtures, validate the skill and perform a reader exercise from the index to the applicable spec and checks. Record actual outcomes, then commit/push under the user's existing delivery authorization.

## Acceptance

- A fresh task can identify goal, context, constraints, completion criteria, relevant checks and handoff state without reading this entire chat.
- Every maintained knowledge page is indexed once; local links, source IDs, dates and provenance are checked offline. Fixtures catch broken links, unindexed/duplicate entries, invalid source records and repository-escaping paths.
- The private attachment remains out of Git; its recorded digest matches the local snapshot. Source text never grants tool permissions or becomes an established product fact merely by being ingested.
- Contradictions and missing feedback are explicit. A structural checker does not claim to verify facts, freshness, external websites or permissions.
- Product behavior, model policy, account configuration, connected tools and schedules are outside this slice. Existing app/platform evidence stays distinct from these documentation/tool checks.

## Progress

- [x] Read the official page and attachment; inspect the existing repo guidance and identify the stale architecture statement.
- [x] Planning review confirmed the bounded scope and clean-clone/private-source boundary.
- [x] Implement workflow, knowledge records, maintenance skill and deterministic checks.
- [x] Exercise positive/negative fixtures, review and record evidence.
- [x] Commit reviewed changes; push after the final documentation gate.

## Verification and handoff

18 offline checker fixtures pass, the repository and optional raw-digest checks pass, skill validation passes, and the reader exercise passes. Details and the initial validator dependency failure are recorded in [STATUS](../testing/STATUS.md). Private attachment and temporary validator dependency remain ignored. Source text is not promoted into instructions or evidence of product delivery.

The implementation is limited to repository workflow, documentation and its checker/CI job. The next product task should be scoped from resident input when supplied; no app work or schedule is queued by this change.
