# Architecture

## Active shape

Android, iOS and desktop launch the shared Kotlin Multiplatform/Compose application. Its current home is **Plan World**, with five structured floor records and five full-floor illustrations. The building domain owns source-derived outer-footprint polygons, courtyard voids, places and approximate anchors; presentation fits each illustration to the plan viewport, clips it to the footprint and voids, and transforms the result for pan, zoom and Fit. Search and the accessible legend use the same place records. Geometry and search remain independent of Compose, operating systems and transport. Platform launchers stay thin; tests mirror feature packages under `commonTest`.

```mermaid
flowchart TD
    Android[Android activity] --> UI[Shared Compose Plan World]
    iOS[SwiftUI host] --> UI
    Desktop[Desktop window] --> UI
    UI --> Plans[Five floor plan data]
    UI --> Search[Place search and legend]
    Search --> Plans
    UI --> Art[Five fictional furnished floor illustrations]
    Art --> Mask[Source-derived footprint / courtyard clipping]
    Legacy[Internal fictional sample fixtures] --> LegacyTests[Route / checkpoint / consent / hunt tests]
    Browser[Standalone browser demo] --> Viewer[Licensed unrelated engine-room sample]
```

There is no edge from the supplied-plan world to sample routing, checkpoint observations, sharing, hunt or the standalone splat viewer. No camera, positioning radio, account provider, analytics, external location upload or multiplayer adapter is implemented for the plan world.

## Plan data and provenance

Each original plan is 2048 × 1448 pixels, X right and Y down. Active tracing lies approximately in X 80–1940, Y 580–1280. These values are source-art positions, not metric coordinates. Five independent datasets preserve differences in outer footprints and courtyard voids. The footprint masks are coarsely extracted from source pixels and simplified; they preserve relative shape, not exact wall dimensions. Full-floor generated art is fitted to this plan viewport and clipped by those masks. Its interior partitions, desks, furniture and characters are fictional. On ground, the full silhouette remains visible in muted charcoal (#252B2D) with a subtle outline; furnished artwork stays in known colored regions and the unassigned grey source area has no furniture. Courtyards remain empty dark voids. Ground/second grey and ground orange source areas have no confirmed occupant assignment; first-floor FARI is detached; fourth-floor 66 / The Sky has a legend entry but no defensible anchor. One place may own multiple anchors when the source repeats a number.

Source-plan images support the silhouette, courtyards and directory labels, not surveyed topology. The five generated floor images create a rich fictional world; their illustrated walls and contents are not source-plan facts. The clipping geometry remains editable as data, though art may need review after large mask corrections. The plans' revision is unknown; future imported editions need explicit provenance and review. See [coordinates](COORDINATES.md) and [ADR 0006](adr/0006-plan-derived-pixel-world.md).

## Parked code and separate viewer

The earlier fictional `sample-building` fixture uses four floors, an illustrative metre coordinate system, a reused coworking raster and a route graph. Its route, optional guide, QR validation, local sharing and same-device hunt behavior remain in internal legacy fixtures/tests. They do not operate on the five real-plan drawings. The disconnected Python reference server still exercises grant authorization, server-clock expiry and revocation with synthetic opaque payloads; the app does not connect to it. Opaque storage is not E2EE.

The licensed Tugboat Bat engine-room splat and PlayCanvas viewer remain bundled offline, with the local browser demo under `exploration/dist/`. This sample is independent of BeCentral. The old mobile reception doorway and Map/3D switches are historical prototype behavior, superseded as the active entry by Plan World. The browser demo is not evidence of mobile performance or room-linked alignment. See [viewer provenance](../exploration/README.md) and [verification](testing/STATUS.md).

## Platform and dependencies

## Room Studio prototype

`room-studio/` is an independent Vite/PixiJS browser editor. Its pure JavaScript domain module validates scene JSON against a fixed convex chamfered room polygon, limits the furniture types and count, and rejects objects that extend outside that trusted boundary. It imports the shared starter fixture from Compose resources. Browser-only persistence uses local storage; export/import uses the versioned JSON schema. There is no server, account, sync, asset upload, AI generation or publishing path.

The Compose `features/studio` domain parses and validates the same schema and keeps the drawing model separate from UI. `RoomStudioPreview` loads the shared fixture and permits JSON paste/apply and reset in a transient preview state. Its edits are not persisted. Plan World reaches this preview from More. Neither the browser nor Compose fixture is linked to a real campus room or tenant record. The demo polygon is immutable in imported scenes, so Studio cannot change source floor geometry. See [ADR 0007](adr/0007-room-studio-experiment.md).

## Platform and dependencies

The project pins Kotlin 2.3.20, Compose Multiplatform 1.11.1, Gradle 9.5.0, Android Gradle Plugin 9.3.1 and Material 3 `1.11.0-alpha07`. Android targets API 26+ and compiles against 36. iOS targets 16+ with ARM64 device/Apple Silicon simulator frameworks. Compose resources package artwork; verify the target bundle and visible screen rather than inferring success from compilation. Plain constructors/functions suffice for dependencies; add a layer or module only for a concrete need. Browser Studio scripts run from `room-studio/`: `npm ci`, `npm test`, `npm run build`, and `npm run dev`.

## Future integration boundary

A real navigation import needs approved, versioned plans; stable room identities; surveyed entrances/connectors; accessibility evidence; and validation of graph connectivity. Source pixels cannot be converted into measured routes by assuming a scale. A room-linked 3D capture needs permitted assets and surveyed alignment. QR scanning, positioning and network sharing each require separate adapters and consent/security checks. Keep last-seen semantics distinct from live position, and keep no location history by default.
