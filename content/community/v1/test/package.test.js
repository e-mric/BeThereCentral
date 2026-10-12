import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { parsePackage, serializePackage, validatePackage } from '../index.js';

const fixtureText = await readFile(new URL('../package-v1.json', import.meta.url), 'utf8');
const fixture = JSON.parse(fixtureText);

test('accepts and round-trips the default room package without changing the legacy scene', () => {
  const result = parsePackage(fixtureText);
  assert.deepEqual(result, fixture);
  assert.notEqual(result, fixture);
  assert.deepEqual(JSON.parse(serializePackage(result)), fixture);
  assert.equal(result.room.room.id, 'studio-demo');
  assert.deepEqual(result.room.objects.map(object => object.id), ['desk-1', 'plant-1', 'sofa-1', 'rug-1']);
});

test('validates transactionally and leaves input untouched when nested scene validation fails', () => {
  const bad = structuredClone(fixture);
  bad.room.objects[1].id = bad.room.objects[0].id;
  const before = structuredClone(bad);
  assert.throws(() => validatePackage(bad), /unique/);
  assert.deepEqual(bad, before);
  assert.deepEqual(validatePackage(fixture), fixture);
});

test('rejects every invalid fixture in the corpus', async () => {
  for (const name of ['extra-field.json', 'unknown-feature.json', 'duplicate-id.json', 'unsafe-asset-path.json', 'unsupported-room-version.json', 'too-many-objects.json']) {
    const text = await readFile(new URL(`../fixtures/invalid/${name}`, import.meta.url), 'utf8');
    assert.throws(() => parsePackage(text), undefined, name);
  }
});

test('rejects oversized input before attempting JSON parsing', () => {
  assert.throws(() => parsePackage(`${' '.repeat(65_537)}{`), /65536 UTF-8 bytes/);
});

test('rejects non-JSON, changed attribution, and remote asset references', () => {
  assert.throws(() => parsePackage('{'), /valid JSON/);
  const attribution = structuredClone(fixture); attribution.attribution[0].text = '';
  assert.throws(() => validatePackage(attribution), /attribution/);
  const remote = structuredClone(fixture); remote.assets[0].path = 'https://example.invalid/atlas.png';
  assert.throws(() => validatePackage(remote), /trusted bundled/);
});

test('accepts equivalent catalog metadata with reordered object keys', () => {
  const reordered = structuredClone(fixture);
  reordered.assets = reordered.assets.map(asset => Object.fromEntries(Object.entries(asset).reverse()));
  reordered.attribution = reordered.attribution.map(entry => Object.fromEntries(Object.entries(entry).reverse()));
  assert.deepEqual(parsePackage(JSON.stringify(reordered)), reordered);
});
