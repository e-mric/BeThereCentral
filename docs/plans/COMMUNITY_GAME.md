# Community game implementation plan

12 October 2026 · Stage 1 implementation resumed · No resident feedback collected

## The idea and the first useful experiment

BeThereCentral should let people discover the BeCentral community through a pixel-art game: explore companies and projects, meet characters, complete small quests, puzzles and learning challenges, then contribute something themselves. A simple browser Studio should eventually let people shape a fictional space, create their character and author supported challenges. Learning happens both by playing and by helping build the project through coding, design, pixel art, AI-assisted work or storytelling. Beginners should have a complete small contribution they can make; experienced contributors should have clear review and extension paths.

The active first experiment is a bounded fictional room package authored in SvelteKit and opened in Godot, with independent validation at both ends. Once that handoff is established, the default next playable experiment is a small open-source contribution task using sample content. A broader explore–meet–learn–reward–contribute loop can follow evidence; the precise learning topic and project are not set, and this plan invents no tenant mission or personal biography.

## Current state and authorization boundary

| Current or in progress | Later scope |
| --- | --- |
| Compose Plan World: five schematic floors, directory/search and place guide | Standalone Godot gameplay, movement, dialogue, learning challenges and local progress |
| Authored reusable floor decoration in source; approximate source-plan anchors | Playable scenes with separately authored fictional collision and interactions |
| Separate local Vite/PixiJS Room Studio and transient Compose preview | Later SvelteKit authoring for characters and supported quests |
| Stage 1 `rooms-v1` package contract and fixtures | Further package features for characters, quests and progress |
| SvelteKit 3.0.1 fictional room editor and Godot 4.7.2 desktop preview in progress | Mobile game targets, gameplay, resident content and publishing |
| Open repository and development documentation | Guided contribution lessons and reviewed community content |

The user explicitly resumed the community implementation plan on 12 October with Stage 1. No resident feedback has been collected. This first slice uses a fictional sample and does not depend on resident participation. The [technical plan](GODOT_SVELTEKIT.md) and [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md) remain authoritative for architecture. The existing Room Studio fixture is a starting point, not a character/quest standard. The unrelated licensed 3D engine-room sample remains a separate Experimental viewer, not a BeCentral capture or evidence of game progress; its menu exposure and Android installation are independent.

The map remains in Compose. Later stages may use optional resident feedback to refine the experience, but feedback is not a gate for this authorized fictional implementation. Real-person portrayals, resident introductions, tenant facts, accounts and public publishing require their own approval and scope.

## Stage 0 — optional discovery for later stages

**Problem:** the public idea invites many kinds of participation but does not establish which experience residents actually want. **Experiment:** discuss the one-room loop with 5–8 consenting residents or classmates, including beginners and prospective content contributors. Use the existing map and room prototype as clearly labelled references, not as a playable-game demonstration.

Record whether people want to play, explain their project, portray themselves, decorate a space or contribute a particular skill; ask what they would want a visitor to learn. Capture anonymous task notes and disagreements. Photos, accounts and movement logs are unnecessary. Select one content owner, one learning outcome, one challenge format, a short completion-time target and intended test devices. Agree measurable usability and performance targets before implementation; do not infer success from this proposal.

**Use:** findings can refine the Stage 2 challenge, success measures and later content decisions. No feedback has been collected as of 12 October. A participant's portrayal approval remains separate from permission to distribute assets or introductions.

## Stage 1 — contract-first room handoff

**Status:** implementation resumed 12 October; the contract and both consumers are in the current slice. **Problem:** a contributor needs confidence that a simple space survives the handoff from editor to game. **Experiment:** one bounded fictional room in SvelteKit Studio exports a portable package consumed by a standalone Godot desktop preview. Use the existing licensed project atlas; retain Compose, Vite/PixiJS and the unrelated Experimental 3D sample. The current versions are SvelteKit 3.0.1 and Godot 4.7.2.

The `rooms-v1` envelope is frozen at seven fields: package version/ID/revision, declared features, trusted asset catalog, attribution and one room scene. The shared validator enforces a 65,536-byte limit, exact keys/feature version, safe IDs, text/geometry bounds, unique object IDs, the 24-object cap, and trusted local asset identifiers. JSON remains declarative; imported paths, URLs and executable resources are not loaded. The author can change a company sign, add/move/remove furniture, undo/redo, save locally and import/export. The room outline is fixed. Keyboard controls and numeric position fields provide alternatives to dragging; invalid loads leave the last valid room unchanged.

**Acceptance:** the same IDs, sign and placements survive Studio → package → Godot. Both consumers independently reject unsupported versions, duplicate IDs, unknown fields/features, malformed or oversized data, and untrusted asset references against common fixtures. Inspect actual browser and Godot screens; record desktop evidence and do not imply mobile support. A clean checkout can run the fictional sample from documented commands without a paid AI account. Provide a beginner lesson to decorate, export and explain one validation error.

## Stage 2 — default fictional open-source contribution challenge

**Default unless the user gives a different preference:** make the first challenge teach one small open-source contribution using fictional sample content, such as finding and correcting a bounded room/package issue, then previewing and explaining the change. This avoids requiring resident feedback, a real company project or a real-person portrayal. **Problem:** package authoring and review are easier to learn when tied to a visible outcome. **Experiment:** a Godot avatar follows a short fictional contribution task, receives a hint, completes the stated condition and sees one reward.

Offer retry and an explanation of the result. Keep dialogue and objectives declarative; do not execute player-supplied code. Resident, tenant or project-specific material requires later approval. Additional challenge templates follow evidence rather than being prerequisites.

**Acceptance:** keyboard movement, layering, collision and task dialogue are understandable on rendered desktop screens; touch/mobile targets are assessed separately before being claimed. Completion requires its stated condition; retries and save/reload cannot duplicate the reward. Local progress is versioned, minimal, resettable and separate from content. A player can explain the open-source contribution step after the task. Deliver a beginner lesson that changes a sample clue or room detail within the supported workflow.

## Stage 3 — create a character and author a challenge

**Depends on:** Stage 2's proven content model. **Problem:** people need control over their portrayal and an approachable way to contribute a learning activity. **Experiment:** one author uses manual modular character options and a supported quest template, previews the result, corrects it and exports it for the same room.

The character workflow includes display name, optional introduction, modular appearance, portrait and every directional idle/walk animation. A short quest form exposes learning objective, approved source/project, dialogue, hints, completion rule, explanation and reward. Include a playable preview or test handoff so an author can discover dead ends before review. Keep space editing within the tested room constraints.

**Acceptance:** character identity, palette, proportions, frame sizes and foot pivots are coherent across portrait and animation. The person can correct or remove their portrayal before distribution; a portrait alone does not pass. Studio exposes text-based alternatives to canvas-only work and useful reference/validation errors. Independent Godot validation rejects missing sprites, unsupported conditions and unreachable required steps. A novice completes one authored change using the lesson without needing to edit an engine script. Deliver pixel-art and design lessons using original, licensed sample parts.

## Stage 4 — review a small community content pack

**Depends on:** Stages 1–3 and participant evidence. **Problem:** content from several contributors needs understandable ownership, compatible versions and a way to correct mistakes. **Experiment:** maintainers review a small set of approved rooms, characters, project introductions and challenges, then assemble one versioned pack. Begin with local exports and repository review; a hosted editor/account system is not required.

**Acceptance:** every included asset and factual introduction has a source, owner and distribution basis; missing approval blocks inclusion. Validate references across rooms and fictional transitions. Demonstrate supported content updates, progress compatibility/reset behavior and rollback to a previous pack. Rehearse a correction or withdrawal: remove content from the next pack and maintained downloads, noting that already downloaded public copies cannot be recalled. Publishing the pack or contacting contributors still needs the appropriate user authorization.

Run the complete enter → discover → learn → reward → edit → export → review journey. Preserve the map/search/legend role chosen in the revised PRD, including uncertain and unplaced entries. Expand toward campus coverage only after this small pack meets the agreed interaction, accessibility and performance targets. Real room associations need confirmed mapping; fictional transitions never claim real routes. Hosted publishing, authorization and moderation require a separate scoped decision.

## Proposed content and trust boundary

The package and room records in `content/community/v1/` implement the Stage 1 subset: one room, a trusted local asset catalog and attribution. Character, quest, company/project and progress records below describe future contract additions. Define their bounds and compatibility only when the corresponding stage is scoped, with fixtures and independent consumer validation.

| Record | Minimum responsibility and validation |
| --- | --- |
| Package | Stage 1 v1 envelope has package/version IDs, features, asset catalog, attribution and one room; later versions may add reviewed records while preserving compatibility |
| Room | Stage 1 supports bounded decoration and a fixed fictional outline; future spawn points, collision and interactions remain separate from source-plan geometry |
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

## 12 October asset and playable-slice amendment

The user requested original coworking props, a Studio design reference pass and Easter eggs, including a hidden entrance to a white Construct room. The active next slice keeps v1 frozen and adds a separate v2 catalog and contract. The shared catalog contains sixteen existing props plus hacker workstation, arcade cabinet, yoga mat, conference screen, secret bookshelf, burgundy chair, CRT and debugging duck. These are fictional decorations, never assertions about real tenant equipment or entrances.

The browser Studio uses a responsive three-pane workspace inspired by the inspected Rive/Figma references: categorized thumbnails, central room canvas and editable properties. Manual character options and one supported learning template follow through the same validated export. The default activity teaches a first open-source contribution using two clues, three answers, a hint, an explanation and one reward. A v1 room can migrate to room-only v2; migration must not invent gameplay spawn points or personal profiles.

Godot validates v2 independently. Fictional movement uses six-pixel player clearance and trusted furniture footprints; a bounded four-pixel grid checks required interaction reachability. The hidden bookshelf opens only the bundled white Construct realm. A persistent Return action and Escape preserve room state, quest progress and a safe return point. The two burgundy chairs/CRT use original assets; no film footage, actors or dialogue are shipped. The debugging duck supplies a small built-in interaction. Content cannot specify scripts, resource paths or arbitrary destinations.

Before calling this slice complete, run the author → edit → export → play → inspect clues → answer/retry → reward → secret → return journey on actual browser and Godot screens. Verify undo/redo, local save/reload, rejected import, character facing/walk states, collision, progress reset and no duplicated rewards. Browser editing remains unverified until this interaction check succeeds. Desktop game evidence does not establish mobile support. No resident study has been conducted. Public portrayal permission for Ninon/Morgane was reported by the user, who also requested their own character as The Architect; photographs remain outside the repository and personal introductions empty.

## Current verification boundary

The fictional v2 development slice now includes original Founder/Patch presets as well as custom modular appearances, 24 props, character/activity authoring, independent Godot validation, local progress, clue journal and the white Construct. The recognizable existing founder identity is retained in new directional source art. Automated consumer/frame checks and the Godot desktop walkthrough are recorded in testing status. The actual Chrome room edit/save/reload, approved portrait selection, activity title edit and downloaded package→Godot handoff now have rendered evidence. Browser file-import UI and assistive-technology verification remain outstanding. Resident discovery and reviewed factual community introductions remain uncollected; the three authorized pixel portrayals are included separately.
