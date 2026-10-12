import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { parsePackage, serializePackage, validatePackage } from '../../../content/community/v1/index.js';
import { fitSprite, placeFurniture, moveFurniture, removeFurniture } from './room.ts';

const source = await readFile(new URL('./sample-package.json', import.meta.url), 'utf8');
const starter = JSON.parse(source);

test('keeps the frozen package fixture valid through JSON round trip', () => {
  const parsed = parsePackage(source);
  assert.deepEqual(JSON.parse(serializePackage(parsed)), starter);
  assert.deepEqual(validatePackage(parsed), starter);
});

test('adds, moves and removes furniture while enforcing the trusted chamfered boundary', () => {
  const added = placeFurniture(starter, 'desk');
  assert.ok(added);
  assert.equal(added.room.objects.at(-1).id, 'desk-2');
  assert.equal(moveFurniture(added, 'desk-2', 0, 0), null);
  const moved = moveFurniture(added, 'desk-2', 30, 30);
  assert.deepEqual([moved.room.objects.at(-1).x, moved.room.objects.at(-1).y], [30, 30]);
  assert.equal(removeFurniture(moved, 'desk-2').room.objects.some(item => item.id === 'desk-2'), false);
});

test('fits the atlas sprite aspect ratio inside object bounds like the game renderer', () => {
  const item = { x: 47, y: 45, width: 72, height: 38 };
  const fitted = fitSprite(item, 256, 219);
  assert.ok(fitted);
  assert.ok(Math.abs(fitted.width / fitted.height - 256 / 219) < 1e-10);
  assert.ok(Math.abs(fitted.height - 38) < 1e-10);
  assert.ok(Math.abs((fitted.x - item.x) - (item.width - fitted.width) / 2) < 1e-10);
  assert.equal(fitSprite(item, 0, 219), null);
});
