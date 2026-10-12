# Godot game and SvelteKit Studio plan

8 October 2026 · Direction agreed · Stage 1 resumed 12 October 2026

Updated 12 October 2026: the [community game implementation plan](COMMUNITY_GAME.md) develops the public idea into a play–learn–contribute journey, dependent stages, content ownership and contribution lessons. The user explicitly resumed implementation with Stage 1, beginning with the shared `rooms-v1` contract. No resident feedback has been collected; it may shape later stages but is not required for this fictional handoff. The separate Experimental 3D sample and existing Android app remain independent.

## Problem and experiment

Contributors should be able to author a bounded fictional room and open it in the standalone game. Stage 1 implements the portable handoff before gameplay: one room, one company sign, a small furniture set and a shared validator contract consumed independently by SvelteKit and Godot. A fictional open-source contribution challenge is the default next learning experiment if the user gives no other preference.

**9 October scope amendment:** the user separately authorized work on the existing map, starting with reusable ground-floor assets. That map work remains in Compose.

**12 October resumption:** the user explicitly authorized Stage 1 community implementation after the planning work. Resident feedback remains an optional input for later scope; none has been collected.

## Agreed direction and open choices

| Agreed | Later decisions and stages |
| --- | --- |
| Godot for the open-source game | Exact first quest, learning objective and reward |
| SvelteKit for browser Studio | Editor layout, roster and content workflow |
| Studio direction covers rooms, characters and quests | Exact schema, asset dimensions and numerical limits |
| Start with manual modular character creation | Photo-assisted generation, hosting and accounts |
| Preserve the current Compose/PixiJS prototypes during evaluation | Eventual replacement or integration with Compose |
| Android and iOS remain intended game targets | Web-game delivery, renderer and performance budgets |

The open-source learning platform has two intended audiences: players learn through playable quests; contributors learn through readable code, sample content, exercises and documentation. Neither a course catalogue nor a whole-campus RPG is required for the first experiment.

The proposed public experience includes discovering approved company/project introductions, making a character, editing a simple fictional space and completing quests, puzzles or learning challenges. Start with one supported challenge template and manual creation. The [community plan](COMMUNITY_GAME.md#learning-to-contribute-at-different-levels) makes coding, design, pixel art, storytelling and optional AI-assisted contribution paths explicit for beginners and experienced contributors.

## Stage 0 — optional discovery for later stages

1. Collect resident/classmate feedback on the useful learning moments, proposed quest, desired self-representation, editing needs and mobile use. Use voluntary, minimal notes; do not collect movement histories or require photos.
2. Record the input, unresolved disagreements and proposed scope changes. Separate a person's approval of their portrayal from permission to publish their assets or personal introduction.
3. Revise `docs/PRD.md`, this plan and the backlog together. Define the first learning outcome, success criteria, content owner, representation permissions and deferred features.
4. If findings materially change scope, agree with the user on the next slice before expanding beyond the already authorized Stage 1. Do not add personal or tenant content without its separate approvals.

This stage is optional for the fictional Stage 1 handoff. No resident feedback has been collected as of 12 October 2026. Use later findings to revise challenge choice, usability targets and product scope; do not treat feedback as authorization or as a prerequisite for the already resumed Stage 1.

## Intended architecture

Godot owns playable scenes, movement, fictional collision, interaction, dialogue, animation, bounded quest state and local progress. Begin with a standalone Godot project so its behavior can be assessed independently. Embedding it in Compose is not promised; replacement or integration needs a later decision supported by mobile evidence.

SvelteKit owns the browser authoring application: forms, navigation, validation feedback, asset selection and import/export. It is not the game renderer. The current Stage 1 Studio uses its own canvas preview. Server-side rendering must not instantiate browser-only rendering code.

Keep modules organized around room authoring, character authoring, quests, content validation and game behavior. Pure validation and quest rules stay independent of UI, platform APIs and transport. Reuse fixtures and contract tests across consumers where practical; do not introduce shared layers without a concrete purpose.

The current Compose Plan World, Compose Studio preview, Vite/PixiJS editor and separate licensed splat viewer remain separate runnable projects. Preserve their behavior and provenance; the Godot selection does not describe a completed migration. Stage 1 uses Godot 4.7.2 and SvelteKit 3.0.1. Mobile export templates and toolchains are not yet part of the claimed target.

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

### Slice 1 — contract-first room handoff (active)

Problem: an author needs confidence that a saved room renders consistently in the game. Experiment: use the frozen `content/community/v1/rooms-v1` contract to move bounded furniture, edit a sign, export a package and load that same fictional room in the standalone Godot desktop scene.

Acceptance: the valid fixture preserves IDs and visible placement through export/import; invalid content reports useful errors without replacing the last valid room. Both validators enforce the same contract against common valid/invalid fixtures. Inspect actual browser and Godot desktop screens. Keep the existing prototypes runnable. Record evidence for each target separately; do not claim mobile support based on the desktop preview.

### Slice 2 — fictional open-source contribution challenge

Default if the user gives no other preference: teach one small open-source contribution using fictional sample content. Problem: contributors need a visible first result and a clear way to understand review. Experiment: one avatar completes a bounded room/package editing task with a hint, retry, explanation and one reward.

Acceptance: animation, foot placement, layering, collision and keyboard controls read correctly on rendered desktop screens; touch and mobile targets are separately assessed before claims. The task cannot complete before its condition or grant duplicate rewards. Local save/reload and reset behave predictably. A player can explain the contribution step. Use no real tenant or resident content without approval.

### Slice 3 — manual character creator and reviewed quest authoring

Problem: residents need understandable control over their portrayal and learning contribution. Experiment: one person chooses modular appearance, edits their name/introduction, previews a portrait and every idle/walk direction, then reviews a bounded quest using supported templates.

Acceptance: the approved export produces the same character and quest in Godot; animation frames, proportions, palette and foot pivots remain consistent; bad asset/quest references are rejected. The person can correct or remove their portrayal before distribution. Keyboard-accessible forms and a usable alternative to canvas-only editing are demonstrated. A portrait alone does not satisfy animated-character acceptance.

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

Problem: the four-frame loop currently advances every160ms while the avatar travels at68 world pixels/second, and it continues when input is blocked. The user wants a gait with recognizable alternating contact/passing poses, using their supplied animation example as motion reference.

First runnable change: advance animation from actual collision-resolved displacement, with a32-world-pixel stride cycle, and reset to idle when no movement occurs. Keep input-based facing and existing room/Construct collision behavior. Verify cardinal directions, diagonal normalization, equal-distance results at different frame rates, obstacle stopping and scene/import resets; inspect the actual desktop animation.

Art experiment: check RetroDiffusion's reference-frame Walking workflow and the exact cost before spending. External generation uses only pre-paid credits after the user confirms the spending limit. The newly installed RetroDiffusion MCP has not yet appeared in this chat’s tool catalog; no generation or credit spend is claimed. Start with one neutral Architect sprite and one direction, review the native animated output for stable identity, bare wrists and planted-feet timing, then decide whether to generate remaining directions. Do not upload original photographs, buy additional credits or claim an animation succeeded from a static sheet. Preserve the current trusted frame contract until a reviewed animation establishes the required frame count.

## References and decision

Technical direction is recorded in [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md). Official references describe capabilities, not repository integration: [Godot 2D](https://docs.godotengine.org/en/stable/tutorials/2d/index.html), [Android export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html), [iOS export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_ios.html), and [SvelteKit introduction](https://svelte.dev/docs/kit/introduction). Godot mobile export remains unverified.
