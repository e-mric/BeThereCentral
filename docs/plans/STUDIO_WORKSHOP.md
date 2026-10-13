# Community Studio workshop layout

13 October 2026 · user-approved visual direction

## Problem and bounded change

The phone editor renders, and the user confirmed that its unfolded layout displays correctly. Its large introduction, status badges and stacked panels make the experience feel generic and push the room below the first screen. The user approved the [generated workshop concept](../design/STUDIO_WORKSHOP_CONCEPT.png) and authorized implementation.

Build a canvas-first creative workspace using the existing trusted pixel art. Keep the current fictional room shape, content contracts, character assets, local storage and Android file-picker host. The concept is a visual reference, not a source of replacement game sprites or new real-building geometry.

## Implementation sequence

1. Replace the introductory hero with a compact cream header: Studio identity, room title, undo/redo, explicit local Save and a file menu.
2. Provide Room, People and Activity navigation around a charcoal workspace. Make the room or character preview the main surface; keep activity fields reachable and readable.
3. Put categorized, correctly clipped pixel-art thumbnails in a cream shelf below the room. Add orange selection handles and contextual Move, Duplicate and Remove. Keep numeric positions, nudges and the object list reachable.
4. Preserve transactional history and checked import/export. Duplicate must retain dimensions, create a unique ID and either find a valid bounded placement or leave the room unchanged. Zoom, if implemented, must share the drawing and pointer transform.
5. Verify the rendered browser at wide, unfolded, folded and short landscape sizes, then the packaged Android editor on an emulator. Rebuild/install/launch on the user's connected foldable and ask the user to verify its appearance.

## Acceptance and evidence

- First screen is the working room, with no marketing hero or decorative slogans.
- Actual catalog sprites have correct crop bounds and a coherent scale; no adjacent atlas pixels leak into thumbnails.
- Room edits, undo/redo, save/reload and bounded file handling continue to work. Furniture hotkeys do not change hidden room objects while editing People or Activity.
- Large character facing/Stand/Walk previews and all existing activity fields remain available.
- Controls remain reachable at narrow width and short height; native Back remains visible. Fold changes retain an unsaved edit within the running app.
- Browser, emulator and physical-device evidence remain separate. Phone inspection services and UI automation remain prohibited; ordinary ADB install/launch and narrowly scoped crash diagnostics are permitted.

Record commands and actual observations in [testing status](../testing/STATUS.md). Update affected experience/demo documentation and CHANGELOG, review the integrated change, then commit and push under existing authorization.

## References and generation

The inspected Mobbin references were [IKEA's room canvas](https://mobbin.com/screens/bbf73f73-8cd8-44ec-a5ee-b398be2ed1f1), [Replika's furniture tray](https://mobbin.com/screens/31058bb0-c060-435d-b030-caaae13e3c78) and [Craft's drawing palette](https://mobbin.com/screens/d4404d9e-bff6-4fd9-83ef-2b84b127c7cb). Their interaction patterns inform original UI; screenshots and proprietary assets are not bundled.

The concept was generated with the built-in imagegen tool. Prompt direction: an unfolded, canvas-first BeThereCentral editor with a compact cream topbar, charcoal work surface, Room/People/Activity rail, orange selected-piece controls and cream category shelf; retain fictional/offline semantics and use coherent original pixel furniture. A refinement removed decorative slogan boards and text from the room. The generated layout is not a measured or tested UI specification; implementation uses the existing artwork and fixed room contract.
