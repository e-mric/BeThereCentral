# ADR 0008: Godot game and SvelteKit browser Studio

Status: accepted technical direction · 8 October 2026 · Implementation on hold

## Context

The user selected Godot for an open-source community game and SvelteKit for its browser Studio. The intended learning platform includes playable learning quests and documentation, sample exercises and contribution paths for people learning to build it. The current implementation remains a Compose Plan World and a separate local Vite/PixiJS one-room editor with a Compose preview.

Resident/classmate input may change the PRD. The user explicitly requested a written plan, commit and push, and required waiting for that input before any implementation. Accepting the technology direction is not permission to scaffold or migrate.

## Decision

Use Godot for the future playable runtime and SvelteKit for browser authoring of rooms, characters and quests. Begin, once authorized, with a standalone Godot experiment consuming a portable versioned content package. Keep the current Compose/PixiJS prototypes runnable during evaluation. No Compose embedding, completed replacement or web-game delivery is promised. Android and iOS remain intended targets, requiring their own export, interaction and performance evidence.

SvelteKit owns the editor application, not necessarily its canvas renderer. Existing PixiJS may remain a client-only room preview if evaluation supports reuse. Choose and pin exact compatible versions at implementation start.

Use stable room, character, quest and asset IDs with a documented data contract and explicit limits. Validate independently in Studio and the Godot client. Imports are declarative content, never arbitrary scripts. Keep source-floor geometry and approximate directory anchors separate from fictional game collision and interactions; playable spaces are not surveyed navigation.

Start with manual modular character creation, consistent portrait and directional idle/walk assets, and a person's review of their portrayal. Photo-assisted AI generation remains optional later work with explicit consent and defined data handling. Keep private source photos out of the repository, and track asset rights separately from the code's MIT licence.

The proposed sequence is one-room import, then walking/NPC/one learning quest and reward, then character/quest authoring, then evidence-led campus expansion. Exact scope and acceptance criteria remain subject to the revised PRD; see the [phased plan](../plans/GODOT_SVELTEKIT.md).

## Alternative considered: Compose for browser Studio

Compose offers shared Kotlin UI across mobile, desktop and web and is viable if a common interface becomes the priority. SvelteKit is preferred here because authoring is a distinct browser workflow and the existing browser editor already uses JavaScript/PixiJS. Choosing Compose for it would require a port; Godot gameplay would still be separate. The cost of SvelteKit is an additional stack and independent consumer validation. Revisit if resident input favors a shared native/browser editor or a Kotlin-focused contribution model. See the [comparison and sources](../plans/GODOT_SVELTEKIT.md#browser-framework-alternative-compose-multiplatform).

## Execution gate

Before any implementation: gather resident input, record findings, revise the PRD and plan, and receive the user's explicit instruction to resume against that scope. Feedback arriving, this ADR being accepted, or planning documents being committed/pushed does not open the gate. No code, configuration, dependencies, scaffolding or production-asset work is authorized by this decision alone.

## Consequences

Game behavior and browser authoring can evolve independently through a tested content boundary. This introduces two consumers whose validation and rendering must be checked rather than assumed equivalent. Small runnable slices must demonstrate actual screens and the complete journey; browser, emulator/simulator and physical-device evidence remain separate.

ADR 0006 continues to govern the current Plan World and its source uncertainty; ADR 0007 continues to describe the implemented one-room experiment. This ADR supersedes the earlier recommendation merely to evaluate Godot as the selected future direction, while leaving those current behaviors intact. A later integration/replacement decision needs resident evidence and mobile validation.

Official framework references: [Godot 2D](https://docs.godotengine.org/en/stable/tutorials/2d/index.html) and [SvelteKit introduction](https://svelte.dev/docs/kit/introduction). These describe technology capabilities, not implemented features.
