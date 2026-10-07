# Building coordinate and identity contract

Status: initial sample convention; interchange transport and building-linked 3D clients are not implemented. The independent sample Explore viewer does not consume this contract.

## Coordinates

- Unit: **meters**, never map pixels.
- One common 2D origin at the sample building's lower-left plan corner.
- `xMeters` increases to the right/east of the plan; `yMeters` increases up/north. The map inverts Y when projecting into screen pixels. This is a drawing convention, not surveyed compass alignment.
- Floor elevations are meters above the ground-floor reference: 0, 4, 8 and 12 for this fictional building.
- Sample floor bounds: 74.4 × 43.6 illustrative meters. Room footprints and routing entrances use the same plan coordinates. UI pan, zoom and viewport size never alter stored geometry.

The approved coworking raster is 1536 × 1024 pixels. Its registered crop is `(x=24, y=48, width=1488, height=872)` at 20 source pixels per illustrative metre: `xMeters=(imageX-24)/20`, `yMeters=(920-imageY)/20`. All four sample floors reuse this footprint. This is an art registration, not measured physical scale or an accessibility survey. Change the raster and registration together, then review every affected doorway/route.

The canonical 3D tuple is `(xMeters, yMeters, elevationMeters)`. For a right-handed, Y-up rendering world (such as a future PlayCanvas scene), use `(X, Y, Z) = (xMeters, elevationMeters, -yMeters)`, then apply an explicitly recorded scene transform if assets have a different origin/scale. Do not silently bake transforms into room IDs or coordinates. This mapping must be verified with three or more non-collinear surveyed control points before real assets are aligned.

## IDs

The sample building ID is `sample-building`. Current floors are `floor-1` through `floor-4` (ground through third floor). Room IDs use a level and semantic sample slug, such as `room-l1-orbit`, `room-l1-meeting-01` and `room-l4-north`. A room's floor-qualified name is a display label, never its identity. The earlier numbered-grid IDs were retired when the prototype adopted the approved coworking illustration; no persistent user data or published interchange depended on them.

Examples of checkpoints are `cp-l1-lobby` and `cp-l1-west`. Current manual demo payloads use:

```text
btcentral://sample-building/checkpoint/cp-l1-lobby
```

The validator accepts only its declared scheme/building and known checkpoint IDs. It does not follow URLs. A copied code does not prove physical presence. The initial format has no version or signature; before a real pilot, introduce explicit schema/building versions with migration and rejection tests. Do not silently reuse changed geometry with an old code.

## Proposed interchange for later clients

An export should include `schemaVersion`, `buildingId`, `buildingVersion`, unit, coordinate-axis convention, floor IDs/elevations, room IDs/footprints/entrances, graph nodes/edges and checkpoint anchors. Mark sample provenance. Each edge should declare movement type and accessibility/availability; never infer step-free status from its appearance.

Gaussian-splat assets require an asset-to-building transform and provenance; PlayCanvas/WebXR objects attach the same `roomId` used by search and routing. Camera pose and map pixels are view state, not authoritative building coordinates. The prototype ships a PlayCanvas splat loader for an independent licensed sample only. No building JSON exporter, surveyed scene alignment, room-linked 3D client or WebXR experience is implemented.

## Prototype sample doorway

The exploration fixture anchors a **3D sample** launch marker to `room-l1-reception`, two illustrative metres inside its entrance. It uses the map projection, so pan and zoom move it with reception. Other floors have no doorway despite repeated artwork. This is a UI link to the independent engine-room viewer, not a scene registration, room association within the capture or positioning observation. No building coordinates are passed into that viewer.
