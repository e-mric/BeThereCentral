# Room Studio browser prototype

A fictional local editor for decorative room layouts. It uses the same trusted room fixture consumed by the native app at `composeApp/src/commonMain/composeResources/files/studio/demo-room.json`; the starter scene is not duplicated. The browser draws rugs behind the other furniture, then draws remaining objects in their JSON order. The company label is HTML above the scene so it remains readable and is not part of the bitmap artwork.

## Run locally

Requires Node.js 20.19+ or 22.12+ and npm. From this directory:

```sh
npm ci
npm test
npm run dev
```

Vite serves only on `127.0.0.1:4174`. The first dependency install needs network access. After the page and its local packages are loaded, editing needs no network, account, backend, API or analytics. The page uses system fonts and has no remote asset requests.

Use the furniture list and directional buttons when dragging is inconvenient. Arrow keys move the selected piece; Delete removes it; Ctrl/Cmd+Z and Shift+Ctrl/Cmd+Z undo and redo. Save writes this browser's local storage; Export and Import exchange the strict JSON scene shape with the native sample.

This is a fictional room prototype. It has no account system, publishing, review workflow, AI content generation, measured room survey or real tenant directory. Import accepts only schema version 1, the exact trusted `studio-demo` room boundary, IDs up to 40 safe ASCII characters, and JSON sources up to 65,536 characters. A rejected file leaves the current scene unchanged.
