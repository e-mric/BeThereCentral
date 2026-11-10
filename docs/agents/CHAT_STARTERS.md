# Chat starters

Copy a starter into a new project chat, then replace the bracketed focus with the work you want. Begin with [project orientation](../index.md) and the dated [current project state](../project-state.md); follow their links to canonical requirements and evidence. These starters provide context and completion criteria, not blanket authorization for external actions.

## Product scope and roadmap

```text
Work with me on BeThereCentral product scope and the working roadmap.

Start by reading AGENTS.md, docs/index.md, docs/project-state.md, then the relevant canonical PRD, backlog, glossary and ADRs. Use docs/knowledge/index.md and its project-knowledge skill when retrieving accumulated evidence or source research. Treat product documents as authoritative and dated state notes as navigation.

Focus: [product question, user problem, or roadmap area].

Separate confirmed requirements, shipped capability, evidence, open questions and proposals. Recommend a small next slice with its user problem, experiment, decision criterion, observable completion criteria, dependencies and risks. A proposal remains unaccepted until I explicitly accept it. Update the working backlog or plan only when the requested outcome includes that edit; do not publish tracker items or contact people without explicit authorization.

Finish with the recommendation, evidence and canonical links, decisions needed from me, and a concrete next-step handoff. If the work spans steps, record outcome, constraints, checks and handoff in PLANS.md/a task plan as the repository workflow directs.
```

## Research and studies

```text
Research a BeThereCentral question and give me source-linked findings I can act on.

Start with AGENTS.md, docs/index.md and docs/project-state.md. Read the relevant PRD, ADRs, experience standards, backlog and verification records before using secondary project notes. Use docs/knowledge/index.md and read .agents/skills/project-knowledge/SKILL.md for accumulated evidence or reference ingestion. If the Matt Pocock research skill is available in your environment, read it before invoking; otherwise follow the source and citation rules below. Update affected existing notes when meaningful knowledge changes; add a research page only when substantive findings justify it. Use docs/agents/SKILLS.md to select one primary workflow per phase and resolve overlap.

Question: [specific question, population, platform or decision].

Use authoritative sources, distinguish sourced fact from inference, and report uncertainty and limits. For a study proposal, define the question, agreed participant group, consent, task, observations, data minimization and decision rule. No human outreach, participant recruitment, or study session is authorized by this prompt alone.

Deliver a concise answer or study protocol with citations/links, what it can and cannot establish, and an actionable handoff to the relevant product, design or implementation decision. Keep proposals distinct from accepted scope.
```

## Design

```text
Help me design a BeThereCentral experience for the focus below.

Start with AGENTS.md, docs/index.md and docs/project-state.md; then read docs/PRD.md, GLOSSARY.md, relevant ADRs and docs/EXPERIENCE.md. Inspect the complete affected journey and actual rendered screens on the agreed target. Consult docs/design/MOBBIN_REFERENCES.md when visual reference material is relevant, and use the knowledge index for dated evidence.

Focus: [screen, journey, user need and constraints]. Target and available evidence: [browser, emulator, simulator or physical device; revision/build if known].

State the user problem and the design question before expanding scope. Describe the proposed interaction, content, states, accessibility considerations and acceptance criteria. Keep visual/map claims within source evidence and distinguish fictional decoration from real building facts. Build a prototype only to answer a bounded question or when I request one; this is ongoing iterative product development, not an automatic throwaway-prototype phase.

Report the design decision or options, unresolved choices, rendered evidence by target, and implementation-ready handoff. A build or renderer-ready log alone is not visual evidence. Do not install inspection services on my phone; follow the ordinary install/launch and user-verification boundary in AGENTS.md.
```

## Implementation

```text
Implement this BeThereCentral change as a small, runnable vertical slice.

Start with AGENTS.md, docs/index.md and docs/project-state.md, then read README.md, docs/PRD.md, GLOSSARY.md, relevant ADRs and the affected feature's own instructions. For interface work also read docs/EXPERIENCE.md and inspect the full journey and rendered screens. Follow docs/agents/WORKFLOW.md. Read a skill before invoking it; use .agents/skills/tdd/SKILL.md for requested test-first work, and consult docs/agents/SKILLS.md for skill provenance and routing.

User problem and requested behavior: [specific change].
Scope/constraints: [platform, feature, files, compatibility or exclusions].

Keep pure domain logic independent of UI, operating systems and transport. Add matching feature tests for changed behavior, update affected docs and CHANGELOG.md, and record only commands actually run and their outcomes in docs/testing/STATUS.md. Preserve privacy, consent, source-plan uncertainty and the distinction between simulated and real integrations. For phone checks, use only the ordinary ADB install/launch and narrow crash diagnostics in AGENTS.md; leave visible phone verification to me unless I authorize another method.

Finish with changed behavior, files, checks and actual outcomes, remaining limits, and the next actionable handoff. Keep proposals separate from shipped behavior and do not commit credentials, local SDK paths, signing assets or ordinary build output.
```

## External testing

```text
Evaluate actual BeThereCentral user journeys independently in the agreed environment. Exercise authorized checks when access is available; prepare a protocol or analyze supplied results when human participation or system access is still missing.

Start with AGENTS.md, docs/index.md and docs/project-state.md, then read the relevant PRD, docs/EXPERIENCE.md, ADRs and docs/testing/STATUS.md. Use the knowledge index for prior evidence and preserve its source links.

Agreed test: [question and tasks]. Target audience: [agreed group]. Target environment and revision/build: [exact browser/device/platform and revision]. Authorization/data limits: [consent, observation, recording and retention boundaries].

Before interpreting results, confirm the target and revision against the agreement. Keep emulator, browser, simulator and physical-device observations distinct. For each finding record task, expected behavior, observed behavior, severity, reproducible steps/evidence and pass, fail or inconclusive result. Separate participant observation from inference; do not claim broader usability or accessibility from a narrow test. Minimize collected data and do not retain location history.

Stay within the explicit authorization/data limits above. Missing access or human participation makes a check unrun or inconclusive; it is not a pass. Ask only for genuinely missing authorization before contacting people, changing production or publishing. Preserve the physical-phone restriction in AGENTS.md. Deliver a concise findings report, unresolved limits and a prioritized handoff linked to the affected requirements. Record verification status only when the test actually ran.
```

## Optional focused starters

Use one of these when the task specifically needs the named kind of work; keep the same project-orientation, evidence and handoff rules above.

- **Architecture:** when defining or reshaping a module boundary, read `.agents/skills/codebase-design/SKILL.md`, the architecture doc and relevant ADRs; return the interface, dependency direction, tradeoffs and migration slice.
- **Review:** when assessing a concrete diff, read `.agents/skills/code-review/SKILL.md`; identify actionable findings against both documented standards and the PRD, with file/line evidence and unverified checks.
- **Security:** when assessing data flows, imports, permissions or trust boundaries, read `SECURITY.md`, the relevant feature contract and privacy requirements; report threat assumptions, concrete failure paths and mitigations. Do not claim a security audit from a narrow review.
- **Release readiness:** when preparing a release or demo, reconcile the exact revision with `docs/testing/STATUS.md`, the relevant platform instructions and the full demo journey; report verified targets, gaps and a handoff. Do not publish, distribute or change release settings without explicit authorization.
