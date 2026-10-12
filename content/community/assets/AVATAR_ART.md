# Original modular character art v1

`avatar-parts-v1.json` is an original, code-native pixel-art set for the fictional community game. No photographs, external sprites, generated image pixels, or runtime assets are used. The two canonical demo characters are illustrative game characters, not depictions of real people. The user asked for stronger, more readable characters after reviewing the earlier flat sprites; these new characters use shaped silhouettes, layered color ramps, face and garment details, and directional poses.

The source frame is 64 × 96 RGBA pixels, shown at 32 × 48 world pixels, with the foot pivot at `[32,88]` and 160 ms frame duration. A frame is composed by painting each matching part in its listed order. `select` keys are existing catalog enums (or `"*"`); `frames` identify direction and pose. Each integer run is `[x,y,length,colorKey]`, with top-left origin and Y downward. Later runs and later parts overwrite earlier bytes. There is no alpha blending. Palette colors are exact RGBA byte arrays; `@bodyPalette.base` resolves through `appearanceRamps.skin[appearance.bodyPalette]` and then that palette's `base` entry. Hair, top and trousers aliases resolve in the same way. Unprefixed keys resolve in `palettes.fixed`. This is bounded data: it has no executable expressions, external resources, or URLs.

This is the custom-parts provider; polished Founder/Patch raster presets are documented separately in `README.md`. The full appearance dimensions stay aligned with `catalog-v2.json`: body (`warm`, `deep`, `light`, `olive`); hair shape (`short`, `curly`, `long`, `bald`) and hair palette (`black`, `brown`, `gold`, `silver`); tops (`tee`, `hoodie`, `jacket`) and top palettes (`teal`, `orange`, `purple`, `cream`); trousers (`slate`, `navy`, `brown`); accessory (`none`, `glasses`). The source JSON repeats all five poses for each of front, back, left and right. Walk poses shift opposing limbs; back faces contain no eyes, glasses only draw one side lens on side views, and bald selections add no hair pixels. Shoes, hems, seams, lapels, hoodie opening and pocket, sleeve slopes, knees and soles use separate visible tones.

All art and palette authorship: BeThereCentral contributors, original for this repository, 12 October 2026; MIT project code licence. Character art rights remain with the project contributors; this does not grant rights to portray a real person. The authoring source used simple integer polygon/rectangle masks and fixed color runs, then serialized only the bounded JSON artifact.

## Preview and byte identity

The review contact sheet is `.scratch/avatar-art-preview.png` (rows show Player, Patch and Bald; columns show front idle/walk poses followed by directional idle views). The exact sample appearance values and SHA-256 matrix are in the tracked `avatar-conformance-v1.json`. Each digest covers all `64 × 96 × 4` row-major RGBA8 bytes after matching selectors and resolving aliases. This can be used to compare the independent Studio and Godot rasterizers. Transparent pixels are four zero bytes; no preview background is included in a frame digest.

Player front hashes provide a concise walk-frame check:

| Pose | SHA-256 of native RGBA8 frame |
| --- | --- |
| idle | `cdbdba4717df483d2da24443f6323581ad524d18e29ec969888042d61b1e5f76` |
| walk-a | `b15f1c103bcc8c475954a3446b8ff97ce949b4f1c025d58cae39acc027be3b43` |
| walk-b | `e1060f6a2e73061474363084afc7560cd99210e680532f4834cd5c1945e54ce5` |
| walk-c | `7552fdcd57800b3d0b7ba8beab77d26b0849d9db436b6f32fee22354028d2b9f` |
| walk-d | `bd5d64bc096df716e7dfaf099b593bddbb070b9ef2844767492d5ade9c77bf93` |

The matrix file is tracked test evidence and is not exported in authored content; the JSON art definition is trusted bundled runtime data. Runtimes must resolve the same appearance enums, layer order, integer coordinates, overwrite behavior and RGBA palette bytes to reproduce identical digests.
