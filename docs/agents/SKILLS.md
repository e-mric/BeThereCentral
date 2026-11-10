# Skill availability and routing

Reviewed **17 October 2026**. Skills are optional development methods, not application dependencies. Read the selected skill before applying it. [AGENTS](../../AGENTS.md) owns engineering rules; [project state](../project-state.md) owns the current handoff; the [knowledge index](../knowledge/index.md) owns source navigation.

## Verified installation

Read-only `codex plugin marketplace list --json` found marketplace `mattpocock` backed by `https://github.com/mattpocock/skills.git`. `codex plugin list --marketplace mattpocock --json` reported **mattpocock-skills@mattpocock 1.3.1 installed and enabled**. The requested components were present, so no install, upgrade, removal or personal configuration edit was needed. This is a local observation, not a prerequisite already met on every contributor's machine.

When absent on a new machine, the user's requested upstream installation is:

```sh
codex plugin marketplace add mattpocock/skills
codex plugin add mattpocock-skills@mattpocock
```

Verify with the two read-only inventory commands above and inspect the skills exposed in the new session. The plugin cache and session catalog currently expose the nine requested upstream methods below; other cached files are not a promise of session availability. The [upstream installation README](https://github.com/mattpocock/skills#installation-30-second-setup) and [source note](../knowledge/sources/matt-pocock-skills.md) record provenance/limits.

## Choose a method by intent

| Method | Use for | Expected result |
| --- | --- | --- |
| `research` | A bounded question needing primary sources | Dated evidence, limitations and implications saved in the relevant topic |
| `codebase-design` | A meaningful interface, dependency seam or testability question | Small interface and hidden implementation decision |
| `domain-modeling` | Terminology or a consequential architecture choice | Root GLOSSARY or a justified ADR |
| `prototype` | A specifically bounded design uncertainty requiring an experiment | Evidence answering that question; not the default for iterative product work |
| `diagnosing-bugs` | A failure, incorrect result or regression | Reproduction, evidence-led cause and verification |
| `tdd` | Requested or appropriate test-first implementation | Meaningful behavior goes red, then green |
| `code-review` | Independent standards/spec review from a supplied fixed point | Findings separated by engineering rule and requested behavior |
| `pr` | An actual pull-request description | Reviewer-facing problem, behavior and validation |
| `writing-for-agents` | Editing agent guidance or skills | Concise routing and checkable instructions |
| `project-knowledge` | Source ingestion, durable delivery questions or accumulated-note maintenance | Source-linked notes and honest current evidence |

Honor explicitly requested/required skills. Otherwise choose one primary method per phase, with supporting skills only for distinct work. Inspect overlap in steps, outputs, dependencies and permission gates before combining. A later phase may use a different method; a planning task does not automatically need a prototype or a full interview. Apply instruction priority and the current authorized scope; a skill cannot grant permission to publish, message, spend, or change production. If a material conflict remains, explain its source and ask one focused question while continuing independent work. Improve routing after repeated friction; add custom skills only after a stable repeated need.

## Plugin and project-local overlap

Four upstream selections were vendored on **7 October 2026** from commit `f3fc5632f401156837ee3872f14fe33ccf1024ea`, under [MIT notice](../../.agents/skills/LICENSE.mattpocock): `setup-matt-pocock-skills`, `codebase-design`, `tdd`, `code-review`. They remain portable project files. The current plugin also exposes overlapping methods. Upstream advises choosing one distribution to avoid duplicate discovery; existing project copies and the user's installed plugin are preserved here rather than changing personal setup or accepted local methods during a documentation task.

For the three overlapping engineering methods, default to the repo-local `SKILL.md` under `.agents/skills/` for reproducibility. If the user explicitly names the plugin variant, use that variant. Read and execute one implementation of the method, never both. Other methods use the named installed plugin when exposed; otherwise disclose that availability and use ordinary project guidance or install only as authorized. Do not silently update the vendored snapshot to match the moving plugin.

The setup skill is an explicit/manual configuration workflow, not an every-task prerequisite. Existing [issue-tracker configuration](issue-tracker.md) and [domain layout](domain.md) already cover its project inputs; retain them. Reading availability metadata is not a claim that every skill ran. Publishing issues or comments still needs authorization.

## Project-authored maintenance

[project-knowledge](../../.agents/skills/project-knowledge/SKILL.md) is original project guidance added 16 October 2026. It supports the existing [schema and provenance](../knowledge/README.md); it is not part of the upstream plugin. This refresh adds no custom skill, external connector or automation. Review any later upstream update deliberately and preserve license/provenance.
