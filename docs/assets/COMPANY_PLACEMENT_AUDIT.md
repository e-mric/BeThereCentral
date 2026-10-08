# Company placement against supplied plans

9 October 2026. The five supplied plans define the intended company names, number mappings and source positions for the product map. This audit records the source badge centers independently of decorative floors, furniture and display layout. The directory names and repeated-number counts matched the supplied legends; several previously entered coordinates needed correction.

## Source identity

The five images supplied again on 9 October are byte-for-byte identical to the 8 October inputs recorded in [pixel art provenance](PIXEL_ART.md#source-input-identity). Each is 2048 × 1448 pixels. The source rasters remain outside the public repository; these hashes identify them without redistributing them.

| Floor | Supplied file basename | SHA-256 |
| --- | --- | --- |
| Ground | `codex-clipboard-2630b31d-bb6a-4274-b8e2-dc3a87e6a368.png` | `e650986b84242ffde65df0158ac2aaf98b2075161c2d6e66d66465f33e4677c1` |
| First | `codex-clipboard-9e256d55-fba6-4275-870c-070bb8d92afe.png` | `cdd8d4b30a5e90a30e5ac42b7137e4e31d24283becaa4a48003e4539743d3416` |
| Second | `codex-clipboard-df442f6c-4fef-4c90-b1ac-59d63f1ae5b5.png` | `41d1520f58b9ff8cdb26bf4d5e969ec5b84d9762f64c22d61732d576278d340b` |
| Third | `codex-clipboard-8de635e9-592f-4010-84f3-e753177f038c.png` | `5682e43e25bccfe3ef9b183f836d0cff0245b6aa43934221b8d776e05f313290` |
| Fourth | `codex-clipboard-85c59fff-efcc-4818-aff5-249b3d5e73e7.png` | `42804ffb11f27e756d4b5801a6b73570fd1055ab3ef6e1e7f13cadbde390280b` |

## Method and confidence

All five original-resolution images were visually inspected, including their legends and every plotted number. Coordinates use the original image origin at top left, X right and Y down. They are source pixels, without a measured scale. No resized screenshot coordinates were used.

For isolated white number badges, a local read-only pixel analysis identified connected near-white components in X 80–1940, Y 580–1280, selected badge-sized components and rounded each bounding-box center to integer pixels. Visual inspection associated those centers with the printed numbers; component detection alone did not determine identity. First-floor 18 and third-floor 50 were read visually because their white circles touch white partition lines. Ground information and entrance symbols were also checked visually.

Confidence is high for the printed names, badge identities, repeated-badge counts and isolated-circle centers. Integer centers are reproducible image references, not surveyed precision. Allow roughly 3 source pixels for rounding/antialiasing on isolated badges and 5 pixels for the visually read centers. Ground FARI has a confirmed colored region but no plotted point; its retained display anchor has region-level support only.

The user permits exaggerating corridor widths for readability. Keep any display adjustments separate from these source anchors. Furniture, label collision avoidance and enlarged corridors must not silently move company reference positions or create real room assignments. Toilet placement is deferred; this audit introduces no toilet locations.

## Ground floor

| Directory entry | Reference position | Evidence |
| --- | --- | --- |
| Entrance Cantersteen 12 | `(1380,1198)` | Source entrance arrow; existing coordinate matches |
| Front Desk | `(1273,1126)` | Source information badge; existing coordinate matches |
| Bike parking | `(1166,1089)` | Source bicycle icon beside the lobby's west edge; dark icon bounds are X 1147–1185, Y 1078–1099 |
| FARI AI Experience Center | No plotted badge; display anchor `(1733,1063)` | Burgundy source region identified by the legend; coordinate is a display choice within that region |

The grey and orange ground regions have no supplied occupant name. Do not assign a company to either.

### Separate bike room

The source bicycle icon belongs to a separate room at the west side of the yellow lobby area. The user's close-up supplied on 9 October confirms the white partition must be retained. Reading that line in the original 2048 × 1448 image gives the approximate centerline `(1199,1058) → (1177,1138) → (1177,1185)`. The sloped part continues down to about Y 1138; it does not turn vertical at Y 1108. Pixel checks locate its white band at X 1184–1188 at Y 1108, X 1179–1180 at Y 1128, and X 1176–1177 from Y 1140 through Y 1180.

The room's approximate source boundary is `(1143,1073), (1199,1058), (1177,1138), (1177,1185), (1141,1185)`, with the bicycle anchor `(1166,1089)` inside it. Boundary vertices are visual source traces with a few pixels of antialiasing tolerance. Keep ordinary lobby furniture east of the partition and bike parking inside this room. The white divider is continuous in the supplied image; do not invent a doorway gap. These source coordinates remain separate from any permitted readability adjustment to displayed corridor widths.

## First floor

“Name not supplied” below describes a blank legend entry, rather than a company name. Existing zone qualifiers can remain for directory context.

| Number | Source name | Badge centers `(x,y)` |
| --- | --- | --- |
| 3 | FARI Offices | `(1747,1072)`, `(1714,1216)` |
| 4 | Name not supplied | `(1134,878)` |
| 5 | Name not supplied | `(1302,1016)` |
| 6 | Name not supplied | `(1242,782)`, `(1270,896)`, `(1334,1199)` |
| 7 | Name not supplied | `(1302,738)`, `(1386,826)`, `(1386,948)`, `(1226,1202)` |
| 8 | Name not supplied | `(1037,1192)` |
| 9 | MakePlan | `(879,1216)` |
| 10 | Be education | `(448,1232)` |
| 11 | Mediawijs | `(346,1206)` |
| 12 | Teach For Belgium | `(164,1099)`, `(176,1174)` |
| 13 | Bibliothèques Sans Frontières | `(246,1046)`, `(370,961)` |
| 14 | Khan Academy | `(314,1003)` |
| 15 | CodeNPlay | `(544,834)` |
| 16 | Name not supplied | `(626,768)` |
| 17 | Name not supplied | `(774,680)` |
| 18 | Privacy Salon | `(364,1055)` |

## Second floor

| Number | Source name or shared area | Badge centers `(x,y)` |
| --- | --- | --- |
| 19 | Field Open Space | `(1112,663)` |
| 20 | Ring Twice | `(1285,746)` |
| 21 | Campfire AI | `(1401,844)`, `(1520,908)`, `(1604,952)` |
| 22 | Name not supplied | `(1655,988)` |
| 23 | MyGrid | `(1720,1046)` |
| 24 | Name not supplied | `(1694,1190)` |
| 25 | Name not supplied | `(1536,1183)` |
| 26 | Name not supplied | `(1450,1183)` |
| 27 | BeCode Team | `(1268,1182)`, `(1316,1182)`, `(1369,1183)` |
| 28 | Name not supplied | `(1166,1161)` |
| 29 | Name not supplied | `(1043,1176)` |
| 30 | Name not supplied | `(873,1190)` |
| 31 | BeCentral Meeting Room | `(762,1190)` |
| 32 | SkillsFactory Class Room | `(446,1198)` |
| 33 | redpencil.io | `(295,1209)` |
| 34 | Name not supplied | `(192,1213)` |
| 35 | Rosa | `(160,1132)`, `(272,1016)` |
| 36 | Backstage Network | `(378,944)` |

Number 19 lists Besecure, Curewiki, EAIF, European Startup Network, Sandora VR, Startup Factory and WeTechCare under Field Open Space. These occupants share its single plotted badge; the plan does not provide separate company anchors. The grey upper-west area remains without an assigned occupant.

## Third floor

| Number | Source name | Badge centers `(x,y)` |
| --- | --- | --- |
| 37 | 42 Belgium Office | `(1124,840)`, `(1353,794)` |
| 38 | 42 Belgium Class Room | `(1094,644)` |
| 39 | Switchfully | `(1835,1178)` |
| 40 | Name not supplied | `(1536,1193)` |
| 41 | Name not supplied | `(1478,1193)` |
| 42 | Name not supplied | `(1428,1193)` |
| 43 | Skipr | `(1332,1192)` |
| 44 | Name not supplied | `(1186,1193)` |
| 45 | Name not supplied | `(1061,1193)` |
| 46 | Democratic Society | `(934,1208)` |
| 47 | D4Dhub | `(760,1194)` |
| 48 | Microstart | `(426,1220)` |
| 49 | NOX Energy | `(270,1034)` |
| 50 | Valkuren | `(364,932)` |
| 51 | Name not supplied | `(387,1020)` |
| 52 | Name not supplied | `(544,824)` |
| 53 | Ecas | `(650,744)` |
| 54 | Optiniti | `(742,810)` |
| 55 | Mbrella | `(184,1178)` |

The third-floor source and canonical geometry contain three courtyard voids. The earlier note describing two was incorrect.

## Fourth floor

| Number | Source name or shared area | Badge centers `(x,y)` |
| --- | --- | --- |
| 56 | Proximus Ada | `(346,1032)` |
| 57 | Proximus CSIRT | `(618,800)` |
| 58 | Proximus Ada | `(804,1209)`, `(939,1209)`, `(1006,1209)` |
| 59 | BeAngels & Scalefund | `(1110,1203)` |
| 60 | Name not supplied | `(1181,988)` |
| 61 | Fari, AI for the Common Good | `(1078,658)`, `(1082,701)`, `(1174,666)`, `(1144,857)`, `(1370,799)`, `(1448,884)` |
| 62 | Lighthouse | `(1528,944)` |
| 63 | DT Services & Consultancy | `(1604,986)` |
| 64 | Réseau Entreprendre Bruxelles | `(1653,1016)` |
| 65 | Poppy, Joule, MyMove | `(1712,1069)` |
| 66 | The Sky | No plotted badge |

Number 65 lists Poppy, Joule and MyMove together at one badge. Number 66 lists Agence Digitale Solidaire, SkillsFactory, Moon 9, Alliance4Europe and I.CY under The Sky. Preserve its unplaced state; unused drawn rooms do not establish its position. Repeated numbers identify one directory entry at several shown anchors, not additional invented companies.

## Significant corrections

These discrepancies exceeded or approached the agreed 10–15 source-pixel placement tolerance. The complete reference tables also normalize smaller differences.

| Floor and badge | Previous coordinate | Source reference |
| --- | --- | --- |
| First 3, upper | `(1747,1044)` | `(1747,1072)` |
| First 4 | `(1132,850)` | `(1134,878)` |
| First 12, upper | `(163,1122)` | `(164,1099)` |
| First 12, lower | `(176,1188)` | `(176,1174)` |
| Second 35, west | `(162,1156)` | `(160,1132)` |
| Second 36 | `(380,906)` | `(378,944)` |
| Third 37, west | `(1123,806)` | `(1124,840)` |
| Third 44 | `(1186,1216)` | `(1186,1193)` |
| Third 45 | `(1059,1216)` | `(1061,1193)` |
| Third 47 | `(760,1216)` | `(760,1194)` |
| Fourth 57 | `(620,827)` | `(618,800)` |
| Fourth 61, northeast | `(1369,826)` | `(1370,799)` |

This is a source-placement audit. Runtime rendering and interaction evidence belongs in [testing status](../testing/STATUS.md); the source audit does not itself establish an emulator, browser or physical-device visual pass.
