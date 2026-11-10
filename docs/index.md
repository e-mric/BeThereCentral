# Project documentation

Start with [AGENTS](../AGENTS.md) and [project state](project-state.md), then follow the task below. This is navigation; each topic keeps its existing authoritative document. Read only relevant branches after the required startup context.

| Task | Read next | Expected durable result |
| --- | --- | --- |
| Understand users, scope or priorities | [README](../README.md), [PRD](PRD.md), [BACKLOG](BACKLOG.md), [community roadmap](plans/COMMUNITY_GAME.md) | Requirements, accepted decisions and proposals kept distinct |
| Plan or implement a bounded change | [Workflow and check selection](agents/WORKFLOW.md), [plan template](../PLANS.md), [starter prompts](agents/CHAT_STARTERS.md) | One outcome, acceptance checks and actionable handoff |
| Investigate a question or maintain evidence | [Knowledge/source index](knowledge/index.md), [maintenance schema](knowledge/README.md), [skill routing](agents/SKILLS.md) | Source-linked conclusions, dates and limits |
| Design or test an interface | [Experience standards](EXPERIENCE.md), [demo journey](DEMO.md), [design references](design/MOBBIN_REFERENCES.md) | Complete journey and actual rendered evidence by target |
| Change an architecture seam or term | [Architecture](ARCHITECTURE.md), [glossary](../GLOSSARY.md), [decisions](agents/domain.md) | Updated domain language or justified ADR |
| Check delivery or prepare a release | [Verification evidence](testing/STATUS.md), [CI](../.github/workflows/ci.yml), [changelog](../CHANGELOG.md), [contributing](../CONTRIBUTING.md) | Exact revision, commands, outcomes and remaining limits |
| Work with source plans or real-building claims | [Coordinates](COORDINATES.md), [placement audit](assets/COMPANY_PLACEMENT_AUDIT.md), [linework audit](assets/SOURCE_LINEWORK_AUDIT.md), [privacy](PRIVACY.md), [security](../SECURITY.md) | Source uncertainty, rights and surveyed facts preserved |

## Component ownership and entry points

| Area | Source/guide | Boundary |
| --- | --- | --- |
| Plan World and shared feature behavior | [composeApp](../composeApp/), [architecture](ARCHITECTURE.md) | Source drawing/search and Compose presentation; thin [Android](../androidApp/) / [iOS](../iosApp/) hosts |
| Community Studio | [SvelteKit guide](../community-studio/README.md) | Local fictional room, people and activity authoring; Android offline authoring host |
| Content handoff | [v1](../content/community/v1/README.md), [v2](../content/community/v2/README.md) | Independent bounded validation across Studio and Godot |
| Community game | [Godot guide](../game/README.md) | Standalone fictional desktop gameplay; mobile export remains future work |
| Experimental 3D | [Viewer guide](../exploration/README.md) | Unrelated licensed sample; no BeCentral capture/alignment |
| Earlier Room Studio | [Vite/PixiJS guide](../room-studio/README.md) | Separate historical local-room editor |
| Sharing reference | [Server guide](../server/README.md) | Disconnected synthetic service, not an app integration |

Engineering guidance already lives in [ARCHITECTURE](ARCHITECTURE.md), [CONTRIBUTING](../CONTRIBUTING.md) and [WORKFLOW](agents/WORKFLOW.md); no parallel engineering specification is needed. [Project state](project-state.md) summarizes the current handoff. The backlog owns task detail, STATUS owns executed evidence, and Git owns edit history.
