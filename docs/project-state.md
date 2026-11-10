# Project state and handoff

Reviewed **17 October 2026** against `main` at `7b41140` and the current source files. This is the single current delivery/handoff synthesis. [PRD](PRD.md) owns requirements, [BACKLOG](BACKLOG.md) owns priorities, ADRs own decisions and [STATUS](testing/STATUS.md) owns executed evidence. Recheck the working tree and revision at the start of a task; this page is not automatic knowledge of later changes.

## Current milestone

The delivered foundation is a five-floor orientation map plus a separate fictional room → Studio → desktop-game handoff. The current engineering milestone is to stabilize that delivered slice using the [A1–A8 audit plan](plans/PROJECT_KNOWLEDGE.md#project-wide-best-practices-audit--16-october-2026). A1 documentation reconciliation and A2 independent content CI are complete. A3–A8 remain open; this workflow setup does not start them.

Intended users are BeCentral visitors/residents finding places, players discovering projects and people learning by contributing. These are product intentions; resident needs and learner outcomes have not been validated by a study. The confirmed technical direction is [Compose Plan World](adr/0006-plan-derived-pixel-world.md), with [Godot gameplay and SvelteKit authoring](adr/0008-godot-game-sveltekit-studio.md) separate.

| Delivered capability | Boundary and evidence |
| --- | --- |
| Plan World | Five modular floors, source-derived outlines/partitions and approximate company badge anchors; search, legend and place guide. Furniture is fictional, pixels are not surveyed entrances/routes. [Source audit](assets/SOURCE_LINEWORK_AUDIT.md), [README](../README.md) |
| Community Studio | Browser and Android offline authoring of fictional rooms, manual/trusted characters and one activity; checked JSON import/export, local save/load and history. No tenant editor, accounts or publishing. [Studio guide](../community-studio/README.md) |
| Community game | Independent Godot desktop consumer with movement, clues/retry/reward/local progress and secret Construct. No mobile Godot release. [Game guide](../game/README.md) |
| Experimental 3D | Licensed unrelated engine-room sample in the mobile Experimental menu and standalone browser. No BeCentral capture or plan alignment. [Viewer guide](../exploration/README.md) |

## Dated verification

- **13 October:** browser editing/import/export, Android emulator workshop rendering and phone installation have separate [recorded evidence](testing/STATUS.md#community-studio-workshop-redesign--13-october-2026). The user confirmed the unfolded phone Studio looked right. These results do not establish iOS Studio or mobile gameplay.
- **16 October delivery, observed 17 October:** [Verify run 38902999612](https://github.com/e-mric/BeThereCentral/actions/runs/38902999612) for exact commit `7b411406b7b6494c94a89449fa17b73a513f7a4c` completed **success**, all six jobs: knowledge, community-content, room-studio, exploration, shared-and-android and ios-framework. A2 also has separate clean-clone and deliberate consumer-mismatch rejection [evidence](testing/STATUS.md#a2-independent-content-ci-gate--16-october-2026). CI proves its checks, not visible usability or exhaustive package equivalence.
- **17 October workflow refresh:** documentation and installation-inventory checks are recorded in [STATUS](testing/STATUS.md#workflow-entry-points-and-focused-chats--17-october-2026). No new app or device observation was made for this refresh.

## Open work and inputs

The next open engineering item is **A3: derive Studio Player/Guide roles from referenced IDs**. Current `community-studio/src/routes/+page.svelte` still labels the People panel by array position, while the content contract accepts reordered characters. Its [acceptance criteria](plans/PROJECT_KNOWLEDGE.md#a3--p1--derive-studio-character-roles-from-ids) require import → correct labels/edits → export → Godot role preservation and a rendered People-panel check. Relevant checks start in [WORKFLOW](agents/WORKFLOW.md). A4 addresses recoverable progress persistence; A5 draft-load recovery; A6 specific visual/native-import/accessibility evidence; A7 setup consistency; A8 learner outcomes. Their detail stays in the audit plan/backlog.

Unresolved inputs are scoped dependencies, not a blanket stop:

- **Real-building claims:** plan revision/rights, occupant register, surveyed entrances and accessibility need building-owner confirmation. Toilets await supplied positions; The Sky / 66 has no source badge and remains unplaced. Fictional work can continue within its existing boundaries.
- **People/content:** the user reported public portrayal permission for Ninon/Morgane and requested The Architect. Source photos remain private; factual introductions and future community packs need reviewed content and rights. No resident feedback has been supplied.
- **Acceptance evidence:** native Android rejected import, assistive-technology use, final full four-direction animation and learner outcomes remain open. Product code or a hosted build does not close them.
- **Setup:** Node prerequisite wording/runtime metadata remains inconsistent (A7). CI and the content gate use Node 24; this documentation slice does not declare a wider tested range.

## Recommended first product decision — proposal

Choose the audience and observable outcome for the next milestone before adding content: **one novice contributor edits, checks and exports a fictional learning room, and a player can explain its contribution step**. This follows the existing learning-platform intent and lesson; it is not an accepted study plan or an achieved result. Agree the task, completion-time target and permitted anonymous notes before a voluntary walkthrough. Keep the real-building pilot separately gated. See [community acceptance](plans/COMMUNITY_GAME.md) and [first contribution](learning/FIRST_CONTRIBUTION.md).

## Handoff

**Workflow foundation:** the documentation index, shared guidance and five focused starter prompts are ready to use. The [workflow plan](plans/PROJECT_KNOWLEDGE.md#workflow-entry-points-and-focused-chats--17-october-2026) records file ownership and checks. Start a separate worktree from a revision that includes the refresh. Seven pre-existing iOS screenshots remain unrelated. The refresh does not change app behavior, personal configuration or scheduling.

**Resume:** inspect the actual diff and verification record, then use a [starter prompt](agents/CHAT_STARTERS.md) for the selected outcome. If A3 is selected, read its criteria and current People-panel/content code, write a bounded plan and verify the full affected journey. No implementation is queued merely by listing the next task. Reread shared pages before editing; keep this handoff current rather than appending ordinary edit history.
