# Community game implementation plan

16 October 2026 · Fictional v1/v2 implementation delivered · No resident or learner study conducted

## The idea and the first useful experiment

BeThereCentral aims to let people discover the BeCentral community through a pixel-art game: explore companies and projects, meet characters, complete small quests, puzzles and learning challenges, then contribute something themselves. Community Studio now lets people shape a fictional room, create manual or preset characters and author one supported activity. Broader challenges and reviewed project introductions remain future scope. Learning can happen through play and through contributing code, design, pixel art or storytelling; the existing lesson has not been validated with learners.

The frozen v1 room handoff remains supported. The delivered separate v2 adds fictional room authoring, manual and trusted-preset characters, and one two-clue/three-answer activity, consumed by the standalone Godot desktop game. Android hosts Community Studio for offline authoring; it does not run Godot gameplay. The 13 October canvas-first workshop is recorded in its completed [implementation plan](STUDIO_WORKSHOP.md). The activity and game code do not establish a learner outcome: no resident or learner study has been conducted.

## Current state and authorization boundary

| Implemented capability | Evidence still missing or future scope |
| --- | --- |
| Compose Plan World: five schematic floors, directory/search and place guide | Plan World user research and later verified accessibility/building data |
| Authored reusable floor decoration in source; approximate source-plan anchors | Verified building survey, real-room associations or routes |
| Separate local Vite/PixiJS Room Studio and transient Compose preview | These remain separate from the delivered SvelteKit Studio and Godot projects |
| Frozen Stage 1 `rooms-v1` contract and fixtures; separate v2 package for current content | Further package features and reviewed community content |
| SvelteKit fictional room/character/activity authoring; Godot 4.7.2 desktop gameplay and local progress | Mobile Godot targets, native invalid-import/accessibility evidence, final four-direction visual walkthrough, learner study and publishing |
| Android offline SvelteKit authoring host, checked on emulator and user-confirmed on phone | Android Godot gameplay; this is not part of the Studio host |
| Open repository, development documentation and the first fictional contribution lesson | Observed learner outcomes and reviewed community content |

The user explicitly resumed the community implementation on 12 October with Stage 1; subsequent bounded v2 and Android offline Studio work is also delivered. No resident or learner study has been conducted. This work uses fictional content and does not depend on participation. The [technical plan](GODOT_SVELTEKIT.md) and [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md) remain authoritative for architecture. The existing Room Studio fixture remains a separate prototype. The unrelated licensed 3D engine-room sample is a separate Experimental viewer, not a BeCentral capture or game progress; its menu exposure and Android installation are independent.

The map remains in Compose. Later stages may use optional resident feedback to refine the experience, but feedback is not a gate for this authorized fictional implementation. Real-person portrayals, resident introductions, tenant facts, accounts and public publishing require their own approval and scope.

## Stage 0 — optional discovery for later stages

**Problem:** the public idea invites many kinds of participation but does not establish which experience residents actually want. **Experiment:** discuss the one-room loop with 5–8 consenting residents or classmates, including beginners and prospective content contributors. Use the delivered map, Studio and standalone fictional Godot room as clearly labelled development references; none is a validated campus service.

Record whether people want to play, explain their project, portray themselves, decorate a space or contribute a particular skill; ask what they would want a visitor to learn. Capture anonymous task notes and disagreements. Photos, accounts and movement logs are unnecessary. Select one content owner, one learning outcome, one challenge format, a short completion-time target and intended test devices. Agree measurable usability and performance targets before further expansion; do not infer success from this proposal.

**Use:** findings can refine the Stage 2 challenge, success measures and later content decisions. No resident or learner feedback has been supplied as of 16 October. A participant's portrayal approval remains separate from permission to distribute assets or introductions.

## Stage 1 — contract-first room handoff

**Implemented:** the `rooms-v1` contract is frozen and both consumers retain it; the later v2 is a separate package. Browser authoring/export and Godot desktop consumption have rendered evidence. **Evidence still missing:** a fresh clean-checkout conformance gate is A2; this plan does not treat code presence as completion of all Stage 1 acceptance. **Future:** mobile Godot and production publishing remain out of scope. The original experiment used SvelteKit Studio and a standalone Godot desktop preview with fictional content.

The `rooms-v1` envelope is frozen at seven fields: package version/ID/revision, declared features, trusted asset catalog, attribution and one room scene. The shared validator enforces a 65,536-byte limit, exact keys/feature version, safe IDs, text/geometry bounds, unique object IDs, the 24-object cap, and trusted local asset identifiers. JSON remains declarative; imported paths, URLs and executable resources are not loaded. The author can change a company sign, add/move/remove furniture, undo/redo, save locally and import/export. The room outline is fixed. Keyboard controls and numeric position fields provide alternatives to dragging; invalid loads leave the last valid room unchanged.

**Acceptance:** the same IDs, sign and placements survive Studio → package → Godot. Both consumers independently reject unsupported versions, duplicate IDs, unknown fields/features, malformed or oversized data, and untrusted asset references against common fixtures. Inspect actual browser and Godot screens; record desktop evidence and do not imply mobile support. A clean checkout can run the fictional sample from documented commands without a paid AI account. Provide a beginner lesson to decorate, export and explain one validation error.

## Stage 2 — default fictional open-source contribution challenge

**Implemented capability:** the fictional Contribution Club activity is authored in Studio and playable in the Godot desktop game, with two clues, three answers, a hint, retry, explanation and one local reward. It is one bounded activity, not a validated learning outcome or a completed learning stage. **Evidence still missing:** no learner has been observed completing and explaining the intended contribution. The lesson remains useful for a voluntary future walkthrough.

Offer retry and an explanation of the result. Keep dialogue and objectives declarative; do not execute player-supplied code. Resident, tenant or project-specific material requires later approval. Additional challenge templates follow evidence rather than being prerequisites.

**Acceptance:** keyboard movement, layering, collision and task dialogue are understandable on rendered desktop screens; touch/mobile targets are assessed separately before being claimed. Completion requires its stated condition; retries and save/reload cannot duplicate the reward. Local progress is versioned, minimal, resettable and separate from content. A player can explain the open-source contribution step after the task. Deliver a beginner lesson that changes a sample clue or room detail within the supported workflow.

## Stage 3 — create a character and author a challenge

**Implemented capability:** Studio supports manual modular appearances, trusted presets, character previews and authoring of the single supported activity; Godot independently consumes the package. **Evidence still missing:** no learner study or represented-person review session is recorded here; technical preview does not prove people can author successfully. Further character/activity formats and reviewed personal introductions remain future scope.

The current authoring workflow supports manual modular appearance and trusted presets, plus the single bounded activity. Its technical preview does not establish that a person can complete authoring or review their portrayal successfully. Further activity forms, factual introductions and reviewed community content remain future scope. Keep future space editing within the tested room constraints.

**Acceptance:** character identity, palette, proportions, frame sizes and foot pivots are coherent across portrait and animation. The person can correct or remove their portrayal before distribution; a portrait alone does not pass. Studio exposes text-based alternatives to canvas-only work and useful reference/validation errors. Independent Godot validation rejects missing sprites, unsupported conditions and unreachable required steps. A novice completes one authored change using the lesson without needing to edit an engine script. Deliver pixel-art and design lessons using original, licensed sample parts.

## Stage 4 — review a small community content pack

**Depends on:** Stages 1–3 and participant evidence. **Problem:** content from several contributors needs understandable ownership, compatible versions and a way to correct mistakes. **Experiment:** maintainers review a small set of approved rooms, characters, project introductions and challenges, then assemble one versioned pack. Begin with local exports and repository review; a hosted editor/account system is not required.

**Acceptance:** every included asset and factual introduction has a source, owner and distribution basis; missing approval blocks inclusion. Validate references across rooms and fictional transitions. Demonstrate supported content updates, progress compatibility/reset behavior and rollback to a previous pack. Rehearse a correction or withdrawal: remove content from the next pack and maintained downloads, noting that already downloaded public copies cannot be recalled. Publishing the pack or contacting contributors still needs the appropriate user authorization.

Run the complete enter → discover → learn → reward → edit → export → review journey. Preserve the map/search/legend role chosen in the revised PRD, including uncertain and unplaced entries. Expand toward campus coverage only after this small pack meets the agreed interaction, accessibility and performance targets. Real room associations need confirmed mapping; fictional transitions never claim real routes. Hosted publishing, authorization and moderation require a separate scoped decision.

## Proposed content and trust boundary

The frozen package in `content/community/v1/` implements its original subset: one room, a trusted local asset catalog and attribution. The separate v2 package adds bounded room, manual/trusted-preset character, one activity and gameplay references under its own contract; Godot consumes these in the standalone desktop game and stores progress separately. Company/project introductions and additional character/activity formats remain future scope. Define further bounds and compatibility only as those features are scoped, with fixtures and independent consumer validation.

| Record | Minimum responsibility and validation |
| --- | --- |
| Package | Frozen v1 envelope has package/version IDs, features, asset catalog, attribution and one room; separate v2 adds implemented room, character, activity and gameplay records; saved progress is not part of the content package |
| Room | v1 supports bounded decoration and a fixed fictional outline; v2 supports fictional gameplay spawn points, collision and interactions separately from source-plan geometry |
| Company/project introduction | Stable identity, supplied description, attributed source, content owner and review date/status; do not infer current occupancy or an endorsement from a plan name |
| Character | v2 supports manual modular appearances and trusted presets with portrait/directional art; broader biography, introductions and review workflow remain future work |
| Activity | v2 supports one two-clue/three-answer activity with hint, explanation and one local reward; learner outcome is unobserved and no arbitrary scripts are allowed |
| Asset | Creator, source, licence, modification/generation notes, distribution permission and technical layout; code's MIT licence is not an asset licence |
| Local progress | Godot stores bounded activity state separately from authored packages, scoped to quest content/revision with earned rewards and reset. No movement history or physical position; broader pack migration remains future work. |

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

The user requested original coworking props, a Studio design reference pass and Easter eggs, including a hidden entrance to a white Construct room. At that point, the active slice kept v1 frozen and added a separate v2 catalog and contract. This slice is implemented. The shared catalog contains sixteen existing props plus hacker workstation, arcade cabinet, yoga mat, conference screen, secret bookshelf, burgundy chair, CRT and debugging duck. These are fictional decorations, never assertions about real tenant equipment or entrances.

The delivered browser Studio uses the canvas-first workshop described in [STUDIO_WORKSHOP.md](STUDIO_WORKSHOP.md), informed by the inspected Rive/Figma references. It has categorized thumbnails, a central room canvas and editable properties. Manual character options and the single supported activity use the validated v2 export. The fictional activity uses two clues, three answers, a hint, an explanation and one reward. A v1 room migrates to room-only v2; migration does not invent gameplay spawn points or personal profiles.

Godot validates v2 independently. Fictional movement uses six-pixel player clearance and trusted furniture footprints; a bounded four-pixel grid checks required interaction reachability. The hidden bookshelf opens only the bundled white Construct realm. A persistent Return action and Escape preserve room state, quest progress and a safe return point. The two burgundy chairs/CRT use original assets; no film footage, actors or dialogue are shipped. The debugging duck supplies a small built-in interaction. Content cannot specify scripts, resource paths or arbitrary destinations.

The 12 October acceptance plan called for an author → edit → export → play → inspect clues → answer/retry → reward → secret → return walkthrough, plus import, animation, collision and progress checks. The later 13 October browser and Android workshop observations supersede the statement that browser editing was wholly unverified: the rendered browser record includes checked and rejected import, while Godot desktop gameplay evidence remains separately recorded. The complete final four-direction animation walkthrough is still pending, as are native Android rejected-file import and accessibility checks; see current [verification status](../testing/STATUS.md). Desktop evidence does not establish mobile Godot support. No resident or learner study has been conducted. Public portrayal permission for Ninon/Morgane was reported by the user, who also requested their own character as The Architect; photographs remain outside the repository and personal introductions empty.

## Current implementation, evidence and future scope

**Implemented:** v1 remains frozen and supported. Separate v2 includes original Founder/Patch presets, custom modular appearances, 24 props, character/activity authoring, independent Godot validation, local progress, clue journal and the white Construct. The recognizable existing founder identity is retained in new directional source art. Core v2 delivery is commit [`228baff`](https://github.com/e-mric/BeThereCentral/commit/228baff). Automated consumer/frame checks, Godot desktop gameplay, browser authoring/save/reload, portrait selection, activity title edit and download→Godot handoff are recorded in the [12 October evidence](../testing/STATUS.md#community-studio-and-godot-slice--12-october-2026); checked/rejected browser import is in the [13 October workshop evidence](../testing/STATUS.md#community-studio-workshop-redesign--13-october-2026).

**Evidence missing:** the final complete four-direction Godot animation walkthrough is pending (see the [walking delivery record](../testing/STATUS.md#retrodiffusion-walking-delivery--12-october-2026)); Android native rejected-file import and accessibility review are pending (see [Android Studio](../testing/STATUS.md#android-offline-community-studio--13-october-2026) and the [workshop record](../testing/STATUS.md#community-studio-workshop-redesign--13-october-2026)). Emulator checks, browser checks and user-confirmed physical-phone appearance are separate evidence. No resident discovery, learner outcome or reviewed factual community introductions are claimed.

**Future:** learner walkthrough, mobile Godot, reviewed community content, factual introductions, accounts and publishing require separate evidence and scope. The three reported-approved pixel portrayals are included; original photographs remain outside the repository and personal introductions are empty.
