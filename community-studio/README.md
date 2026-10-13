# BeThereCentral Community Studio

A local SvelteKit authoring app for fictional sample spaces. The three-pane workspace pairs a categorized room-piece catalog with a large room preview and a details panel. Room, People, and Learning activity tabs cover the frozen community v2 package: a fixed 320×224 chamfered room, two characters, and one two-clue/three-answer activity. No publishing or network service is included.

## Run

Requires Node.js 22.17+ and npm. From this directory:

```sh
npm ci
npm run dev
```

Open `http://127.0.0.1:4175/`. `npm run dev` and `npm run build` run `npm run sync-assets` first. That script copies the catalog and original trusted art from `../content/community/assets/` into `static/assets/community/` after verifying source hashes. Run `npm run check`, `npm test`, and `npm run build` to verify the app; the static adapter writes to `build/`.

Choose a category and a thumbnail to add pieces. Drag pieces in the preview, select them to edit the X/Y values, or use the arrow keys for four-pixel moves. Undo and redo cover room and learning-content edits in the open session. The People tab starts with the Founder and Patch looks; Ninon, Morgane, and The Architect are also available as named portraits. Choose Custom character to edit clothing and colors. The large preview shows front, back, and side views plus direction-specific walking frames (The Architect has eight front/side frames and four retained back frames). Presets are cropped from trusted atlases into exact 64×96 RGBA frames shared with the game; custom looks use the shared 64×96 pixel-art run definition. Both display at 32×48 with crisp pixel scaling. The guide greeting is spoken when approached; the player introduction is profile text. The Learning activity tab edits the fixed activity template and all five in-room positions. Every edit is checked against the shared v2 contract before it becomes active.

Save and load are explicit actions using this browser’s local storage. Export downloads a checked v2 package. Import accepts checked v2 files and validated legacy v1 room files; an invalid file leaves the current work unchanged. Files are limited to 65,536 UTF-8 bytes.

The room and label are fictional and not mapped to a tenant. The app has no account, publishing, analytics, or external upload. `npm run sync-assets` copies the catalog, furniture art, and shared avatar definition from `../content/community/assets/` into the local static asset directory. Artwork provenance is documented in `../docs/assets/PIXEL_ART.md` and `../docs/design/generation-prompts.md`.

## Android offline host

The Android map app also opens this Studio through **More → Community Studio · offline**. The APK includes the production HTML, JavaScript and art, served through an isolated local HTTPS WebView origin with outside requests/navigation blocked. It does not connect to a public website or require a server. Save remains explicit browser-local storage; reopen Studio and use Load saved work. Android’s document picker handles user-selected JSON import and bounded JSON export; cancelled exports do not report success. The browser still uses its usual file download.

The Android Gradle build runs `npm run build` and stages output into its dedicated generated-assets folder; run `npm ci` here first. The underscore asset filter is adjusted so SvelteKit’s `_app` runtime is included. Generated output is not committed. Foldable screen/density changes retain the open editor, while unsaved work is not promised after process death. This hosts the authoring UI, not mobile Godot gameplay.
