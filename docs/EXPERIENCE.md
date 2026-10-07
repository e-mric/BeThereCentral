# Experience standards

## Point of view

**Make an unfamiliar building feel familiar—and enjoyable to explore.**

The space is the main interface. Useful orientation, curiosity and control matter more than decorative polish. Minimalism means removing competing messages while keeping the next action understandable. A small, dependable journey is a better experiment than a broad, confusing prototype.

The current capture and map describe different places. Explore shows a licensed engine-room sample; Map demo shows a fictional four-floor building. Never imply that their coordinates, rooms or routes are connected. A permitted, surveyed capture is a later experiment.

## The demonstration journey

| Moment | Intended experience | Observable acceptance |
| --- | --- | --- |
| Open Explore | Recognise a space worth exploring | Actual scene pixels and usable controls appear within the viewport; a loading state covers preparation |
| Understand the sample | Know what is being shown | A short scene title and sample label remain visible; provenance and fuller explanation are available in Info |
| Explore | Predict the effect of a gesture or button | Drag changes the view; repeated zoom-out moves back far enough to see the capture from outside; zoom-in reverses it within bounds; pan has a recoverable result |
| Recover | Regain orientation immediately | Overview stops automatic movement and restores the approved camera, including target and zoom |
| Choose Map demo | Understand the change of place | The initial map card identifies a separate fictional four-floor building; no transition pretends to align it with the capture |
| Preview a route | Understand start, destination and floor changes | A simulated lobby checkpoint establishes last seen; Orbit on the third floor produces a cross-floor route; Step-free uses the lift |
| Switch views | Keep the task intact | Destination, last-seen timestamp and route preference survive Explore → Map demo; scene recovery remains available |
| Encounter a failure | Know how to continue | A readable error and Retry replace competing viewer controls; the native Map demo switch stays usable; failed rendering releases resources |

## Interface rules

- Use a compact, specific scene title instead of a large promotional headline. Let captured landmarks provide the character.
- Keep essential recovery and zoom controls visible. Explain advanced gestures and attribution in Info; avoid a permanent wall of instructions.
- Use at least 48 dp native / 48 CSS-pixel web touch targets, readable contrast and meaningful labels. Do not use colour alone to communicate state.
- Use Material 3 Expressive-style button shapes, clear filled/tonal emphasis and visible selected/pressed states. Keep touch targets at least 48 dp even when the visible art is smaller.
- Draw the map and scene edge to edge behind system bars; apply safe insets to interactive overlays rather than padding the entire canvas. Maintain readable status icons and keep bottom actions away from the home gesture.
- Respect safe areas and small windows. A full-size native WebView must produce a matching HTML viewport; loading and error content must remain reachable in short layouts.
- Do not begin an automatic camera tour without a deliberate action. Direct interaction and hiding the page stop it. Avoid surprise camera motion and honour reduced-motion preferences.
- A sample camera is not a person's position. Keep last-seen wording and the observation time wherever location matters.
- No new modal onboarding, backend or custom design-system CLI is required for this experiment. Add shared components or rules when an actual recurring decision needs them.

## BeCentral and WeAreFounders design context

BeThereCentral is intended for BeCentral. Its creator participates in WeAreFounders. This context informs the prototype; building permission and brand approval have not been established.

The [BeCentral homepage](https://www.becentral.org/) and [WeAreFounders program page](https://www.becentral.org/programs/we-are-founders) were visually inspected on 7 October 2026. Their public styles use deep blue (`#171978`), vivid orange (`#F74B23`), warm white and pale peach (`#FEE7E1`), generous rounding, bold headings and short, direct actions. WeAreFounders uses orange much more prominently than the campus homepage. These are observations of the websites, not an official brand specification.

Adapt that family into a restrained spatial interface: retain BeCentral blue in branding, controls and accents, with warm light text and orange emphasis. The creator clarified that the map's surrounding environment should be soft charcoal; this does not remove blue from the overall theme. Keep the sample scene dominant. Use sufficient contrast for small text, including dark ink on orange; do not assume white on a brand colour passes accessibility requirements. Route, destination and last-seen markers retain distinct shapes and labels. Avoid large marketing headlines over the scene.

On 8 October the creator refined the appearance: keep the map canvas charcoal in both modes. Light panels use warm white, peach and navy text; Dark panels use dark surfaces. Both retain blue controls and orange emphasis. Dark panels remain the default. The artwork stays unchanged; initial portrait framing brings the floor closer, while Fit restores the whole floor and calculated routes keep automatic overview. Keep the introduction compact. Put the panel choice in More options and remember it for the current app session across floor and Explore/Map changes. Explore remains dark. Canvas-level text and native system icons stay light over charcoal, without recreating the app or resetting routes, checkpoints or the guide.

Use the full product name **BeThereCentral** in the app title and logo wordmark. Do not shorten its identity to “BE THERE”. The original logo is distinct from BeCentral's official identity.

The sites identify Azo Sans and Satoshi font families in their styles. The prototype uses existing platform sans-serif fonts with a similar weight hierarchy; it does not redistribute those website fonts, copy their logos or imply official endorsement. A supplied brand kit can refine this later. Keep sample-map names and geometry fictional until actual campus data is approved.

## Pixel guide

The creator approved the [coworking concept](design/cowork-concept-approved.png): furnished suites for four fictional companies, meeting rooms, phone booths, a café, lounge, quiet room and reception. The map uses a registered raster background with independently defined rooms, entrances and corridor routes. Remove baked-in people; draw the optional guide separately. The same illustrated footprint is reused across four sample floors, with floor-qualified searchable room names. The scale and routes are illustrative, not surveyed. Furniture must not obstruct route segments. Original GPT-generated assets and navigation-driven artwork corrections are documented in [asset provenance](assets/PIXEL_ART.md).

A tiny founder with an orange backpack is an optional guide for the fictional map. Its walking animation previews the selected route; it does not represent a tracked person. Start it deliberately, pause at connectors and use a named next-floor action. Keep the stationary last-seen marker and observation time unchanged.

Use one compact guide row in the journey card. At arrival, introduce only the sample room name/category. Pause when the map is hidden, backgrounded or covered by search/a panel; changing routes resets the preview. Keep the path, room labels and static route usable without animation. Verify the sprite at fitted-map scale and ensure guide controls do not obscure the route on narrow screens.

## What counts as verified

A successful build proves compilation and packaging. A renderer-ready event proves internal progress. Neither proves that pixels reach the screen or that the journey is understandable.

For a viewer change, inspect the actual opening, changed camera and recovered view on the target being claimed. Check that all controls are reachable, then switch to Map demo and back. For navigation changes, exercise a sample cross-floor route and its step-free alternative. Record the device or emulator, observed results and a representative screenshot in [verification status](testing/STATUS.md). Test failures and limitations belong there too.

Use pure tests for camera invariants and feature behaviour. Visual smoke checks complement them; screenshots alone do not establish TalkBack/VoiceOver usability, physical-device performance or real-building accuracy. Shared changes must state which Android, iOS and browser targets were actually checked. Do not describe an emulator result as a physical-phone pass.

The user's phone must not receive instrumentation inspection services. Use the dedicated emulator for automated layout inspection; phone installation and narrowly scoped diagnostics use ordinary ADB. Let the user confirm phone appearance.

## Editing and ownership

- **Astra:** review the complete journey and visual evidence, challenge misleading claims, and identify the most important quality gap.
- **Sol:** integrate feature transitions, resolve inconsistencies and report what is actually verified.
- **Luna:** implement bounded changes and behaviour tests using these standards.
- **Human reviewers:** judge whether the experience is useful, enjoyable and specific to the intended place. The building team validates real-site permissions and context.

These are development responsibilities, not runtime agents. Keep one named integrator accountable for the overall result rather than treating independent feature completion as product completion.

## Small experiment

Use the [demo guide](DEMO.md) with 5–8 consenting participants. Ask them to explore, recover the overview, identify whether the scene and map represent the same place, and preview a step-free sample route. Record unaided completion, confusion, recovery and one thing they found interesting. Summarise anonymously; do not collect movement traces.

The next decision is whether this small experience is understandable and worth a permitted building capture. It is not whether we can add more features. Treat the PRD's thresholds as hypotheses, not achieved results.

## Interface references

The requested control direction follows [Material 3 Expressive](https://m3.material.io/get-started). Edge-to-edge layout follows [Android inset guidance](https://developer.android.com/develop/ui/compose/system/insets-ui): background content can fill the window while individual controls remain inset. The Compose Multiplatform implementation must be verified on both mobile hosts; an Android API name alone does not prove identical iOS behaviour.

## Inspiration

The user's supplied [presentation and transcript](https://www.youtube.com/watch?v=GLvFTMtw4Jk) informed these standards: establish a point of view (5:44), encode intent in the building process (9:00), experience the output as a user (12:25), and protect creative work (16:32). The rules above are BeThereCentral's application of those ideas, not claims that the speaker reviewed this project.
