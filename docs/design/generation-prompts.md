# Coworking concept generation prompts

Generated with the built-in GPT image tool on 7 October 2026. The creator approved Concept 01, shown in `cowork-concept-approved.png`, and separately requested BeCentral blue controls with a charcoal map surround. Concept 02 was an unselected colour study. The implemented background removes baked people so decorative figures cannot be confused with a location-sharing integration.

## Approved Concept 01

```text
Use case: stylized-concept.
Asset type: visual design proposal for BeThereCentral's fictional coworking-floor map, for human approval before implementation.
Create one beautiful, highly polished TOP-DOWN PIXEL ART coworking floor, like an inviting explorable 16-bit indie game world. This is a whole-floor design, not a UI screenshot, not a sprite sheet, not a technical CAD plan.
Composition: wide landscape image, a single coherent rectangular floor floating on a pure near-black charcoal background #0C1114 with generous dark margin. Straight orthographic overhead view, aligned horizontal and vertical walls, no isometric tilt, no perspective vanishing points. Roof removed, short chunky cutaway walls, consistent pixel size, crisp stepped outlines, carefully hand-placed-looking pixel clusters, no blur or photorealism.
Design a believable multi-company coworking campus: four distinct company suites around a generous, visibly connected central circulation spine and shared social heart. Suites have several workstations with monitors, task chairs, meeting tables, storage, small plants and personal touches. Give the four fictional suites short legible integrated signs: "ORBIT", "MOSS", "SPARK", "NORTH". Use muted sage, terracotta, cream and dusty mauve as subtle suite identities; no dominant blue floor or blue environment.
Shared spaces: a welcoming entrance and reception near the lower edge; a cozy communal cafe/kitchen with coffee counter, stools and long table; an orange-sofa lounge with plants and books; two enclosed meeting rooms; two tiny phone booths; a quiet work room; clearly recognizable stairs and a lift next to each other; compact bathrooms. Every room must have a visible open doorway to the circulation network; keep corridors wide and traversable, furniture cannot block doors.
Add a handful of tiny original pixel people working or talking, and one identifiable tiny founder with dark hair, cream shirt, navy trousers and vivid orange backpack walking in the central corridor. People remain small relative to the floor. No route overlay yet.
Palette: black/charcoal surround, warm cream and peach interiors, natural warm wood, orange #F74B23 accents, muted green plants, restrained dark outlines. Warm, calm, friendly, crafted, sophisticated rather than noisy or childish. A rich sense of a small inhabited world with coherent room composition, not an empty schematic grid. Different flooring patterns in rooms versus shared corridor, readable boundaries and room functions at a glance.
Typography: only a few clean compact pixel-font signs integrated into room floors, with exact text "MEET 01", "MEET 02", "CAFE", "QUIET", "LOUNGE", plus the four company signs. Small unobtrusive footer in the dark margin: "FICTIONAL COWORKING FLOOR · CONCEPT 01". No real company logos or BeCentral logo, no claims of a real building, no promotional headline, no buttons or app chrome, no watermark. The floor itself occupies most of the image.
```

## Clean registered map background

Input: the approved Concept 01. Output: `composeApp/src/commonMain/composeResources/drawable/cowork_floor.png`.

```text
Edit the supplied APPROVED Concept01 into a clean game-map background asset. Preserve the exact canvas dimensions/aspect ratio, straight top-down composition, building bounds, walls, every door opening, stairs, lift, all room signs, furniture layout, colors, pixel-art detail and charcoal exterior background.
Only remove every human figure from the image, including the orange-backpack figure in the center corridor and all people seated or standing in the offices, meeting room, cafe, lounge, phone booths, quiet room and reception. Reconstruct the empty chair, desk, floor or sofa naturally beneath each removed person. Keep all furniture including chairs and their positioning. Characters will be drawn separately by the app.
Also remove the small footer "FICTIONAL COWORKING FLOOR · CONCEPT 01", replacing it with matching plain charcoal background. Keep all interior room/company labels ORBIT, MOSS, SPARK, NORTH, MEET 01, MEET 02, CAFE, LOUNGE, QUIET and WELCOME exactly. Do not add new objects or rearrange any architecture. Do not recolor to blue. Preserve the warm woods, sage/terracotta/mauve company rooms and orange lounge sofa. No new text, no UI, no route line. This must remain geometrically registered to the original so navigable room and doorway coordinates stay identical.
```

## Navigation corrections

After removing the figures, the built-in image tool adjusted the lift's north entry and shortened the cafe seating footprint. These edits preserve the shared image registration while creating visibly open approach corridors. Final source: `cowork_floor.png`, checksum recorded in the asset note.

### Lift edit prompt

```text
Make ONE precise architectural correction to this pixel-art map asset. Preserve the image's exact 1536x1024 framing, ALL room positions, ALL room signs, ALL furniture, every other wall/door, palette, people-free state, and charcoal surround.
Correction target: the lift/elevator bay at the bottom of the image, immediately to the RIGHT of the stairs and to the LEFT of the NORTH company suite. Its approximate image bounds are x940..1042, y650..885.
Make this lift clearly accessible directly from the main horizontal corridor along its NORTH/TOP edge. Put the lift entrance and its doors at approximately x965..1025, y654..687, facing up toward the corridor. Show a visible gap/open threshold at that north edge and a small two-arrow elevator sign that reads clearly as a lift, with an empty elevator cabin floor immediately behind it. Keep this entrance around pixel (990,660) and keep the main corridor around (990,630) free. Remove the old south-facing tall door at y735..857 and replace that lower part of the bay with the elevator cabin floor, bounded by its existing side and bottom walls. It must read as a top-down elevator entered from the top/main corridor, not a door at the inaccessible bottom.
Do not move, resize or alter the adjacent stairs or reception. Do not change any other part of the floor plan. No people, no new labels, no route overlays. Geometric registration to the input is critical; leave all pixels outside the lift bay visually unchanged.
```

### Cafe edit brief

Preserve the full 1536 × 1024 layout except the cafe seating island. Move its communal table, chairs and rug upward and shorten its lower boundary, leaving a clear cream-tiled passage beneath it, above Spark's wall. Keep the lift's north approach open. Preserve all room signs, furniture elsewhere, palette, empty-of-people state and charcoal surround. Review the actual output rather than assuming requested pixel positions are exact.
