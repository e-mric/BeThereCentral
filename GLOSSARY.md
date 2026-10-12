# Shared vocabulary

- **Supplied plan**: one of five schematic BeCentral floor images supplied on 8 October 2026, ground through fourth floor. Revision and survey accuracy are unverified.
- **Plan World**: active five-floor pixel-art map and directory, built iteratively with source-derived outer shapes, courtyard voids and company positions. Fictional decoration does not establish a navigable survey.
- **Modular floor scene**: independently placed materials, wall/window segments and reusable furniture assets in source-pixel coordinates. All five active floors use this recipe and the shared renderer; original full-floor PNGs remain historical references, not active fallback art.
- **Source pixel**: coordinate in an original 2048 × 1448 plan image, X right and Y down. It has no metric scale.
- **Floor**: named plan level with its own identity and shape. The plans do not establish reliable elevations.
- **Region**: source-derived area or footprint used to organize the drawing. Its illustrated interior does not establish a real room, tenant or accessible space.
- **Place**: one numbered or named floor-legend entry. A repeated number can have multiple approximate anchors for the same place.
- **Unplaced place**: a source-listed occupant with no defensible map anchor, still available in search and the accessible legend; fourth-floor number 66 / The Sky is an example.
- **Plan anchor**: approximate source-label position shown over fictional room art. It does not register that art to a real room or establish an entrance, route endpoint or measured coordinate.
- **Fictional interior**: desks, furniture, window styling, characters and other decorative details that make the world explorable; they do not claim actual room inventory or access.
- **Room Studio**: one-room local editor experiment using a fictional convex chamfered demo room. Its trusted outline is immutable; a company sign and up to 24 furniture items are editable. It is not an editor for mapped BeCentral tenant rooms.
- **Room scene JSON**: the original versioned interchange format shared by Vite/PixiJS Room Studio and its Compose preview. It validates fixed demo-room geometry and bounded furniture data; Community package v1 is a separate contract.
- **Studio preview**: Compose-only rendering of the shared demo scene; pasted/imported JSON changes this preview until reset/close and is not persisted.
- **Ground unassigned area**: grey source-plan area shown within the complete building silhouette using muted charcoal fill and a subtle outline; it remains without furniture because its occupant assignment is unconfirmed.
- **Bike room**: separate room west of the ground-floor lobby stairs, identified by the supplied bicycle icon and continuous angled/vertical white divider. Furniture stays within that traced boundary; no entrance is inferred from the continuous line.
- **Courtyard void**: empty dark area in the current five-floor illustrations, preserving the courtyard opening visible in the supplied plan. A planted garden is a possible future decorative idea, not current content or a surveyed landscape.
- **Detached region**: separated plan shape, such as first-floor FARI, without a proven connection to the main footprint.
- **Sample building**: earlier fictional four-floor fixture retained for internal tests; not the five-plan BeCentral world.
- **Checkpoint / last seen / route / step-free**: legacy sample concepts. A checkpoint is a validated sample payload; last seen is its latest timestamped observation, not live tracking; a route traverses a fictional graph; step-free excludes declared inaccessible sample edges, not surveyed real access.
- **Grant / team simulation / discovery stop**: legacy sample sharing consent, same-device player contributions and ordered fictional hunt content. No networked team session or real recipient delivery is implemented.
- **Guide preview / room host**: character animating an old fictional sample route, and pixel character introducing a selected place. The active Plan World host states source-supported facts only, never an invented tenant mission.

- **Community Studio**: separate SvelteKit browser authoring project. The current separate browser source authors fictional v2 rooms, characters and one supported learning activity; browser interaction verification remains pending. The original Vite/PixiJS Room Studio is a different prototype.
- **Community content package v1**: bounded JSON envelope containing one fictional room scene, declared feature, trusted local asset catalog and attribution. Studio and Godot validate independently; packages contain no arbitrary scripts, URLs or caller-selected resources.
- **Fictional open-source contribution challenge**: default next learning experiment if no other preference is supplied; teaches an edit/review/export task using sample content, without requiring resident or tenant material.
- **Game content package v2**: bounded data for rooms, manual or trusted preset characters and one supported quest, independently validated by Studio and Godot. It remains separate from player progress and contains no arbitrary scripts.
- **Fictional game collision**: geometry controlling movement in a playable scene; it does not establish real building routes or accessibility.

- **Source linework**: immutable source-coordinate pixels and vectors for visible schematic partitions and stair marks. A decorative finish may expand partition lines by at most three source pixels, while preserving source positions and gap locations; stairs remain raw fine marks. The style mask is separate from the raw source audit and does not establish door semantics, interior windows or a navigable graph.
- **Decorative partition mask**: generated pixel-art wall treatment derived from source partitions, with a dark slate body, lighter cap and upper-left highlights. Its bounded cosmetic expansion does not change company anchors or source geometry and cannot create doors or routes.

- **Community catalog v2**: trusted original prop crops, dimensions, collision footprints and manual character enums shared by Studio and Godot. Imported content references its fixed revision rather than choosing files or scripts.
- **White Construct**: fictional built-in Easter egg realm reached through a secret bookshelf. It contains original burgundy chairs and a CRT, retains quest state, and always offers a return. It is not a building room or actual doorway.
- **Contribution Club**: fictional first learning activity using two clues and three answers; completion and a single local reward are practice, not a secure assessment or certificate.

- **Founder/Patch preset**: original bundled directional character artwork. A package selects a whole trusted preset ID; custom palette/part choices use the separate manual appearance. Source atlas sampling scale and normalized world frame scale are separate.
