# ADR 0004: Gaussian exploration as a core experience

Status: accepted; first sample exploration slice implemented · 7 October 2026

Entry update: [ADR 0005](0005-reception-sample-doorway.md) replaces tabs and the opening scene with a pixel-art home and a labelled reception sample doorway. Gaussian exploration remains core.

## Implementation update

The first slice now bundles PlayCanvas 2.23.1 and an attributed CC BY 4.0 sample of the Tugboat Bat engine room (650,000 splats after reduction). Android hosts the offline viewer through WebViewAssetLoader; iOS uses WKWebView restricted to local bundled files. The real scene has rendered in the browser, iOS simulator and a dedicated Android emulator. Physical Fold appearance still needs user confirmation. A focused demo follow-up introduces a curated Overview, bounded lens-and-distance zoom and a clearly separate Map demo; [experience standards](../EXPERIENCE.md) define its acceptance checks. This explicitly unrelated sample contains no room-map overlay or positioning integration. The desktop browser demo is separately runnable. See [verification](../testing/STATUS.md) and [viewer/asset provenance](../../exploration/README.md).

The original decision below records why this became the next milestone. Renderer selection is now resolved for this prototype; the later independent PlayCanvas/WebXR experience is still planned.

## Context

The user clarified that exploring the building with Gaussian splatting is central to the fun of BeThereCentral. Treating 3D as an optional investment after proving 2D navigation misses that intent. No capture of the real building has been supplied.

## Decision

Make an interactive Gaussian-splat exploration slice the next product milestone. Keep the shared Kotlin/Compose application and its 2D map, routing, last-seen and accessibility behavior. The target interface gives Explore a prominent place and offers the map as a complementary view and fallback.

First validate the experience with an actual splat asset whose redistribution rights and source are recorded. Label it as a sample scene. An unrelated scene must not inherit the fictional building's room IDs or show routes pretending to match its geometry. Room-linked exploration requires an explicitly aligned scene and validated metadata under the coordinate contract.

Evaluate the rendering adapter on Android and iOS before choosing native rendering or a web surface within the app. A standalone browser demo alone does not complete the in-app milestone. The separate PlayCanvas/WebXR experience remains planned. Do not add inactive 3D controls to the current app or claim a renderer, capture or integration exists before it works.

## Consequences

Lean development now tests whether a small explorable scene is enjoyable and usable on phones. Navigation-study results are no longer a prerequisite for that experiment. Loading, failure, memory/performance and return-to-map behavior belong in its acceptance checks. Real building capture can follow the sample experiment.

This supersedes ADR 0003 only where it makes 2D the exclusive opening experience or defers 3D priority. Its minimal controls, retained state, readable overlays and accessible touch targets still apply. Current mobile behavior includes sample Explore and the separate fictional 2D map, as recorded in the implementation update.
