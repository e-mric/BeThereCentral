# Changelog

## Unreleased — map accuracy and modular ground floor

- Audited all 64 numbered directory entries, repeated badge positions and shared-area occupants against the supplied plans; corrected source badge centers and documented the references.
- Kept the ground-floor bike icon in its separate west-side room and traced the supplied divider continuously without adding a doorway gap.
- Rebuilt ground decoration from reusable surface patterns, wall/window modules and individually placed furniture from the existing campus atlas. Upper four floors retain their illustrations.
- Kept company positions separate from any visual corridor widening; labels wrap and use leader lines as zoom changes, repeat at repeated positions, and disappear when their markers are offscreen. Markers and leader lines render beneath signs to keep names unobscured.
- Preserved source geometry, courtyard openings and the empty charcoal ground silhouette; reserved stairs remain free of furniture. Toilet positions remain pending user input.
- Continued the map and directory product iteration. Godot/SvelteKit migration, gameplay and character creation remain on hold pending resident feedback.


## Unreleased — community game and Studio planning

- Recorded Godot gameplay and SvelteKit browser Studio as the selected next direction, with Compose web considered as an alternative and the existing prototypes retained.
- Added a phased plan for portable content, one playable learning quest, manual character creation and later campus expansion.
- Put implementation on hold until resident feedback is incorporated into the PRD and the user explicitly resumes. This change contains documentation only.

## Unreleased — courtyard walls

- Replaced bare courtyard mask edges with substantial slate wall caps, framed blue-grey windows and corner piers. These decorative details scale with the map and stay on the building side of the source-derived courtyard boundary.
- Kept courtyard interiors empty charcoal, the original floor geometry unchanged, and the ground grey area unfurnished. Window placement is fictional illustration, not a surveyed facade.
- Recorded the open-source community-game direction and future Studio character creation in the PRD/backlog; Godot evaluation, playable characters and quests are planned, not implemented.

## Unreleased — bounded Room Studio and plan-world presentation

- Added a local PixiJS one-room editor prototype using the shared fictional room JSON fixture; supports furniture editing, undo/redo, browser-local save/reload and JSON import/export.
- Added a Compose preview under More that accepts scene JSON or resets to the starter fixture; preview edits are transient and are not saved or published.
- Kept the demo room's chamfered geometry immutable and unconnected to real tenants or plan anchors. AI generation, accounts, publishing and real-room editing remain future work.
- Kept the main canvas charcoal with empty courtyard voids, one floor selector sheet and a compact translucent zoom/Fit overlay. Preserved the full ground silhouette in muted charcoal while leaving the unassigned grey source area unfurnished. Garden planting with perspective scaling remains a future art option.
- Recorded the bounded experiment and its limits in ADR 0007; updated the plan-world decision amendment and product/demo documentation.

## Unreleased — five-floor BeCentral pixel world

- Replaced the active fictional rectangular map with five distinct interpretations of the supplied ground-to-fourth-floor schematics, with editable geometry, courtyard cutouts, unmapped areas and stair-core patches.
- Added five richly furnished GPT floor illustrations with explicitly fictional interiors, clipped to source-derived outline/courtyard masks, plus source-labelled company/place search, floor selection, pan/zoom/Fit and factual pixel-host introductions. Repeated badges share a legend entry; fourth-floor The Sky remains unplaced.
- Parked in-app 3D, fictional directions, checkpoints, sharing and hunt to prevent mixing their sample coordinates with the supplied plans. The experiments and tests remain in the repository.
- Recorded unknown scale, approximate trace and illustrative furnishings rather than treating schematics as surveyed navigation.

## Unreleased — Android CI SDK setup

- Overrode setup-android’s legacy default package list to request platform-tools, avoiding failure on the retired tools package before Gradle runs. Android 36 and Build Tools 36.0.0 remain explicitly installed.

## Unreleased — capture planning and costs

- Expanded the building pilot guide with LiDAR, photos/video and pixel-art reference capture, a one-room export test, sourced software/store pricing, and phased spending allowances with labor and recurring costs separate.
- Distinguished Scaniverse Classic on-device processing from its cloud workflow, and recorded export/publication checks before scaling capture.

## Unreleased — direct Map and 3D navigation

- Replaced the four-action bottom menu with direct Map/3D switches and top-right More in both views.
- Kept Find a room visible on Map; Plan a route opens simulated starting-point choices, and route details can change the start.

- Added a building-team pilot request checklist for permitted pixel-art and 3D capture work.
- Recorded an unresolved iOS simulator accessibility crash during sheet dismissal; Android verification and Samsung installation are documented separately.

## Unreleased — direct 3D camera controls

- Added a compact right-side Top / + / − stack, clear of the bottom view switches.
- Top now frames the full sample from an inclined, zoomed-out view and fits portrait/landscape dimensions.
- Kept attribution and optional Look around behind the information button.

## Unreleased — room hosts and clearer discovery

- Installed this update on the physical Samsung after reconnection; ordinary ADB replacement installation and cold launch succeeded.

- Added pixel hosts explaining all 48 sample rooms, fictional company missions, reception community links and an explicit Get directions action.
- Kept bottom navigation visible in both map and 3D, with pixel icons and a high-contrast blue/white directions button.
- Added a bottom 3D shortcut to the same unrelated reception sample, preserving map state on return.
- Replaced the reception pill with an original GPT-generated wooden pixel-art sign and an accessible 48 dp minimum hit target.
- Removed text chevrons, explained Start point, aligned lobby names with map floors and put QR-code testing behind a secondary action.
- Expanded the local QR hunt with educational area/company cards, readable without playing.
- Reduced the Android launcher B mark by 20%, adding a 10% inset on every side.

## Unreleased — pixel-art home and quieter controls

- Replaced Explore/Map tabs with a ground-floor reception doorway into the clearly unrelated 3D sample; retained map viewport and feature state on return.
- Moved search, floor/zoom controls, route details and secondary tools into bottom sheets; kept three map actions and two scene controls visible.
- Added the approved BeThereCentral wordmark on Android/iOS and a GPT-generated Android launcher mark derived from it.
- Added doorway placement/floor tests and updated the PRD, demo, coordinate contract and architecture decision.
- Installed this update and its launcher logo on the physical Samsung; replacement installation and cold launch succeeded without phone instrumentation.

## Unreleased — usable Gaussian-splat zoom-out

- Fixed zoom-out stopping after roughly one tap at the lens limit: zoom now continues by moving the camera backwards to an exterior view.
- Kept zoom-in reversible within bounds and all zoom inputs on the same behavior; Top now fits the full inclined capture.
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
- Curated a wider, lower-looking interior opening and camera recovery. Lens zoom holds the camera position to avoid moving through nearby captured surfaces; tests cover bounded zoom and recovery.
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
