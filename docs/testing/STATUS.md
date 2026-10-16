# Verification status

7–9 October 2026 · local Apple Silicon macOS environment

## Environment inspected

Initial folder was empty and not a Git repository. Available tools: Eclipse Temurin JDK 17.0.20.1, Xcode 26.6, iOS 26.5 simulator runtime, Android SDK platform 36, ADB, Python 3 and XcodeGen. No Android device was connected. No GitHub remote or GitHub CLI was configured. A local Git repository was initialized; nothing was published.

## Checks

| Check | Outcome |
| --- | --- |
| `:composeApp:desktopTest` | 12 tests passed after map redesign; shared feature behavior on JVM |
| `:composeApp:testAndroidHostTest` | 12 tests passed after map redesign; same shared suite on Android host test target |
| `:androidApp:assembleDebug` | Passed; debug APK generated |
| `:composeApp:linkDebugFrameworkIosSimulatorArm64` | Passed; simulator framework linked |
| Xcode Debug simulator host build with `CODE_SIGNING_ALLOWED=NO` | Passed |
| iPhone 17 Pro / iOS 26.5 simulator launch | Passed after fixing required Compose plist entry |
| iOS manual interaction | Sample QR recorded timestamp; top-floor destination selected; step-free preference toggled |
| `:composeApp:createDistributable` | Passed; standalone macOS app created and launched |
| Desktop manual interaction | Sample checkpoint, cross-floor destination, route summary/map; hunt rejects out-of-order finds and keeps accepted progress across tabs; sharing requires a checkpoint |
| `python3 -m unittest discover -s server/tests -t . -v` | 11 tests passed, including real loopback HTTP and wall-clock rollback regression |
| Hosted GitHub Actions | Configuration included; first remote run not yet verified |

Shared behavior tests live in feature packages. The server tests cover authentication/recipient isolation, supported durations, expiry at the exact boundary, monotonic protection against clock rollback, revocation, replacing the latest payload and restart without persistence. They use only synthetic data.

Astra reviewed scope, privacy and the implementation. Its clock-rollback, phone-floor-control, hunt-state-retention, connector-rendering and domain-dependency findings were fixed; a final targeted source review found no remaining blockers in those fixes. This does not replace independent security or accessibility review.

## Repeat locally

### Map-first interface follow-up

After the interface redesign, the combined Gradle gate passed again: `:composeApp:desktopTest` (12 tests), `:composeApp:testAndroidHostTest` (12 tests), `:androidApp:assembleDebug`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`, and `:composeApp:createDistributable`. The additional geometry regression checks that every room entrance opens onto the central corridor. The unsigned Xcode simulator host was rebuilt successfully with the full-screen SwiftUI wrapper and teal route-chip palette.

Manual checks on iPhone 17 Pro / iOS 26.5 confirmed room search, selecting Library 4, accepting the simulated Level 1 lobby checkpoint, automatic route preview, changing stairs to a lift with Step-free, switching to the third floor, zoom in/out reversal and overview reset. Search hides map controls to avoid overlap. The host's dark safe areas and the route preview were inspected visually; the [captured screen](../screenshots/ios-step-free-route.png) records the final layout. Astra's targeted interface review prompted safe-area, keyboard-dismissal and shared panel-opening fixes.

These are manual smoke checks, not automated UI coverage. The unchanged server suite was not rerun for this interface and sample-geometry follow-up; its earlier result remains recorded above.

### Interactive sample Explore follow-up

The combined Gradle gate passed after the mobile viewer integration: `:composeApp:desktopTest` (12 tests), `:composeApp:testAndroidHostTest` (12 tests), `:androidApp:assembleDebug`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`, and `:composeApp:createDistributable`. The unsigned Xcode simulator host build passed again after enabling native accessibility on the embedded view. APK inspection confirmed all six required offline viewer files were packaged.

`npm run build` and `npm test` in `exploration/` passed: six tests cover camera behavior, source asset checksum/splat count, required bundle files and rejection of missing resources. The build and native packaging validate scripts, styles and licence notices. The new hosted viewer CI job has not run.

Manual browser checks confirmed actual Gaussian rendering, zoom, reset, attribution and a missing-scene failure message with Retry. One local Codex browser observation reported a 2.82-second first frame and approximately 25 FPS. This is a single development observation, not a physical-phone benchmark; GPU memory has not been measured.

On iPhone 17 Pro / iOS 26.5 simulator, the bundled scene visibly rendered inside the app. Map → Explore → Map retained Library 4, the exact last-seen checkpoint/time and the selected Step-free lift route. See the [Explore capture](../screenshots/ios-splat-explore.png). Native accessibility is enabled, but the inspection tool did not expose embedded web controls: VoiceOver usability remains unverified. Hunt and grant view-switch retention are supported by shared state ownership, not separately exercised in this smoke check.

Android rendering was not exercised: the available emulator was in use by another task. No physical Samsung Fold or iPhone was tested. The unchanged Python server suite was not rerun for this slice; its earlier result stands. No building imagery was captured or uploaded.

### Physical Android startup repair

A connected Samsung SM_F971B exposed a launch failure that assembly and shared host tests had missed: `MainActivity.setContent` raised `NoSuchMethodError`. The Android launcher did not apply the Compose compiler plugin, so its call used `Function0` instead of the transformed composable signature. Adding the plugin repaired the call; independent host bytecode inspection confirmed `Function2`. `python3 scripts/check_android_compose.py` passed locally; CI now runs the same host-only regression check after assembly.

`:androidApp:assembleDebug` and `:composeApp:testAndroidHostTest` passed after the fix. The APK was reinstalled on the physical phone with ordinary `adb install -r --user 0`. A single explicit cold launch returned `Status: ok` (395 ms Activity launch, **not** scene first-frame time); the app process remained present and its bounded crash-buffer check was empty. Visual splat rendering, interaction and phone performance remain unverified by the agent.

The first CLI-based layout attempt reported that its instrumentation APK could not be installed. The user then prohibited instrumentation inspection because of battery cost. No further instrumentation commands were run; installation, launch and crash checks used ordinary ADB. The restriction is recorded in `AGENTS.md`. No phone screenshot was retained as verification evidence.

### Android Explore graphics repair

The user's phone screenshot subsequently showed Explore's failure UI, despite the successful Activity launch. A bounded process-scoped Chromium log identified rejected `RGBX_8888` AHardwareBuffer texture allocation followed by graphics-device creation failure. The viewer now requests an alpha/RGBA context (the camera still clears to opaque), avoiding that allocation path. Its error panel hides scene controls, can scroll, clears the loading timer and releases graphics resources through deferred destruction. Late asynchronous completions cannot revive a failed viewer. Android system-bar/window styling now matches the dark app.

`npm run build`, all seven viewer tests and `:androidApp:assembleDebug` passed. The new lifecycle test verifies deferred destruction without assuming a nonexistent engine stop method. Astra reviewed the final patch. Browser rendering and zoom were visually checked again; a missing-scene browser fixture showed the heading, explanation and Retry without overlapping controls.

The updated APK was installed using ordinary ADB and cold-launched successfully on the connected Samsung SM_F971B. One process-scoped log snapshot reached graphics-device, scene-load, first-frame and ready, reporting 705 ms from viewer startup to its first splat frame. The earlier RGBX failure was absent in that snapshot. This is a single log observation, not a performance benchmark or visual validation; the user’s confirmation of the phone image is pending. No instrumentation service, screenshot capture or continuous monitoring was used for this repair. iOS was not rebuilt or rerun for this shared viewer change.

### Android emulator viewport regression

The user confirmed the RGBA update still left Explore blank on the phone. Its prior ready/first-frame markers only established renderer submission; they did **not** establish visible pixels. A dedicated `betherecentral-demo` Pixel 7 / Android API 36 ARM64 emulator reproduced a blank scene with only the HTML header visible and the footer absent.

The Android adapter now assigns `MATCH_PARENT` width and height before loading the WebView and invalidates once after a visual-state callback. A one-time debug-only snapshot reports dimensions, not page content. `:androidApp:assembleDebug` passed. After reinstalling on the emulator, native bounds of 1080×2033 matched the density-scaled HTML viewport/body/canvas of 412×775, and the engine-room splat and all footer controls were visually present. Zoom input changed the view; Map opened; returning to Explore visibly restored the scene. The [Android emulator screenshot](../screenshots/android-splat-explore.png) records the result.

All visual inspection and layout tooling for this follow-up ran on the dedicated emulator. No instrumentation was installed or run on the physical phone. The repaired APK was then installed on the phone with ordinary ADB. Its native bounds (1248×1655) and density-scaled HTML viewport/body/canvas (475×630) also agreed; the app was brought to the foreground. Physical-device appearance still requires user confirmation; emulator success is not a Fold performance or lifecycle guarantee. Shared logic and the unchanged server were not retested for this Android-only adapter change.

### Focused demonstration journey

`npm run build` and all eight viewer tests passed, including bounded lens zoom that preserves camera position and canonical Overview recovery. `:composeApp:testAndroidHostTest` passed (12 tests); `:androidApp:assembleDebug`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`, the unsigned Xcode simulator host build, and `python3 scripts/check_android_compose.py` passed. Astra found no critical blocker in the camera, lifecycle and sample-separation changes.

Actual browser checks confirmed the approved opening, zoom in/out, orbit and Overview recovery. On the dedicated Pixel 7 / API 36 ARM64 emulator, the final scene and all controls were visible; Zoom in enlarged the scene and Overview restored it. The Map demo introduction identified the fictional building. A simulated Level 1 lobby checkpoint, Library 4 search, default stairs route and Step-free lift route were exercised; switching to floor 3 showed the destination segment. Explore loaded again after switching, and returning to Map retained Library 4, Step-free and the exact last-seen timestamp. The route displayed 47 m / approximately 2 min / Lift. Screenshots were refreshed for [Explore](../screenshots/android-splat-explore.png) and [the route](../screenshots/android-step-free-route.png).

Android's device connection and layout service were intermittently unavailable before recovering. An emulator keyboard stylus tutorial interrupted search; dismissing it allowed the search check to complete. These are manual smoke checks, not automated UI coverage or phone performance measurements.

On iPhone 17 Pro / iOS 26.5 simulator, the rebuilt app visibly rendered the new opening, compact identity, Overview controls and fictional-map introduction; the [Explore screenshot](../screenshots/ios-splat-explore.png) was refreshed. Camera interactions for this slice were exercised in the browser and Android emulator, not repeated on iOS. VoiceOver remains unverified. The physical phone was disconnected and received no new install or instrumentation. Unchanged sharing/server behavior was not rerun.

### Pixel world, guide and dark edge-to-edge interface

The combined Gradle gate passed: `:composeApp:desktopTest` and `:composeApp:testAndroidHostTest` each ran 19 tests (including seven guide timeline tests), `:androidApp:assembleDebug`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64`. `npm run build` and all eight viewer tests passed. The Android APK and final iOS app bundle were inspected for both generated PNG atlases. Android KMP resources needed explicit enablement; the first APK omitted the artwork despite compiling successfully. This was corrected before visual verification.

The unsigned Xcode host build passed using the specific iPhone 17 Pro simulator destination. An earlier generic simulator build selected an unsupported x86_64 architecture; choosing the configured ARM64 simulator resolved that build failure. A later presentation-only correction moved bottom-row room labels clear of the sofa, followed by successful Android, iOS framework and Xcode host rebuilds.

Actual browser, Pixel 7 / API 36 emulator and iPhone 17 Pro / iOS 26.5 simulator views showed the charcoal Explore environment, floating rounded controls, persistent sample badge and edge-to-edge scene with safe interactive overlays. The iOS map visibly loaded generated furniture, tiled floors and generic room labels; the final label correction was checked on screen. On iOS, simulated Level 1 lobby → Library 4 selection, Step-free (47 m / approximately 2 min / Lift), the generated founder walking, all three explicit lift-floor continuations and arrival at Library 4 were exercised. The last-seen checkpoint and exact timestamp stayed unchanged throughout the guide preview.

Astra reviewed guide separation, artwork anchoring, route visibility, entrance geometry, resource packaging and the sample distinction. Its embedded sample-label and searchable-label findings were fixed. These checks are manual smoke tests, not comprehensive accessibility, landscape, foldable or background-lifecycle coverage. The Android inspection service was intermittently unavailable/offline and recovered; it was used only on the dedicated emulator. The physical phone was disconnected and received no new install or instrumentation. The unchanged reference server was not rerun.

### Approved coworking illustration and final appearance — 8 October

The final Gradle gate passed `:composeApp:desktopTest` and `:composeApp:testAndroidHostTest` (24 tests each), `:androidApp:assembleDebug`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64`. The final unsigned ARM64 iPhone 17 Pro host build also passed. Guide tests cover facing during westbound, vertical and stationary segments, pause/arrival and floor continuation; route tests verify graph endpoint continuity. Astra reviewed the bounded layout change and its fixed light-on-charcoal wordmark.

The new sample has registered room doors and corridors on the approved coworking illustration; all four floors repeat the fictional footprint. On iOS, Level 1 lobby → Orbit routes were exercised before final panel polish: Ground floor 38 m, Third floor 71 m by stairs or 79 m by lift with Step-free. All three lift continuations were invoked; guide progress did not update the checkpoint timestamp. Final arrival was not separately captured for this illustrated route.

The final Android APK was installed on the dedicated Pixel 7 / API 36 emulator. Explore visibly rendered the sample scene. Map search selected Orbit · Ground floor after a simulated lobby checkpoint, calculated 38 m, and rendered the guide facing left on the westbound corridor. The saved [guide capture](../screenshots/android-coworking-guide.png) shows the illustration, route, last-seen timestamp and preview disclosure. This verifies the reported backwards-facing defect visually as well as in tests.

The final iPhone 17 Pro / iOS 26.5 host visibly rendered the larger initial portrait floor, full BeThereCentral title, compact introduction, light panels on charcoal, and white system icons. Fit restored the whole floor. The [light-panel screenshot](../screenshots/ios-coworking-light.png) records the new appearance. These remain manual smoke checks; no physical-phone installation, performance benchmark or accessibility audit is implied. The phone stayed disconnected and received no instrumentation.

Before publication, all 11 reference-server tests passed again, and `scripts/check_android_compose.py` verified the compiled Activity signature. A scoped scan of publishable files found no matching private-key or common API-token patterns; no file exceeds 50 MB. This is not a comprehensive security audit.

Earlier screenshots and checks above describe earlier prototype slices. The README now uses current illustrated-map captures. Room Studio is deferred to the next milestone. The generated logo is a separate transparent raster concept, documented in `docs/brand/README.md`.

### Physical Android update — 8 October

Installed the final illustrated-map/panel-theme debug APK on the connected Samsung SM-F971B using ordinary ADB replacement installation. Installation succeeded; a cold launch of `com.betherecentral/.android.MainActivity` returned `Status: ok`, and the app process remained present in the subsequent check. No instrumentation service was installed or run on the phone. Visible rendering and interaction on this physical device still require user confirmation; emulator/simulator visual results above are separate evidence.

### Publication status — 8 October

The prototype was pushed successfully to `main` at `https://github.com/e-mric/BeThereCentral` on 8 October 2026 after GitHub CLI authentication. The initial HTTPS attempt had failed because credentials were unavailable. Source publication is complete; this is not an app-store release. The first hosted CI run is tracked on the repository Actions page; local build and test evidence above does not imply a hosted CI pass.

### Gaussian-splat zoom-out fix — 8 October

The reported phone limitation came from lens-only zoom: the opening 75° field of view reached its 85° cap after one minus tap. Zoom now uses a reversible framing value: lens range 35°–85°, followed by backward camera movement up to 35 sample-scene units. Buttons, pinch, wheel and keyboard call the same function. The zoom buttons also stop automatic Look around.

All 10 viewer tests and `npm run build` passed. Android `:androidApp:assembleDebug :composeApp:testAndroidHostTest` passed (24 host tests). The APK viewer asset checksum matches the rebuilt runtime bundle. Android's offline WebView now uses `LOAD_NO_CACHE` so app updates read the bundled viewer rather than an older cached script. Astra reviewed the bounds, inverse behavior, invalid inputs and canonical reset.

A fresh 412×915 browser viewport showed the whole engine-room capture after ten minus taps, enlarged it again after three plus taps, restored the interior with Overview, and stopped Look around after a zoom button. The original local browser tab had reused an older script; a fresh local origin confirmed the rebuilt bundle. The dedicated Pixel 7 / API 36 emulator also visibly reached the exterior view after ten minus taps: [actual capture](../screenshots/android-splat-zoom-out.png). Pinch shares the tested zoom function but was not separately exercised as a physical gesture in this check.

The rebuilt APK was installed successfully on the Samsung SM-F971B through its existing ADB service on port 5038. An initial launch encountered Android's package-update screen; a subsequent explicit cold launch returned `Status: ok` for the app, and its process was present. No phone instrumentation was installed or run; physical rendering still needs user confirmation. The iOS host was not rebuilt or visually rechecked for this change; its next build will consume the updated shared viewer bundle.

### Pixel-art home, reception doorway and sheets — 8 October

User problem: persistent tabs, floor buttons, zoom controls and a large introductory card competed with the space. The experiment opens the pixel-art world, with Rooms / Set start / More actions, a floor chip and a compact journey card. Ground-floor reception launches an explicitly unrelated 3D sample. This is not a surveyed connection to the capture.

Commands run with the local Android SDK supplied through `ANDROID_HOME`:

```sh
npm test --prefix exploration
npm run build --prefix exploration
./gradlew :androidApp:assembleDebug :composeApp:desktopTest :composeApp:testAndroidHostTest :composeApp:linkDebugFrameworkIosSimulatorArm64
python3 scripts/check_android_compose.py
xcodebuild -project iosApp/BeThereCentral.xcodeproj -scheme BeThereCentral -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=CBB251C4-BDA9-415D-9D6B-DF10DB661509' CODE_SIGNING_ALLOWED=NO build
```

Results: 10 viewer tests passed; 26 shared feature tests passed on each of desktop JVM and Android host (including two new doorway placement/floor tests). Android APK and iOS simulator framework/host builds succeeded. The Android Activity signature check passed. Packaged APK `assets/index.html`, `viewer.js` and `style.css` match the rebuilt runtime bundle. An initial build without the local SDK environment failed; supplying it resolved the build. An initial packaging check assumed a nested asset directory and was corrected to the actual `assets/` entries.

**Dedicated Android emulator (Pixel 7 / API 36, emulator-5556):** visibly checked the map opening, logo, reception entry, real rendered engine-room scene, View controls sheet and About attribution. Used the sheet's zoom-out button, returned to Map, changed map zoom and entered/returned again: the reception marker retained the changed projected position. Switching to the first floor removed the doorway. Native return-to-map remained available while scene information was open. Visually checked the new B launcher icon in the app drawer. Screenshots: [map home](../screenshots/android-minimal-map.png), [scene](../screenshots/android-minimal-explore.png), [launcher](../screenshots/android-launcher-logo.png). The scene identity was given an explicit line height after visual inspection caught its second line being clipped.

**iPhone 17 Pro / iOS 26.5 simulator:** visually checked the map/wordmark and actual scene pixels entered from reception, then returned to Map. Set simulated Level 1 lobby, searched Orbit, selected Third floor and opened route details. Default showed Stairs; Step-free changed to Lift. Played the guide through all three explicit floor transitions to arrival; last seen remained Level 1 lobby. More opened the secondary tools and Light panels retained the charcoal environment. These are simulator smoke checks, not an accessibility audit or physical-device performance result.

**Physical Samsung:** initially disconnected, then reconnected on 8 October. Installed the APK built from `db56135`, including the new launcher logo, on the Samsung SM-F971B through ordinary ADB on port 5038. Replacement installation returned `Success`; explicit cold launch returned `Status: ok` for the app (605 ms), and a subsequent process check found it running. No phone instrumentation service was installed or run. Visible appearance and interaction remain for the user to confirm; simulator screenshots are separate evidence.

```sh
adb -P 5038 -s <connected-Samsung> install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb -P 5038 -s <connected-Samsung> shell am force-stop com.betherecentral
adb -P 5038 -s <connected-Samsung> shell am start -W -n com.betherecentral/com.betherecentral.android.MainActivity
adb -P 5038 -s <connected-Samsung> shell pidof com.betherecentral
```

**Browser:** viewer tests and bundle build passed; standalone browser visuals were not rechecked in this slice. The HTML/CSS controls were visually exercised in Android's embedded browser. Reference-server behavior was unchanged and its tests were not rerun in this slice.

Astra/Sol/Luna delegation was attempted but the delegated agents hit usage limits before editing. The parent completed implementation and review directly; this slice does not claim an independent Astra review. Remaining follow-up includes physical-phone confirmation, full screen-reader/large-text checks, gesture testing on hardware and Android system-back navigation from the secondary scene (the visible native Map button is the verified return path).

### Commands

```sh
./gradlew :composeApp:desktopTest :composeApp:testAndroidHostTest :androidApp:assembleDebug
python3 -m unittest discover -s server/tests -t . -v
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

For the iOS host, select an available simulator in Xcode, or use:

```sh
xcodebuild -project iosApp/BeThereCentral.xcodeproj \
  -scheme BeThereCentral -configuration Debug -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
  -derivedDataPath iosApp/build/DerivedData CODE_SIGNING_ALLOWED=NO build
```

Stop `:composeApp:run` before running another Gradle build in the same checkout: the foreground app task may hold the project lock. A standalone app created by `:composeApp:createDistributable` can stay open during builds.

## Remaining limits

- Physical Android installation and cold launch checked as above; Android emulator visual smoke checks completed as above; no signed iPhone/device test, App Store/Play deployment or Intel simulator verification.
- No automated Compose UI regression suite yet. Manual checks are smoke tests, not a full accessibility audit. VoiceOver/TalkBack, large text, narrow screens and device lifecycle require dedicated follow-up.
- No camera scanning, continuous positioning, real recipient sharing, real multiplayer, production identity/TLS or E2EE verification: these integrations do not exist yet.
- No real-building geometry/accessibility survey or navigation usability study. PRD thresholds remain hypotheses.
- Future edits must update this record with the actual commands/results; never count `NO-SOURCE`, compilation alone or an unexecuted CI configuration as test coverage.

### Pixel signs, room hosts and persistent 3D navigation — 8 October 2026

Problem/experiment: make the sample doorway fit the pixel world, make route origins understandable, and let people learn about rooms before asking for directions. The bottom menu is available in both the map and 3D. The global 3D action intentionally opens the same unrelated reception demo from any floor; it never establishes a location or registered capture position.

Final commands and outcomes:

- `ANDROID_HOME=<local Android SDK> ./gradlew :androidApp:assembleDebug :composeApp:desktopTest :composeApp:testAndroidHostTest :composeApp:linkDebugFrameworkIosSimulatorArm64` passed. Both host suites contain 29 passing tests, including the new room-introduction and trail-content checks.
- `npm --prefix exploration run build` and `npm --prefix exploration test` passed (10 viewer tests). This bundle change only removes the old chevron from Map help text.
- `xcodebuild -project iosApp/BeThereCentral.xcodeproj -scheme BeThereCentral -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=CBB251C4-BDA9-415D-9D6B-DF10DB661509' CODE_SIGNING_ALLOWED=NO build` passed with the local SDK/JDK environment. The generated sign is included in the host resource-copy phase.
- `git diff --check` passed before delivery.

Dedicated Pixel 7 / API 36 emulator evidence: a map room tap opened the host without setting a start; Get directions opened the clearer start sheet; choosing Third floor lobby produced a route to Orbit on Ground floor. Step-free changed 71 m / Stairs to 79 m / Lift. The bottom 3D action opened the real sample and Map returned to the same floor and last-seen point. The discovery sheet displayed fictional Spark context and Demo scan advanced progress to 1/5, Found by Alex. After the final presentation updates, the wooden sign opened 3D, its scene and controls rendered above the persistent bottom menu, and Rooms returned to map search. The enlarged pixel host, blue/white action and smaller launcher B were visually inspected. Current captures are [map](../screenshots/android-minimal-map.png), [3D](../screenshots/android-minimal-explore.png), [host](../screenshots/android-room-host.png), [discovery](../screenshots/android-discovery-trail.png) and [launcher](../screenshots/android-launcher-logo.png).

On iPhone 17 Pro / iOS 26.5 simulator, the final rebuilt app launched; Rooms → Orbit showed the enlarged host and blue/white directions action. Get directions opened the start sheet; Ground floor lobby established the explicitly last-seen route origin. Layout and text were visually inspected. The final persistent 3D menu was visually checked on Android, not iOS. Full VoiceOver/TalkBack, physical-device rendering, external-link browser launch and a new complete co-op trail were not exercised in this slice. Company link targets are covered by the allowlist tests. Server/sharing behavior is unchanged and was not rerun.

The Samsung was disconnected from both local ADB connections when checked, so this update was not installed on the phone. No phone instrumentation, screenshot capture or background monitoring was used. Emulator/Simulator results are not physical-device evidence.

Astra reviewed the final source and the four final Android captures (map, host, 3D and launcher) and found no actionable defects. This review did not independently verify iOS or the phone.

### Samsung installation — room hosts and persistent 3D navigation

After reconnection on 8 October 2026, the debug APK from commit `288573d` was installed on the physical Samsung SM_F971B using `adb -P 5038 -s <Samsung serial> install -r --user 0 androidApp/build/outputs/apk/debug/androidApp-debug.apk` (Success). A cold launch with `shell am force-stop com.betherecentral` followed by `shell am start -W -n com.betherecentral/com.betherecentral.android.MainActivity` returned `Status: ok`, 521 ms Activity launch time. A subsequent package-specific `pidof` confirmed the process remained present. This is installation/startup evidence, not visual or performance validation. No phone instrumentation, screenshot capture or continuous monitoring was used; visible appearance awaits user confirmation.


### Direct Map / 3D navigation and inclined Top view — 8 October 2026

The bottom bar now contains Map and 3D destinations, which switch views without opening a sheet. More moved to the shared top-right header. Find a room stays visible on the map; Plan a route opens simulated start choices, and route details retain Change start point. Header fitting and a shared search/floor row avoid competing fixed widths. The separate viewer replaces Overview / View controls with Top, +, − and information; Top fits the sample capture at an inclination. Very short landscape layouts use a lower-right horizontal control row.

Commands actually run:

- `npm --prefix exploration run build` and `npm --prefix exploration test`: passed, 12 viewer tests, including inclined fit across portrait/landscape and reversible zoom.
- `./gradlew :composeApp:compileKotlinDesktop`: passed (executor check).
- `ANDROID_HOME=… ./gradlew :composeApp:testAndroidHostTest :androidApp:assembleDebug`: passed, 29 Android host feature tests. Subsequent presentation corrections were rebuilt with `:androidApp:assembleDebug`; the domain suite was not rerun after those corrections.
- Unsigned `xcodebuild -project iosApp/BeThereCentral.xcodeproj -scheme BeThereCentral -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=CBB251C4-BDA9-415D-9D6B-DF10DB661509' CODE_SIGNING_ALLOWED=NO build`: passed after final corrections.
- `git diff --check`: passed.

On the dedicated Pixel 7 / API 36 emulator (5556), visually inspected the map and rendered inclined Top scene with the two-item native bar and top-right More. Plan a route → simulated Ground floor lobby → Find a room → Orbit · Third floor → room host → Get directions produced a stairs route (71 illustrative metres); Step-free changed it to Lift (79 illustrative metres). Map → 3D → More → dismiss → Map returned directly without a sheet, retaining the route and map projection (reception sign bounds unchanged). Third-floor selection also survived a 3D round trip. The final follow-up build additionally exercised the no-destination route card → Change start point path. [Map](../screenshots/android-minimal-map.png) and [Top scene](../screenshots/android-minimal-explore.png) captures show the final navigation layout; they precede only the additional Change start point action. These are emulator checks, not physical-device performance or accessibility certification.

Browser inspection confirmed visible Top framing and controls at 1280×720 and an unobstructed horizontal control row at 800×280; the temporary viewport override was reset. Earlier checks in the same slice exercised +/− reversal and the information dialog.

**iOS is partially verified, with an unresolved crash.** On iPhone 17 Pro / iOS 26.5 simulator, the final map rendered correctly and Plan a route → Ground floor lobby → route details → Change start point worked. During subsequent accessibility-tool sheet dismissal, the app crashed with EXC_BAD_ACCESS in Compose `AccessibilityElement.cachedProperties` / `accessibilityTraits`, reached through Apple's delayed activation callback (incident 8 October 2026, 10:14:47 Europe/Brussels). This does not establish the root cause or prove a normal touch path is safe. No speculative dependency change or accessibility disabling was applied. The final iOS 3D round trip and VoiceOver remain unverified. Reopened the simulator app after recording the failure.

Installed the final APK on the connected Samsung SM_F971B with ordinary `adb -P 5038 -s … install -r --user 0`, then cold-launched the explicit activity: `Status: ok`, 498 ms activity launch, process present. This is installation/startup evidence only; phone appearance remains for the user to confirm. No phone instrumentation, screenshots or continuous monitoring were used. No unchanged server tests, full desktop journey, physical iPhone or hosted CI checks were run for this slice.


### Capture workflow and budget documentation — 8 October 2026

Documentation-only update to the pilot guide, README link, demo guide and changelog. Checked the published Apple RoomPlan, Polycam capture/pricing, Niantic Scaniverse pricing/cloud guide, Apple Developer membership and Google Play registration pages. Vendor prices remain USD with their billing periods; EUR figures are proposed internal spending allowances, not quotes, converted vendor totals or authorized purchases. Astra reviewed the assumptions; clarified that the LiDAR pass is optional and that export compatibility does not establish redistribution rights.

`git diff --check` passed. An inline Python check resolved local Markdown link targets in README.md, docs/DEMO.md and docs/PILOT_REQUEST.md and verified the two labor arithmetic examples (40×50=2,000; 40×75=3,000); both passed. No application builds, device tests, captures, uploads, subscriptions or purchases were performed for this documentation-only change.


### Android CI setup failure — 8 October 2026

The supplied GitHub runner log fails in `android-actions/setup-android@v3` before Gradle: its `sdkmanager tools` call reports `Failed to find package 'tools'` and exits 1. The action's [v3 metadata](https://github.com/android-actions/setup-android/blob/v3/action.yml) defines a default package list including that legacy package. Set its `packages` input to `platform-tools`; the subsequent workflow step still installs Android platform 36 and Build Tools 36.0.0. This directly removes the failing request; Node deprecation warnings were not the fatal error. Local exact-configuration assertions and `git diff --check` passed. A hosted pass is not yet claimed. No SDK packages or licenses were installed/accepted on the user's machine for this workflow edit.


### Five-floor furnished pixel world - 8 October 2026

The active app now uses five supplied BeCentral schematics (ground through fourth) as relative footprint references. Source-pixel exterior/courtyard vectors use a 6-pixel grid and 8-pixel simplification. The creator approved fictional interiors in the earlier coworking style: five distinct full-floor illustrations were generated with the built-in GPT image tool, saved unchanged, registered to the source viewport and clipped to its independent silhouette/courtyard masks. Internal partitions, furniture and decorative characters are fictional. This does not establish measured dimensions, real entrances, routes or accessibility. In-app 3D and the old fictional route/QR/sharing/hunt UI are parked; fixtures and tests remain separate.

Commands actually run:

- `ANDROID_HOME=SDK_PATH ./gradlew :composeApp:desktopTest :composeApp:testAndroidHostTest :androidApp:assembleDebug`: passed, 39 tests on each test target. New checks cover five-floor/legend identity, courtyard exclusions and selectable anchors, shared-occupant and unplaced directory results, coordinate round trips, stable pinch centroid and Fit recovery. Later cosmetic name-label/guide-notice changes were rebuilt with `:androidApp:assembleDebug`; no domain logic changed after the 39-test gate.
- Unsigned `xcodebuild -project iosApp/BeThereCentral.xcodeproj -scheme BeThereCentral -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=CBB251C4-BDA9-415D-9D6B-DF10DB661509' CODE_SIGNING_ALLOWED=NO build`: passed after final corrections.
- Regenerated the trace with `scripts/trace_plan_geometry.py` and the five supplied input files using the bundled Python with Pillow/NumPy. `cmp` confirmed byte-for-byte agreement with checked-in TracedPlanGeometry.kt. This emits vectors, not modified source images.
- An inline Python APK check found all five plan PNG resources; changed Markdown local links resolved; `git diff --check` passed.

**Android emulator:** installed and cold-launched on dedicated Pixel 7 / API 36 emulator 5556. Inspected all five actual furnished floors at Fit against their source outlines/courtyards. Floor-specific art changed correctly; controls stayed outside the drawing viewport. Captures: [ground](../screenshots/android-world-ground.png), [first](../screenshots/android-world-first.png), [second](../screenshots/android-world-second.png), [third](../screenshots/android-world-third.png), [fourth](../screenshots/android-world-fourth.png). Search for The Sky showed fourth-floor 66 without a guessed marker; its scrollable host sheet listed all five supplied occupants and the absent-position notice. Searching Campfire AI selected second-floor 21, opened its source-only host, and focused the anchors. Explicit zoom enlarged the furnished world: [detail](../screenshots/android-world-detail.png). Captures precede only the final cosmetic removal of duplicate full-name labels and the added fictional-interior notice in guide sheets. Manual checks do not certify TalkBack, measured accuracy or phone performance.

**iOS simulator:** installed and launched final build on iPhone 17 Pro / iOS 26.5. Dismissed an unrelated Apple Account prompt with Not Now, entering no credentials. Visually inspected the furnished ground floor, switched to second and observed distinct art and selected-floor state. Full search/sheet journey was not repeated on iOS; the earlier Compose accessibility crash remains unresolved in the backlog. This limited smoke check does not establish its repair or VoiceOver safety.

Astra reviewed final code and all Android floor/detail captures without remaining material blockers for the artistic preview. Fixed findings included hidden shared occupants, incorrect FARI attribution to unnamed 60, unclipped canvas, non-scrolling guide content, floor selection semantics and duplicated search logic. A first manual courtyard draft was replaced after visual inspection exposed a missing broad courtyard arm.

No phone was connected on the Samsung's ADB port at the final check, so this slice was not installed on the phone. No phone instrumentation, source-plan upload, location service, backend integration, unchanged Python server rerun or browser-rendering claim was made.

**Hosted CI repair:** [run 37825229211](https://github.com/e-mric/BeThereCentral/actions/runs/37825229211) at 6c4c272 passed shared/Android, iOS framework and exploration after removing the obsolete SDK tools request. This proves the setup repair at that commit, not a later pixel-world commit before its own hosted run completes.

### Room Studio experiment and map presentation — 8 October 2026

User problem: a company needs to edit its own decorations without changing the building geometry; the map also needs a clearer silhouette and quieter controls. This slice experiments with one fictional room shared between a PixiJS browser editor and the native preview. It does not publish edits into the BeCentral floors.

Commands actually run:

- `./gradlew :composeApp:desktopTest :composeApp:testAndroidHostTest :androidApp:assembleDebug` (local SDK environment configured) — passed; desktop 47 tests, Android host 46 tests, zero failures/errors. An initial Android-host resource test failed because Android resources require a runtime; that bundled-fixture test was moved to desktopTest, with seven pure Studio tests retained in commonTest.
- `npm test && npm run build` in `room-studio/` — passed, five tests. A dedicated GitHub Actions job now runs these checks; remote execution is not yet verified for this change.
- `xcodebuild -project iosApp/BeThereCentral.xcodeproj -scheme BeThereCentral -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=CBB251C4-BDA9-415D-9D6B-DF10DB661509' CODE_SIGNING_ALLOWED=NO build` — passed with local Android SDK/JDK environment configured.
- `git diff --check` — passed.

Browser evidence: edited the company label to Garden Lab, moved the desk from x47 to x51, exercised undo/redo, saved and reloaded the local draft, dragged furniture and undid the move, and exported JSON. Checked the rendered editor at desktop and 390×844 viewport sizes. The final renderer uses the same four furniture recipes as native. See [browser Room Studio](../screenshots/browser-room-studio.jpg). File import has domain-test coverage but was not separately exercised through the browser file chooser.

Android emulator evidence (dedicated emulator-5556, not the phone): loaded the bundled room, entered the actual browser export, applied it, and observed Garden Lab with desk x51/y45 and all four objects. A first fast ADB text entry was truncated; validation retained the previous scene. Chunked entry of the complete 463-character export succeeded. See [native imported room](../screenshots/android-studio-import.png); the emulator keyboard toolbar remains visible in that capture. Reinstalled the final APK and inspected the full ground silhouette, empty unassigned grey region, dark courtyard, single floor picker and second-floor company signs. See [ground silhouette](../screenshots/android-ground-silhouette.png) and [second-floor signs](../screenshots/android-world-signs.png).

The ground source's coarse grey polygon overlaps its coloured regions. An initial subtraction mask removed too much artwork; visual inspection caught this, and the final renderer instead intersects the art with explicitly assigned regions while retaining the full muted building shell.

iOS simulator evidence (iPhone 17 Pro, iOS 26.5): rebuilt and installed the final app, inspected the full grey ground silhouette and Fit action, tapped the far left of the entrance sign to open its guide, and verified the white-on-blue Back to plan action. See [guide contrast](../screenshots/ios-plan-guide-contrast.png). The Room Studio fixture was visibly rendered earlier in this slice; JSON text entry through the simulator control tool did not succeed, so iOS import is not claimed as manually verified.

Astra's focused final source review found no remaining blocker in schema limits, label hit targets, capacity validation, shared fixture loading or the ground mask. This is not an accessibility audit. No physical-phone installation or inspection was performed for this slice; no instrumentation service was installed on the user's phone. Company anchors remain approximate source-plan positions and the furnished interiors remain fictional. AI generation, real tenant editing, accounts and publishing are future work.

### Courtyard wall treatment — 8 October 2026

Replaced bare courtyard mask edges with a renderer-only pass for layered slate masonry, cap blocks, piers and blue-grey window sections. The user problem was the thin geometric edge cutting through rich illustrated walls. The experiment keeps the courtyard shape and charcoal interior unchanged while making its boundary part of the illustrated building. Every decorative layer clips to the existing building shell and scales in source pixels; source plan data, labels, search and routing are unchanged.

- `./gradlew :composeApp:desktopTest :androidApp:assembleDebug` with the local Android SDK environment configured: passed, 47 desktop tests, zero failures/errors; Android APK assembled. Android host tests were not rerun for this renderer-only change.
- `xcodebuild -project iosApp/BeThereCentral.xcodeproj -scheme BeThereCentral -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,id=CBB251C4-BDA9-415D-9D6B-DF10DB661509' CODE_SIGNING_ALLOWED=NO build` with local SDK/JDK environment: passed after the final wall refinement. The final app was installed/launched in the simulator, but visual inspection stopped because the user was interacting with Simulator; no iOS rendered pass is claimed for this slice.
- `git diff --check`: passed.
- Dedicated Android emulator-5556: inspected ground at Fit and first enlarged, then second through fourth at Fit, second enlarged and the Campfire AI → 42 Belgium → The Sky search/guide journey. Courtyards remained dark, windowed caps appeared on the building side, and The Sky remained unplaced. See [detailed smoke record](courtyard-smoke.md), [ground](../screenshots/android-courtyard-walls-ground.png), [first close view](../screenshots/android-courtyard-walls-first.png) and [second close view](../screenshots/android-courtyard-walls-second.png).

The initial visual pass looked too smooth, so slate variation, piers and mullions were added before the final checks. Astra reviewed clipping, polygon winding, zoom scaling and bevel joins without a blocking finding. No image assets or plan geometry were changed. No phone installation, phone instrumentation, browser or assistive-technology check was performed. The source review noted that a decorative midpoint seam may cross a window; it did not prevent readable windows in the checked views.

User steering toward an open-source community game and Studio character creation is recorded in PRD/backlog as planned work. Godot remains a recommended candidate to evaluate; no engine migration, character editor or quest gameplay is implemented by this change.

### Godot / SvelteKit planning only — 8 October 2026

Recorded the selected future game/Studio architecture, the Compose-for-web alternative and the resident-feedback hold. Current application behavior remains unchanged.

- `git diff --check`: passed.
- Ran a Python `pathlib`/regular-expression check over local Markdown link targets in the nine affected product/architecture documents: all 53 targets exist. This checks file existence, not remote URL availability or heading anchors.
- Astra reviewed the documentation diff and new plan/ADR: no content blockers; confirmed the explicit hold, current-versus-planned distinction, balanced framework alternative and acceptance checks.
- No app builds, runtime tests, browser rendering, emulator/simulator or physical-device checks were run for this documentation-only change. No code, configuration, dependencies or generated assets changed. Existing untracked screenshots are excluded from the commit.

### Modular ground-floor assets and company-placement audit — 9 October 2026

The existing-map product iteration resumed independently of the Godot/Studio hold. All 64 numbered directory entries were checked against the supplied five plans, including repeated badges and the unplaced Sky entry. The ground floor now composes reusable materials, walls/windows and furniture from the existing atlas. Bike parking occupies a separate room with the supplied continuous angled/vertical partition; no door gap or toilet position was guessed.

`./gradlew :composeApp:desktopTest :androidApp:assembleDebug` passed using JDK 17 and the configured Android SDK. A final `./gradlew :composeApp:desktopTest` passed after the source-divider regression was tightened: 50 tests, zero failures/errors/skips. `git diff --check` passed.

The updated APK was installed and launched on the dedicated Pixel 7 / API 36 emulator (`emulator-5556`). Actual rendered checks covered all five floor overviews, closer zoom/pan, bike parking, Campfire AI search/selection, tapping 42 Belgium Office, Fit recovery and The Sky's unplaced guide. Controls covering a label and marker dots painting over text were found visually and fixed. Long names wrap at closer zoom; dense full-floor views still abbreviate or omit some labels, with search exposing the full directory. See the [complete smoke record and captures](floor-assets-smoke.md).

iOS, browser, desktop rendering and physical phones were not checked for this slice. The JVM desktop test suite is shared-logic evidence only. No phone instrumentation was used. The four upper-floor interiors remain fictional raster illustrations, and no real route/accessibility claim follows from these plan positions.

### Five modular floors — 9 October 2026

Replaced the four remaining full-floor illustrations with authored reusable asset recipes. `:composeApp:clean :androidApp:clean :composeApp:desktopTest :androidApp:assembleDebug` initially stopped at an obsolete minimum-two-dividers assertion after one unsafe fourth-floor divider was removed. The corrected nonempty-wall check was then compiled and `:composeApp:desktopTest :androidApp:assembleDebug` passed: **52 tests, zero failures/errors**. No source-plan geometry, company anchors or atlas bitmap changed. Tests cover complete prop rectangles, solid overlap, 18-source-pixel exterior/courtyard clearance, buffered walls, stair/unmapped reservations and the unplaced Sky entry.

The first incremental APK retained deleted PNGs in generated resources. Cleaning both modules removed that stale output; final ZIP inspection confirmed **no `plan_*.png`** and the retained `campus_props.png`. Historical PNGs moved unchanged to documentation.

Installed and launched the final APK on dedicated Pixel 7 / API 36 ARM64 **emulator-5556**. Inspected all five floors at Fit and detail scale, Campfire AI and 42 Belgium searches/guide/return, unplaced The Sky, bike-room separation, and Fit recovery after pan/minus. Search exposes the readable directory list. The first layout request after restart returned an instrumentation-server response error; retry recovered. The checks are manual visual smoke evidence, not a physical-phone, iOS, browser, performance or assistive-technology pass. Those targets and unchanged server/viewer suites were not run for this slice. Astra reviewed source boundaries and modular rendering; see [five-floor asset walkthrough](all-floor-assets-smoke.md).

### Character-led place guide — 9 October 2026

The existing Compose map now animates toward the exact tapped source marker and presents the character with Directions and a type-specific About choice. This verifies the provisional marker interaction, not entrance-facing placement or real walking directions.

Commands actually run:

- `env JAVA_HOME=/Users/emericvanfrausum/Library/Java/JavaVirtualMachines/jdk-17.0.20.1+1/Contents/Home ANDROID_HOME=/Users/emericvanfrausum/Library/Android/sdk ./gradlew :composeApp:desktopTest :androidApp:assembleDebug` — passed, 57 shared JVM tests, zero failures/errors/skips; Android APK built. Log: `/tmp/btc-place-guide-build.log`.
- `adb -s emulator-5556 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk` — passed. Force-stop and explicit MainActivity launch succeeded.
- `android layout --device emulator-5556` and `android screen capture --device emulator-5556 -o ...` — actual emulator layout and pixels inspected at each recorded state. First layout after relaunch returned an unrecognized instrumentation response; retry succeeded. No phone inspection service used.
- `adb -s emulator-5556 shell settings get system font_scale` returned `1.0`; `settings put system font_scale 1.3` exercised larger text and was restored with `settings put system font_scale 1.0`. Changing this setting recreated the Activity and reset the map; the meeting-room journey was repeated after recreation.
- `git diff --check` — passed before commit.

Manual Android journey (Pixel 7 / API 36 ARM64 emulator, 1080 × 2400):

| Action / expectation | Result |
| --- | --- |
| Search Campfire, choose result, see character and selected marker above sheet | Passed; [company guide](../screenshots/android-place-guide-company.png) |
| Open Directions | Passed; source floor/marker only, explicit unconfirmed entrances/routes; [directions](../screenshots/android-place-guide-directions.png) |
| Back to choices → About the company | Passed in layout; missing supplied mission stated, no invented description or link |
| Back to map → tap third Campfire sign at `(847,1080)` | Passed; third marker moves to the focus point, not the first; [repeated marker](../screenshots/android-place-guide-repeated-marker.png) |
| Search Meeting → Room purpose | Passed; room 31 identified and purpose/facilities marked unsupplied; [meeting purpose](../screenshots/android-place-guide-meeting-purpose.png) |
| Repeat meeting selection with font scale 1.3 | Passed; name, greeting, both choices and dismissal readable, marker above sheet; [large text](../screenshots/android-place-guide-large-text.png) |
| Search Sky → Directions | Passed; fourth floor stays at Fit, no fabricated location, position unknown explained; [unplaced entry](../screenshots/android-place-guide-unplaced.png) |

Search inputs were entered only after the layout exposed FOCUSED. Standard-font search used header `(367,382)`, field `(540,949)`, `input text Campfire/Meeting/Sky`, then sole result `(540,2166)`. Guide actions used Directions `(540,1895)`, About or Back to choices `(540,2053)`, Back to map `(540,2211)`, with fresh layout checks between actions. Large-text search used header `(340,392)`, field `(540,941)`, result `(540,2148)`.

Astra source review found no blocker in selection/provenance/guide behavior. Optional supplied text/links are domain-validated, but there is no resident content editor or populated company profile yet. No iOS, browser or physical-device test was run for this slice. No hosted CI result is claimed.

### Five-floor source-line audit — 9 October 2026

The supplied ground–fourth schematics and hash manifest are preserved under `docs/assets/source-plans/`. The [audit report](../assets/SOURCE_LINEWORK_AUDIT.md) links all five source overlays and actual full-floor Compose/Skia renders. Source partitions, curved marks, visible openings and stair motifs replace the fictional divider screens. Company anchors are unchanged. Furniture is fitted or omitted rather than moving source geometry.

Commands actually run:

- `PYTHONPATH=/tmp/btc-linework/python-deps <bundled-python> scripts/trace_plan_linework.py --kotlin-out composeApp/src/commonMain/kotlin/com/betherecentral/features/building/data/SourcePlanLinework.kt`: passed. Pillow/NumPy came from the bundled runtime; OpenCV was installed only under the temporary dependency directory. All five extracted contour masks round-trip without missing/added candidate pixels.
- Repeated extraction with `--out /tmp/btc-linework-repeat --kotlin-out /tmp/SourcePlanLinework-repeat.kt`, then `cmp` against the checked-in generated source: identical.
- `BTC_RENDER_AUDIT=1 ./gradlew :composeApp:desktopTest :androidApp:assembleDebug`, with local JDK 17 and Android SDK environment variables: final run **passed, 64 tests, zero failures/errors/skips**, Android debug build successful. An intermediate unresolved variable in the contrast mask was corrected before this final run.
- `PYTHONPATH=/tmp/btc-linework/python-deps <bundled-python> scripts/audit_plan_renders.py`: passed on all five floors; zero missing/extra candidate pixels outside a one-source-pixel envelope in the actual source-only Compose render. This measures rendering retention, not architectural semantics or surveyed completeness.
- `git diff --check`: passed before delivery.

Actual renderer images were visually reviewed for all five floors against the supplied drawings. Astra independently reviewed first/third partition fidelity and source-layer ordering; its stair contrast finding was addressed. Fine lines retain source width and need zoom at phone scale. Existing coarse silhouette/courtyard masks and decorative window caps remain approximate.

The final APK was installed on the **Android emulator `betherecentral-demo` (`emulator-5556`)**, using ordinary ADB on port 5039. Launch, floor selection (ground to third), tapping NOX Energy, its correct Plan 49 character guide, returning to the map and Fit reset were visually verified. Evidence: [ground](../assets/linework-audit/android-ground.png), [third](../assets/linework-audit/android-third.png), [guide](../assets/linework-audit/android-guide.png). Earlier in this pass, first-floor rendering and Fit were also inspected. The Android CLI inspector could not connect to this alternate ADB server; emulator-only UI dumps and screenshots were used instead. No physical phone inspection service was installed.

This pass did **not** rerun iOS, browser, physical-device or hosted CI verification. The source drawings remain unscaled schematics; no real route graph, door interpretation or toilet locations were inferred.

### Landscape floor-selector clipping — 9 October 2026

Problem: the non-scrollable five-row selector exceeded the landscape sheet height and clipped Fourth floor. The selector now scrolls vertically, retaining its existing 52dp minimum targets and selection semantics. Astra reviewed the bounded approach; Luna applied the modifier and experience/changelog updates.

- `JAVA_HOME=<local JDK 17> ANDROID_HOME=<local SDK> ./gradlew :androidApp:assembleDebug`: passed.
- `adb -P 5039 -s emulator-5556 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk` and explicit MainActivity launch: passed on the BeThereCentral Android emulator. Ordinary emulator UI dumps/screenshots were used because of the previously recorded Android CLI connection limitation.
- Landscape: opened the selector, swiped upward, confirmed the complete Fourth floor target, selected it and visually confirmed the fourth-floor map/header. [Selector](../screenshots/floor-selector/landscape-floor-selector.png) · [Selected map](../screenshots/floor-selector/landscape-fourth.png).
- Portrait: all five options visible without scrolling. [Capture](../screenshots/floor-selector/portrait-floor-selector.png).
- Landscape with system font scale 1.3: scrolled to the fully visible Fourth floor target. [Capture](../screenshots/floor-selector/landscape-floor-large-text.png). Restored font scale 1.0 and original automatic-rotation setting afterward.
- `git diff --check`: passed. No new unit test for this one-modifier layout change; actual rendered interaction is the acceptance check. iOS, browser, physical devices and assistive-technology interaction were not reverified in this pass.

### Experimental mobile 3D access and community plan — 12 October 2026

Implemented More → Experimental → 3D exploration · sample using the existing offline licensed Tugboat Bat capture. The native header reserves space above the viewer, identifies the unrelated sample and keeps Back to map visible. Android system Back returns to Plan World; opening cancels pending focus animation, removes map hit targets/semantics, retains map state and destroys the Android WebView on exit. The More sheet scrolls in landscape. The community roadmap is written and linked to the selected Godot/SvelteKit direction; game/Community Studio implementation remains on hold pending resident feedback and explicit resumption.

Commands/results:

- `JAVA_HOME=<local JDK 17> ANDROID_HOME=<local SDK> ./gradlew :composeApp:desktopTest :androidApp:assembleDebug`: final shared-code run passed; **64 tests, zero failures/errors/skips**, Android build passed. An intermediate invalid explicit weight import was removed before the successful run. The opt-in floor image audit was not enabled in this pass.
- `cd exploration && npm run build && npm test`: passed, **12 tests**. Regenerated source and `dist/` together after updating older map/return wording; renderer, scene bytes and attribution remain the existing sample.
- `./gradlew :androidApp:assembleDebug` with the same JDK/SDK environment after bundle regeneration: passed. Installed this final APK on the dedicated Android emulator and physical phone.
- `git diff --check` and local planning-link validation: passed. System Git/Python shims initially required an unaccepted Xcode licence; direct CommandLineTools Git and bundled Python/Node completed the work without accepting it.

Actual **Android emulator `betherecentral-demo` (`emulator-5554`, API emulator)** checks: portrait menu/sample entry and scene pixels; native labels/return contrast; visible attribution; successful landscape render and unobstructed About close/header; landscape More scrolling; Android system Back from About to the retained second-floor zoomed map. Before/after canvas pixels (excluding system bars) were identical. Native Back to map also returned successfully in landscape. Source-only builds/log markers were not used to establish these visual results. Evidence: [menu](../screenshots/experimental/android-menu.png), [portrait viewer](../screenshots/experimental/android-viewer.png), [landscape](../screenshots/experimental/android-landscape.png), [About](../screenshots/experimental/android-about-landscape.png), [map before](../screenshots/experimental/android-before.png), [map after](../screenshots/experimental/android-after.png).

Map regression walkthrough: searched Campfire AI and confirmed Second floor / Plan 21 guide; searched 42 and selected 42 Belgium Office, confirming Third floor / Plan 37; searched The Sky, selected its Fourth floor / Plan 66 guide, and opened Directions to confirm “position is not shown” and unconfirmed entrance/route wording. [42 guide](../screenshots/experimental/android-42-guide.png) · [The Sky](../screenshots/experimental/android-sky-guide.png). The earlier landscape search attempt was dismissed before result selection; successful named-search checks were completed in portrait. No search fix is claimed.

**Physical Android phone `SM_F971B`:** ordinary serial-targeted `adb install -r` succeeded; explicit MainActivity launch succeeded; the app process remained running and its PID-filtered crash buffer was empty immediately afterward. No UI dump, screenshot, instrumentation inspection service or broad device logs were requested on the phone. Visible appearance and phone 3D performance await the user's verification.

This pass did not verify iOS, standalone browser rendering, TalkBack/VoiceOver or enlarged-text layouts. It did not force a native renderer failure/loading screen; existing viewer failure disposal remains covered by its tests and the native return is outside the viewer. The sample is unrelated to BeCentral and real capture remains conditional on building permission and privacy review.

### Bounded pixel wall finish and Mobbin reference pass — 12 October 2026

A separate generated decorative partition mask gives source lines a slate body, shaded face and narrow upper-left edge. Source vectors/anchors are unchanged; stair strokes remain fine. Final facade/courtyard caps cover partition tips at junctions, clearing noisy marks from glass. Per-floor bevel geometry is cached in source space and transformed for interaction. See [wall finish audit](../assets/PARTITION_WALL_FINISH.md) and [Mobbin design notes](../design/MOBBIN_REFERENCES.md).

Commands actually run:

- Installed OpenCV 4.12.0.88 only in `/tmp/btc-wall-python`; bundled Python supplies NumPy/Pillow. `scripts/trace_plan_linework.py --out /tmp/btc-linework` passed.
- `PYTHONPATH=/tmp/btc-wall-python <bundled-python> -m unittest scripts.tests.test_style_plan_partitions -v`: **6 tests passed**. Generator output reports all five masks preserve foreground-component/hole counts, original pixels and vector round-trip, with no growth in stairs/outside occupancy and a maximum added distance of 3 source pixels. Repeated generation into temporary files plus `cmp` of both Kotlin and JSON audits: identical.
- During generator development, invalid reuse of filtered shell hierarchy caused an OpenCV contour traversal hang. Diagnostic shell occupancy now uses parity fill; the old process was stopped. Partition/stair contour export retains full valid hierarchy. Intermediate topology/indexing and hole-export defects were corrected before final tests and generation.
- `BTC_RENDER_AUDIT=1 ./gradlew :composeApp:desktopTest :androidApp:assembleDebug` with local JDK 17/Android SDK: final cached-renderer run **passed, 65 tests, zero failures/errors/skips**, Android build passed. The post-build source changes are comments and an equivalent explicit OpenCV keyword argument only.
- `scripts/audit_plan_renders.py`: raw source-only comparison passed on all five floors, zero missing/extra candidates outside the original one-source-pixel envelope. Styling is excluded from that measurement.
- `git diff --check`: passed.

All five actual Compose/Skia colored floor renders plus an enlarged ground crop were reviewed. Astra identified noisy window overlaps and excessive bevel contrast; final caps were reordered above partition ends and the highlight was narrowed/softened. Source-stepped curves and tiny gaps remain conservative. Furniture-clearance behavior is covered against the actual generated skin, including raster-edge clearance.

Android emulator `betherecentral-demo` (`emulator-5554`) received the final APK. Launch/Fit, repeated zoom and pan to inspect the orange ground region were visually checked. The first uncached bevel attempt made dragging slow; caching removed repeated path operations, and the final drag moved the view across the orange/lobby region. [Actual emulator detail](../assets/partition-finish/android-ground-detail.png). This is a functional observation, not a measured frame-rate claim. The complete company-search/unplaced walkthrough was verified earlier in this day's Experimental slice, with unchanged directory data; it was not repeated for wall paint.

Physical Android `SM_F971B`: ordinary serial-targeted install and MainActivity launch succeeded; app process was running and PID-filtered crash output was empty immediately afterward. No phone screenshots, UI dumps or inspection services were used. Visible phone appearance/performance awaits user verification. iOS/browser/assistive-technology checks were not rerun in this wall pass.

Mobbin screen previews were visually examined for pixel characters, avatar editors, inventories and learning interactions; canonical links and observations are saved in design notes. No Mobbin screenshot or third-party character asset was copied into the shipped asset pack.

## Community Studio and Godot slice — 12 October 2026

The independent community implementation resumed after the map wall finish. The user reported public pixel-portrayal approval for Ninon and Morgane and explicitly requested their own character as The Architect. Original source photographs stay outside the repository; personal introductions remain empty. The room, decoration and learning activity are fictional. No resident study has been conducted; Compose/emulator/phone evidence above remains separate.

Commands actually run after the corresponding fixes:

- `node --test content/community/v1/test/package.test.js content/community/v2/test/*.test.js room-studio/test/*.test.js`: **19 passed** (6 v1 + 8 v2 + 5 legacy Studio), including exact schemas/appearance unions, rejection corpus, byte/text limits, reachable interactions, lossless legacy migration and idempotent clue/reward progress.
- `node content/community/v2/test/acceptance-runner.js`: completed during contract integration; the same accepted/rejected corpus is exercised independently by Godot.
- Studio `npm test`: **15 passed**, including 60 manual-frame and 100 preset-frame RGBA hashes, normalized world pivots/nameplate anchors, paint/hit order, ASCII tie order, bounded caches, oversized storage input, and reactive-proxy edit/serialization regressions. `npm run check`: **0 errors/warnings**. `npm run build`: static adapter build **passed**, with synchronization of14 trusted image hashes. The final preview-copy amendment was included in the subsequent parent build.
- Godot4.7.2 `--headless --path game --script res://tests/run_contract.gd`: **301 assertions passed**, including shared v1/v2 corpus, all160 manual/preset frame hashes, cardinal-direction four-pose/idle checks, static room previews, malformed version rejection, collision/reachability, dialogue/journal, rewards, isolated progress saves and secret return. `--headless --path game --editor --quit`: import/parse passed. Approved guide/self package startup checks also exited cleanly.
- Trusted native PNGs retain their generated RGBA bytes. All24 props and five character atlases have bounded crops and pinned SHA-256 values. The final Architect atlas removes all wrist accessories, alternates back-view feet and adds side stride/passing phases. Initial GPU-imported pixels failed exact identity; trusted raw PNG loading and independent browser decoding now match the golden hashes.
- `git diff --check` and `scripts/check_docs.py HEAD^` passed for the community delivery commit. The documentation check used the Command Line Tools Git binary; the unaccepted Xcode license was not changed.

### Actual browser evidence

The in-app browser control initially timed out, and standard Chrome was absent. A later inventory exposed Chrome for Testing; a separate test window on `http://127.0.0.1:4175/` was used, leaving unrelated tabs intact. A hung owned Vite listener was restarted. Actual native browser controls exercised room rename to **Community Workshop**, desk movement **47→51,45**, undo/redo, adding/removing the hacker workstation, explicit local Save, reload and Load saved work. People opened the playable starter. Selecting The Architect, Ninon and Morgane set each approved name and cleared the earlier fictional introduction. Front/back/left/right preview and Walk controls rendered the final bare-wrist art; back and side walking frames were inspected. The activity title was edited to **Review a community room**.

Two actual JSON downloads completed. The first loaded through Godot's **Open room…** picker, preserving room name, desk51,45 and The Architect. The final downloaded file contained The Architect and Morgane with empty introductions and the edited activity title; a fresh Godot desktop startup opened that exact download. Neither handoff was fabricated from a Node-authored substitute. Browser proof: [final characters/export](../screenshots/community/studio-architect-export.png), [back walk](../screenshots/community/studio-architect-back-walk.png), [side walk](../screenshots/community/studio-architect-side-walk.png). Game proof: [downloaded package](../screenshots/community/godot-studio-handoff.png).

The rendered walkthrough caught Svelte reactive proxies failing `structuredClone` on edits and serialization, labels covering faces, and stable atlas URLs retaining old wrist accessories in browser cache. JSON-only clone boundaries, normalized nameplates and trusted SHA query versions fixed these; automated regressions and repeated actual Save/Load/export checks cover the behavior. Browser import picker automation did not complete reliably, so no browser file-import interaction pass is claimed. Domain rejection is tested; earlier Godot UI rejected an invalid file while preserving the edited v1 room.

### Actual Godot desktop evidence

Founder/Patch rendered at authored positions; arrow-key movement/facing, both clues and full journal text, wrong-answer hint/retry, correct-answer explanation/single badge, cold-restart progress and reset were exercised. The bookshelf entered the white Construct; movement stopped at a chair footprint; Return restored source position and completed progress. Escape was exercised in the earlier iteration. All24 props were inspected in the scrollable gallery. Final Architect/Morgane artwork and the edited activity rendered from the browser download; all four directional inputs were operated after the gait update.

Evidence: [original room](../screenshots/community/godot-founders-room.png), [retry](../screenshots/community/godot-quest-retry.png), [completion](../screenshots/community/godot-quest-complete.png), [journal](../screenshots/community/godot-clue-journal.png), [white Construct](../screenshots/community/godot-white-construct.png), [prop gallery](../screenshots/community/godot-new-props.png), [guide greeting](../screenshots/community/godot-guide-greeting.png), [invalid import](../screenshots/community/godot-invalid-import.png).

Astra reviewed earlier integration fixes. The final new review could not launch because the agent-thread limit was reached, so the parent reviewed the last asset/state changes. No mobile Godot export, iOS build, physical-device installation, screen-reader test, hosted CI result or resident study is claimed for this community slice. Existing Compose phone evidence remains separate; no phone inspection service was used.

## Movement-linked walking — 12 October 2026

The user supplied a motion reference and asked for a stronger walk cycle. At this earlier movement-only checkpoint, four-frame art remained in place and the RetroDiffusion experiment awaited authentication/spending approval. The later approved generation and mixed-frame delivery are recorded below. Personal account configuration and credentials remain outside the repository.

Godot now advances from collision-resolved travel, one pose per8 world pixels and a full cycle per32 pixels. Blocked movement retains input-facing but returns to idle; stop/restart, imports, dialogs and secret-room transitions reset the accumulator.

- Godot4.7.2 `--headless --path game --script res://tests/run_contract.gd`: **306 assertions passed**, including cardinal facing/four poses, equal-distance results across frame chunks, normalized diagonal distance, obstacle stopping, idle/restart, Construct movement/return and import reset.
- `--headless --path game --editor --quit`: passed; default sample headless startup passed.
- Parent reviewed the movement/state diff and restarted the owned development game with the actual Studio export. Native Right/Left/Up/Down inputs changed position/facing and released to idle. [Static smoke screen](../screenshots/community/godot-distance-walk-smoke.png) records the resulting room; it is not a video or proof of final sprite-animation quality.
- No browser build or mobile platform check was rerun for this game-only behavior change.

## RetroDiffusion walking delivery — 12 October 2026

The user approved a bounded pre-paid-credit animation experiment followed by testing, commit and push. The configured RetroDiffusion MCP endpoint authenticated successfully; the current chat did not expose native RD tool bindings, so a private MCP client read owner-only local credentials in memory. Credentials, account balances, signed URLs and submission logs remain outside tracked files. Only neutral sprites rendered from already-approved art were uploaded; original photographs were not uploaded.

Art gate: the first right GIF had palette/body corruption confirmed in Chrome; its PNG retry had planted feet and was rejected. The first left sheet had corrupt later frames. One left retry failed with a provider502, returned no output, and its refund was verified before an identical technical retry. The successful left retry has visible stride/passing steps and bare wrists. The front action-directed sheet has alternating foot lifts and a restrained shuffle. Both back sheets had corrupt later colours and were rejected. The shipped32-frame atlas uses front8, left8, mirrored-right8, preserved-back4 and unchanged four idles. Sparse green edge pixels remain in the accepted native sources, visible at magnified dark-background inspection. This is an iterative improvement, not a finished all-direction animation upgrade.

Commands actually run on the final source/data:

- `python3 content/community/assets/tools/pack_architect_walk.py`: rebuilt32 frames, verified pinned native source hashes and preserved all four idle hashes. A second rebuild matched the atlas, catalog and conformance file byte-for-byte. A fixed sequence anchor keeps the highest front hair pixels within the64×96 frame; no per-pose fitting/recolouring/background removal is performed.
- `node game/tools/sync_catalog_assets.mjs` and Studio `npm run sync-assets`: verified/copied24 props, five presets and14 unique images. All three catalog copies match.
- `node --test content/community/v1/test/package.test.js content/community/v2/test/*.test.js room-studio/test/*.test.js`: **19 passed**.
- Studio `npm test`: **18 passed**, including60 manual and112 preset RGBA hashes, mixed directional sequences, elapsed-time chunk invariance, preserved640ms cycle duration, cache bounds, content round trips and existing room edits. `npm run check`: **0 errors/warnings**. `npm run build`: static build **passed**.
- Godot4.7.2 `--headless --path game --script res://tests/run_contract.gd`: **336 assertions passed**, including exact frame hashes, front8/back4/left8/right8 ordered sequences,32-world-pixel cycle wrap, blocked/idle/reset behavior, phase chunk invariance and rejecting negative/fractional/non-finite crops, non-finite pivots and six/nine-pose directions. Editor import and sample boot also passed earlier in this slice.
- Parent review found/dedented an unreachable coordinate-validation loop, added matching rejection regressions, then reran the full Godot suite. Astra reviewed code, trusted metadata and native art; no remaining blocking defect was found. Ten stale headless task processes were verified by command/cwd and stopped, retaining the live GUI.

Actual browser: Chrome for Testing displayed the new left/right walk and front foot-lift cycle, and the preserved back view through Stand/Walk/direction controls. Initial Vite hot reload temporarily paired new crop metadata with an already-loaded old atlas; full reload after asset sync restored correct pixels. Final source changes were then tested/built. [Right preview](../screenshots/community/studio-rd-right-walk.png), [retained back](../screenshots/community/studio-rd-back-retained.png). These screenshots are stills. The [animated frame preview](../screenshots/community/architect-walk-preview.webp) is a lossless derived asset loop in front/back/left/right order, not a recording of browser or Godot UI.

Actual Godot: the updated source pack rendered The Architect and Morgane from the prior actual browser download. Native Left input changed position and released to the left idle pose. Further held-key checks were interrupted by user-control notifications, so a new complete four-direction native-animation walkthrough is **not claimed**. This observation predates the final one-pixel front-anchor adjustment; final RGBA/sequence tests cover the updated data. Earlier complete quest/secret/progress walkthrough evidence remains above; these unchanged journeys were exercised by the336-assertion runtime suite, not repeated end-to-end through native controls in this art pass. No phone, emulator, iOS, assistive-technology or hosted-CI pass is claimed.

Final delivery hygiene: `git diff --cached --check` passed before commit; staged text was checked for RD credentials/authorization headers/signed-output URLs. `scripts/check_docs.py HEAD^` passed for the walking delivery commit. Unrelated iOS screenshots remain unstaged.

## Android offline Community Studio — 13 October 2026

The user requested the latest phone build and clarified a phone-only foldable presentation for14 October. Android now bundles the separate SvelteKit authoring UI behind More → Community Studio · offline; this does not include Godot gameplay. The production bundle is regenerated into ignored build assets, not committed as ordinary build output. Native import/export uses user-selected JSON documents and an isolated local HTTPS origin; no remote page/resources, account, analytics or location upload was added.

Commands and outcomes:

- `./gradlew :androidApp:assembleDebug :composeApp:desktopTest`: **passed**; the shared JVM reports total **67 tests, zero failures/errors**, including the two new strict-object/UTF-8 export-boundary tests. The native host executor also ran `:composeApp:desktopTest --rerun-tasks` successfully. The invalid initial JDK path was replaced by the discovered local JDK; no SDK paths were committed.
- Studio `npm test`: **18 passed**. `npm run check`: **zero errors/warnings**. The Android Gradle build ran the static Studio build and Sync staging successfully. CI now provisions Node24/browser dependencies before Android packaging. Hosted CI was not observed.
- The first generated-source registration rejected a Provider; registering its resolved directory fixed Gradle configuration. Emulator visual review then exposed a blank editor because aapt omitted SvelteKit’s underscore-prefixed `_app` directory. The asset-ignore pattern now retains it. `python3 scripts/check_studio_apk.py androidApp/build/outputs/apk/debug/androidApp-debug.apk`: **32 production files match byte-for-byte**, guarding against missing/stale HTML/JS/CSS/art. `python3 scripts/check_android_compose.py`: **passed**.
- Astra found omitted build inputs; authored static files and the sync scripts are now included, excluding generated static assets. Native export queue/disposal is synchronized and a released host cannot open a queued picker. Root URL loading avoids a nonexistent `/200.html` client route.

Actual emulator evidence (`emulator-5554`, separate from phone): opened the native menu and Studio, saw prop thumbnails/room art, opened People and observed the decoded Founder preview. Narrow inspector sections stack correctly. At1600×2000 pixels/density320, the editor uses the unfolded layout. Dragging a desk moved it from47,45 to76,61. The first density resize restarted the Activity; adding density to the handled configuration changes fixed this. With the final manifest, resetting size/density retained the unsaved room and76,61 position. These dimensions simulate window changes; they are not an observation of the Samsung fold hinge.

Save on this device persisted that edit. Native Export opened the system save picker and wrote a1,516-byte `community-room.json`; pulling this emulator test file confirmed four objects and desk76,61. Native Import selected the same file and reported that it matched the room. Wi-Fi and data were disabled **on the emulator only**; after force-stop/cold launch Studio still rendered its bundled art, and Load saved work restored76,61. Networking and display overrides were restored. A subsequent cancellation attempt lost the foreground/returned to the map without a crash log; export cancellation and invalid-file import are not claimed as visually verified. Their domain/host handling is covered by source review and validator tests, not a full native journey.

[Offline room](../screenshots/community/android-studio-offline-room.png) · [character preview](../screenshots/community/android-studio-character-preview.png) · [unfolded layout](../screenshots/community/android-studio-unfolded.png) · [retained folded edit](../screenshots/community/android-studio-fold-retained.png) · [export completed](../screenshots/community/android-studio-exported.png) · [offline Load](../screenshots/community/android-studio-offline-load.png). Screenshots are emulator evidence. The Android CLI layout service returned an unrecognized instrumentation response on the emulator, so screenshot observations were used instead.

Physical Samsung **SM_F971B**: ordinary serial-targeted `adb install -r` returned **Success**, and explicit MainActivity launch returned **Status: ok / COLD**. The final app process was running; PID-filtered AndroidRuntime diagnostics contained no crash output. No phone screenshots, UI dumps, gesture automation or inspection services were used. The user opened More → Community Studio · offline, unfolded the phone and replied “Yes, it displays correctly” on13 October. This is user-reported layout confirmation of the earlier bundled editor, not agent phone inspection or evidence for the later workshop redesign. No iOS, physical-phone gesture/layout, accessibility or Android Godot-export pass is claimed. Existing source-plan map/Experimental sample behavior was preserved; the complete five-floor search journey was not rerun for this editor-host slice.

Offline Studio delivery hygiene: staged diff whitespace check passed; `scripts/check_docs.py HEAD^` passed for the implementation commit. Only source, docs and emulator proof files were staged; APK/generated assets and unrelated iOS screenshots remain untracked/ignored.

## Community Studio workshop redesign — 13 October 2026

The user approved the generated workshop direction and authorized implementation. The existing trusted art, fixed fictional room, v2 package rules, storage key and Android host remain. The editor opens directly on a room canvas with a compact cream header, orange selection controls and a cream categorized furniture shelf. People and Activity use dedicated working surfaces. The plan was written before implementation in `docs/plans/STUDIO_WORKSHOP.md`; the generated image is a concept, not a shipped screenshot or sprite atlas.

Commands actually run:

- Studio `npm test`: **21 passed**, including three new bounded-duplication cases (custom dimensions/source immutability, object limit/missing selection, occupied candidate placements). `npm run check`: **0 errors/warnings**. Static `npm run build`: **passed**. The parent then made two CSS-only fitting fixes and rebuilt successfully through Android Gradle.
- `./gradlew :androidApp:assembleDebug`: **passed** on the final source. Native Kotlin tasks were unchanged/up-to-date; no new shared JVM or iOS pass is claimed in this UI slice.
- `python3 scripts/check_studio_apk.py androidApp/build/outputs/apk/debug/androidApp-debug.apk`: **32 production files matched byte-for-byte** on the final APK.
- `git diff --check`: passed during implementation/review. Hosted CI was not observed.

Source review and fixes: Astra reviewed the layout plan and implementation. Keep the import input mounted while the native picker is open. Clear old file messages on subsequent edits/save/load so success/errors cannot be masked. Redraw selection after duplicate/remove/import. Furniture shortcuts stay in Room; global history remains available outside form fields. Normal button groups replace incomplete listbox/tab/menu semantics. The furniture list stays available on narrow screens. Thumbnail factor and crop bounds follow actual viewport dimensions. Room details is closed initially; short landscape keeps actions, sprite labels and status reachable. Compact portrait with details open can scroll without compressing the preview below its useful height.

Actual production browser evidence: Codex in-app browser (Chrome control was unavailable in this session) rendered the new room/People/Activity views at800×950,360×740,390×640 with Move open, and800×375. A wide1280×720 draft was also inspected; final proof below uses the production bundle. Default page width matched the narrow viewport; only the intended asset/category trays scrolled horizontally. Duplicate/remove/undo/redo, a hacker-workstation addition, character look selection and Left/Walk preview were operated. Save, reload and File → Load saved work restored the room. Export downloaded a checked v2 file. Importing `{}` showed rejection and kept the room unchanged; importing the exported file restored the four-object room from an edited five-object room, and Undo/Redo restored/reapplied that transaction. A subsequent Save replaced the file-operation status with “Saved on this device.” Native keyboard/assistive-technology use was not comprehensively tested.

The browser automation’s fill/typing plus blur did not commit an Activity title; this alone was not treated as an application failure. The unchanged event handler was checked through actual Android emulator text input: selecting/replacing the title with “Workshop practice” and moving focus updated both field and preview. No per-keystroke validation or speculative handler change was introduced. Preview servers needed a restart after static rebuilds to stop serving cached entry manifests; loaded stylesheet hashes were compared to the production entry before final visual checks.

Actual Android emulator (`emulator-5554`) installed and cold-launched the final APK. More → Community Studio · offline showed the new layout. At1600×2000/density320, native Back remained visible and the room/shelf rendered cleanly. A desk drag moved47,45 to74,57. Resetting display size/density retained the open unsaved edit and those exact numeric positions. The new File → Export room file opened Android’s document save picker; Back returned to Studio with “Export cancelled. Your room is unchanged” and the74,57 edit intact. The screen/density change is an emulator window test, not a hinge observation. Display overrides were restored. Native export-write/import success and offline cold start retain their separately recorded earlier evidence; they were not fully repeated here.

[Browser room](../screenshots/community/studio-workshop-unfolded.jpg) · [People](../screenshots/community/studio-workshop-people.jpg) · [folded](../screenshots/community/studio-workshop-folded.jpg) · [landscape](../screenshots/community/studio-workshop-landscape.jpg) · [Android unfolded](../screenshots/community/android-workshop-unfolded.png) · [retained edit](../screenshots/community/android-workshop-fold-retained.png) · [native text edit](../screenshots/community/android-workshop-activity-edit.png) · [native export cancellation](../screenshots/community/android-workshop-export-cancelled.png).

Physical Samsung SM_F971B: serial-targeted `adb install -r` returned **Success**; MainActivity launch returned **Status: ok / COLD**. The app process remained running; PID-filtered AndroidRuntime logs had no crash output. The user opened Studio unfolded and replied **“Yes, it looks right.”** This is human visual confirmation, separate from emulator/browser screenshots. No physical-phone screenshots, UI dumps, gesture automation or inspection services were used. Existing saved phone work was not cleared. No Godot mobile release, full five-floor map/Experimental journey rerun, iOS, screen-reader or hosted-CI pass is claimed.

Workshop delivery hygiene: the initial staged whitespace check found an extra trailing blank line in the generation notes; it was removed. The final staged whitespace check and `scripts/check_docs.py HEAD^` passed. Generated runtime/APK output and unrelated iOS simulator screenshots remain unstaged.

## Project knowledge and agent workflow — 16 October 2026

Documentation/tooling slice only: added the task-plan template, conditional workflow guidance, indexed knowledge/provenance records and a project-authored maintenance skill. Applied the official guide and user-supplied LLM Wiki as references, preserving the PRD/backlog/glossary/ADRs as canonical. Corrected superseded architecture statements about community implementation, source partitions and the Experimental mobile viewer from existing evidence. No application behavior or account configuration changed.

Commands actually run from the repository root (the local bundled Python 3.12 executable was used for the Python checks):

- `python3 -m unittest discover -s scripts/tests -p 'test_check_knowledge.py' -v`: **18 passed**. Fixtures cover clean checkout metadata without a private attachment, broken and escaping links, missing/duplicate index entries, metadata/dates/IDs, malformed/credentialed URLs, absolute paths, symlink containment, optional attachment digest mismatch, CLI exit status and non-mutation.
- `python3 scripts/check_knowledge.py`: **passed**, using only committed-intended notes/metadata, without private raw input or network requests.
- `python3 scripts/check_knowledge.py --raw-source llm-wiki-pattern=.scratch/knowledge/raw/llm-wiki.md`: **passed**. The copied attachment matched its recorded SHA-256; `git check-ignore .scratch/knowledge/raw/llm-wiki.md` confirmed it is ignored. Its full text and personal Downloads path are not committed.
- Skill Creator's `quick_validate.py .agents/skills/project-knowledge`: **passed**. The initial attempt failed because the local runtime lacked PyYAML; installed pinned PyYAML 6.0.2 only under ignored `.scratch/knowledge/validation-deps`, then reran the validator with that local `PYTHONPATH`. No application/checker dependency was added.
- A one-off local check resolved inline links in the new plan/workflow/skill pointers and changed architecture/contributor guidance; parsed the CI YAML and checked the knowledge job's fixture and repository commands: **passed**.
- `git diff --check`: **passed**.

Astra's reader exercise passed: AGENTS → index → delivery snapshot leads to phone Studio capabilities, canonical requirements, dated browser/emulator/physical-phone evidence and relevant Studio/Android checks. Review found an escaping registry symlink could be read; containment is now checked before reading, with synthetic regression fixtures. Parent review also removed a caller-controlled metadata-validation bypass. Neither review used private source material. Structural validity does not establish factual freshness, source rights, external link availability or platform behavior.

A dedicated knowledge CI job was added; hosted execution is not claimed. Existing app jobs still run under their existing triggers. No app, browser, emulator, iOS or physical-phone checks were rerun for this documentation/tooling slice; prior dated results remain separate.

Delivery checks: `git diff --cached --check` passed. An initial full-file privacy scan matched historical text in STATUS; scanning the added staged lines found no credential patterns or personal filesystem paths. `python3 scripts/check_docs.py HEAD^` passed for this delivery commit. Astra confirmed the containment and metadata fixes; no remaining blocker in the targeted review.

## Project-wide best-practices audit — 16 October 2026

Audited product baseline: `68476e5a9eae214af4e9a4f8c336f604775f51d5`. Astra independently reviewed canonical documentation and representative code boundaries; Luna inventoried CI/component checks. The parent verified and integrated findings into A1–A8 in [the existing plan](../plans/PROJECT_KNOWLEDGE.md). This is source/evidence review, not an exhaustive security audit or a fresh app walkthrough. Existing seven untracked iOS screenshots were untouched.

Actual audit checks:

- `python3 -m unittest discover -s scripts/tests -p 'test_check_knowledge.py' -v`: **18 passed**, run by the check-inventory executor. `python3 scripts/check_knowledge.py` also passed at the audited baseline. Semantic review nevertheless found stale canonical scope/verification statements; structural validity cannot establish factual consistency.
- `gh run list --limit 6` with JSON fields, then `gh run view 38886767636 --json jobs` (executor) and `gh run view 38886767636 --json url,headSha,conclusion,updatedAt,jobs` (parent): the [exact-baseline Verify run](https://github.com/e-mric/BeThereCentral/actions/runs/38886767636) completed **success**, updated `2026-10-16T10:27:52Z`. All five configured jobs passed: room-studio, knowledge, ios-framework, exploration, shared-and-android. This newly observed result supersedes earlier “not observed” wording only for this commit/run. It does not cover Godot, standalone package suites or visual behavior.
- Read-only Node `--input-type=module` probe imported `validatePackage` from `content/community/v2/index.js`, loaded `package-founders-v2.json` in memory and reversed `characters`. Validation **accepted** it; first character `guide`, player ID `player`, guide ID `guide`. Inspection of Studio `+page.svelte:359` confirmed the displayed roles still use array position. No fixture file was written.
- Source inspection found saved-draft Load clears current histories (`+page.svelte:116`), and Godot `_save()` ignores storage results while Reset always claims device persistence (`local_progress.gd:151`, `main.gd:784`). No actual disk failure or user data-loss incident was reproduced. A4/A5 specify focused failure/recovery tests for future implementation.

The audit reread the official best-practices HTML guide; the plain URL initially yielded navigation-heavy content, the translation-fallback URL exposed the guide, and its advertised Markdown variant returned 404. No account settings or private sources were inspected. No product code, CI configuration, browser, emulator, phone, Godot runtime or reference server was changed/run by this audit. Remaining interaction, accessibility and learner outcomes stay explicitly unverified.

Documentation verification after the audit updates: `python3 scripts/check_knowledge.py` passed; `git diff --check` passed; a one-off local-link scan of the audit plan and backlog found no missing file targets. Heading anchors and external page availability are not covered by that scanner.

Final review found no substantive blocker; baseline line-reference wording was made explicit. Added staged lines passed the privacy scan, `git diff --cached --check` passed, and `python3 scripts/check_docs.py HEAD^` passed for the audit delivery.

## A1 canonical documentation reconciliation — 16 October 2026

Reconciled the README, PRD, glossary, backlog, community/technical/workshop/source-linework plans and relevant ADRs against the existing delivery commits and dated evidence. The frozen v1 and separate v2 content contracts remain distinct; saved progress is separate from authored content. Android offline authoring remains distinct from desktop Godot gameplay. Browser import is evidenced; native rejected import, accessibility, the final complete four-direction animation walkthrough and learner outcomes remain open. No app behavior changed or new runtime/platform check was claimed.

Actual checks: `python3 scripts/check_knowledge.py` passed; `git diff --check` passed; a one-off scan of changed Markdown file targets and 20 heading links passed. Astra's final reading review passed after correcting v2/progress wording and the historical/current mixed-pose gait description. Delivery commits referenced in the documents were verified against local Git history. The executor's earlier `check_docs.py HEAD` examined only the committed comparison, so it was not used as proof for the working-tree edits; the parent runs the final documentation gate against `HEAD^` after committing A1.

A1 precedes A2 implementation. A2's content/Godot CI gate is still pending at this boundary. No browser, emulator, phone, iOS or Godot runtime was launched for A1. Unrelated screenshots remain unstaged.
