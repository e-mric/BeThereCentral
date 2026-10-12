import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import {
  answerQuestion, avatarPresetCatalog, createProgress, getAvatarPreset, getProp, observeClue, parsePackage, parseProgress, propCatalog,
  resetProgress, serializePackage, serializeProgress, upgradeV1Package, validatePackage, validateProgress
} from '../index.js';

const base = new URL('../', import.meta.url);
const read = name => readFile(new URL(name, base), 'utf8');
const roomText = await read('package-v2.json');
const playText = await read('package-play-learn-v2.json');
const room = JSON.parse(roomText);
const play = JSON.parse(playText);
const founder = JSON.parse(await read('package-founder-v2.json'));
const founders = JSON.parse(await read('package-founders-v2.json'));
const ninon = JSON.parse(await read('package-ninon-v2.json'));
const morgane = JSON.parse(await read('package-morgane-v2.json'));
const architect = JSON.parse(await read('package-architect-v2.json'));

test('validates room-only and playable handoff packages and round-trips without mutation', () => {
  assert.deepEqual(parsePackage(roomText), room);
  assert.deepEqual(parsePackage(playText), play);
  assert.deepEqual(parsePackage(JSON.stringify(founder)), founder);
  assert.deepEqual(parsePackage(JSON.stringify(founders)), founders);
  assert.deepEqual(parsePackage(JSON.stringify(ninon)), ninon);
  assert.deepEqual(parsePackage(JSON.stringify(morgane)), morgane);
  assert.deepEqual(parsePackage(JSON.stringify(architect)), architect);
  assert.deepEqual(JSON.parse(serializePackage(play)), play);
  const before = structuredClone(play);
  const copy = validatePackage(play);
  assert.notEqual(copy, play);
  assert.deepEqual(play, before);
});

test('matches the shared acceptance corpus, including every trusted catalog prop', async () => {
  const acceptance = JSON.parse(await read('acceptance.json'));
  for (const name of acceptance.valid) {
    const source = await read(name);
    assert.doesNotThrow(() => parsePackage(source), name);
  }
  for (const name of acceptance.invalid) {
    const source = await read(`fixtures/invalid/${name}`);
    assert.throws(() => parsePackage(source), undefined, name);
  }
  assert.equal(propCatalog().length, 24);
  for (const prop of propCatalog()) assert.ok(getProp(prop.id), prop.id);
  assert.equal(getProp('../scene'), null);
});

test('upgrades only a fully validated v1 room package and refuses unrepresentable geometry', async () => {
  const old = await read('../v1/package-v1.json');
  const upgraded = upgradeV1Package(old);
  assert.equal(upgraded.packageVersion, 2);
  assert.deepEqual(upgraded.features, ['rooms-v2']);
  assert.equal(upgraded.room.objects.length, 4);
  const invalidLegacy = JSON.parse(old);
  invalidLegacy.room.objects[0].x = -1;
  assert.throws(() => upgradeV1Package(invalidLegacy), /valid|inside/i);
  const oversizedLegacy = JSON.parse(old);
  oversizedLegacy.room.objects[0].width = 200;
  assert.throws(() => upgradeV1Package(oversizedLegacy), /valid|fit|catalog/i);
});

test('key order does not affect exact trusted metadata and unknown fields remain rejected', () => {
  const reordered = structuredClone(room);
  reordered.assets = Object.fromEntries(Object.entries(reordered.assets).reverse());
  reordered.room = Object.fromEntries(Object.entries(reordered.room).reverse());
  reordered.room.objects[0] = Object.fromEntries(Object.entries(reordered.room.objects[0]).reverse());
  assert.doesNotThrow(() => parsePackage(JSON.stringify(reordered)));
  reordered.room.extra = false;
  assert.throws(() => validatePackage(reordered), /schema version|shape/);
});

test('enforces numeric, UTF-16, optional intro, and byte limits', () => {
  const badBoolean = structuredClone(room);
  badBoolean.room.objects[0].x = false;
  assert.throws(() => validatePackage(badBoolean), /integer/);
  const unsafe = structuredClone(room);
  unsafe.contentRevision = Number.MAX_SAFE_INTEGER + 1;
  assert.throws(() => validatePackage(unsafe), /revision/);
  const longName = structuredClone(play);
  longName.characters[0].displayName = '😀'.repeat(21);
  assert.throws(() => validatePackage(longName), /Character fields/);
  const emptyIntro = structuredClone(play);
  emptyIntro.characters[0].introduction = '';
  assert.doesNotThrow(() => validatePackage(emptyIntro));
  assert.throws(() => parsePackage(`${' '.repeat(65_537)}{`), /65536 UTF-8 bytes/);
});

test('character appearance accepts only manual fields or a trusted preset reference', async () => {
  assert.equal(validatePackage(play).characters[0].appearance.hair, 'short');
  assert.deepEqual(validatePackage(founder).characters[0].appearance, { presetId: 'founder-v1' });
  assert.deepEqual(validatePackage(founders).characters.map(character => character.appearance.presetId), ['founder-v1', 'patch-v1']);
  assert.deepEqual([ninon, morgane].map(packageData => validatePackage(packageData).characters[1].appearance.presetId), ['ninon-v1', 'morgane-v1']);
  assert.equal(validatePackage(architect).characters[0].appearance.presetId, 'architect-v1');
  assert.equal(validatePackage(architect).characters[0].introduction, '');
  for (const name of ['bad-character-mixed-preset.json', 'bad-character-unknown-preset.json', 'bad-character-resource-injection.json']) {
    const source = await read(`fixtures/invalid/${name}`);
    assert.throws(() => parsePackage(source), undefined, name);
  }
  const preset = getAvatarPreset('founder-v1');
  assert.ok(preset?.frames?.front?.idle);
  preset.image = '../injected.png';
  assert.notEqual(getAvatarPreset('founder-v1').image, '../injected.png');
  assert.equal(getAvatarPreset('../founder-v1'), null);
  assert.ok(Object.hasOwn(avatarPresetCatalog(), 'founder-v1'));
  assert.ok(getAvatarPreset('patch-v1')?.frames?.front?.idle);
});

test('requires referenced roles, exactly two clues and three answers, and reachable gameplay', async () => {
  for (const mutate of [
    p => { p.gameplay.playerCharacterId = 'missing'; },
    p => { p.gameplay.npc.characterId = p.gameplay.playerCharacterId; },
    p => { p.quest.clues.pop(); },
    p => { p.quest.answers.pop(); },
    p => { p.quest.correctAnswerId = 'missing'; },
    p => { p.gameplay.clues[0].clueId = 'missing'; },
    p => { p.gameplay.spawn.x = 65; }
  ]) {
    const invalid = structuredClone(play);
    mutate(invalid);
    assert.throws(() => validatePackage(invalid));
  }
  const blocked = JSON.parse(await read('fixtures/invalid/blocked-path.json'));
  assert.throws(() => validatePackage(blocked), /reachable/);
  const doorIsolated = structuredClone(play);
  doorIsolated.room.objects = [
    {id:'door',kind:'secret-bookshelf',x:140,y:28,width:36,height:52},
    {id:'wall-a',kind:'desk',x:100,y:20,width:40,height:64},
    {id:'wall-b',kind:'desk',x:180,y:20,width:40,height:64},
    {id:'wall-c',kind:'desk',x:120,y:12,width:80,height:24},
    {id:'wall-d',kind:'desk',x:120,y:64,width:80,height:32}
  ];
  assert.throws(() => validatePackage(doorIsolated), /interaction|approach/);
  const offGrid = structuredClone(play);
  offGrid.room.objects.find(object => object.kind === 'secret-bookshelf').x = 253;
  assert.doesNotThrow(() => validatePackage(offGrid));
});

test('quest progress keeps clues after retries and grants the reward once after clues plus correct answer', () => {
  let progress = createProgress(play.quest);
  const wrong = answerQuestion(play.quest, progress, 'large-and-silent');
  assert.equal(wrong.correct, false);
  assert.equal(wrong.progress.completed, false);
  progress = observeClue(play.quest, wrong.progress, 'small-change');
  progress = observeClue(play.quest, progress, 'small-change');
  assert.deepEqual(progress.foundClueIds, ['small-change']);
  const correctEarly = answerQuestion(play.quest, progress, 'focused-and-tested');
  assert.equal(correctEarly.progress.completed, false);
  progress = observeClue(play.quest, correctEarly.progress, 'show-evidence');
  assert.equal(progress.completed, true);
  assert.deepEqual(progress.rewardIds, ['review-ready']);
  const repeated = answerQuestion(play.quest, progress, 'focused-and-tested');
  assert.equal(repeated.completedNow, false);
  assert.deepEqual(repeated.progress.rewardIds, ['review-ready']);
  assert.deepEqual(resetProgress(play.quest), createProgress(play.quest));
  const persisted = parseProgress(play.quest, serializeProgress(play.quest, progress));
  assert.deepEqual(persisted, progress);
  const tampered = structuredClone(persisted);
  tampered.completed = false;
  assert.throws(() => validateProgress(play.quest, tampered), /completion/);
});
