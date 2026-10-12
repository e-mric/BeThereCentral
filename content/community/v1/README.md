# Community content package v1

This package is the Stage 1 handoff for one fictional room. Its envelope is frozen at seven fields: `packageVersion` (integer `1`), `packageId` (`betherecentral.sample-room`), `contentRevision` (positive safe integer, currently `1`), `features` (exactly `rooms-v1`), `assets` (the trusted catalog), `attribution` (required entries), and `room` (RoomScene schema v1). The nested room keeps its original four keys and existing Studio geometry/object rules. Unknown keys and feature versions are rejected.

Use the Node module `index.js`: `validatePackage(value)` validates an already parsed object and returns a clone; `parsePackage(source)` checks a maximum of 65,536 UTF-8 bytes before parsing; `serializePackage(value)` validates before producing JSON. Failed validation throws and does not mutate caller data, so consumers should validate fully before replacing a loaded scene.

IDs and labels use the legacy validator's ASCII safe-ID rule and JavaScript string `.length` bounds (UTF-16 code units), including the 60-unit company label limit. A Godot consumer should count UTF-16 code units for matching text limits. Room furniture remains capped at 24; object IDs must be unique; geometry and kinds must pass the legacy scene validator. Assets are identifiers in a pinned local catalog, never URLs or caller-selected paths. Packages contain declarative JSON only; arbitrary resources, scripts, and remote references are not supported.

The trusted `campus-props-atlas` entry points to the original project atlas and records its provenance in `docs/assets/PIXEL_ART.md` and `docs/design/generation-prompts.md`. It is catalog metadata; consumers still resolve assets from their trusted bundle. The sample room and sign are fictional and do not represent a mapped tenant room. Its supplied furniture coordinates remain independent of any campus map positions.

`package-v1.json` is the valid default fixture. `fixtures/invalid/` contains representative rejected package files. Run the contract tests from the repository root with `node --test content/community/v1/test/*.test.js`.
