# Verification status

7–8 October 2026 · local Apple Silicon macOS environment

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
