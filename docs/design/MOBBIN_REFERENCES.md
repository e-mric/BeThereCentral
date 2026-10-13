# Pixel world and Studio design references

12 October 2026 · Mobbin searches requested by the user. Screens were inspected visually; links are the canonical Mobbin references. These guide original artwork and interaction design. They are not distributable game assets.

## Selected references

| Reference | Observed design | Application to BeThereCentral |
| --- | --- | --- |
| [Life Reset character profile](https://mobbin.com/screens/28db14a2-e6a1-4f49-a304-a048fa1880b4) | Warm pixel portrait in a small interior, dark profile panels, progress bar, equipment slots and categorized items | Cozy character proportions and a profile combining approved introduction, contribution interests and earned learning badges. Combat/stat mechanics are not required. |
| [Life Reset illustrated onboarding](https://mobbin.com/screens/00e8a7ae-9ded-4a9a-81f3-1831288e1f2d) | Three short illustrated panels with repeated character styling and one Next action | Explain explore, learn and contribute through small visual steps, keeping the same character palette across portrait and world sprites. |
| [Tolan appearance editor](https://mobbin.com/screens/c68a3c1c-20b6-49e4-bbf0-ee6a9027fd73) | Large live character preview, category icons and broad color swatches with clear selection | Studio character preview above compact hair/clothing/palette controls; retain text labels and keyboard alternatives. |
| [Finch outfit catalog](https://mobbin.com/screens/fd25240a-f998-4164-bbcf-abbe3fff15d4) | Character preview, item-category strip, inventory grid, selected-item feedback, Outfits/Furniture/Micropets tabs | Separate character parts from furniture, preview before applying, and show an obvious selected state. Begin with a small original asset catalog. |
| [Uniswap pixel portrait grid](https://mobbin.com/screens/d2418657-ea15-4d9a-8f07-71ada2025861) | Consistent portrait scale with varied hair, skin, clothing and accessories | Modular portrait parts with consistent frame/pivot proportions and varied appearance. Financial/NFT pricing is irrelevant to this project. |
| [Babbel pixel learning challenge](https://mobbin.com/screens/22c0a95b-e826-4a3f-b82c-ab18906f8c5b) | Pixel character, short prompt, selectable answer pieces, attempt status and hint action | Clear learning prompts, hints, retry and answer explanation in a readable panel layered over the world. |
| [Babbel how-to-play panel](https://mobbin.com/screens/2b3b57f2-b1ab-440f-a8ce-e966512df81f) | One illustrated interaction, a short rule and a Next action in a framed modal | Teach one control or quest rule at a time; support skipping/reopening help. |

## Search outcome and direction

Queries covered a top-down pixel world, avatar editing, Life Reset world content, Finch decoration and Gather-style virtual offices. The strongest matches were character and interaction patterns. The Gather-oriented search did not return a matching office world; no result is presented as a Gather screen. Several broad matches were irrelevant and omitted. Mobbin is useful here for product flows and creator controls; the supplied Sea of Stars and HYKE references remain the user's direction for detailed environments and atmosphere.

Build a warm, readable pixel world with original reusable props and coherent portrait/directional animation parts. Keep the game HUD compact, and use ordinary clear form controls in SvelteKit Studio around a live preview. Character appearance can be created manually; real resident names/introductions and representations require their approval. Study the patterns, rather than importing screenshots or third-party sprites into the open-source asset pack.

This reference pass does not change source company coordinates, building approval or privacy boundaries. No Mobbin image was copied into the shipped app or asset pack.

## Browser Studio workspace references

The follow-up search inspected [Rive's workspace](https://mobbin.com/screens/50d95b13-5861-4795-9f8c-f81185c426e1) and [Figma's asset browser](https://mobbin.com/screens/6f26ce58-d438-4e67-a4db-84d493b94789). Rive places hierarchy/assets left, a large canvas centrally and properties right. Figma makes asset categories and thumbnails easy to browse beside that canvas. Use that three-pane structure with beginner-friendly labels, visible selections and numeric alternatives to dragging. Group original props into Workspaces, Play, Wellness, Meetings, Lounge and Secrets. Tolan remains the reference for a large character preview and labelled appearance choices. No screenshot or third-party sprite is included in the asset catalog.

## Character redraw follow-up

The user rejected the flat initial sprites and requested a focused Mobbin character search. A detailed human RPG-profile query and a Habitica-style customization query again returned [Life Reset's profile](https://mobbin.com/screens/28db14a2-e6a1-4f49-a304-a048fa1880b4) as the strongest actual pixel-art match: expressive brown hair, shaped face, layered green clothing and warm interior shading. The Habitica query did not return a Habitica screen; no result is presented as one. [Finch's outfit editor](https://mobbin.com/screens/d6fd99b7-80c0-4684-9b66-c66a6751daaa) offers a large character preview with category icons and item slots; its vector character art is a workflow reference rather than the pixel style.

The next original modular redraw targets shaped silhouettes, hair volume, clothing folds, separated footwear and genuinely directional walking poses. Study the Life Reset design, but do not copy its character pixels or screenshots into the distributed pack. Shared source art and consumer RGBA parity remain separate from the actual visual-quality review.

## Workshop direction — 13 October 2026

The user confirmed that the Android offline editor displayed correctly on the unfolded phone, but found its presentation generic. A fresh Mobbin pass visually inspected [IKEA's room editor](https://mobbin.com/screens/bbf73f73-8cd8-44ec-a5ee-b398be2ed1f1), which gives the room most of the screen and places object tools around it; [Replika's decoration screen](https://mobbin.com/screens/31058bb0-c060-435d-b030-caaae13e3c78), with a browsable furniture tray; and [Craft's drawing editor](https://mobbin.com/screens/d4404d9e-bff6-4fd9-83ef-2b84b127c7cb), with compact tactile tools beneath a spacious canvas.

The user approved the [original generated workshop concept](STUDIO_WORKSHOP_CONCEPT.png). Its direction is a compact cream header, charcoal canvas, Room/People/Activity navigation, orange selection details and a cream categorized asset shelf. Remove the introductory marketing hero and decorative slogan boards. Keep the current trusted art, editable fields, explicit local storage and fictional-room distinction. The [implementation plan](../plans/STUDIO_WORKSHOP.md) records the scope and verification gates. The image is a visual proposal, not a shipped screenshot or replacement sprite atlas.
