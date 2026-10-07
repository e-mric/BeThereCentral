# Sample Gaussian-splat viewer

A real PlayCanvas WebGL2 Gaussian-splat viewer, bundled into the Android and iOS app. This is a separate, clearly labelled sample space. It has no room IDs, route overlay or implied alignment with the fictional four-floor map or the proposed real building.

## Run and rebuild

The checked-in `dist/` works without npm or an external service at runtime. Android packages it as application assets; the iOS build copies it into the app bundle. On a laptop:

```sh
cd exploration
python3 -m http.server 4173 --bind 127.0.0.1 --directory dist
```

Open `http://127.0.0.1:4173`. Stop with Ctrl-C. This serves only on your computer. It does not publish the demo. The desktop Compose app remains the 2D preview; use this browser viewer for the laptop's 3D presentation.

To change the viewer (Node.js 24 used locally):

```sh
cd exploration
npm ci --ignore-scripts
npm run build
npm test
```

The lockfile pins dependencies. Commit source and rebuilt `dist/` together. `build.mjs` verifies all required scripts, styles and licence notices; mobile packaging also checks those files. Tests cover camera behavior, scene integrity and missing bundle resources. Native smoke-test evidence belongs in `../docs/testing/STATUS.md`.

## Controls and limits

Drag to orbit; scroll/pinch or use +/− to zoom. Shift/right-drag or use two fingers to pan. Keyboard arrows rotate, +/− zoom, and R returns to Overview. Look around starts an optional slow orbit; Pause, direct interaction or hiding the page stops it. No movement starts automatically. About shows provenance and first-frame timing/recent rendering rate. These measurements are local; no telemetry is sent.

This is free camera exploration, not a walkable collision model or indoor positioning. Moving through captured surfaces can reveal holes and missing detail. Overview returns to the curated interior view, restoring the camera target, orbit and lens together. Zoom first changes the field of view between 35° and 85°, then moves the camera backwards up to 35 sample-scene units so you can see the capture from outside. Zooming in reverses that path. Passing through captured surfaces can briefly obscure the view; this sample has no collision model. All zoom inputs share the same range, and zoom buttons stop Look around. These scene units are not surveyed building metres. The native **‹ Map** action stays available independently of WebGL. The mobile app enters this viewer through a clearly labelled sample doorway at ground-floor reception; the capture does not depict that room. Overview stays visible; View controls opens a bottom sheet with zoom, Look around and attribution. The sample has no floor switching; the separate fictional map has four floors.

The renderer uses a CPU sorting worker, not SharedArrayBuffer or a remote service. Its WebGL2 canvas requests RGBA (`alpha: true`) for Android WebView compatibility; the camera still clears to an opaque background. Failure clears the watchdog and destroys renderer resources; hidden pages do not render. Local console markers record loading stages and first frame, without telemetry. The asset is embedded in a local classic script and passed as bytes to the loader, so iOS does not need unrestricted `file://` fetch. The base64 transport increases on-disk size; the compressed PLY itself is 10,583,540 bytes. JavaScript, decoding, GPU textures and sorting require additional memory. No physical Fold performance claim is made.

## Asset provenance and reproduction

**Tugboat Bat engine room, Trieste Italy (XGRIDS PortalCam)** by **Tosolini**, [creator profile](https://superspl.at/user/tosolini). The [original scene page](https://superspl.at/scene/1a14e1e7) lists **CC BY 4.0** and the creator's public `.splat` download. Source and licence were checked on 7 October 2026. The licence and attribution travel with both the source asset and mobile/browser bundles. No endorsement is implied.

This capture shows a tugboat's engine room, not the building proposed for BeThereCentral. The creator describes capture using XGRIDS PortalCam and Lixel Cyber Color processing. We reduced the original **7,939,602** Gaussians to **650,000** and converted to compressed PLY using MIT-licensed `@playcanvas/splat-transform` **3.10.0**. This trades detail for a manageable prototype. Rotation and initial camera pose are display settings only.

To reproduce from the creator's linked download, save it outside the repository as `/tmp/bethere-engine-room.splat`, verify its SHA-256 below, and run from this directory:

```sh
./node_modules/.bin/splat-transform /tmp/bethere-engine-room.splat \
  --decimate 650000 /tmp/bethere-engine-room-reduced.ply
./node_modules/.bin/splat-transform /tmp/bethere-engine-room-reduced.ply \
  public/engine-room.compressed.ply
npm run build
```

| Artifact | SHA-256 |
| --- | --- |
| Creator's source `.splat` (254,067,264 bytes) | `1ac9d094cfe5a54da77524d10cee88e035f4e9cc60568fbb08bb854a72e30890` |
| Bundled derivative `.compressed.ply` | `def08d55dd968491f8940398379c0592490c239bfaa144db8d679d30dc3d97f1` |

Conversion ran locally; the source download and large intermediate are not committed. A future conversion on another platform may differ in floating-point output; review it and deliberately update the integrity fixture. See [asset licence notice](public/SCENE-LICENSE.txt). Asset licensing is separate from the project's MIT code licence. Renderer: [PlayCanvas 2.23.1, MIT](dist/PLAYCANVAS-LICENSE.txt). No Inria research implementation is bundled.
