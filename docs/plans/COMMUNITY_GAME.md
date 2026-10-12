# Community game implementation plan

12 October 2026 · Written proposal · Game and Community Studio implementation on hold

## The idea and the first useful experiment

BeThereCentral should let people discover the BeCentral community through a pixel-art game: explore companies and projects, meet characters, complete small quests, puzzles and learning challenges, then contribute something themselves. A simple browser Studio should eventually let people shape a fictional space, create their character and author supported challenges. Learning happens both by playing and by helping build the project through coding, design, pixel art, AI-assisted work or storytelling. Beginners should have a complete small contribution they can make; experienced contributors should have clear review and extension paths.

The first experiment is deliberately one room: explore, meet one approved character, discover one approved project, complete one short learning challenge and receive a visible reward. A contributor can then change that room or challenge, preview it and export a working content package. This tests the full play–learn–contribute loop before investing in a whole campus or a publishing service. The precise learning topic and project are not selected yet; no tenant mission or personal biography is invented by this plan.

## Current state and authorization boundary

| Available now | Planned after the feedback gate |
| --- | --- |
| Compose Plan World: five schematic floors, directory/search and place guide | Standalone Godot gameplay, movement, dialogue, learning challenges and local progress |
| Authored reusable floor decoration in source; approximate source-plan anchors | Playable scenes with separately authored fictional collision and interactions |
| Separate local Vite/PixiJS Room Studio and transient Compose preview | SvelteKit Community Studio for rooms, characters and supported quests |
| Manual JSON handoff for one fictional room | Independently validated, versioned game content packages |
| Open repository and development documentation | Guided contribution lessons and reviewed community content |

Godot and SvelteKit are selected, not implemented replacements. The [technical plan](GODOT_SVELTEKIT.md) and [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md) remain authoritative for architecture. The current Room Studio fixture is a starting point, not an existing character/quest standard. The unrelated licensed 3D engine-room sample is an Experimental viewer, not a BeCentral capture or evidence of game progress; its separately authorized menu exposure and Android installation do not resume this plan.

Before Stage 1, record resident feedback, revise the PRD and plans, and obtain the user's explicit instruction to resume game/Studio implementation. Feedback arriving alone does not open this gate. Planning does not authorize engine projects, dependencies, scaffolding, production assets, accounts or publishing. Existing authorized Compose map work can continue independently.

## Stage 0 — agree on one learning moment

**Problem:** the public idea invites many kinds of participation but does not establish which experience residents actually want. **Experiment:** discuss the one-room loop with 5–8 consenting residents or classmates, including beginners and prospective content contributors. Use the existing map and room prototype as clearly labelled references, not as a playable-game demonstration.

Record whether people want to play, explain their project, portray themselves, decorate a space or contribute a particular skill; ask what they would want a visitor to learn. Capture anonymous task notes and disagreements. Photos, accounts and movement logs are unnecessary. Select one content owner, one learning outcome, one challenge format, a short completion-time target and intended test devices. Agree measurable usability and performance targets before implementation; do not infer success from this proposal.

**Exit evidence:** a short findings record, a revised PRD/backlog and explicit user resumption. A willing participant's portrayal approval is separate from their permission to distribute assets or introductions. If feedback rejects the first room or learning topic, revise the proposal before building it.

## Stage 1 — edit a room and open it in the game

**Depends on:** Stage 0. **Problem:** a contributor needs confidence that their simple space survives the handoff from editor to game. **Experiment:** one bounded fictional room in SvelteKit Studio, using licensed reusable props, exports to a standalone Godot scene. Retain the current prototypes during evaluation; choose exact compatible tool versions at this stage.

The author changes a sign, adds/moves/removes furniture, uses undo/redo, saves locally and imports/exports a package. Keep the outline fixed for this first slice. Keyboard controls and property fields provide an alternative to dragging. Loading, validation failures and reset must be understandable.

**Acceptance:** the same IDs, sign and visible placements survive Studio → package → Godot; malformed or oversized imports cannot replace the last valid scene. Both consumers reject unsupported versions, duplicate IDs, broken references and unsafe paths/types against common fixtures. Inspect actual browser and Godot screens and document the first measured performance baseline. A clean checkout can run the fictional sample from documented commands without a paid AI account. Deliver a beginner lesson: decorate the sample, export it and explain one validation error.

## Stage 2 — play one learning challenge

**Depends on:** Stage 1 and approved challenge content. **Problem:** reading a directory is different from learning through play. **Experiment:** one controllable avatar meets one approved NPC, reads a short project introduction, completes a bounded challenge and sees one reward. Use a fictional character if no person has approved representation.

Choose one initial template after feedback, for example inspect two project clues and answer a question, or put a small set of steps in order. Offer hints, retry and an explanation of the answer. Keep dialogue and objectives declarative; do not execute player-supplied code. Additional puzzle templates follow evidence rather than being prerequisites.

**Acceptance:** keyboard and touch movement, layering, collision, all directional idle/walk animations and dialogue are understandable on rendered screens. Completion requires its stated condition; retries and save/reload cannot duplicate the reward. Local progress is versioned, minimal, resettable and separate from content. A player can describe the intended learning outcome after the task, and the content owner verifies accuracy. Record named Android and iOS test targets independently, explicitly marking any unverified target. Deliver a storytelling lesson that changes a clue and explanation within the supported template.

## Stage 3 — create a character and author a challenge

**Depends on:** Stage 2's proven content model. **Problem:** people need control over their portrayal and an approachable way to contribute a learning activity. **Experiment:** one author uses manual modular character options and a supported quest template, previews the result, corrects it and exports it for the same room.

The character workflow includes display name, optional introduction, modular appearance, portrait and every directional idle/walk animation. A short quest form exposes learning objective, approved source/project, dialogue, hints, completion rule, explanation and reward. Include a playable preview or test handoff so an author can discover dead ends before review. Keep space editing within the tested room constraints.

**Acceptance:** character identity, palette, proportions, frame sizes and foot pivots are coherent across portrait and animation. The person can correct or remove their portrayal before distribution; a portrait alone does not pass. Studio exposes text-based alternatives to canvas-only work and useful reference/validation errors. Independent Godot validation rejects missing sprites, unsupported conditions and unreachable required steps. A novice completes one authored change using the lesson without needing to edit an engine script. Deliver pixel-art and design lessons using original, licensed sample parts.

## Stage 4 — review a small community content pack

**Depends on:** Stages 1–3 and participant evidence. **Problem:** content from several contributors needs understandable ownership, compatible versions and a way to correct mistakes. **Experiment:** maintainers review a small set of approved rooms, characters, project introductions and challenges, then assemble one versioned pack. Begin with local exports and repository review; a hosted editor/account system is not required.

**Acceptance:** every included asset and factual introduction has a source, owner and distribution basis; missing approval blocks inclusion. Validate references across rooms and fictional transitions. Demonstrate supported content updates, progress compatibility/reset behavior and rollback to a previous pack. Rehearse a correction or withdrawal: remove content from the next pack and maintained downloads, noting that already downloaded public copies cannot be recalled. Publishing the pack or contacting contributors still needs the appropriate user authorization.

Run the complete enter → discover → learn → reward → edit → export → review journey. Preserve the map/search/legend role chosen in the revised PRD, including uncertain and unplaced entries. Expand toward campus coverage only after this small pack meets the agreed interaction, accessibility and performance targets. Real room associations need confirmed mapping; fictional transitions never claim real routes. Hosted publishing, authorization and moderation require a separate scoped decision.

## Proposed content and trust boundary

These are design requirements for the future contract, not files or schemas already implemented. Set exact bounds in Stage 1 and extend them only with fixtures and compatibility tests.

| Record | Minimum responsibility and validation |
| --- | --- |
| Package | Schema/content versions, stable IDs, asset manifest, attribution and compatibility rules; reject unknown required features before loading |
| Room | Bounded decoration, spawn points, fictional collision and interactions; source-plan geometry and optional approved place association remain separate |
| Company/project introduction | Stable identity, supplied description, attributed source, content owner and review date/status; do not infer current occupancy or an endorsement from a plan name |
| Character | Approved display name/introduction, modular choices, portrait and directional animation references; optional project/quest role without requiring real identity or a photo |
| Quest/challenge | Learning objective, approved subject matter, supported objectives/conditions, hints, explanation, reward and tested state transitions; no arbitrary scripts |
| Asset | Creator, source, licence, modification/generation notes, distribution permission and technical layout; code's MIT licence is not an asset licence |
| Local progress | Package/content version, bounded quest state and earned rewards; no movement history, physical position or unnecessary personal profile |

Keep private approval records and contact information out of exported packs; distributable metadata can reference an internal review record without exposing personal evidence. Before inclusion, distinguish permission to portray a person, publish their introduction, distribute their image/sprites and attribute them by name. Define draft → reviewed → approved → withdrawn handling and who can approve each kind of content. Before any shared service, add authorization, revision ownership, reporting and rollback as explicit acceptance criteria.

Limit package bytes, item counts, text length, coordinates, image dimensions, animation frames and quest transitions. Treat imports as untrusted declarative data in both consumers; never load arbitrary engine resources, remote asset URLs or executable content from a pack. Test normal round trips as well as malformed data, missing references and unsupported version updates. Keep asset loading within the reviewed package boundary.

Manual creation comes first. Optional later AI assistance may help with drafts, code explanations or original art exploration, but a person reviews facts, licences and results. Photo-assisted generation requires a separately agreed provider and upload/retention/deletion policy, explicit consent and approval of the resulting portrayal. Source photos and unapproved personal material stay out of the public repository. No AI subscription is needed to play, create manually or contribute.

## Learning to contribute at different levels

Each lesson states prerequisites, a small visible outcome, a sample fixture, how to preview/check it and how to ask for review. Keep a beginner path that works locally and an extension task for experienced contributors; do not promise that one editor accommodates every advanced workflow.

| Contribution | First bounded lesson | Review evidence |
| --- | --- | --- |
| Coding | Add or fix one validation rule against a documented fixture | Observable valid/rejected behavior tests in the matching feature package |
| Design | Improve one form/error state or keyboard editing action | Before/after rendered journey plus keyboard and text-size checks |
| Pixel art | Add one original prop, then a coherent character part | Provenance, dimensions, palette and in-game placement/animation preview |
| Storytelling | Write one clue, hint and answer explanation | Content-owner accuracy review and a successful player walkthrough |
| AI-assisted practice | Document a reviewed optional draft or explain a code change | Human corrections, appropriate generation provenance and a manual equivalent |

Avoid publishing contributor identities or content without approval. Track learning evidence through voluntary anonymous observations; analytics and external location upload require explicit consent. Do not add location history by default or imply that future sharing is end-to-end encrypted.

## Delivery and verification

Implement each resumed stage as small runnable changes with feature-matched behavioral tests, affected documentation and changelog updates. Record commands actually run and their outcomes in [verification status](../testing/STATUS.md). Each user-facing slice needs review of its actual screens and complete journey; compilation or renderer-ready output alone is insufficient. Keep browser, emulator/simulator and physical-device evidence separate. The user's phone permits ordinary ADB install/launch and narrow crash diagnostics only; the user verifies visible behavior unless they authorize another inspection method.

The first delivery excludes multiplayer, live positioning, real routes, arbitrary user scripting, AI dialogue, external generation, cloud accounts and automatic publishing. These can receive later proposals if feedback justifies them. This plan completes the written planning request; it does not claim any future stage has shipped or passed testing.
