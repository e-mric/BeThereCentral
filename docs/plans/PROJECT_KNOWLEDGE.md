# Project knowledge and agent workflow

16 October 2026 · workflow foundation delivered; project-wide audit and follow-up plan

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
- [x] Deliver the workflow foundation: commit `68476e5`, pushed to `main`.

## Verification and handoff

18 offline checker fixtures pass, the repository and optional raw-digest checks pass, skill validation passes, and the reader exercise passes. Details and the initial validator dependency failure are recorded in [STATUS](../testing/STATUS.md). Private attachment and temporary validator dependency remain ignored. Source text is not promoted into instructions or evidence of product delivery.

The implementation is limited to repository workflow, documentation and its checker/CI job. The next product task should be scoped from resident input when supplied; no app work or schedule is queued by this change.

## Project-wide best-practices audit — 16 October 2026

**Request:** audit the project against the best practices and identify remaining work. **Baseline:** `68476e5a9eae214af4e9a4f8c336f604775f51d5`. This section is the follow-up plan; [BACKLOG](../BACKLOG.md) remains the working roadmap. The workflow foundation above is complete, not a claim that the whole project meets every acceptance criterion.

File/line references below identify the audited baseline; documentation additions can shift current line numbers.

**Method:** reread the [official guide](https://learn.chatgpt.com/guides/best-practices?translationFallback=ja-JP), inspect current instructions/plans, review representative source boundaries, compare CI commands with component test instructions, and follow dated evidence. Astra reviewed documentation and code separately; Luna inventoried checks; the parent verified and integrated findings. Reviews were read-only with separate scopes. Private configuration, credentials and reference photographs were not inspected.

**Limits:** this is a source, workflow and evidence audit, not an exhaustive security review, benchmark or fresh UI walkthrough. Existing app behavior was not changed or newly demonstrated. Findings below are observations or proposed improvements, not permission to publish content, contact residents or enable services.

### Assessment

| Practice | Observed state | Remaining work |
| --- | --- | --- |
| Clear task context and completion criteria | AGENTS, PLANS and component instructions provide a useful foundation. | Reconcile contradictory current-state sections; apply the handoff template to completed plans (A1). |
| Planning and focused delivery | Small fictional slices and an explicit content boundary are established. | Give each remaining gap a narrow outcome and acceptance; do not equate implemented features with completed learner validation (A6–A8). |
| Practical persistent guidance | Root instructions are concise and point to canonical documents. | Semantic lint must check the linked canonical documents, not only the new knowledge pages (A1). |
| Testing and review | Meaningful local suites and independent reviews exist; the inspected hosted workflow passed. | CI omits Godot and standalone package suites; three source-observed behavior gaps need focused coverage (A2–A5). |
| Consistent development setup | Versions and per-component commands are documented. | Align Node support and provide one discoverable clean-checkout verification sequence (A7). |
| Reusable skills | The local knowledge skill has ingest/query/lint examples, structural fixtures and a reader exercise. | Reuse and refine from observed friction; no additional skill framework is justified by this audit. |
| External context and tools | Connected tools serve real design/art tasks; secrets and personal sources stay outside the repository. | Preserve those boundaries; tool/account configuration was not audited and is not a missing product feature. |
| Scheduled work and chat organization | Bounded review assignments work; the knowledge index supports handoffs. | Use a concrete next-task brief. Scheduling is optional, user-requested work after the manual procedure is dependable. |

### Evidence established in this audit

- [Hosted Verify run 38886767636](https://github.com/e-mric/BeThereCentral/actions/runs/38886767636) completed successfully for the exact baseline commit, updated `2026-10-16T10:27:52Z`. Its five jobs were `knowledge`, `room-studio`, `exploration`, `shared-and-android` and `ios-framework`. This closes the prior *unobserved hosted run* gap for that commit. It does not cover Godot or prove visible UI behavior.
- The offline knowledge checker and all 18 fixtures passed. Structural validity did not catch the canonical-document contradictions below; that needs semantic review.
- A read-only, in-memory Node probe reversed the two characters in `content/community/v2/package-founders-v2.json`. `validatePackage` accepted it: the first character became `guide`, while `gameplay.playerCharacterId` remained `player`. Studio still labels the first entry Player. No source fixture was modified.
- Earlier browser file-import verification is recorded in [STATUS](../testing/STATUS.md), 13 October workshop section. Missing native-import, accessibility and final-animation observations must remain separate.

### Prioritized remaining work

Priorities express impact on reliable development: **P1** before relying on the next product delivery; **P2** bounded follow-up before broader use or claims. A completion box stays open until its acceptance evidence exists. Each task requires its own small implementation plan and affected-feature checks; this audit does not start those changes.

#### A1 · P1 · Reconcile current scope and close old handoffs

**Evidence:** [PRD](../PRD.md) lines 59–67 describe gameplay/character work as future while its later v2 amendment records delivery. [Community plan](COMMUNITY_GAME.md) lines 13–20 and 68 put implemented capabilities in later scope; its final verification boundary still calls browser file-import UI unverified. [BACKLOG](../BACKLOG.md) lines 21–27 and 60 repeat those gaps despite 13 October evidence. [ADR 0007](../adr/0007-room-studio-experiment.md) line 23 and [source-linework plan](SOURCE_LINEWORK_AUDIT.md) retain old holds; ADR 0008 explicitly supersedes them. [Workshop plan](STUDIO_WORKSHOP.md) has acceptance but no delivered outcome/handoff.

**Action and ownership:** parent owns the canonical docs/roadmap reconciliation and handoffs. Preserve historical rationale, mark superseded holds with a forward link, and distinguish implemented capability, remaining acceptance evidence and future expansion. Link completed plans to their delivery commit and dated checks. Keep a single backlog, not another copy of its tasks.

- [ ] **Done when:** a fresh reader reaches the same current v1/v2, desktop gameplay and Android Studio scope through README, PRD, backlog, plans and ADRs. Completed browser import is evidenced; native invalid-file import and accessibility remain open. No whole stage is declared complete solely because code exists.

#### A2 · P1 · Cover both content consumers in CI

**Evidence:** [CI](../../.github/workflows/ci.yml) runs Studio/shared/Android/server checks but no Godot command or standalone v1/v2 package suite. [Game instructions](../../game/README.md) document the independent contract and import checks; [v2 contract](../../content/community/v2/README.md) documents `acceptance.json` and the normalization runner. [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md) requires independent validation.

**Action and ownership:** bounded executor owns CI/test orchestration and any necessary asset-sync checks; parent reviews. Use the project's pinned Godot version and trusted asset sync, run v1/v2 Node suites plus Godot headless contracts/import, and compare shared accepted/rejected cases and normalized data across consumers. Do not replace validation with two unrelated green builds.

- [ ] **Done when:** a fresh hosted checkout runs those checks; a deliberately incompatible consumer fails the gate; a clean run links to the exact commit. Document any parity limit explicitly. No new mobile/runtime claim follows from CI.

#### A3 · P1 · Derive Studio character roles from IDs

**Evidence:** `community-studio/src/routes/+page.svelte:359` labels Player/Guide and their introduction fields by array index; `:182` also chooses fallback appearance by index. `content/community/v2/index.js:133` resolves gameplay roles by referenced IDs, and the reversed-order probe above succeeds.

**Action and ownership:** bounded executor owns People-panel role resolution and focused Studio behavior tests. Derive labels and role-specific defaults from `gameplay.playerCharacterId` / `gameplay.npc.characterId`; preserve IDs and package order.

- [ ] **Done when:** a valid reversed-order import shows correct Player/Guide labels and introduction prompts; edits and export keep the referenced identities, and the same package plays with those roles in Godot. Add a behavioral regression and inspect the rendered People panel.

#### A4 · P1 · Make progress persistence failures visible and recoverable

**Evidence:** `game/scripts/local_progress.gd:151` ignores directory/open/write results and opens the sole save directly for overwrite. `game/scripts/main.gd:784` always reports that reset occurred on the device. A failed write can retain old disk progress while memory is reset; interrupted replacement risks the previous save. This is a source-observed failure path, not a reproduced user incident.

**Action and ownership:** bounded executor owns the progress module, caller feedback and matching Godot tests. Return persistence results, write a temporary record successfully before replacing the saved record, and report session-only progress when persistence fails. Keep progress minimal and local.

- [ ] **Done when:** controlled directory/open/write/replace failures preserve the last valid disk record and produce honest feedback; successful completion/reset survives restart and rewards remain single-grant. Verify the failure message on a rendered desktop screen.

#### A5 · P2 · Make saved-draft replacement recoverable

**Evidence:** Studio `+page.svelte:116` replaces current work and clears both histories when loading a saved draft; file import at `:140` is undoable. Accidental Load can lose unsaved edits. An undoable Load is the recommended policy, not an already approved product requirement.

**Action and ownership:** bounded executor owns saved-draft transaction behavior and its Studio tests. Prefer the existing import transaction semantics; avoid adding a new persistence layer.

- [ ] **Done when:** save A → edit B → load A → Undo restores B. Invalid saved content preserves current work/history. Dirty-state feedback accurately describes the result, confirmed in a rendered browser.

#### A6 · P2 · Finish specific interaction and accessibility evidence

**Evidence:** [STATUS](../testing/STATUS.md), final walking-art observation (line 480), Android offline host (line 497) and workshop observations (lines 518–522), explicitly leave these cases incomplete. Browser valid/rejected import was already exercised; do not list it as missing again.

**Action and ownership:** parent scopes one target/journey at a time; bounded executors handle only the selected target and record evidence separately.

- [ ] **Final Godot art:** walk and release in all four directions, collide with a blocker and return idle using final atlas/pivot data; record visible foot movement and any remaining art limitations.
- [ ] **Android native rejected import:** from an edited room, select malformed/unsupported JSON through the native picker; error is understandable and the current room survives. Cancellation also preserves the draft. Use the emulator; physical-phone visibility remains user-verified under AGENTS.
- [ ] **Keyboard and assistive technology:** exercise Room → People → Activity, menus, selection, numeric edits, Save/Load and file errors; focus is visible, controls labelled and no task depends solely on dragging. Separately check Plan World search/legend/floor selection at large text, including fourth floor on short landscape screens. Record target/tool and actual blockers; a pass on one surface does not transfer to another.

#### A7 · P2 · Make the clean-checkout check sequence consistent

**Evidence:** Studio's README says Node 22.17+, CI selects 24, and `community-studio/package.json` has no `engines` declaration. Component commands exist but the workflow table requires readers to assemble the Studio/package/Godot sequence themselves. No new clean-checkout setup was exercised during this audit.

**Action and ownership:** bounded executor owns contributor quickstart/runtime metadata and, only if useful, a small check entry point. Declare the supported Node baseline/range, document asset sync/import order and prerequisites, and link the same sequence from contributor guidance and CI. Local SDK and account secrets stay personal.

- [ ] **Done when:** a clean checkout with documented prerequisites completes the selected package/Studio/Godot checks without private paths, cached untracked assets or paid APIs. Record exact versions and commands; all declared supported runtimes are checked or the claim is narrowed.

#### A8 · P2 · Demonstrate the learning outcome

**Evidence:** [community-plan acceptance](COMMUNITY_GAME.md) lines 48 and 56 asks for a player to explain the contribution step and a novice to complete an authored change. [The lesson](../learning/FIRST_CONTRIBUTION.md) exists, but recorded walkthroughs are development checks, not learner evidence. No resident/learner study has been supplied.

**Action and ownership:** parent prepares a bounded voluntary walkthrough of the existing fictional lesson. Define an observable learning criterion before running it: a player explains the step, and a novice edits/checks/exports one change using the lesson. Set the completion-time target when scoping the session; do not invent achieved results. Collect only anonymous completion/confusion notes. Preparing this task does not authorize outreach or data collection.

- [ ] **Done when:** the agreed learner tasks have observed outcomes, confusing steps are recorded and resulting lesson changes are checked. Keep broad resident discovery optional and separate from this narrow learning evaluation.

### Expansion boundaries already known

Reviewed factual community introductions, content owners and withdrawal/rollback rehearsal remain [Stage 4](COMMUNITY_GAME.md#stage-4--review-a-small-community-content-pack) work. Approved source-plan revision/rights, toilet positions, surveyed entrances and accessibility are prerequisites for corresponding real-building claims. Mobile Godot, accounts/publishing, multiplayer and building capture remain separate future scope. Their absence is not a failure of the agent-workflow guide; do not implement them merely to complete this checklist.

### Next task and handoff

**Recommended first slice:** A1, the canonical-doc reconciliation, followed by A2, the missing CI consumer checks. A3/A4 then address source-observed behavior risks; A5–A8 are independently bounded follow-ups. Optional settings, extra connectors, new skills and schedules have no required task in this audit.

The audited product baseline is `68476e5`; the audit delivery is a documentation-only commit after it. Seven pre-existing untracked iOS screenshots are unrelated and remain untouched. Find actual audit commands/outcomes in [STATUS](../testing/STATUS.md). No production defect above is fixed by writing this plan, and no missing platform/learner evidence is marked complete.
