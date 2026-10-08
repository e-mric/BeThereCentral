# BeCentral capture and pixel-world pilot

A meeting checklist for the building-team demo on 8 October 2026. This is a proposal, not evidence that permission has been granted.

## The request

“Could we create a private pilot covering reception, one corridor and two or three rooms? We would make a pixel-art map and an explorable 3D capture, let your team review both, and agree what may be published before expanding.”

## Decisions to get from the team

| Ask for | Why it is needed |
| --- | --- |
| A named building contact and approval for the exact pilot areas | Someone can authorize access, coordinate tenants and review the result. |
| Separate agreement on capture, storage and publication | Permission to enter or photograph an area does not establish that its plans, logos or 3D model may be published in an open-source repository. Agree where each asset may appear. |
| A quiet capture slot and access arrangements | Avoid people, confidential screens, papers, badges, keys and access codes; agree which areas must stay excluded. |
| Available floor plans, floor names, room names and stable room identifiers | Build the map and connect room content consistently across pixel art and 3D. Start with the pilot floor; request permission to use the plans. |
| A few measured distances and identifiable reference points | Align the pixel layout and capture to a common coordinate system. Record floor height, room entrances and connection points; do not assume a photograph provides surveyed measurements. |
| Staff-confirmed entrances, stairs, lifts and access restrictions | Establish which routes can be represented. Step-free claims need on-site validation before the map guides real visitors. |
| Tenant participation | For each participating company, get an approved name, logo, short mission, website link and review contact. Confirm permission for a pixel-style logo adaptation. |
| A review and release process | Decide who checks the private preview, what corrections are required, and whether the first release is private, internal or public. |

## What each version needs

**Pixel world:** a permitted plan or measured sketch, room and floor labels, reference photographs where allowed, and approved company content. The illustration may simplify furniture, but entrances, routes and room identifiers must remain coherent. Keep unverified areas clearly marked as illustrative.

**Gaussian-splat exploration:** an actual photographic capture of the approved spaces, captured with enough coverage for reconstruction, followed by processing and visual review. Plans alone cannot generate a faithful 3D reconstruction. Test the capture workflow on one room before scheduling the rest. Confirm asset storage, access, retention and distribution with the building team before collection.

**Connecting them:** agreed reference points, scale and room identifiers. A 3D doorway should open the matching approved capture at a reviewed camera position. The current engine-room sample is unrelated and demonstrates the interaction only.

## A lean first agreement

Leave the meeting with a pilot boundary, one accountable contact, a capture date, the available plans, two or three willing room/company contacts, and a review date. First deliver a private preview. Expand to four floors only after the team approves the result and the capture effort is understood.

No positioning hardware is needed to demonstrate this pilot. Physical QR checkpoints are a separate installation decision; until installed and validated, keep scans simulated and positions labelled last seen. Open-source code does not require public building captures or plans.

## Capture workflow and planning costs

This is a planning guide for the 8 October 2026 discussion. Prices below are public USD list prices checked for this proposal; checkout region, tax, platform fees and future price changes can alter the amount. They are not quotes or approval to spend.

### Capture in two passes

1. Agree on the rooms, access date, processing/storage arrangements and permitted sharing with the building contact; keep a written record. Ask whether plans and tenant marks may be used, and whether any people, screens, signs, artwork or security details must be excluded.
2. Test one ordinary room before booking a full capture. Bring a recent LiDAR-capable iPhone or iPad if available, a charged phone, a spare battery and a computer with enough free storage. A borrowed device keeps the pilot estimate low; do not buy hardware until the test establishes a need.
3. Make an optional structural pass with a scanning app that uses Apple's RoomPlan on a supported LiDAR device. Skip this pass if permitted plans and reference measurements already provide the required geometry. RoomPlan is an API for developers, not a standalone camera mode or an app already integrated into BeThereCentral. RoomPlan creates a dimensioned room plan using camera and LiDAR; it is useful for geometry and review, but it is not a Gaussian-splat capture. [Apple RoomPlan](https://developer.apple.com/augmented-reality/roomplan/)
4. Make a separate visual pass for the explorable scene. Use a capture app that explicitly creates Gaussian splats, such as Polycam or Scaniverse. Walk slowly, keep the subject in view, and make overlapping coverage from several heights and angles. Capture adjoining views through doorways only where authorized. Avoid motion blur and moving people. Glass, mirrors and blank walls can be difficult: include nearby textured surfaces and inspect these areas for gaps or distortion.
5. Prefer sharp, overlapping still photos when the chosen workflow supports photo-based reconstruction and you can take them steadily. A video can be quicker to collect, but ordinary video is not depth data and blur or fast movement can weaken reconstruction. Polycam describes Gaussian-splat creation from photos or video; confirm the selected export and processing route before the pilot. [Polycam Gaussian Splatting](https://poly.cam/tools/gaussian-splatting)
6. Use approved reference photos for furniture, signage, colours and decoration. Keep measured plans and the site walkthrough authoritative for walls, doors and dimensions; do not let generated pixel art invent entrances or change navigable geometry.
7. Keep the original captures private and unchanged. Record the device, app and version, capture date, room IDs, permitted processing destination, export format and any scale/alignment measurements in a private project log. Do not put raw captures, plans or identifying metadata in the public repository.
8. Review the result with the building contact before using it. Check missing surfaces, warped edges, privacy-sensitive details, room labels, scale and the connection between room IDs and the model. Treat measurements as approximate unless separately surveyed; a phone scan is not a certified building survey.
9. Import one room's export into both Android and iOS builds before capturing the remaining rooms. Confirm that the selected format loads, remains usable on target devices, and can be aligned to the same floor and room identifiers as the map. Also verify that the service terms and building approval permit exporting and redistributing that asset inside the app; successful export alone does not establish those rights. Keep the sample scene clearly distinct until that alignment is reviewed.
10. Delete working copies and cloud uploads according to the written retention agreement. Ask the capture provider about cloud processing, account access, deletion controls, commercial usage and export rights before uploading building imagery.

Apple's developer program is listed at **$99/year** where available. Google Play Console registration is listed at **$25 one-time**. The existing locally installed Android demo and iOS simulator do not need new store enrollment. Budget Apple membership for TestFlight or App Store distribution, even for a private TestFlight pilot, and Google registration for Google Play publishing; use an existing eligible account if available. [Apple Developer Program](https://developer.apple.com/programs/whats-included/) · [Google Play Console registration](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en)

### Software and pilot allowances

Prices are in USD as published; no currency conversion is implied. Check current local checkout totals, taxes and terms before selecting a plan.

| Item | Published price or planning treatment |
| --- | --- |
| Structural scanning app | RoomPlan is a framework, not a priced end-user app. Budget the chosen scanning app separately; Polycam's floor-plan features, for example, are listed under Business below. Existing approved plans may avoid needing a paid floor-plan export. |
| Scaniverse cloud workflow | Free: $0 with 20,000 monthly credits (about 10 minutes of mobile capture processed); Plus: $20/month or $200/year with 40,000 credits (about 20 minutes of mobile capture); Pro: $50/month or $500/year with 105,000 credits (about 60 minutes of mobile capture). Pro includes commercial rights. Verify current plan limits and rights at checkout. The newer workflow uploads captures for cloud processing; its legacy Classic on-device workflow is a separate option. [Scaniverse pricing](https://www.nianticspatial.com/pricing) · [Capture guide](https://www.nianticspatial.com/en/capture/scaniverse-getting-started) |
| Polycam | Basic is listed at $30/month or $150/year when paid annually. Business is $400/user/year and includes floor plans and advanced measurement features. Confirm the needed export on the one-room trial: mesh PLY and splat PLY are distinct formats, and an available mesh export does not establish splat export support. [Polycam pricing](https://poly.cam/pricing) |
| Storage and delivery | No extra hosting cost is required for an offline bundled pilot. Any online storage or delivery estimate depends on capture size, number of downloads, access controls and retention; measure the pilot before quoting it. |
| AI or runtime services | The current app has no runtime AI fee. Log any separately chosen image or AI service usage against its actual plan; no additional model price is assumed here. |

For an initial budget conversation, ask the team to sponsor access, plans, staff review and a loan phone. A proposed initial cash ceiling is **€500**, not approved spend; re-estimate after the one-room test. These are planning allowances, not quotes:

| Scope | Indicative allowance (EUR) | Assumptions |
| --- | ---: | --- |
| One-room workflow test | €0–€100 | Borrowed LiDAR phone if available, existing computer, free/local tools where suitable, one-room capture and review; excludes labor. |
| Private reception, corridor and two or three rooms | €100–€500 | Existing computer and borrowed device, modest capture/processing or travel expenses, private review and a small number of exports; excludes labor and hardware purchase. |

These scopes are staged budgets, not amounts to automatically add together. Existing purchases or subscriptions are not counted as new spend; record any incremental charges. Reserve part of the selected allowance for a repeat capture.

These allowances do not include paid annual Polycam Business, app-store enrollment, QR printing or installation, travel beyond a local visit, backup hardware, production hosting, security review, authenticated key exchange or production E2EE. They also do not promise that a free plan permits the intended commercial use. Confirm rights for each asset and service in writing.

Do not quote the four-floor expansion before the one-room import and private pilot review. Measure the pilot hours for capture, cleanup, map/art alignment, room content, device testing and updates; agree a rate with the project owner; then estimate each floor from measured scope. For scale only, every 40 hours at an assumed €50/hour adds €2,000; at €75/hour it adds €3,000. These are arithmetic examples, not market rates or project quotes, and do not imply that 40 hours covers a whole building. Add separately measured software, travel, storage and distribution costs.


**Four-floor estimate worksheet:** list the floor area, room count, number of capture zones and required views, plan quality, access windows, content approvals and target devices. Estimate person-hours separately for coordination, capture/retakes, reconstruction/cleanup, pixel-art adaptation, coordinate registration/routing, app integration and acceptance checks. Multiply by the agreed rates, then add selected vendor fees and an explicit contingency. Four floors are not necessarily four times the pilot; a furnished room, a long blank corridor and a stairwell have different capture effort. No fixed building-wide total is defensible before this scope is known.

**Ongoing operation:** offline viewing needs no app server today. Plan staff time for room/company changes, map verification, app/OS maintenance and replacement captures. If remote scene delivery is added, estimate storage plus downloads from measured asset sizes, keep a spending limit, and include any continued software subscriptions and annual store renewal. Borrowing equipment is preferred for the test; obtain rental/purchase quotes only if access to a suitable phone or computer is unavailable.

**Not included in this capture budget:** Room Studio, PlayCanvas/WebXR, continuous indoor positioning and its hardware, production sharing/co-op, or a professional survey. These require separate scoped estimates. No service has been purchased, no subscription has been started, and no building data has been uploaded by preparing this guide.
