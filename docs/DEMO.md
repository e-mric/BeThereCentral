# Building-team demo

Built for a proposed BeCentral experience by a WeAreFounders participant, this is a prototype conversation starter, not a map or capture of your building. The interactive Gaussian-splat scene is a licensed example space with its own scene identity. The four-floor map, rooms, QR checkpoints and routes are fictional. We need the building team's permission and verified source information before linking a captured scene to rooms or offering on-site guidance.

## Run the presentation

The offline viewer is bundled at `exploration/dist/`. To show it in a desktop browser from the repository root:

```sh
python3 -m http.server 4173 --bind 127.0.0.1 --directory exploration/dist
```

Open `http://127.0.0.1:4173/`. The server is loopback-only and the viewer uses bundled files; no scene is uploaded. The Android and iOS apps package the same viewer and open Explore by default. Build Android with `./gradlew :androidApp:assembleDebug`, then install `androidApp/build/outputs/apk/debug/androidApp-debug.apk` with `adb install -r`. For iOS, open `iosApp/BeThereCentral.xcodeproj` in Xcode, select a simulator and Run. See [README](../README.md) for setup details.

## One focused demonstration

1. **Recognise the space.** Open Explore and let the actual scene appear. Say: “This is a licensed example engine room, not your building.” Keep attention on the captured space rather than on its technical implementation.
2. **Explore and recover.** Drag to change the view, try zoom, then use **Overview** to recover the starting composition. Let the participant perform those actions. Ask whether the response matched what they expected.
3. **Try the separate Map demo.** Point out that it is a fictional four-floor building using one illustrated coworking footprint. Orbit, Moss, Spark and North are fictional companies. Choose a simulated Level 1 lobby start, search for **Orbit**, select **Third floor**, then choose **Step-free**. Show the lift and destination floor. Distances use an illustrative scale, not a survey. The checkpoint is a last-seen observation, not live positioning. Optionally play the orange-backpack pixel guide. Pause at the lift, continue to the named next floor, and show the sample room introduction. The guide previews a route; it does not move the last-seen marker. Switch to Explore and back to demonstrate that the route remains.
4. **Discuss the smallest permitted pilot.** Ask which one room or short route the team would permit us to capture first, who can approve imagery and redistribution, and whether licensed plans, room identifiers, connector/accessibility information and safe QR locations are available. Capture-to-map alignment must be surveyed before room hotspots or routes can appear in Explore.

Aim for about two minutes of demonstration, then discussion. If the opening view or recovery is confusing, fix that before adding more features. The participant should be able to explain that the sample capture and map describe different places.

The local hunt and short-lived sharing controls can be shown after this walkthrough. Sharing currently demonstrates consent on one device; no real person receives a position, and the separate reference server is not connected to the app.

## What approval would unlock

We would first agree on capture boundaries and timing, people/privacy handling, an imagery license, current plan ownership, accessibility review and a point of contact for changes. Then we can build a small surveyed room-linked pilot and test both enjoyment and wayfinding with participants. Nothing in the current sample scene or fictional routes proves accuracy in a real building.

## Before presenting

Cold-open the exact build on the presentation device. Visually check the scene and controls, change the view, recover with Overview, and complete the Map demo route. A successful build or ready log is insufficient. Keep the laptop browser available as a separately labelled fallback; do not claim it validates the phone. Record actual checks in [verification status](testing/STATUS.md). See [experience standards](EXPERIENCE.md) for the review criteria.
