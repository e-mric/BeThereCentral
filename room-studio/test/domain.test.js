import test from 'node:test';
import assert from 'node:assert/strict';
import fixture from '../../composeApp/src/commonMain/composeResources/files/studio/demo-room.json' with { type: 'json' };
import { createObject, makeScene, moveObject, positionObject, removeObject, validateScene } from '../src/domain.js';

test('accepts and clones the shared native room fixture', () => {
  const scene = makeScene(fixture);
  assert.deepEqual(scene, fixture);
  assert.notEqual(scene, fixture);
});

test('rejects extra keys and any changed trusted room geometry', () => {
  assert.throws(() => validateScene({ ...fixture, accountId: 'x' }), /exactly/);
  const changed = structuredClone(fixture); changed.room.polygon[0][0] = 25;
  assert.throws(() => validateScene(changed), /geometry/);
});

test('rejects furniture whose rectangle crosses the chamfered boundary', () => {
  const changed = structuredClone(fixture);
  changed.objects = [{ id: 'desk-2', kind: 'desk', x: 0, y: 0, width: 40, height: 40 }];
  assert.throws(() => validateScene(changed), /fully inside/);
});

test('rejects duplicate IDs, unsupported kinds, controls and over-capacity scenes', () => {
  const duplicate = structuredClone(fixture); duplicate.objects[1].id = duplicate.objects[0].id;
  assert.throws(() => validateScene(duplicate), /unique/);
  const kind = structuredClone(fixture); kind.objects[0].kind = 'lamp';
  assert.throws(() => validateScene(kind), /type/);
  const name = structuredClone(fixture); name.companyName = 'Team\nOne';
  assert.throws(() => validateScene(name), /control/);
  const full = structuredClone(fixture); full.objects = Array.from({ length: 25 }, (_, i) => ({ id: `item-${i}`, kind: 'plant', x: 140, y: 90, width: 16, height: 16 }));
  assert.throws(() => validateScene(full), /at most 24/);
});

test('add, move, and remove return valid immutable room snapshots', () => {
  const start = makeScene(fixture);
  const added = createObject(start, 'desk');
  assert.equal(added.objects.length, 5);
  assert.equal(start.objects.length, 4);
  const moved = moveObject(added, 'desk-2', 8, 0);
  assert.equal(moved.objects.find(item => item.id === 'desk-2').x, 136);
  assert.equal(moveObject(moved, 'desk-2', -400, 0), null);
  assert.equal(positionObject(moved, 'desk-2', -8, 0), null);
  assert.equal(removeObject(moved, 'desk-2').objects.length, 4);
});
