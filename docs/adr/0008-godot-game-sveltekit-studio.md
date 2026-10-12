# ADR 0008: Godot game and SvelteKit browser Studio

Status: accepted technical direction · 8 October 2026 · Stage 1 implementation resumed 12 October 2026

## Context

The user selected Godot for an open-source community game and SvelteKit for its browser Studio. The intended learning platform includes playable learning quests and documentation, sample exercises and contribution paths for people learning to build it. Compose Plan World and the original Vite/PixiJS one-room editor with Compose preview remain separate existing experiences. Stage 1 now adds a contract-first Community Studio and standalone Godot desktop preview using fictional content.

Resident/classmate input may change later product scope. On 12 October 2026, after the written plan, the user explicitly resumed implementation with a bounded Stage 1 contract-first fictional room handoff. No resident feedback has been collected. The original feedback gate is superseded for this fictional slice; consent and source review remain required before using real people, projects or tenant facts.

## Decision

Use Godot for the playable runtime and SvelteKit for browser authoring of rooms, characters and quests. Begin with a contract-first standalone Godot preview consuming the portable v1 room package and a SvelteKit room editor that produces it. Pin the current experiment to Godot 4.7.2 and SvelteKit 3.0.1. Keep the current Compose/PixiJS prototypes runnable. No Compose embedding, completed replacement or web-game delivery is promised. Android and iOS remain later targets requiring their own export, interaction and performance evidence.

SvelteKit owns the editor application, not the game renderer. The current editor uses its own canvas preview. Stage 1 pins SvelteKit 3.0.1 and Godot 4.7.2; mobile export toolchains are not included in this desktop experiment.

Use stable room, character, quest and asset IDs with a documented data contract and explicit limits. The Stage 1 `rooms-v1` contract is implemented in `content/community/v1/`; validate independently in Studio and Godot. Imports are bounded declarative content, never arbitrary scripts or caller-selected resources. Keep source-floor geometry and approximate directory anchors separate from fictional game collision and interactions; playable spaces are not surveyed navigation.

Start with manual modular character creation, consistent portrait and directional idle/walk assets, and a person's review of their portrayal. Photo-assisted AI generation remains optional later work with explicit consent and defined data handling. Keep private source photos out of the repository, and track asset rights separately from the code's MIT licence.

The sequence is the active contract-first one-room handoff, then a fictional open-source contribution challenge by default, then character/quest authoring and evidence-led expansion. Resident feedback can refine later scope. See the [phased plan](../plans/GODOT_SVELTEKIT.md).

## Alternative considered: Compose for browser Studio

Compose offers shared Kotlin UI across mobile, desktop and web and is viable if a common interface becomes the priority. SvelteKit is preferred here because authoring is a distinct browser workflow and the existing browser editor already uses JavaScript/PixiJS. Choosing Compose for it would require a port; Godot gameplay would still be separate. The cost of SvelteKit is an additional stack and independent consumer validation. Revisit if resident input favors a shared native/browser editor or a Kotlin-focused contribution model. See the [comparison and sources](../plans/GODOT_SVELTEKIT.md#browser-framework-alternative-compose-multiplatform).

## Former feedback gate — superseded 12 October 2026

The original plan asked for resident input and a later explicit resumption before any implementation. The user explicitly resumed Stage 1 on 12 October 2026. Resident feedback has not been collected and is no longer a prerequisite for this bounded fictional package/editor/preview experiment. It remains useful for revising gameplay and future product scope. Approval, licensing and privacy checks still apply before adding a real person's portrayal, personal introduction or real tenant/project content.

## Consequences

Game behavior and browser authoring can evolve independently through a tested content boundary. This introduces two consumers whose validation and rendering must be checked rather than assumed equivalent. Small runnable slices must demonstrate actual screens and the complete journey; browser, emulator/simulator and physical-device evidence remain separate.

ADR 0006 continues to govern the current Plan World and its source uncertainty; ADR 0007 continues to describe the implemented one-room experiment. This ADR supersedes the earlier recommendation merely to evaluate Godot as the selected future direction, while leaving those current behaviors intact. A later integration/replacement decision needs resident evidence and mobile validation.

Official framework references: [Godot 2D](https://docs.godotengine.org/en/stable/tutorials/2d/index.html) and [SvelteKit introduction](https://svelte.dev/docs/kit/introduction). These describe technology capabilities, not implemented features.

## Historical map-only scope amendment — 9 October 2026

At that time, the user separately resumed work on the existing map, explicitly selecting reusable assets starting with the ground floor. That amendment authorized a bounded Compose map/art change while the community implementation remained on hold. The later Stage 1 resumption below supersedes that hold; the map remains a separate Compose experience.

## Stage 1 resumption amendment — 12 October 2026

The user explicitly resumed the community implementation plan after the separate map asset work. Implement the shared `content/community/v1` room contract first, then its SvelteKit Studio author and standalone Godot desktop consumer. Keep the fictional sample bounded and preserve Compose Plan World and the existing Experimental 3D experience. The next default learning experiment, absent further preference, is an open-source contribution task using fictional sample content; no resident feedback has been collected. This amendment does not claim a mobile Godot build, resident study, or production publishing flow.
