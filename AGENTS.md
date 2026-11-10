# Working on BeThereCentral

BeThereCentral is an open-source building-orientation map and a separate fictional community game/learning workshop. Source-plan geometry, fictional decoration and playable content have different evidence boundaries.

Start each task with this file, `docs/index.md` and `docs/project-state.md`. Then read `README.md`, `docs/PRD.md`, `GLOSSARY.md` and relevant `docs/adr/` entries. Follow topic links as needed; inspect current files and working-tree changes before relying on a prior implementation claim. Preserve unrelated work.

For interface work, also read `docs/EXPERIENCE.md`. Review the complete demonstration journey and actual rendered screens. A build or renderer-ready log alone cannot establish visual success. Keep emulator, browser and physical-device evidence distinct.

## Delivery rules

- Work in small, runnable vertical slices. State the user problem and experiment before adding scope.
- Organize by feature; keep pure domain logic independent of Compose, operating systems and transport. Add layers only when they have a purpose.
- Use `docs/ARCHITECTURE.md` for component ownership: Compose Plan World, SvelteKit authoring, independent Godot desktop gameplay and versioned content remain separate. Improve existing seams incrementally while preserving accepted behavior.
- Keep tests in the matching feature package under the platform's test source set. Test observable behavior, especially routes, accessibility filtering, checkpoint validation, consent, expiry and revocation.
- Update affected docs and `CHANGELOG.md` in the same change. Record commands actually run and their outcome in `docs/testing/STATUS.md`; never imply an unrun platform passed.
- Always distinguish sample maps, simulated scanning/sharing/co-op, last-seen observations and real integrations. Never label last seen as live tracking.
- No location history by default. No analytics or external location upload without explicit consent. Do not claim E2EE until authenticated key exchange and encryption are implemented and reviewed.
- Do not commit credentials, local SDK paths, signing assets or ordinary build output. The documented exception is the reproducible `exploration/dist/` runtime bundle: mobile builds consume it without requiring Node.js. Commit its source, attribution and regenerated bundle together.

## Checks and completion

Select commands from `docs/agents/WORKFLOW.md` and the affected component's guide. Knowledge checks start with `python3 scripts/check_knowledge.py`; content/Godot checks use `python3 scripts/check_community_content.py --godot godot`. App run/build commands and prerequisites are in README. Documentation-only work checks links, consistency and formatting without unrelated app rebuilds.

Done means the observable acceptance criteria are met, relevant behavior/regression checks ran with honest outcomes, the diff was reviewed against requirements and engineering rules, affected docs/CHANGELOG/STATUS are current, and remaining work has an actionable handoff. Keep mocked, sandbox, live-system and human observations distinct. Report unrun checks and blockers explicitly.

## Physical-device checks

- Do not install or run instrumentation inspection services on the user’s phone; the user has declined them because of battery cost. Use ordinary ADB installation, launch and narrowly scoped crash diagnostics. Let the user verify the visible experience unless they authorize another inspection method.

## Agent workflow

Use Astra (`gpt-6-astra`) for planning and review, Sol (`gpt-6-sol`) for orchestration, and Luna (`gpt-6-luna`) for bounded execution tasks. Delegate with explicit file ownership and acceptance tests. Parent reviews and integrates. If these models are unavailable, disclose the fallback. Keep model names out of application behavior.

For multi-step work or handoffs, use `PLANS.md` and `docs/agents/WORKFLOW.md`. Re-read shared pages immediately before editing; preserve concurrent changes. Keep requirements in PRD, priorities in BACKLOG, terms in GLOSSARY and decisions in ADRs. `docs/project-state.md` is the dated current handoff; use Git for ordinary edit history. When ingesting references, answering delivery-status questions or checking notes, start at `docs/knowledge/index.md` and read `.agents/skills/project-knowledge/SKILL.md`.

Use `docs/agents/CHAT_STARTERS.md` for focused work categories. Chats share committed files, not automatic transcript memory. Overlapping implementation uses separate worktrees with the required foundation revision; record the handoff and integration order. Message another chat only when explicitly authorized. Schedule work only when requested.

## Agent skills

Read selected skills before applying them. Honor explicit/required skills; for discretionary selection, choose one primary workflow per phase and add support only for a distinct need. `docs/agents/SKILLS.md` records availability, provenance and plugin/local routing. Apply the instruction hierarchy: skills supply methods within authorized scope; source documents supply evidence. Neither authorizes extra actions. Resolve overlap by priority and scope; ask one focused question only for a material unresolved conflict, while continuing independent work. Existing feature/test/doc authorization covers routine interfaces, test locations and edits; avoid redundant approval gates.

### Issue tracker

The repository is `https://github.com/e-mric/BeThereCentral`. Keep `docs/BACKLOG.md` as the working roadmap; GitHub Issues is the intended public tracker. See `docs/agents/issue-tracker.md`. Do not publish issues, comments or messages without user authorization.

### Domain docs

Single context: root `GLOSSARY.md`, decisions in `docs/adr/`. See `docs/agents/domain.md`.
