# ADR 0003: Map-first interface with contextual controls

Status: accepted · 7 October 2026

Priority update: [ADR 0004](0004-gaussian-exploration-priority.md) establishes Gaussian exploration as a core experience, with its first sample slice now implemented. This document describes the implemented 2D shell; its interaction principles continue to apply.

## Context

The first prototype displayed navigation forms before the map on phones and used a persistent sidebar on desktop. The user requested the most minimal interface possible and supplied a Matterport dollhouse screenshot and [tour](https://my.matterport.com/show/?m=RFxTxqcbUTB). Direct inspection of the tour's 3D, dollhouse and floor-plan modes showed a dominant spatial canvas with small edge controls and contextual information.

## Decision

Make the 2D map fill the available viewport, with floating search, compact floor selection and a contextual destination/route card. Put less frequent tasks in dismissible panels. Use a dark neutral canvas and a restrained accent so the plan and route remain prominent. Keep persistent sample status compact and explain limitations in the relevant panel.

State belongs above conditionally displayed panels so hiding a control never silently resets a route, hunt or sharing session. Map projection must reserve space for visible controls. Preserve accessible text descriptions and touch targets even when the visual control is compact.

## Consequences

The map is immediately available on mobile and desktop, while advanced tasks take an intentional extra action. Verify discoverability through the PRD's navigation experiment. This visual change does not add real 3D, copy Matterport assets, alter the building coordinate contract or implement missing integrations. The accessible 2D experience remains the product's baseline.
