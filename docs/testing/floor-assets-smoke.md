# Modular ground floor and company placement checks

9 October 2026. Scope: current Compose map and reusable assets. Godot/gameplay and Community Studio remain on hold.

## Source audit

All five original 2048 × 1448 images were compared with directory data. The [placement audit](../assets/COMPANY_PLACEMENT_AUDIT.md) records the source identity, method, all 64 numbered entries, repeated badges, shared occupants, and corrected coordinates. Number 66 has no plotted badge and remains unplaced. Source registration is not a survey or confirmation of present-day tenancy.

Ground bike parking follows the supplied icon and separate-room boundary. The rendered partition follows `(1199,1058) → (1177,1138) → (1177,1185)` continuously. No door gap or toilet position was inferred. Furniture stays out of stairs, courtyard openings, the unassigned grey area and the bike room unless explicitly assigned there. No corridor widening was applied in this slice.

## Automated checks actually run

- `./gradlew :composeApp:desktopTest :androidApp:assembleDebug` with JDK 17 and the configured Android SDK: passed after renderer, anchor and label changes. Desktop suite: 50 tests, zero failures/errors/skips. Android debug APK assembled.
- `./gradlew :composeApp:desktopTest`: rerun after replacing an unhelpful immutability assertion with a source-divider regression. The two new scene tests check containment against source regions/shell/stairs and the continuous bike partition plus landmark alignment. The existing plan tests gained representative independent source-center assertions and absence of guessed toilet markers.
- `git diff --check`: passed.

An intermediate test compilation error was fixed before the passing runs. Final production code differs from the installed APK only by a comment; the last functional change was the verified sign-paint ordering.

## Rendered Android emulator journey

Dedicated Pixel 7 / API 36 ARM64 AVD `betherecentral-demo`, serial `emulator-5556`, 1080 × 2400 portrait. Installed the debug APK with ordinary ADB and launched `com.betherecentral/com.betherecentral.android.MainActivity`. Android CLI layout inspection and screenshots ran on this emulator only.

| Action | Observed result |
| --- | --- |
| Open ground, Fit | Complete muted silhouette, empty grey area and courtyard, modular colored regions; four named ground entries available |
| Search list → Bike parking → Back to plan → zoom | Ground selected; bike rack inside a distinct narrow room; divider slopes then runs vertically; lobby reception remains east of it |
| Select every upper floor | Correct floor identity and its distinct existing illustration; source company ordering agrees with the audit |
| Zoom/pan first-floor west wing | Long Bibliothèques Sans Frontières name wraps; repeated locations appear; map pans without detached labels clamping to screen edges |
| Search Campfire AI from another floor | Result identifies second floor/21; guide opens; return centers the company and its repeated positions |
| Fit after company selection | Restores the full-floor view while preserving the selection |
| Zoom third floor and tap 42 Belgium Office sign | Guide identifies third floor/37; Back to plan returns to the map |
| Zoom/pan fourth-floor east wing | Long FARI, Réseau Entreprendre Bruxelles and DT Services & Consultancy names wrap; Poppy and Lighthouse remain distinguishable |
| Search The Sky → guide | Fourth floor/66, all five listed occupants, explicit missing-position text and readable white Back to plan action; no guessed marker |

Visual inspection found and fixed controls obscuring a company label at Fit (moved controls to the top-right) and later markers drawing over earlier names (all dots/leaders now paint below signs). The completed source review found no further material correctness issue.

At full-floor scale, long names can be abbreviated and crowded labels can be omitted. Zoom or search reveals full names. Repeated long labels on the fourth floor still occupy substantial space at intermediate zoom; they do not redefine room boundaries. The upper floors still use fictional full-floor illustrations, so furniture/partitions do not establish real tenant-room geometry.

## Evidence and limits

- [Ground overview](../screenshots/android-modular-ground-fit.png), [bike-room detail](../screenshots/android-modular-bike-room.png).
- [First](../screenshots/android-map-audit-first.png), [second](../screenshots/android-map-audit-second.png), [third](../screenshots/android-map-audit-third.png), [fourth](../screenshots/android-map-audit-fourth.png) floor overviews.
- [Campfire positions](../screenshots/android-map-audit-campfire.png), [fourth-floor long names](../screenshots/android-map-audit-fourth-detail.png), [unplaced Sky guide](../screenshots/android-map-audit-unplaced-guide.png).

These are actual emulator captures, not generated mockups. iOS, browser, desktop rendering and physical phones were not rerun for this slice. Desktop tests exercise shared behavior, not desktop rendering. No phone instrumentation was installed or used. Browser/server/legacy route/QR/co-op flows are unchanged and were not retested. This is a manual map journey, not exhaustive accessibility or device-performance validation.
