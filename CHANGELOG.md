# Changelog

## Unreleased — usable Gaussian-splat zoom-out

- Fixed zoom-out stopping after roughly one tap at the lens limit: zoom now continues by moving the camera backwards to an exterior view.
- Kept zoom-in reversible within bounds and Overview as the exact opening reset; all zoom inputs share the same behavior.
- Made zoom buttons stop automatic Look around, matching other direct camera interactions.
- Disabled cache reuse for the Android offline viewer so app updates load the current bundled scene controls.

## Unreleased — project identity and publication

- Added an original GPT-generated BeThereCentral logo with its prompt and provenance.
- Updated clone/setup instructions, current screenshots, agent workflow and the Room Studio roadmap for the public repository.

## Unreleased — light and dark map appearance

- Added session-level Light/Dark map appearance, with warm-white/peach light panels, BeCentral blue accents and an always-charcoal map canvas. Dark panels are the default.
- Kept the floor artwork and navigation session independent of appearance; Explore retains its dark scene and Map restores the selected theme.
- Kept native system icons light against the charcoal canvas while preserving edge-to-edge rendering.
- Brought the initial portrait map closer, preserved explicit full-floor Fit and automatic route overview, and shortened the introductory card.
- Recorded Room Studio as the next milestone after app publication.

## Unreleased — approved coworking floor

- Adopted the creator-approved GPT-generated coworking concept with fictional Orbit, Moss, Spark and North company suites and shared spaces.
- Registered tappable rooms, entrances, corridor paths and guide overlays to the illustration; reused the footprint across four explicitly fictional floors with an illustrative scale.
- Removed baked-in people, clarified the lift approach and widened the café passage in the artwork so the sample guide can follow open paths.
- Preserved BeCentral blue in the interface palette while keeping the map surround charcoal, following the creator's clarification.
- Used the full BeThereCentral name in the app identity and corrected guide facing so westbound movement no longer appears to walk backwards.

## Unreleased — expressive controls and edge-to-edge layout

- Restyled actions with Material 3 Expressive-inspired shapes and filled/tonal emphasis.
- Extended scene/map surfaces edge to edge while reserving safe insets for interactive overlays.

## Unreleased — generated pixel-art world

- Bundled original GPT-generated founder and office-prop atlases with transparent backgrounds and recorded provenance/checksums.
- Restyled the fictional floor map with pixel-style floors, walls and decorative furniture, and exposed generic room names from the existing room register.

## Unreleased — sample pixel guide

- Added an optional pixel-art founder with an orange backpack to preview fictional-map routes, with pause/resume, explicit connector continuation and a sample destination introduction.
- Kept guide progress separate from checkpoint observations, sharing and hunt state; changing routes resets the preview.

## Unreleased — BeCentral design direction

- Adapted the prototype palette from the BeCentral and WeAreFounders websites: warm light text, peach and orange accents, with readable dark ink on selected orange controls. The creator’s subsequent preference keeps the environment and panels near-black rather than blue.
- Recorded the campus context, observed visual references and font/logo boundaries in the experience guide and PRD.

## Unreleased — focused demonstration journey

- Replaced the large promotional overlay with a compact Engine room / Sample scene identity, essential controls and a short gesture hint; added fuller controls in Info.
- Curated a wider, lower-looking interior opening and an explicit Overview recovery action. Lens zoom holds the camera position to avoid moving through nearby captured surfaces; tests cover bounded zoom and canonical recovery.
- Renamed the native map entry to Map demo and clarified that its fictional four-floor building is separate from the captured scene.
- Added experience standards for complete journeys, agent review responsibilities and visual verification. Updated the PRD, README and building-team demonstration guide.

## Unreleased — Android WebView viewport repair

- Reproduced the blank Explore scene in a dedicated Android emulator, then fixed the embedded WebView’s full-screen layout with explicit `MATCH_PARENT` layout parameters.
- Added a one-time visual-state redraw and debug-only viewport-size diagnostics; verified actual scene pixels and controls rather than relying on renderer submission logs.

## Unreleased — Android Explore compatibility

- Changed the WebGL canvas request to RGBA to avoid the RGBX backing format rejected by the connected phone’s WebView.
- Improved failure layout, graphics cleanup and local loading diagnostics; kept the native Map fallback available.
- Set dark Android window/system-bar styling so status icons remain readable.

## Unreleased — Android startup repair

- Enabled the Compose compiler in the Android launcher module, fixing a `NoSuchMethodError` at `MainActivity.setContent` on a physical phone.
- Added a host-only compiled-call regression check and documented phone checks without instrumentation services, respecting the user’s battery constraint.

## Unreleased — interactive sample Explore

- Added a real PlayCanvas Gaussian-splat viewer with a reduced, CC BY 4.0 engine-room sample and bundled offline assets. The scene is independent of the fictional building and has no room hotspots or route overlay.
- Added Android `WebViewAssetLoader` and iOS `WKWebView` adapters, a prominent Explore/Map switch, mobile bundle checks, scene attribution, loading/error treatment and a 2D fallback. Map, checkpoint, hunt and local sharing state stays in the shared app when switching views.
- Kept a loopback browser demo for desktop contributors; the native desktop app still opens on Map. Android/iOS performance and accessibility on physical devices remain to be measured.
- Added camera/bundle/asset-integrity tests and a viewer CI job; enabled native accessibility for the iOS embedded viewer. Documented a two-minute building-team presentation and asset reproduction. Extended the documentation-change gate to viewer sources and recorded the deliberate checked-in runtime bundle exception.

## Unreleased — immersive Explore direction

- Made Gaussian-splat exploration a core product experience alongside 2D navigation and accessibility alternatives.
- Set the next slice as real interactive splat rendering with a distributable licensed sample asset, independent IDs and no faux room hotspots for unmapped scenes, orbit/pan/zoom/reset, loading/error states, state preservation and a 2D fallback.
- Specified mobile loading/first-frame/memory measurements and an enjoyment/orientation study. Renderer choice was open at that planning stage; surveyed room-linked captures remain a later phase.

## Unreleased — map-first interface

- Replaced the form-first layout with a full-viewport dark map and contextual controls, following the user's Matterport design reference.
- Added compact room search and floor selection; secondary tasks appear in dismissible panels while navigation, hunt and sharing state remain at application scope.
- Enlarged sample room footprints, drew corridor-facing door openings and kept search results clear of map controls. The iOS host uses a dark full-screen background with Compose-managed safe areas.
- Kept sample/2D and last-seen status explicit; no 3D assets or new external integrations were introduced.
- Updated the PRD and architecture decision to make immediate map visibility, readable controls and progressive disclosure product requirements.

## 0.1.0 — 2026-10-07 (local prototype)

- Created Kotlin Multiplatform / Compose Multiplatform app with Android, iOS and desktop launchers and pinned Gradle wrapper.
- Added fictional four-floor building with 48 rooms, search, room selection, zoom/pan, floor switching, route previews, stairs/lift choices and step-free routing.
- Added validated sample QR input and explicitly last-seen observations; no camera or continuous positioning integration.
- Added local, opt-in sharing simulation with selected sample people, 5/10/15-minute choices, expiry and revocation. Added disconnected loopback reference server with server-clock/monotonic expiry, recipient authorization and latest-only opaque payload storage.
- Added same-device cooperative checkpoint hunt with ordered progress and player contributions; no networked multiplayer.
- Organized domain/data/presentation code by feature with matching feature tests. Fixed review findings for reverse connector floors, room entrance geometry, phone floor controls, connector markers and state retention across tabs.
- Added PRD with Lean hypotheses, architecture and coordinate contract, privacy/E2EE design, setup/contribution/security guides, CI and documentation-change check.
- Added project-local Matt Pocock skills with MIT notice and recorded source revision; documented Astra/Sol/Luna development roles.
- Built an unsigned iOS simulator host and fixed the required Compose high-refresh-rate plist entry; respected iOS safe areas.

No app-store release, production sharing/E2EE, real building plans/capture, positioning hardware or WebXR integration is claimed. Sample Gaussian-splat rendering is implemented as described above. See `docs/testing/STATUS.md` for verification evidence.
