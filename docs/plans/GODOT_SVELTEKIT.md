# Godot game and SvelteKit Studio plan

8 October 2026 · Direction agreed; implementation on hold pending resident input.

## Problem and experiment

Residents should be able to recognize their community, play short learning quests and contribute original rooms and characters. The next proposed experiment is one fictional room, one controllable avatar, one approved resident/classmate NPC, one conversation, one short learning/discovery quest and one visible reward. This tests whether playing and authoring are enjoyable before expanding to the campus.

**9 October scope amendment:** the user separately authorized work on the existing map, starting with reusable ground-floor assets. That map-only slice can proceed in Compose; the game and Studio work below remains on hold.

**Do not begin game/Studio implementation from this plan.** The user explicitly wants resident input first because it may change the PRD. Writing, reviewing, committing and pushing these planning documents does not authorize scaffolding, migration, dependency changes or implementation.

## Agreed direction and open choices

| Agreed | Provisional until feedback and revised PRD |
| --- | --- |
| Godot for the open-source game | Exact first quest, learning objective and reward |
| SvelteKit for browser Studio | Editor layout, roster and content workflow |
| Studio direction covers rooms, characters and quests | Exact schema, asset dimensions and numerical limits |
| Start with manual modular character creation | Photo-assisted generation, hosting and accounts |
| Preserve the current Compose/PixiJS prototypes during evaluation | Eventual replacement or integration with Compose |
| Android and iOS remain intended game targets | Web-game delivery, renderer and performance budgets |

The open-source learning platform has two intended audiences: players learn through playable quests; contributors learn through readable code, sample content, exercises and documentation. Neither a course catalogue nor a whole-campus RPG is required for the first experiment.

## Gate 0 — resident input before any implementation

1. Collect resident/classmate feedback on the useful learning moments, proposed quest, desired self-representation, editing needs and mobile use. Use voluntary, minimal notes; do not collect movement histories or require photos.
2. Record the input, unresolved disagreements and proposed scope changes. Separate a person's approval of their portrayal from permission to publish their assets or personal introduction.
3. Revise `docs/PRD.md`, this plan and the backlog together. Define the first learning outcome, success criteria, content owner, representation permissions and deferred features.
4. Obtain the user's explicit instruction to resume implementation against that revised scope. Resident feedback arriving by itself does not open this gate.

Until all four steps are complete, this plan permits only authorized planning/documentation work. The separately authorized existing-map asset slice may change map code/assets; it does not permit engine projects, Studio migration, gameplay or character creation.

## Intended architecture after the gate

Godot owns playable scenes, movement, fictional collision, interaction, dialogue, animation, bounded quest state and local progress. Begin with a standalone Godot project so its behavior can be assessed independently. Embedding it in Compose is not promised; replacement or integration needs a later decision supported by mobile evidence.

SvelteKit owns the browser authoring application: forms, navigation, validation feedback, asset selection and import/export. It is not the game renderer. The existing PixiJS room canvas may be reused as a client-only preview if useful; evaluate that after the gate rather than assuming SvelteKit requires a new canvas engine. Server-side rendering must not instantiate browser-only rendering code.

Keep modules organized around room authoring, character authoring, quests, content validation and game behavior. Pure validation and quest rules stay independent of UI, platform APIs and transport. Reuse fixtures and contract tests across consumers where practical; do not introduce shared layers without a concrete purpose.

The current Compose Plan World, Compose Studio preview, Vite/PixiJS editor and separate licensed splat viewer remain runnable references. Preserve their present behavior and provenance; the Godot selection does not describe a completed migration. Pick and pin compatible engine, export-template, framework and toolchain versions only when implementation starts.

## Browser framework alternative: Compose Multiplatform

Compose can share Kotlin UI across Android, iOS, desktop and web. It would be the stronger candidate if the goal were to deliver essentially the same map/editor interface on all those platforms with a Kotlin-focused team. It is a viable alternative, not excluded because it cannot run in a browser.

For this proposal, recommend SvelteKit: browser Studio has a different job from the Godot game, with forms, content editing, asset selection and learning pages. Svelte uses HTML/CSS components and SvelteKit supplies routing and rendering options. The current browser editor already uses JavaScript/PixiJS, so retaining its domain/canvas code is a concrete reuse opportunity. Moving browser UI to Compose would be a new port, not simply keeping the existing browser implementation.

The tradeoff is maintaining a web stack alongside Godot and, temporarily, the existing Kotlin prototype. Share portable content and conformance fixtures rather than promising a single UI or executable codebase. Do not create a new Kotlin bridge solely to reuse a small validator. Revisit this choice if resident feedback prioritizes the same native/browser editor, an offline native Studio, or a Kotlin-only contributor team. Accessibility, browser support and performance still require testing whichever framework is chosen. Browser gameplay, if later requested, is a separate Godot-export decision; SvelteKit does not replace the game engine.

Sources: [Compose shared UI](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-and-jetpack-compose.html), [SvelteKit introduction](https://svelte.dev/docs/kit/introduction). Framework capabilities are sourced; suitability for this project is an architectural judgment.

## Portable content boundary

Design a documented, versioned data package with stable IDs and explicit references between rooms, characters, quests and assets. The existing room JSON is a starting fixture, not already a complete game-content standard. Preserve its current consumers until an explicit compatibility/migration path exists.

- Rooms describe fictional decoration, spawn/interaction points and game collision separately from source-plan geometry. Any association with a real place is optional and requires approved mapping.
- Characters describe approved display name/introduction, modular appearance choices, portrait reference, directional idle/walk sprite references and optional quest roles.
- Quests use a small declarative set of objectives, dialogue choices, completion conditions and rewards. Content imports cannot introduce arbitrary scripts or executable engine resources.
- Assets carry stable references, type, dimensions/frame layout, provenance, licence and distribution permission. Specify palette, frame dimensions, directional coverage and foot pivots before producing animation sets.
- Define finite limits for package bytes, object counts, coordinates, text lengths, image dimensions, frames and quest transitions in the revised contract. Reject unsupported versions, duplicate IDs, dangling references, disallowed paths/types and out-of-range data.
- Validate in Studio for clear author feedback and independently in the Godot client before loading. An exported file is untrusted input; Studio validation does not replace runtime validation. Invalid imports must leave the last valid scene intact.
- Test valid round trips, supported migrations and rejected malformed/oversized packages against common fixtures. Save progress separately from authored content, with explicit version handling and reset behavior.

Source-plan pixels, approximate anchors and fictional collision remain separate datasets. Collision that makes a game walkable does not establish physical corridors, entrances, measured routes or step-free access. Retain the unplaced/uncertain directory entries and the source-grounding constraints of ADR 0006.

## Runnable slices after explicit resumption

### Slice 1 — one room from Studio to Godot

Problem: an author needs confidence that a saved room renders consistently in the game. Experiment: move bounded furniture, edit a sign, export a package and load that same fictional room in a standalone Godot scene.

Acceptance: the valid fixture preserves IDs and visible placement through export/import; invalid content reports useful errors without replacing the last valid room. Inspect the actual SvelteKit browser editor and Godot scene. Keep the existing prototypes runnable. Choose test-device and performance budgets with the revised PRD, then record a baseline instead of claiming unmeasured performance.

### Slice 2 — walk, meet one NPC and complete one learning quest

Problem: a room must support an understandable, enjoyable learning interaction. Experiment: one avatar walks to one approved NPC, reads a portrait-led conversation, completes one bounded task and receives one reward.

Acceptance: directional idle/walk animation, foot placement, layering and collision read correctly; keyboard and touch controls work; dialogue is readable; the quest cannot complete before its condition or grant duplicate rewards. Local save/reload and reset behave predictably. Observe participants explaining what they learned. Inspect named Android and iOS targets separately; record unsupported or unverified targets honestly.

### Slice 3 — manual character creator and reviewed quest authoring

Problem: residents need understandable control over their portrayal and learning contribution. Experiment: one person chooses modular appearance, edits their name/introduction, previews a portrait and every idle/walk direction, then reviews a bounded quest using supported templates.

Acceptance: the approved export produces the same character and quest in Godot; animation frames, proportions, palette and foot pivots remain consistent; bad asset/quest references are rejected. The person can correct or remove their portrayal before distribution. Keyboard-accessible forms and a usable alternative to canvas-only editing are demonstrated. A portrait alone does not satisfy animated-character acceptance.

### Slice 4 — expand only after evidence

Problem: a successful room may not generalize to a campus. Experiment: add a small approved set of rooms/characters and explicit fictional transitions before considering all five floors.

Acceptance: IDs and saves survive supported content updates; transitions and quest references validate; directory uncertainty remains visible; performance and controls meet the agreed budgets on named targets. Demonstrate the complete journey, including the map/search/legend role chosen in the revised PRD. Confirm rights and exact room assignments before presenting any real tenant as an editor of their actual room.

Each slice includes matching feature tests, updated docs/changelog and commands/results in `docs/testing/STATUS.md`. Rendered browser, emulator/simulator and physical-device observations are distinct. Builds alone are not visual evidence. On the user's phone, use only ordinary ADB installation/launch and narrowly scoped crash diagnostics; visible verification remains with the user unless another method is authorized.

## Learning, contribution and permissions

Ship a small fictional sample pack and a documented exercise with each useful capability: decorate/import a room, add an NPC animation set, then author a supported learning quest. Explain validation failures and provide contribution/review guidance so someone can learn and contribute without an AI subscription. Review contributor content for accuracy, consent and licensing before distribution.

Manual modular creation comes first. Optional photo-assisted AI generation is a later, separately scoped experiment requiring explicit consent, a chosen processing provider, clear upload/retention/deletion behavior and review of outputs. Keep source photos and unapproved personal material out of the public repository. Generated portraits must still be converted into coherent, reviewed animation assets before gameplay use.

The code's MIT licence does not automatically cover supplied plans, portraits, sprite packs, music or third-party scenes. Maintain per-asset provenance and distribution terms; artistic references are inspiration, not asset sources. Define how approved personal content is withdrawn from future distributed versions without promising erasure from copies already downloaded.

Accounts, cloud publishing, multiplayer, AI dialogue, external generation, analytics and real-world positioning are outside the first proposal. Keep local progress minimal and resettable; no location history by default, no external location upload without explicit consent, and no E2EE claim without implementation and review.

## References and decision

Technical direction is recorded in [ADR 0008](../adr/0008-godot-game-sveltekit-studio.md). These official references describe capabilities, not completed repository integration: [Godot 2D](https://docs.godotengine.org/en/stable/tutorials/2d/index.html), [Android export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_android.html), [iOS export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_ios.html), and [SvelteKit introduction](https://svelte.dev/docs/kit/introduction) (framework reference only). Reviewed 8 October 2026; recheck toolchain requirements when implementation is authorized.
