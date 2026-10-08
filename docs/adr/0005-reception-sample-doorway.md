# ADR 0005: Pixel-art home and a reception sample doorway

Status: accepted · 8 October 2026

## Context

The creator wants fewer persistent buttons and a spatial transition into 3D at a map location, rather than Explore/Map tabs. They explicitly chose a labelled sample doorway at reception while no matching building capture exists.

## Decision

Open the pixel-art map. Place one clickable wooden pixel-art **3D SAMPLE** sign just inside ground-floor reception, projected through the same world coordinates as the floor. The bottom **3D** action always opens this same reception sample, as explicitly chosen for the prototype, from any map floor. It preserves the map floor, viewport, destination and last-seen observation; it does not reposition the user or pretend to enter a matching capture. The marker launches the independent engine-room sample; it does not claim that reception has been captured or that scene coordinates align.

Use bottom **Map** and **3D** switches for direct view changes, with **More** at the top right in both views. Map keeps **Find a room** visible; **Plan a route** in the journey card opens simulated starting-point choices, and route details can change the start. Put floor/view controls and route details in sheets. In the viewer, keep sample identity and a right-side Top / + / − stack. Preserve the map viewport and feature state on return. Pause the guide while any sheet or the viewer covers the map.

## Consequences

The sample is spatially discoverable, but its unrelated provenance must remain explicit. A future permitted capture needs validated registration before it can become a real room-linked scene. This supersedes ADR 0004's opening/entry treatment only; Gaussian exploration remains a core experience. The product experiment now includes finding the doorway and explaining whether the capture represents reception.

Room taps and search results open a pixel host introduction before route selection. Only **Get directions** changes the destination. Sample company missions are fictional; external community links are explicitly labelled and do not imply affiliation with those companies.

The bottom view switches remain visible in 3D, with 3D selected and viewer controls kept to the right and clear of them. Navigation, directions and resource-link buttons use pixel icons alongside text labels; rounded controls retain their touch targets.

Top fits the independent sample capture bounds at a 60° inclination, reserving space for the control rail. It is not a surveyed building floor view. The initial interior opening stays unchanged; direct camera input stops automatic movement.
