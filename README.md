# BeThereCentral

<img src="docs/brand/betherecentral-logo-dark.png" alt="BeThereCentral logo" width="560" />

[Logo and generation notes](docs/brand/README.md)

**Make an unfamiliar building feel familiar—and enjoyable to explore.**

BeThereCentral is an open-source spatial exploration experiment for a proposed BeCentral experience by a WeAreFounders participant. The active app home is a rich pixel-art world built around five supplied schematic plans, from ground through fourth floor. Each illustrated floor has a different source-plan footprint and courtyard shape, filled with fictional coworking interiors and characters. Search known company names and plan numbers, pan and zoom each floor, and use the legend to read occupants that cannot yet be placed. The plans' revision and survey accuracy are unconfirmed. This is a visual directory prototype, not an officially approved campus service, measured route map or live positioning app.

Android and iOS share Kotlin behavior and a Compose Multiplatform interface; a desktop preview supports development. The earlier fictional route, guide, QR, sharing and hunt examples are retained as internal legacy fixtures and tests, not integrated with the five plans. A licensed interactive Gaussian-splat engine room remains a separate browser experiment; it does not depict BeCentral.

[Product requirements](docs/PRD.md) · [Architecture](docs/ARCHITECTURE.md) · [Experience standards](docs/EXPERIENCE.md) · [Building-team demo](docs/DEMO.md) · [Coordinates](docs/COORDINATES.md) · [Plan-world decision](docs/adr/0006-plan-derived-pixel-world.md) · [Privacy](docs/PRIVACY.md) · [Backlog](docs/BACKLOG.md) · [Verified checks](docs/testing/STATUS.md)

## What the current app shows

Each of the five floors has its own illustrated interior clipped to a source-derived outer footprint and courtyard voids. Those masks preserve the plans' approximate relative proportions; the generated partitions, desks, furniture and characters are fictional. The source drawings are 2048 × 1448 pixels, with X right and Y down. Their source-pixel positions are useful for drawing, not metres, walking distances, entrances or step-free paths. Pan, zoom and Fit help inspect a floor. Search and the accessible legend expose known plan numbers and occupants, including an explicit unplaced state.

Try **Campfire AI** on the second floor and **42 Belgium** on the third. On the fourth, **The Sky** appears as number **66** in the legend without a guessed map marker. The ground-floor grey/orange and second-floor grey source areas remain unassigned in the directory even where the illustration furnishes them. The first-floor FARI shape is depicted as detached, with no assumed connection. A compact pixel host uses only source-supported facts, never fictional tenant missions. A repeated plan number belongs to one place even if the plan draws it at several approximate anchors.

Five generated floor illustrations provide the full fictional interiors. Source-derived, editable footprint and courtyard masks determine where each illustration appears; directory records determine its known labels and approximate anchors. Artwork walls and furniture are not surveyed room inventory or a route graph. No route, QR scan, sharing, team session, backend, analytics or external location upload is connected to this plan world. It is not emergency or accessibility guidance. See [source and art provenance](docs/assets/PIXEL_ART.md) and the [current demo](docs/DEMO.md).

Earlier screenshots in [docs/screenshots](docs/screenshots/README.md) may show the historical fictional four-floor map or separate sample scene. Check [verification status](docs/testing/STATUS.md) for what has actually rendered on browser, emulator and physical device; a build alone does not establish visual success.

<img src="docs/screenshots/android-world-detail.png" alt="Android emulator: furnished fictional pixel world based on the supplied second-floor outline" width="300" />

[All five floor captures](docs/screenshots/README.md) · [Original floor artwork and provenance](docs/assets/PIXEL_ART.md)

## Run it locally

No app account, API key, backend or positioning hardware is needed. The first build needs internet access for Gradle dependencies.

```sh
git clone https://github.com/e-mric/BeThereCentral.git
cd BeThereCentral
```

Prerequisites: JDK 17 and Git; Android SDK platform 36 for Android; macOS, Xcode and an iOS simulator for iOS. Set `JAVA_HOME` if Java is not found, and `ANDROID_HOME` or an untracked `local.properties` `sdk.dir` for Android. A physical iOS build needs an Apple development team. Python 3.9+ is only needed for the optional browser viewer and disconnected reference server. Use the checked-in Gradle wrapper; Windows users can use `gradlew.bat`.

### Desktop

```sh
./gradlew :composeApp:run
```

This opens the shared Compose app. To inspect the independent licensed engine-room experiment in a desktop browser:

```sh
python3 -m http.server 4173 --bind 127.0.0.1 --directory exploration/dist
```

Open `http://127.0.0.1:4173/`. It uses local bundled scene files and does not establish alignment with the supplied plans. See [viewer provenance](exploration/README.md).

### Android

Open the project in Android Studio and run `androidApp` on an emulator or device, or build the debug APK:

```sh
./gradlew :androidApp:assembleDebug
```

The APK is at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. `./gradlew :androidApp:installDebug` installs it on a connected target. Android 8.0/API 26 is the minimum. Do not install instrumentation inspection services on the user's phone; ordinary ADB install, launch and narrowly scoped crash checks are the agreed method.

### iOS

Open `iosApp/BeThereCentral.xcodeproj` in Xcode, choose the **BeThereCentral** scheme and an Apple Silicon iPhone simulator, then Run. The checked-in project invokes Gradle for its Kotlin framework and targets iOS 16+. XcodeGen is needed only if you change `iosApp/project.yml` and regenerate. The shared simulator framework can be compiled separately:

```sh
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

If Xcode cannot find Java, configure `JAVA_HOME` in its build environment. Intel simulator support is not configured.

### Optional disconnected sharing reference

```sh
python3 -m server
```

This loopback-only server prints temporary demo credentials and uses synthetic opaque payloads. The active app does not connect to it. Read [its guide](server/README.md) before running requests. It does not provide E2EE.

## A short walkthrough

1. Open the ground-floor world. Explore its furnished fictional interior within the source-derived outline; the grey/orange source areas have no confirmed occupant assignment. Pan, zoom and Fit.
2. Switch across all five illustrated floors. Compare their different source-derived outlines and courtyard openings. On first, note detached FARI; on second, the former grey source area may be furnished in the illustration but remains unassigned.
3. Find **Campfire AI** on second and **42 Belgium** on third using search or the legend. Their markers are approximate source-plan anchors.
4. Find **The Sky** on fourth. Its number **66** is searchable and listed, while the map explicitly leaves it unplaced.

The walkthrough intentionally stops at visual orientation. A source-plan line is not a validated corridor, and a marker is not a surveyed entrance. The [building-team demo](docs/DEMO.md) lists questions needed before a real pilot.

## How it is built

```text
composeApp/
  src/commonMain/kotlin/com/betherecentral/features/
    building/                 # Plan World data, search and presentation
    ...                       # Parked sample navigation, guide, checkpoints, sharing, hunt
  src/commonTest/             # Feature-matched tests
  src/desktopMain/            # Desktop entry point
  src/iosMain/                # Compose UIViewController
androidApp/                   # Android launcher
iosApp/                       # SwiftUI host and checked-in Xcode project
exploration/                  # Independent licensed sample viewer and provenance
server/sharing/               # Disconnected reference sharing service
docs/                         # Requirements, decisions, source notes and verification
```

Feature domain logic stays independent of Compose, platform APIs and transport. The five-plan data uses source-image pixels only; [the coordinate contract](docs/COORDINATES.md) keeps it separate from the fictional sample's illustrative metres and a possible future surveyed system. Thin platform launchers host the shared app. Versions are pinned in Gradle; see [architecture](docs/ARCHITECTURE.md).

## Tests and verification

Run shared JVM tests and build Android with:

```sh
./gradlew :composeApp:desktopTest :androidApp:assembleDebug
```

Run disconnected server tests with:

```sh
python3 -m unittest discover -s server/tests -t . -v
```

The viewer has its own build/test workflow in [exploration/README.md](exploration/README.md). Run only checks relevant to a change and record commands and outcomes in [verification status](docs/testing/STATUS.md). For interface changes, inspect actual rendered screens and the complete five-floor search/legend journey. Emulator, browser and physical-device evidence are distinct. Tests of the old fictional route graph do not validate routes on the supplied plans.

Every behavior change updates its feature tests, affected docs and [changelog](CHANGELOG.md). No location history is stored by default. Analytics or external location upload need explicit consent. Do not label last-seen observations live tracking or claim end-to-end encryption before it is implemented and reviewed.

## Contribute

Start with [CONTRIBUTING.md](CONTRIBUTING.md) and the [backlog](docs/BACKLOG.md). The source repository is [e-mric/BeThereCentral](https://github.com/e-mric/BeThereCentral). GitHub Issues are the intended public tracker; the local backlog remains the working roadmap. This is not an app-store release.

The repository uses Astra for planning/review, Sol for integration and Luna for bounded implementation, as [AGENTS.md](AGENTS.md) describes. These are development roles, not app features. Human contributors can build and modify the app without an AI subscription. Selected vendored Matt Pocock skills have [provenance](docs/agents/SKILLS.md).

Room Studio is a later proposed editor for tenant-owned decorative content. The next milestone is review of the five-plan reconstruction and its source revision before that editor proceeds. Code is [MIT licensed](LICENSE); the independent splat scene keeps its [CC BY 4.0 attribution](exploration/public/SCENE-LICENSE.txt). See [SECURITY.md](SECURITY.md) for security boundaries.
