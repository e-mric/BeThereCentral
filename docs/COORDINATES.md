# Coordinate and identity contracts

## Active Plan World: source-image pixels

The five BeCentral schematic plans supplied on 8 October 2026 use independent **2048 × 1448 source-image pixel** spaces: X increases right, Y increases down. The traced viewport is approximately X 80–1940 and Y 580–1280. Each floor owns a source-derived outer-footprint mask, courtyard voids and place anchors. Pan and zoom transform these coordinates for display; Fit frames the selected floor. Five generated full-floor images are fitted into this viewport and clipped to those masks. The mask retains approximate relative plan proportions but is coarsely traced; it does not establish exact dimensions. Do not convert these pixels to metres or infer scale, north, floor elevation, doors, access or walking distance. Image revision and survey status are unconfirmed.

A source-plan number identifies one legend entry on one floor. Repeated glyphs may produce several approximate anchors for that entry. A name can be searchable with no anchor; number 66 / The Sky on the fourth floor stays unplaced. Ground-floor grey and orange and second-floor grey source areas have no confirmed occupant assignment even where the generated images furnish them. First-floor FARI is detached; no connection is inferred. Structured source geometry and directory facts stay separate from the fictional full-floor illustrations. Their partitions and furniture are not building data.

Stable internal IDs distinguish floor, region and place and retain source provenance. They are not verified building room IDs. Neither UI coordinates nor source anchors may be passed to routing, positioning or a 3D scene as if surveyed.

## Parked fictional sample convention

The earlier `sample-building` fixture uses four floors, `floor-1` through `floor-4`, and illustrative metre coordinates. Its generated coworking raster has a registered crop `(24, 48, 1488, 872)` at 20 art pixels per fictional metre. The sample origin is lower left, X right and Y up; illustrative elevations are 0, 4, 8 and 12 metres. Room and checkpoint IDs such as `room-l1-orbit` and `cp-l1-lobby` exist only in that fixture. Do not conflate it with the new five-plan space.

The old reception **3D sample** marker was a UI launch link to an unrelated licensed engine-room capture. It never established a capture-to-building transform. The browser viewer remains separately runnable and consumes no Plan World coordinates.

## Future measured contract

After an approved survey, a versioned interchange may define `schemaVersion`, `buildingId`, `buildingVersion`, floor IDs/elevations, stable room IDs, measured room footprints/entrances, graph edges with accessibility evidence and checkpoint anchors. Use metres with a documented origin and axes. A future Y-up 3D scene can map surveyed plan `(xMeters, yMeters, elevationMeters)` to `(X, Y, Z) = (xMeters, elevationMeters, -yMeters)` only after its transform is recorded and checked against at least three non-collinear surveyed control points. Current schematic pixels cannot satisfy that test. No exporter, real route graph, aligned capture or room-linked WebXR client is implemented.
