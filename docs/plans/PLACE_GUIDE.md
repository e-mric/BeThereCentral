# Place guide interaction

9 October 2026. User problem: tapping a place should introduce it through a character and offer directions or its company mission / meeting-room purpose.

## Iteration plan

1. Keep selection anchored to the supplied place marker until entrance positions are provided. Preserve the exact clicked repeated marker. Search selects the first known anchor; unplaced entries never get invented positions.
2. Zoom toward that marker, above a compact character guide. Offer Directions and About the company / Room purpose / About this place according to explicit place type. Keep controls readable and dialogue scrollable.
3. For this data stage, Directions gives the known floor and source number only, explicitly saying entrance and route information is not yet confirmed. No origin, path, distance or accessibility claim.
4. Keep resident-supplied mission, room purpose, extra information and labelled HTTPS links separate from source geometry. Attribute provided content; show honest empty states while it is absent. Do not invent company descriptions or booking/capacity details.
5. Validate selection, repeated anchors, unplaced entries and profile states in shared tests; inspect the actual Android character/menu/directions/company/meeting-room journey. Update docs and push the runnable slice.

This extends the existing Compose map. Game migration, character creation, accounts and a publishing/editor workflow stay deferred. Actual entrance-facing placement and walking routes require later entrance/corridor data.

## Outcome

Implemented the provisional source-marker focus and character choices in the existing map. Shared JVM tests (57 total) and Android APK build passed. Android emulator checks covered company choices, missing mission, source-only Directions, exact repeated marker focus, meeting-room purpose, larger text and unplaced The Sky. See [verification status](../testing/STATUS.md#character-led-place-guide--9-october-2026). Entrances, walking routes and resident-supplied content remain future data work.
