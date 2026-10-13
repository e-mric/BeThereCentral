import test from 'node:test';
import assert from 'node:assert/strict';
import { clonePackage, STARTER, updatePackage } from './content.ts';
import { duplicatePropNear } from './workshop.ts';

test('duplicates a selected piece at a clear nearby position with its authored dimensions and a unique id', () => {
  const source = clonePackage(STARTER);
  const item = source.room.objects[0];
  item.width -= 4;
  item.height -= 2;
  const before = clonePackage(source);

  const next = duplicatePropNear(source, item.id);

  assert.ok(next);
  const copy = next.room.objects.find(candidate => candidate.id !== item.id && candidate.kind === item.kind && candidate.id.startsWith(`${item.kind}-copy-`));
  assert.ok(copy);
  assert.deepEqual([copy.width, copy.height], [item.width, item.height]);
  assert.ok(Math.abs(copy.x - item.x) <= item.width + 8 || Math.abs(copy.y - item.y) <= item.height + 8);
  assert.ok(!source.room.objects.some(candidate => candidate.id === copy.id));
  assert.deepEqual(source, before, 'the original room package stays unchanged');
});

test('returns no duplicate when the package has reached its object limit or selection is missing', () => {
  const full = updatePackage(STARTER, draft => {
    while (draft.room.objects.length < 24) {
      const source = draft.room.objects[0];
      draft.room.objects.push({ ...source, id: `extra-${draft.room.objects.length}` });
    }
  });
  assert.equal(duplicatePropNear(full, full.room.objects[0].id), null);
  assert.equal(duplicatePropNear(STARTER, 'missing-piece'), null);
});

test('returns no duplicate when every bounded nearby placement is occupied', () => {
  const source = STARTER.room.objects[0];
  const blocked = updatePackage(STARTER, draft => {
    for (const [index, [x, y]] of [[127,45],[47,91],[127,91],[207,45],[47,137],[207,91],[127,137],[47,183],[127,183]].entries()) {
      draft.room.objects.push({ ...source, id: `block-${index}`, x, y });
    }
  });
  assert.equal(duplicatePropNear(blocked, source.id), null);
});
