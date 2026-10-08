# Courtyard wall rendered smoke check

8 October 2026 · Android emulator `emulator-5556` · installed courtyard-wall APK. This is emulator evidence only. The parent check covered ground and first floor; this record covers second through fourth. It does not establish browser, iOS, physical-phone or assistive-technology behavior.

## Result

| Check | Observed result |
| --- | --- |
| Second floor at Fit | The source-shaped floor is visible with a large empty dark courtyard. Slate edges and repeated blue-grey window sections appear on the building side of the courtyard boundary. A smaller right-hand opening also stays dark. |
| Second floor enlarged | Two taps on `+` after Fit enlarged the courtyard edge. Caps, piers and window sections remain visible; no art fills the central void. [Rendered capture](../screenshots/android-courtyard-walls-second.png). |
| Campfire AI search | Search returned `21 · Campfire AI · Second floor`. Opening it showed a plan guide naming Campfire AI, source-plan number 21 and the fictional-interior notice. |
| Third floor at Fit | The distinct third-floor artwork and outline rendered; the central and smaller courtyard openings remained dark. Window sections were visible on their building-facing edges. |
| 42 Belgium search | Search returned `37 · 42 Belgium Office` and `38 · 42 Belgium Class Room`, both on third. Opening Office showed the corresponding plan guide, number 37 and the fictional-interior notice. |
| Fourth floor at Fit | The distinct fourth-floor artwork and outline rendered; its courtyard openings remained dark with visible windowed edges. No The Sky badge appeared in the rendered fit view. |
| The Sky search and guide | Search returned `66 · The Sky · Fourth floor · position not shown on plan`. Opening it said the legend entry has no plotted badge and showed `Position not shown on plan`. |

## Method and limits

I used `android layout --device=emulator-5556 --flat` to locate controls, `adb -s emulator-5556 shell input tap` and `adb -s emulator-5556 shell input text` for the above actions, and `android screen capture --device=emulator-5556 --output=...` for rendered captures. Each PNG was visually inspected immediately after capture. Fit captures for all three floors and an additional second-floor enlarged capture remain under `/tmp/btc-courtyard-*.png`; the selected enlarged second-floor PNG is saved in the repository at the link above. The search and guide assertions came from the emulator's displayed UI layout. This was a bounded smoke check, not a pixel-by-pixel geometry audit or an accessibility service test.
