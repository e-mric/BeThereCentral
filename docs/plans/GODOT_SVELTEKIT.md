# Godot game and SvelteKit Studio plan

8 October 2026 · Direction agreed · Implementation status reconciled 16 October 2026

The [community game implementation plan](COMMUNITY_GAME.md) develops the public idea into a play–learn–contribute journey, dependent stages, content ownership and contribution lessons. **Implemented:** frozen `rooms-v1` remains supported; the separate v2 adds room, manual/trusted-preset character and one-activity authoring, with Godot desktop gameplay. Core v2 delivery is commit [`228baff`](https://github.com/e-mric/BeThereCentral/commit/228baff); its browser and desktop evidence is in the [12 October status record](../testing/STATUS.md#community-studio-and-godot-slice--12-october-2026). Android provides an offline Studio authoring host only. **Evidence missing:** native Android invalid-file import, accessibility review, final complete four-direction Godot animation walkthrough, and resident/learner study. The separate Experimental 3D sample and Compose Plan World remain independent.

## Problem and experiment

The original experiment was a bounded fictional room authored in Studio and opened in the standalone game. The frozen v1 handoff and its consumers remain supported. The delivered separate v2 adds room, character and one activity authoring plus desktop gameplay. Contribution Club implements the original fictional open-source learning-task direction; learner outcomes remain unobserved.

**9 October scope amendment:** the user separately authorized work on the existing map, starting with reusable ground-floor assets. That map work remains in Compose.

**12 October resumption:** the user explicitly authorized Stage 1 community implementation after the planning work. Resident feedback remains an optional input for later scope; none has been collected.

## Selected direction and remaining scope

| Selected or implemented | Still future or unverified |
| --- | --- |
| Godot 4.7.2 desktop game and SvelteKit 3.0.1 Studio | Mobile Godot export, gameplay and performance |
| v1 frozen handoff; separate v2 rooms, manual/trusted-preset characters and one activity | Additional character/activity formats and reviewed factual introductions |
| Canvas-first browser workshop and Android offline Studio host | Native Android invalid-file import and accessibility evidence |
| Compose/PixiJS prototypes preserved | Eventual replacement/integration with Compose and web-game delivery |
| Desktop game behavior and local progress | Final complete four-direction animation walkthrough and learner outcome study |

The open-source learning platform has two intended audiences: players learn through playable quests; contributors learn through readable code, sample content, exercises and documentation. Neither a course catalogue nor a whole-campus RPG is required for the first experiment.

The proposed public experience includes discovering approved company/project introductions, making a character, editing a simple fictional space and completing quests, puzzles or learning challenges. Start with one supported challenge template and manual creation. The [community plan](COMMUNITY_GAME.md#learning-to-contribute-at-different-levels) makes coding, design, pixel art, storytelling and optional AI-assisted contribution paths explicit for beginners and experienced contributors.

## Stage 0 — optional discovery for later stages

1. Collect resident/classmate feedback on the useful learning moments, proposed quest, desired self-representation, editing needs and mobile use. Use voluntary, minimal notes; do not collect movement histories or require photos.
2. Record the input, unresolved disagreements and proposed scope changes. Separate a person's approval of their portrayal from permission to publish their assets or personal introduction.
3. Revise `docs/PRD.md`, this plan and the backlog together. Define the first learning outcome, success criteria, content owner, representation permissions and deferred features.
4. If findings materially change scope, agree with the user on the next slice before further expansion beyond the delivered fictional v1/v2 scope. Do not add personal or tenant content without its separate approvals.

This stage is optional for the fictional Stage 1 handoff. No resident feedback has been collected as of 12 October 2026. Use later findings to revise challenge choice, usability targets and product scope; do not treat feedback as authorization or as a prerequisite for the already resumed Stage 1.

## Intended architecture

Godot owns playable scenes, movement, fictional collision, interaction, dialogue, animation, bounded quest state and local progress. Begin with a standalone Godot project so its behavior can be assessed independently. Embedding it in Compose is not promised; replacement or integration needs a later decision supported by mobile evidence.

SvelteKit owns the browser authoring application: forms, navigation, validation feedback, asset selection and import/export. It is not the game renderer. The current Stage 1 Studio uses its own canvas preview. Server-side rendering must not instantiate browser-only rendering code.

Keep modules organized around room authoring, character authoring, quests, content validation and game behavior. Pure validation and quest rules stay independent of UI, platform APIs and transport. Reuse fixtures and contract tests across consumers where practical; do not introduce shared layers without a concrete purpose.

The current Compose Plan World, Compose Studio preview, Vite/PixiJS editor and separate licensed splat viewer remain separate runnable projects. Preserve their behavior and provenance; Godot is a separate desktop game, not a completed migration of Plan World. Stage 1 uses Godot 4.7.2 and SvelteKit 3.0.1. Android offline Studio hosting is implemented and does not use a Godot mobile export. Godot mobile export templates and toolchains are not part of the claimed target.

## Browser framework alternative: Compose Multiplatform

Compose can share Kotlin UI across Android, iOS, desktop and web. It would be the stronger candidate if the goal were to deliver essentially the same map/editor interface on all those platforms with a Kotlin-focused team. It is a viable alternative, not excluded because it cannot run in a browser.

For this proposal, recommend SvelteKit: browser Studio has a different job from the Godot game, with forms, content editing, asset selection and learning pages. Svelte uses HTML/CSS components and SvelteKit supplies routing and rendering options. The current browser editor already uses JavaScript/PixiJS, so retaining its domain/canvas code is a concrete reuse opportunity. Moving browser UI to Compose would be a new port, not simply keeping the existing browser implementation.

The tradeoff is maintaining a web stack alongside Godot and, temporarily, the existing Kotlin prototype. Share portable content and conformance fixtures rather than promising a single UI or executable codebase. Do not create a new Kotlin bridge solely to reuse a small validator. Revisit this choice if resident feedback prioritizes the same native/browser editor, an offline native Studio, or a Kotlin-only contributor team. Accessibility, browser support and performance still require testing whichever framework is chosen. Browser gameplay, if later requested, is a separate Godot-export decision; SvelteKit does not replace the game engine.

Sources: [Compose shared UI](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-and-jetpack-compose.html), [SvelteKit introduction](https://svelte.dev/docs/kit/introduction). Framework capabilities are sourced; suitability for this project is an architectural judgment. The selected [Mobbin references](../design/MOBBIN_REFERENCES.md) guide original UI patterns and are not source assets.

## Portable content boundary

The Stage 1 contract is implemented and documented in [`content/community/v1`](../../content/community/v1/README.md). It carries one bounded fictional room with stable IDs, attribution and references into a pinned local asset catalog. It is not the future full game-content standard. Extend it only with a versioned compatibility plan and fixtures for each consumer.

- Rooms describe fictional decoration, spawn/interaction points and game collision separately from source-plan geometry. Any association with a real place is optional and requires approved mapping.
- Characters describe approved display name/introduction, modular appearance choices, portrait reference, directional idle/walk sprite references and optional quest roles.
- Quests use a small declarative set of objectives, dialogue choices, completion conditions and rewards. Content imports cannot introduce arbitrary scripts or executable engine resources.
- Assets carry stable references, type, dimensions/frame layout, provenance, licence and distribution permission. Specify palette, frame dimensions, directional coverage and foot pivots before producing animation sets.
- The current contract limits input to 65,536 UTF-8 bytes and room furniture to 24 objects, with bounded text/geometry and safe IDs. Future package features must add explicit limits for image dimensions, animation frames and quest transitions before those features are accepted. Reject unsupported versions, duplicate IDs, unknown fields/features, untrusted asset references and out-of-range data.
- Validate in Studio for clear author feedback and independently in the Godot client before loading. An exported file is untrusted input; Studio validation does not replace runtime validation. Invalid imports must leave the last valid scene intact.
- Test valid round trips, supported migrations and rejected malformed/oversized packages against common fixtures. Save progress separately from authored content, with explicit version handling and reset behavior.

Source-plan pixels, approximate anchors and fictional collision remain separate datasets. Collision that makes a game walkable does not establish physical corridors, entrances, measured routes or step-free access. Retain the unplaced/uncertain directory entries and the source-grounding constraints of ADR 0006.

## Runnable slices

### Slice 1 — contract-first room handoff (v1 frozen; consumers retained)

Problem: an author needs confidence that a saved room renders consistently in the game. Experiment: use the frozen `content/community/v1/rooms-v1` contract to move bounded furniture, edit a sign, export a package and load that same fictional room in the standalone Godot desktop scene.

Delivered: the frozen v1 room package and its browser/Godot consumers remain in the repository; separate v2 capability is documented in the community plan. Verification is recorded in dated status entries. A2 still owns fresh clean-checkout cross-consumer CI coverage; native Android invalid-import and accessibility checks are separate open evidence. Do not label the whole later game/learning work complete or derive mobile support from the desktop preview.

### Slice 2 — fictional learning activity (implemented; learning outcome unvalidated)

Default if the user gives no other preference: teach one small open-source contribution using fictional sample content. Problem: contributors need a visible first result and a clear way to understand review. Experiment: one avatar completes a bounded room/package editing task with a hint, retry, explanation and one reward.

Implemented: one fictional two-clue/three-answer activity with hint, retry, explanation and a single local reward, plus desktop movement and progress behavior. A contribution result or a working game does not prove a learner can explain the intended step. No learner study has been conducted; animation's final complete four-direction visual walkthrough remains pending. Mobile gameplay is not claimed.

### Slice 3 — character and activity authoring (implemented; human acceptance future)

Problem: residents need understandable control over their portrayal and learning contribution. Experiment: one person chooses modular appearance, edits their name/introduction, previews a portrait and every idle/walk direction, then reviews a bounded quest using supported templates.

Implemented: Studio exports manual modular appearances and trusted presets with the single supported activity; Godot independently consumes bounded package content. Human evidence missing: learner usability, accessibility review and represented-person correction/withdrawal walkthrough have not been conducted. The final complete four-direction animation observation is also open. A portrait alone does not satisfy animated-character acceptance.

### Slice 4 — expand only after evidence

Problem: a successful room may not generalize to a campus. Experiment: add a small approved set of rooms/characters and explicit fictional transitions before considering all five floors.

Acceptance: IDs and saves survive supported content updates; transitions and quest references validate; directory uncertainty remains visible; performance and controls meet the agreed budgets on named targets. Demonstrate the complete journey, including the map/search/legend role chosen in the revised PRD. Confirm rights and exact room assignments before presenting any real tenant as an editor of their actual room.

Begin this expansion with a small reviewed community content pack, as [Stage 4 of the community plan](COMMUNITY_GAME.md#stage-4--review-a-small-community-content-pack) describes. Prove content correction, withdrawal from future releases and pack rollback through local export/repository review before separately scoping hosted publishing or accounts.

Each slice includes matching feature tests, updated docs/changelog and commands/results in `docs/testing/STATUS.md`. Rendered browser, emulator/simulator and physical-device observations are distinct. Builds alone are not visual evidence. On the user's phone, use only ordinary ADB installation/launch and narrowly scoped crash diagnostics; visible verification remains with the user unless another method is authorized.

## Learning, contribution and permissions

Ship a small fictional sample pack and a documented exercise with each useful capability: decorate/import a room, add an NPC animation set, then author a supported learning quest. Explain validation failures and provide contribution/review guidance so someone can learn and contribute without an AI subscription. Review contributor content for accuracy, consent and licensing before distribution.

Manual modular creation comes first. Optional photo-assisted AI generation is a later, separately scoped experiment requiring explicit consent, a chosen processing provider, clear upload/retention/deletion behavior and review of outputs. Keep source photos and unapproved personal material out of the public repository. Generated portraits must still be converted into coherent, reviewed animation assets before gameplay use.

The code's MIT licence does not automatically cover supplied plans, portraits, sprite packs, music or third-party scenes. Maintain per-asset provenance and distribution terms; artistic references are inspiration, not asset sources. The [Mobbin references](../design/MOBBIN_REFERENCES.md) guide original UI patterns only; do not copy their screenshots or third-party sprites into the asset pack. Define how approved personal content is withdrawn from future distributed versions without promising erasure from copies already downloaded.

Accounts, cloud publishing, multiplayer, AI dialogue, external generation, analytics and real-world positioning are outside the first proposal. Keep local progress minimal and resettable; no location history by default, no external location upload without explicit consent, and no E2EE claim without implementation and review.

## Walking refinement — 12 October 2026

Historical problem: the original four-frame loop advanced every 160ms while the avatar travelled at 68 world pixels/second, and continued when input was blocked. The user wants a gait with recognizable alternating contact/passing poses, using their supplied animation example as motion reference.

Implemented first change: advance animation from actual collision-resolved displacement, with a32-world-pixel stride cycle, and reset to idle when no movement occurs. Keep input-based facing and existing room/Construct collision behavior. Verify cardinal directions, diagonal normalization, equal-distance results at different frame rates, obstacle stopping and scene/import resets; inspect the actual desktop animation.

The original gait issue was resolved: delivered changes tie pose advancement to collision-resolved movement, with a 32-world-pixel cycle; four-pose directions advance every 8 pixels ([`6d9e56a`](https://github.com/e-mric/BeThereCentral/commit/6d9e56a)); reviewed mixed directional art and frame checks followed ([`6bff0fe`](https://github.com/e-mric/BeThereCentral/commit/6bff0fe)). The current Architect front/left/right directions have eight walking poses, advancing every 4 world pixels; back retains four poses, every 8 pixels. All complete a 32-pixel cycle. The accepted front/left frames, mirrored right, current idle and back-walk art are packed into the trusted catalog. The final complete four-direction native-animation walkthrough was not completed; see the [12 October walking delivery record](../testing/STATUS.md#retrodiffusion-walking-delivery--12-october-2026). Preserve the 32-world-pixel Godot gait and 640ms Studio preview. Do not infer visual acceptance from static sheets or hashes.

## References and decision

Technical direction is recorded in [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md). Official references describe capabilities, not repository integration: [Godot 2D](https://docs.godotengine.org/en/stable/tutorials/2d/index.html), [Android export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html), [iOS export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_ios.html), and [SvelteKit introduction](https://svelte.dev/docs/kit/introduction). Godot mobile export remains unverified.
