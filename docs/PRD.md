# BeThereCentral — Product Requirements

Version 0.7 · 8 October 2026 · Status: five-plan pixel-world experiment

## Problem and current experiment

People should recognize BeCentral’s shape, enjoy exploring a full pixel-art coworking world, and find a named occupant or numbered place without mistaking fictional interiors for surveyed navigation. Five supplied schematic plans cover ground through fourth floor. Their revision, ownership and current accuracy are unconfirmed. The immediate experiment is whether visitors can explore five distinct illustrated floors and understand which directory facts come from the plans.

The app opens on **Plan World**, a five-floor illustrated experience. Each generated floor image fills a source-derived outer footprint while preserving courtyard voids and approximate relative proportions. Interior walls, desks, furniture and characters are fictional. Search finds known company names and plan numbers; a legend lists known occupants and unplaced entries. A compact pixel host introduces only factual source-plan information. A result reveals its approximate plan anchor or an explicit **not placed on this plan** state. No route, arrival time, live position or accessibility claim follows from an anchor.

Ask 5–8 consenting people to find Campfire AI on the second floor and 42 Belgium on the third, then find The Sky in the fourth-floor legend and explain why it has no marker. Ask whether they can tell that the furnished grey/orange ground-floor and grey second-floor source areas have no confirmed occupant assignment. Record anonymous task outcomes and confusion, without movement traces. These are proposed checks, not achieved results.

## Active slice: plan-derived pixel world

- Show ground, first, second, third and fourth floors independently, preserving identity during pan, zoom and Fit. A floor switch does not imply physical movement.
- Keep each floor's source-derived outer footprint, courtyard voids, source numbers and approximate anchors editable as structured data in the original 2048 × 1448 plan-image coordinate space, with Y increasing downward. The active drawing viewport is approximately X 80–1940, Y 580–1280. Pixels are source-art coordinates, not metres. Coarse traced masks preserve relative shape, not exact surveyed walls or dimensions.
- Use five generated full-floor illustrations, one per plan, to make the world feel furnished and lived in. Clip them to their floor's footprint and void masks. Treat all illustrated partitions, furniture and characters as fictional rather than observed room inventory.
- Search source-confirmed names and plan numbers case-insensitively. A repeated number is one legend place with multiple approximate anchors, not several invented tenants. Keep entries without a defensible anchor in the accessible legend and search results, with no guessed marker.
- Make occupants readable in map and list form. Ground-floor grey and orange source areas and second-floor grey source areas have no confirmed occupant assignment even though the generated interiors may furnish them. First-floor FARI is detached without an asserted connection; fourth-floor number 66 / The Sky remains unplaced.
- Label the scene as **PIXEL WORLD** and **FICTIONAL INTERIORS** in the main view. Explain in More that outer shapes and courtyards come from supplied plans while the furnished interiors are imagined.
- Provide labelled floor picker, Fit, zoom, search, results and legend with keyboard, touch and assistive-technology access. Inspect actual rendered screens on each platform claimed; compilation alone is insufficient.
- Attribute the supplied plans and record their unverified revision. Do not upload them, add a backend or collect location data for this slice.

## Limits

Source-plan anchors are approximate and may sit within fictional illustrated rooms; they do not register a real room interior. Source silhouettes, schematic fills and generated walls do not prove entrances, navigable connectivity, accessible paths, measured dimensions or floor elevations. Do not derive routes, step-free guidance, QR checkpoints, room-linked 3D alignment or live tracking from them. This is not emergency guidance. A building owner must verify plan revision, rights, occupant register, entrances and accessibility before an on-site pilot.

## Parked product goals and samples

The broader goal remains: **make an unfamiliar building feel familiar—and enjoyable to explore.** The codebase retains a fictional four-floor sample with graph routes, optional guide animation, simulated QR observations, local consent and same-device discovery. These are internal legacy fixtures and tests while the active home uses the five supplied plans. They do not work against those plans. The disconnected loopback sharing server tests grant expiry and revocation but is not connected to the app; opaque payload storage is not end-to-end encryption.

The bundled licensed Gaussian-splat engine-room viewer and `exploration/dist/` browser demo remain a standalone experiment. The captured room is unrelated to BeCentral and has no verified map transform or room hotspots. Immersive exploration remains a long-term direction; the earlier reception doorway and bottom Map/3D journey are historical prototype behavior, not the current five-plan walkthrough. See [ADR 0006](adr/0006-plan-derived-pixel-world.md).

Room Studio is proposed after source-plan review. Tenant decorations must not alter authoritative footprints, entrances or route geometry. Permission, ownership, review, asset rights and rollback precede publishing.

## Future pilot gates

| Goal | Evidence needed before a claim |
| --- | --- |
| Site directory | Approved plan revision and occupant register, stable identifiers and update owner |
| Routes and step-free guidance | Surveyed entrances, connectors, graph and accessibility review; on-site validation |
| QR last-seen observation | Approved checkpoint locations, versioned payloads, camera integration and freshness display |
| Sharing | Identity, TLS, verified recipient keys, audited client encryption, expiry/revocation integration and consent |
| Room-linked 3D | Permitted capture, provenance and surveyed alignment with shared building/version IDs |
| Room Studio | Tenant rights, bounded authoring, review and rollback |

No location history is kept by default. Analytics and external location upload require explicit consent. Never call a checkpoint observation live tracking or claim E2EE before authenticated key exchange and encryption are implemented and reviewed.

## Architecture and verification

Kotlin Multiplatform shares pure building/search behavior and Compose Multiplatform presentation across Android, iOS and desktop. Keep plan data and search independent of Compose and transport; put behavior tests in matching feature packages. Preserve thin platform launchers and the checked-in offline sample viewer. Update affected docs, changelog and [verification status](testing/STATUS.md) together. Distinguish browser, emulator and physical device evidence.
