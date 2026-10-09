# Journey: Five floors assembled from reusable assets

9 October 2026. Dedicated Pixel 7 / API 36 ARM64 emulator, `emulator-5556`, portrait 1080×2400. Actual app captures, not generated mockups. The new interiors are deliberately simpler than the historical illustrations; this pass establishes independent asset placement.

## Build and installation ✅

Used JDK 17 and the local Android SDK with `./gradlew :composeApp:clean :androidApp:clean :composeApp:desktopTest :androidApp:assembleDebug`, then reran the test/build tasks after correcting the divider-count assertion. Final: 52 tests passed and APK assembled. ZIP inspection found zero retired floor PNGs and one campus atlas. `adb -s emulator-5556 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk`, force-stop and ordinary Activity launch succeeded.

## Open and choose each floor ✅

Used `android layout --device=emulator-5556 --flat` to locate controls. Floor selector tap `(886,382)`, then ground `(540,1517)`, first `(540,1685)`, second `(540,1853)`, third `(540,2021)` or fourth `(540,2189)`. Captured and immediately visually inspected each PNG with `android screen capture`.

- [Ground](../screenshots/android-assets-ground.png): complete muted shell, empty court and grey area, separate bike room.
- [First](../screenshots/android-assets-first.png): independent furniture, source shape and detached FARI retained; [closer view](../screenshots/android-assets-first-detail.png) after two + taps.
- [Second](../screenshots/android-assets-second.png): northwest unmapped area stays plain, furnishings extend through Beta.
- [Third](../screenshots/android-assets-third.png): distinct tiled/wood zones and original markers.
- [Fourth](../screenshots/android-assets-fourth.png): central unassigned strip stays plain; [closer view](../screenshots/android-assets-fourth-detail.png) retains wrapped repeated FARI names. Labels remain visually dominant at this scale.

## Search Campfire AI and 42 Belgium ✅

Tapped search `(367,382)`, focused field `(540,949)`, confirmed FOCUSED in layout before `adb shell input text`. Queried `Campfire`, selected the sole result `(540,2166)`, verified source floor/number 21 in guide, then Back to plan `(540,2200)`. [Campfire detail](../screenshots/android-assets-campfire.png) shows three source positions and intact sprites. Repeated with `42%sBelgium%sOffice`, verified Third floor / 37 and [both office markers](../screenshots/android-assets-42-belgium.png).

## Check unplaced entry and ground bike room ✅

Queried `Sky`, selected 66. [Guide](../screenshots/android-assets-sky.png) explicitly states position is not supplied and lists the source occupants. No invented marker. Returned, opened the directory and selected Bike parking `(540,1362)`. Guide identifies its separate room; Back plus one + tap produced [bike-room detail](../screenshots/android-assets-bike.png), showing the continuous angled divider west of the lobby.

## Map controls and explanation ✅

Used + `(983,535)`, − `(983,682)`, canvas pan `(700,1300)→(450,1300)` over 600ms, and Fit `(983,829)`. Final Fit restored the whole ground floor. More `(975,220)` exposed the supplied-plan/fictional-interior explanation. No crash was observed during the walkthrough.

No physical phone or iOS/browser run, TalkBack audit, route verification or performance benchmark is claimed. Toilets remain unplaced.
