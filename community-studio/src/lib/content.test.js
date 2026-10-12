import test from 'node:test';
import assert from 'node:assert/strict';
import { STARTER, addProp, avatarPresetCatalog, clonePackage, hitTestObject, makePlayPackage, paintElements, parsePackage, serializePackage, updatePackage, validatePackage } from './content.ts';
import { readFile } from 'node:fs/promises';
import { createHash } from 'node:crypto';
import { avatarNameplateAnchor, avatarRasterCacheSize, avatarWorldRect, clearAvatarRasterCache, renderAvatarPresetRgba, renderAvatarRgba } from './avatar-art.ts';
import { decodePngRgba } from './png-rgba.ts';

test('creates valid v2 room and playable packages from the bundled starter', async () => {
  const roomFixture = JSON.parse(await readFile(new URL('../../../content/community/v2/package-v2.json', import.meta.url), 'utf8'));
  const foundersFixture = JSON.parse(await readFile(new URL('../../../content/community/v2/package-founders-v2.json', import.meta.url), 'utf8'));
  const ninonFixture = JSON.parse(await readFile(new URL('../../../content/community/v2/package-ninon-v2.json', import.meta.url), 'utf8'));
  const morganeFixture = JSON.parse(await readFile(new URL('../../../content/community/v2/package-morgane-v2.json', import.meta.url), 'utf8'));
  const architectFixture = JSON.parse(await readFile(new URL('../../../content/community/v2/package-architect-v2.json', import.meta.url), 'utf8'));
  assert.deepEqual(validatePackage(STARTER).features, ['rooms-v2']);
  assert.deepEqual(validatePackage(roomFixture), roomFixture);
  assert.deepEqual(validatePackage(foundersFixture), foundersFixture);
  assert.deepEqual(validatePackage(ninonFixture), ninonFixture);
  assert.deepEqual(validatePackage(morganeFixture), morganeFixture);
  assert.deepEqual(validatePackage(architectFixture), architectFixture);
  assert.equal(ninonFixture.characters.find(character => character.id === 'guide').introduction, '');
  assert.equal(morganeFixture.characters.find(character => character.id === 'guide').introduction, '');
  assert.equal(architectFixture.characters.find(character => character.id === 'player').displayName, 'The Architect');
  assert.equal(architectFixture.characters.find(character => character.id === 'player').introduction, '');
  const playable = makePlayPackage(STARTER);
  assert.deepEqual(playable.features, ['rooms-v2', 'play-learn-v1']);
  assert.equal(playable.characters.length, 2);
  assert.equal(playable.quest.clues.length, 2);
  assert.equal(playable.quest.answers.length, 3);
  assert.deepEqual(playable.characters.map(character => character.appearance), [{ presetId: 'founder-v1' }, { presetId: 'patch-v1' }]);
  assert.deepEqual(parsePackage(serializePackage(playable)), playable);
});

test('rasterizes every trusted atlas preset against its 20 canonical frame hashes', async () => {
  clearAvatarRasterCache();
  const presets = avatarPresetCatalog();
  for (const [presetId, preset] of Object.entries(presets)) {
    const imageBytes = new Uint8Array(await readFile(new URL(`../../../content/community/assets/${preset.image}`, import.meta.url)));
    const atlas = await decodePngRgba(imageBytes);
    const sampleName = presetId.replace(/-v1$/, '');
    const conformance = JSON.parse(await readFile(new URL(`../../../content/community/assets/${sampleName}-conformance-v1.json`, import.meta.url), 'utf8'));
    assert.equal(conformance.presetId, presetId);
    for (const [direction, poses] of Object.entries(conformance.hashes)) {
      for (const [pose, expected] of Object.entries(poses)) {
        const studioPose = ({ walk1:'walk-a', walk2:'walk-b', walk3:'walk-c', walk4:'walk-d' })[pose] ?? pose;
        const bytes = renderAvatarPresetRgba(presetId, preset, atlas.rgba, atlas.width, atlas.height, direction, studioPose);
        assert.equal(createHash('sha256').update(bytes).digest('hex'), expected, `${presetId} ${direction} ${pose}`);
      }
    }
  }
});

test('places manual and preset frames at the same world-size foot anchor', () => {
  const foot = [120, 160];
  const manual = avatarWorldRect(...foot, [64,96], [32,48], [32,88]);
  const preset = avatarWorldRect(...foot, [64,96], [32,48], [32,88]);
  assert.deepEqual(manual, { x:104, y:116, width:32, height:48 });
  assert.deepEqual(preset, manual);
  const nameplate = avatarNameplateAnchor(preset);
  assert.deepEqual(nameplate, { x:120, y:112 });
  assert.ok(nameplate.y < preset.y, 'character label sits above the sprite rather than over its face');
});

test('v2 catalog additions remain bounded by the trusted room validator', () => {
  const next = addProp(STARTER, 'arcade-cabinet');
  assert.ok(next);
  assert.equal(next.room.objects.at(-1).kind, 'arcade-cabinet');
  assert.throws(() => validatePackage({ ...next, room: { ...next.room, objects: [...next.room.objects, { id: 'bad', kind: 'unknown', x: 10, y: 10, width: 20, height: 20 }] } }));
});

test('keeps the existing v1 import path by upgrading only valid legacy room files', async () => {
  const legacy = await readFile(new URL('./sample-package.json', import.meta.url), 'utf8');
  const upgraded = parsePackage(legacy);
  assert.equal(upgraded.packageVersion, 2);
  assert.equal(upgraded.room.companyName, 'Your company');
  assert.deepEqual(upgraded.features, ['rooms-v2']);
  assert.equal(upgraded.room.objects.length, 4);
});

test('hit testing chooses the topmost object using the renderer draw order', () => {
  const overlapping = [
    { id: 'desk', kind: 'desk', x: 40, y: 40, width: 50, height: 40 },
    { id: 'rug', kind: 'rug', x: 40, y: 40, width: 50, height: 40 }
  ];
  assert.equal(hitTestObject(overlapping, 50, 50)?.id, 'desk');
  assert.equal(hitTestObject(overlapping, 110, 50), null);
});

test('sorts props and actors by shared depth, then stable ID, and hit-tests props only', () => {
  const objects = [
    { id: 'high-prop', kind: 'desk', x: 40, y: 50, width: 48, height: 30 },
    { id: 'low-prop', kind: 'desk', x: 40, y: 70, width: 48, height: 30 },
    { id: 'under-rug', kind: 'rug', x: 40, y: 70, width: 48, height: 30 }
  ];
  const actors = [{ id: 'z-guide', x: 60, y: 90 }, { id: 'a-player', x: 60, y: 100 }];
  const elements = paintElements(objects, actors);
  assert.deepEqual(elements.map(element => `${element.type}:${element.id}`), [
    'prop:under-rug', 'prop:high-prop', 'character:z-guide', 'character:a-player', 'prop:low-prop'
  ]);
  assert.deepEqual(paintElements([
    { id: 'lower', kind: 'desk', x: 0, y: 0, width: 20, height: 20 },
    { id: 'Upper', kind: 'desk', x: 0, y: 0, width: 20, height: 20 },
    { id: 'A_2', kind: 'desk', x: 0, y: 0, width: 20, height: 20 }
  ]).map(element => element.id), ['A_2', 'Upper', 'lower']);
  assert.equal(hitTestObject(objects, 50, 80)?.id, 'low-prop');
});

test('rejects valid JSON padded beyond the shared package byte limit', () => {
  const source = `${JSON.stringify(STARTER)}${' '.repeat(65_536)}`;
  assert.throws(() => parsePackage(source), /65536 UTF-8 bytes/);
});

test('edits a reactive proxy package at the JSON contract boundary', () => {
  const proxyTree = (value) => value && typeof value === 'object'
    ? new Proxy(value, { get(target, key) { return proxyTree(Reflect.get(target, key)); } })
    : value;
  const reactivePackage = proxyTree(clonePackage(STARTER));
  const renamed = updatePackage(reactivePackage, draft => { draft.room.companyName = 'A new sample name'; });
  assert.equal(renamed.room.companyName, 'A new sample name');
  assert.equal(reactivePackage.room.companyName, STARTER.room.companyName, 'editing a draft leaves the active package untouched');
});

test('serializes a reactive package for explicit save and export', () => {
  const proxyTree = (value) => value && typeof value === 'object'
    ? new Proxy(value, { get(target, key) { return proxyTree(Reflect.get(target, key)); } })
    : value;
  const reactivePackage = proxyTree(clonePackage(STARTER));
  const saved = serializePackage(reactivePackage);
  assert.deepEqual(parsePackage(saved), STARTER);
});

test('rasterizes the shared art definition byte-for-byte against canonical RGBA hashes', async () => {
  const definition = JSON.parse(await readFile(new URL('../../../content/community/assets/avatar-parts-v1.json', import.meta.url), 'utf8'));
  const matrix = JSON.parse(await readFile(new URL('../../../content/community/assets/avatar-conformance-v1.json', import.meta.url), 'utf8'));
  clearAvatarRasterCache();
  for (const [sample, appearance] of Object.entries(matrix.appearances)) {
    for (const [direction, poses] of Object.entries(matrix.hashes[sample])) {
      for (const [pose, expected] of Object.entries(poses)) {
        const bytes = renderAvatarRgba(definition, appearance, direction, pose);
        const digest = createHash('sha256').update(bytes).digest('hex');
        assert.equal(digest, expected, `${sample} ${direction} ${pose}`);
      }
      const walkingHashes = ['walk-a','walk-b','walk-c','walk-d'].map(pose => poses[pose]);
      assert.equal(new Set(walkingHashes).size, 4, `${sample} ${direction} must have four distinct walk frames`);
    }
  }
  const bald = matrix.appearances.Bald;
  assert.equal(bald.hair, 'bald');
  for (const direction of ['back','left','right']) assert.ok(matrix.hashes.Bald[direction].idle, `Bald ${direction} frame is covered by the shared matrix`);
  const baldBack = renderAvatarRgba(definition, bald, 'back', 'idle');
  const [irisR, irisG, irisB] = definition.palettes.fixed.iris;
  let backEyePixels = 0;
  for (let i = 0; i < baldBack.length; i += 4) {
    if (baldBack[i] === irisR && baldBack[i + 1] === irisG && baldBack[i + 2] === irisB) backEyePixels += 1;
  }
  assert.equal(backEyePixels, 0, 'back-facing avatars do not show eyes');
});

test('keeps the shared avatar raster cache bounded while exploring variants', async () => {
  const definition = JSON.parse(await readFile(new URL('../../../content/community/assets/avatar-parts-v1.json', import.meta.url), 'utf8'));
  const tiny = { ...definition, width:1, height:1, palettes:{fixed:{ink:[0,0,0,255]}}, appearanceRamps:{}, parts:[{id:'one-pixel',select:{},frames:[{direction:'front',pose:'idle',offset:[0,0],runs:[[0,0,1,'ink']]}]}] };
  const frame = { crop:[0,0,1,1], sourcePivot:[0,0] };
  const preset = { label:'tiny',image:'tiny.png',sha256:'tiny',frameSize:[1,1],worldSize:[1,1],footPivot:[0,0],frameDurationMs:160,sourceScale:1,attribution:'test',frames:Object.fromEntries(['front','back','left','right'].map(direction=>[direction,Object.fromEntries(['idle','walk1','walk2','walk3','walk4'].map(pose=>[pose,frame]))])) };
  clearAvatarRasterCache();
  for (let i=0;i<70;i++) {
    renderAvatarRgba(tiny,{bodyPalette:'x',hair:String(i),hairPalette:'x',top:'x',topPalette:'x',trousersPalette:'x',accessory:'x'},'front','idle');
    renderAvatarPresetRgba(`preset-${i}`,preset,new Uint8Array([0,0,0,0]),1,1,'front','idle');
  }
  assert.equal(avatarRasterCacheSize(),128);
});
