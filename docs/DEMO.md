# Building-team demo

This is a proposed BeCentral experience by a WeAreFounders participant. The active app presents five pixel-art floors shaped approximately by supplied schematic plans. Their outer footprints retain relative proportions and courtyard openings remain empty dark voids. On ground, the full silhouette stays visible as muted charcoal with a subtle outline; the unassigned grey source area has no furniture, and fictional artwork stays in known colored regions. Other room interiors and furnishings are fictional. Labels and anchors are approximate and do not identify exact tenant rooms. Plan revision, occupant accuracy, rights and survey quality have not been confirmed. Treat the world as an exploration and visual directory discussion, not on-site navigation, accessibility guidance or an officially approved campus service.

## Run

Use [README](../README.md) for Android, iOS and desktop setup. Build/launch the exact target you intend to show and inspect its actual opening screen. The standalone licensed engine-room browser viewer remains available from `exploration/dist/`, but it is a separate experimental scene unrelated to BeCentral.

## Current walkthrough

1. Open the ground floor. Notice the full silhouette in muted charcoal, with art only in known colored regions and no furniture in the unassigned grey area. The courtyard is an empty dark void. Pan, zoom and use Fit. More explains the source/art boundary.
2. Open the floor selector sheet and visit all five illustrated floors. Compare their approximate outlines. On first, describe FARI as detached in the source drawing without asserting a connection. Explain that source labels do not provide exact room assignments.
3. Search **Campfire AI** and open its second-floor result. Read its known plan number/name in the pixel host; show its approximate source-plan anchor within the fictional art. Avoid a mission, room-use, entrance or route claim.
4. Search **42 Belgium** on third and use the legend to locate it. If a number repeats in the source drawing, one legend entry can point to multiple approximate anchors.
5. Search **The Sky** on fourth. Show number **66** in the accessible legend and its **not placed on this plan** state. Do not invent a marker.
6. Ask the team which plan revision and occupant register are current, who owns changes, which areas may be shown publicly, and who can validate entrances and accessibility before any navigation pilot.

The useful outcome is feedback on visual appeal, floor recognition, names and uncertainty. Ask a participant to find the three examples unaided; note confusion without recording movement history. Source-derived proportions are not measured dimensions, and a source pixel position is not a surveyed distance or route endpoint.

## Room Studio prototype

Open **More → Room Studio preview** to show the transient Compose preview of the shared fictional demo room. Paste/import a JSON file exported from the browser Studio and apply it; reset returns to the starter fixture. The preview does not save or publish edits. In the standalone browser project (`cd room-studio && npm ci && npm run dev`), demonstrate changing the company sign, arranging desk/plant/sofa/rug props, undo/redo, local save/reload and JSON export/import. It is one fictional chamfered room with immutable geometry, not a company's actual BeCentral room. There are no tenant accounts, AI generation or publishing flow.

The courtyard garden is a future visual option only. Keep courtyards empty and dark in this demonstration. A later perspective design should make a garden appear smaller from higher floors; do not suggest that this visual effect is already implemented.

## Separate historical experiment

The old fictional four-floor sample with routes, guide, QR, sharing, hunt and reception **3D SAMPLE** doorway is retained internally for fixtures and tests. It is not the active five-plan journey. The licensed engine-room splat viewer is separately runnable in a local browser and does not depict the supplied plans. Do not demonstrate a fictional sample route as if it reaches a real BeCentral occupant. The browser rendering does not establish mobile or physical-device quality.

## Before presenting

Cold-open and inspect all five actual rendered floors, key search results, zoom/Fit, legend and unplaced state on the presentation device. Record the device and outcome in [verification status](testing/STATUS.md). A build or renderer-ready log is insufficient. Use the [pilot permissions guide](PILOT_REQUEST.md) to discuss plan rights, tenant information, any future capture boundaries and private review.
