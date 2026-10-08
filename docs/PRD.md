# BeThereCentral — Product Requirements

Version 0.6 · 8 October 2026 · Status: interactive sample Explore + fictional-building prototype

## Problem and audience

The intended campus is BeCentral; the project creator participates in WeAreFounders. Official building and brand approval remain pending.

Visitors, staff and event participants need to find rooms in an unfamiliar four-floor building. People who need a step-free route must understand which connectors a route uses. Friends may want to meet without permanently disclosing their movements. A cooperative discovery trail can introduce areas and companies through shared exploration, with checkpoint finds providing the game structure.

No real floor plans, room register, accessibility survey, positioning hardware or backend infrastructure have been supplied. The first delivery uses a fictional building. It must not be used for emergency evacuation or relied upon as surveyed accessibility guidance.

## Product goal

**Make an unfamiliar building feel familiar—and enjoyable to explore.**

Make indoor spaces engaging to explore through a prominent immersive experience, while helping visitors choose rooms and understand routes with dependable 2D navigation and accessibility alternatives. Splat exploration is a core product experience; its initial sample scene does not imply a mapped real building.

## Lean hypotheses and experiment

These are proposed success criteria, not measured results. The next experiment tests enjoyment and orientation in one interactive sample splat scene with 5–8 consenting participants, including people with relevant accessibility needs. Ask participants to explore, recognise landmarks, reset the view and return to the map. Evaluate fictional-building navigation separately; an unrelated scene cannot validate those routes. Repeat a building pilot later with permission and surveyed real plans.

| Hypothesis | Measure | Provisional decision threshold |
| --- | --- | --- |
| Gaussian exploration is enjoyable and understandable | Unaided camera/reset use, landmark recognition and willingness to explore again | At least 80% complete the exploration tasks; interview evidence of enjoyment |
| QR-anchored navigation is useful without continuous tracking | Unassisted destination-task completion | At least 80% |
| Search and routing are understandable | Time from task start to first route | Median under 30 seconds |
| Floor transitions are clear | Wrong-floor selections or turns | No more than one per task |
| People understand stale positions | Explain what “last seen” means after a task | At least 90% correct |
| Short sharing is comprehensible | Select recipient, duration and revoke unaided | At least 90% complete; zero accidental recipients |
| Co-op exploration adds value | Qualitative enjoyment and willingness to repeat | Interview evidence before network investment |

Collect observations manually without recording movement history. Summarize anonymously; participation is optional. If last-seen comprehension fails, fix language and interaction before adding sensors. Wayfinding findings guide map and route improvements; they do not gate immersive exploration. Keep one experiment active at a time.

## Product requirements

### Explore and navigate

- Make immersive Gaussian-splat exploration a prominent destination in the product experience.
- The prototype implements real interactive splat rendering with a distributable licensed sample asset, orbit, pan, zoom and reset. A screenshot or mesh is not a splat viewer.
- If the sample is not surveyed and mapped to the sample building, label it as a separate example space with independent scene IDs and no faux room hotspot registration.
- Include clear loading and failure states and a usable 2D fallback. Preserve destination, last-seen observation, hunt progress and consent when switching views.
- Measure loading, first frame and memory use on Android and iOS; set budgets from the chosen sample. Study enjoyment and orientation in the sample space.
- The current mobile prototype bundles a PlayCanvas viewer in Android and iOS web surfaces. Evaluate its measured performance and accessibility before committing to a production renderer.
- Later, support surveyed room-linked captures using verified building, floor and room identities and the shared meter-based coordinate contract.
- Keep searchable 2D floor maps, route previews and step-free choices available as navigation and accessibility alternatives.

### Minimal interface

[Experience standards](EXPERIENCE.md) define the opening, exploration, recovery and separate-map journey, including required visual evidence. Prototype scope can be small; the implemented journey must remain dependable.

The building is the main interface. The mobile prototype opens on the pixel-art map, with a clearly labelled **3D sample** doorway at ground-floor reception. It launches an unrelated capture, not a surveyed room view. A native **‹ Map** action returns to the retained map viewport and session; there are no Explore/Map tabs. The desktop Compose preview opens on the 2D map; a local browser presents the same sample Explore viewer. Users must not scroll past forms to reach either view. Keep the map surround soft charcoal, while retaining BeCentral blue branding/control accents and WeAreFounders orange/peach warmth. Keep geometry readable and test contrast. The reference is the spatial emphasis and contextual controls of the supplied [Matterport tour](https://my.matterport.com/show/?m=RFxTxqcbUTB), not its proprietary imagery or 3D assets.

- Open Explore at a reviewed wide interior view. Zoom-out must continue beyond lens widening to let the user see the capture from outside. Buttons, pinch, wheel and keyboard share the same bounds. Overview restores the exact opening pose and zoom. Keep the scene title/sample status compact and put extended instructions and attribution in View controls.
- Identify Map demo as a separate fictional building before presenting its next action; do not imply that it represents the captured engine room.
- In Explore, show Overview and View controls; move zoom buttons, Look around and scene information into a bottom sheet. Keep the native bottom menu visible below these controls. In Map, show a floor chip, a collapsed journey card and bottom actions Rooms, Start point, 3D and More. Search, floors, zoom/Fit, routes and secondary tools open in bottom sheets.
- Show route preferences in the contextual journey card; reveal checkpoint entry, sharing and the hunt in dismissible panels. Opening or closing panels must preserve the route, last-seen observation, hunt progress and active local grant.
- Use a compact persistent sample indicator with details on demand: **Sample · 2D** for the current map and explicit sample-scene status for Explore. Retain last-seen wording where location is shown.
- Destination selection dismisses search/keyboard and brings the route into the visible map area. Map fit must account for controls rather than frame a path underneath them.
- Use Material 3 Expressive-style controls with clear selected states and rounded/pressed shapes. Extend map and scene backgrounds edge to edge; inset only interactive overlays around status bars, cutouts and home gestures.
- Offer Light and Dark panels in More options, with Dark as the initial preference. Keep the map canvas charcoal in both modes. Light panels use BeCentral-inspired warm white, navy and peach; Dark panels use dark surfaces, blue and warm text. Bring the initial portrait floor view closer, provide a whole-floor Fit action and retain automatic route overview. Preserve the same artwork and navigation state, remember the choice in-session, and keep native system-bar contrast readable. Explore keeps its dark presentation and restores the map's preference when returning.
- Floor and icon controls need accessible labels and touch targets of at least 48 dp. Keep overlays readable, respect platform safe areas and support panel dismissal.
- Make the Explore entry discoverable when the viewer works; do not ship inactive controls implying it is already available.

Add a usability observation to the first experiment: can participants find a destination and establish their starting point without help when advanced controls are initially hidden? Minimalism must improve comprehension, not just remove labels.

### Discover and navigate

- Kotlin Multiplatform domain and Compose Multiplatform UI shared by Android and iOS; desktop preview lowers contributor setup costs.
- Four floors with stable room IDs and many searchable rooms. Display a persistent sample-building label.
- Search room names/IDs without case sensitivity; support empty results and clear selection.
- Select a destination from search or map. Switch floors independently of destination and starting location.
- Pan and zoom the 2D floor map; provide explicit zoom and overview controls.
- Calculate a graph route from a validated checkpoint. Distinguish walking, stairs and lift transitions.
- Offer default, lift-oriented and step-free routing. Step-free excludes stairs and inaccessible edges. Unavailable routes produce an understandable empty state.
- Automatically fit a route overview after route selection or changes; expose the floors and transitions involved, including destination floor.
- Show last-seen observation separately from destination. Floor switching never moves a person.

Room discovery: tap a map room or search result to see a pixel host introduction. Company missions are fictional; BeCentral and WeAreFounders links are external community resources. **Get directions** chooses the destination; browsing does not establish a last-seen position. The bottom **3D** action always opens the unrelated reception sample without changing the map floor or position. Matching real 3D capture coordinates remain future work.

### Planned Room Studio

Company members should eventually personalize their own illustrated spaces through a separate authoring tool: upload a logo, preview a pixel-art treatment, place furniture/decorations, and export or submit the room design. This is a proposed next milestone, not a working integration. Open-source availability does not grant editing rights to every tenant's space.

Start with a local sample editor and a versioned room-appearance manifest keyed by building, floor and room ID. Keep visual customization separate from authoritative walls, doors, corridors, accessibility and route graphs. Placement must respect room bounds and designated clear entrances. Include undo/reset and a preview before export. Tenant ownership, review/publication permissions, asset rights and shared editing are later explicit requirements; a building administrator should approve changes affecting the published map.

### Optional pixel guide

- Present the approved fictional coworking floor as a furnished pixel-art world, with company suites and shared rooms. Register the artwork, tappable rooms, entrances, corridors and guide through one coordinate transform. Routes must follow visibly open passages around furniture. Bundle original GPT-generated background/character assets with provenance and expose floor-qualified room names as searchable, accessible text.
- Explicitly disclose the illustrated footprint reused across four sample floors, fictional company names and illustrative distance estimates. The artwork is not a surveyed BeCentral plan and its figures are not live positions.
- Offer one tiny founder character with an orange backpack in Map demo. It is a sample route preview, never the user or another real person.
- Start only on request after a route exists. Follow its walk segments, pause at stairs/lifts, and require an explicit action before showing the next floor.
- Provide pause/resume and replay. Pause for manual floor changes, search, dialogs, leaving Map and backgrounding; do not catch up with elapsed background time.
- Reset the guide when its route, start or preference changes. It must never modify checkpoint observations, last-seen time, route policy, sharing or hunt state.
- Introduce the destination at its entrance using the sample room's existing name and category. Do not invent actual BeCentral occupants, room uses or schedules.
- Keep the guide distinct from the stationary last-seen marker, with a visible demo label, meaningful accessible text and controls at least 48 dp. The static route remains useful without animation.

### QR and position

- Validate checkpoint payloads against the current building and known checkpoints. Reject unknown, malformed and wrong-building payloads.
- Save only the latest accepted observation and its time in the local prototype.
- Clearly label manual entry and sample checkpoint actions as a scanner simulation. Real camera scanning is a separate adapter, not implied by a QR button.
- Do not draw a moving live dot or infer progress from time, selected floor or planned route.

### Optional sharing

- Off by default. Explicitly select one or more people and 5, 10 or 15 minutes.
- Explain that a shared position is last seen, not continuously tracked.
- Revoke immediately. The server must independently deny reads and writes at expiry, regardless of client countdown or connection state.
- Keep only the latest encrypted payload per active grant; no location history by default. Remove payloads after expiry/revocation and exclude them from logs/backups.
- Design for E2EE with verified recipient keys, authenticated encryption and distinct keys per sharing session. Do not present architecture or opaque payload storage as implemented E2EE.
- This prototype has a local UI simulation and a separately runnable reference server for authorization/expiry tests. They are not connected. Production accounts, transport security, key exchange and encryption remain a release gate.
- Revocation cannot erase information already read, copied or captured by a recipient; disclose this in the production consent flow.

### Cooperative discovery trail

- Ordered QR checkpoints, a shared team objective and contributions from selected sample players.
- Each stop explains its area and nearby sample rooms or fictional companies using the same building dataset as the map. Label these descriptions as sample content; do not imply they describe real BeCentral occupants.
- Invalid/out-of-order/repeated scans cannot incorrectly advance progress.
- Clearly label same-device team simulation. Real shared sessions, identity and synchronization are future work.
- Respect accessibility: do not require inaccessible routes to participate in a real pilot.

The small experiment is whether a visitor can describe one area and a nearby fictional company after using the trail, without mistaking the sample content for a verified campus directory. Keep the orientation text readable before a simulated scan so discovery is useful even when a stop cannot be reached.

## User journeys

1. Visitor opens the pixel-art map, enters the explicitly unrelated 3D sample at ground-floor reception, tries the camera and reset, returns to the same map viewport, then establishes a last-seen point using a sample checkpoint, searches for a room and previews its route.
2. Visitor chooses step-free, sees lift transitions and inspects each floor from the route overview.
3. Visitor tries the sharing demo, selects sample people and 5/10/15 minutes, observes expiry or revokes. UI makes clear that no person receives data.
4. Two players use one device in demo mode, read the sample area and company descriptions, switch the active sample player and contribute checkpoint finds to a shared discovery trail.

## Release boundaries

| Capability | Prototype expectation | Production gate |
| --- | --- | --- |
| Maps/search/routes | Working with fictional data | Surveyed, licensed plans; validated graph/accessibility |
| QR observation | Working validation and manual simulation | Native camera adapters, permissions, on-site checkpoint integrity |
| Indoor positioning | Absent; last seen only | Hardware trial, accuracy/freshness model, separate consent |
| Sharing | Local demo + disconnected reference server | Identity, TLS, integrated server, audited E2EE and lifecycle |
| Hunt | Same-device simulation | Authorized team sessions and conflict-safe sync |
| Immersive Explore | Interactive licensed sample, bundled offline on Android/iOS; browser preview; camera controls and loading/error states | Physical-device performance/memory and accessibility validation; supported device baseline |
| Surveyed room-linked captures | Not implemented | Building permission, surveyed captures and verified room/coordinate mapping |

## Architecture and maintainability

Feature-first clean architecture with pure behavior in each feature's domain package, sample implementations in data packages and UI in presentation packages. Match feature tests in `commonTest`; platform launchers contain only platform integration. Keep build reproducible with a checked-in Gradle wrapper and pinned dependencies. Publish readable setup, limitations, contribution and security documents. Every behavior change updates its tests and affected docs in the same pull request.

## Later

The initial splat scene may be an unrelated licensed sample and must use independent scene IDs until surveyed mapping exists. Later room-linked captures must use the same building/version, floor and room IDs and meter-based coordinates as navigation. Immersive exploration and the accessible 2D experience are complementary. See `COORDINATES.md`.

The separate PlayCanvas/WebXR experience remains planned and uses that same contract. It does not replace the in-app Explore milestone.

## Open questions before a real pilot

Building owner and rights to plans; canonical room register; accessible connector availability; offline requirements; supported OS/device baseline; QR placement and tamper checks; retention/legal context; real identity provider; recipient key verification; deployment owner. Resolve these through evidence and a small pilot, not invented integration claims.
