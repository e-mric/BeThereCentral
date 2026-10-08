import { Application, Container, Graphics } from 'pixi.js';
import fixture from '../../composeApp/src/commonMain/composeResources/files/studio/demo-room.json';
import { createObject, makeScene, moveObject, positionObject, removeObject, snapshot, STORAGE_KEY, validateScene } from './domain.js';
import './style.css';

const $ = selector => document.querySelector(selector);
const canvas = $('#room-canvas'), stage = $('#canvas-stage');
let scene = makeScene(fixture), selectedId = scene.objects[0]?.id ?? null;
let undoStack = [], redoStack = [], drag = null, toastTimer;
let companyEditStart = null;
let isDirty = false;
const app = new Application();
const world = new Container();
app.init({ canvas, width: 640, height: 448, resolution: Math.min(window.devicePixelRatio || 1, 2), autoDensity: true, antialias: false, backgroundAlpha: 0, roundPixels: true, preference: 'webgl' }).then(() => {
  app.stage.addChild(world);
  render();
  new ResizeObserver(resize).observe(stage);
  resize();
}).catch(error => {
  console.error('Room Studio canvas unavailable', error);
  showToast('The room canvas could not start. Reload this page to try again.');
});

function clone(value) { return structuredClone(value); }
function remember(before) { undoStack.push(before); if (undoStack.length > 80) undoStack.shift(); redoStack = []; updateHistory(); }
function markDirty() { isDirty = true; $('#save-status').textContent = 'Unsaved changes. Save to keep them on this device.'; }
function commit(next, before = clone(scene)) { if (!next) return false; if (JSON.stringify(next) === JSON.stringify(scene)) return false; remember(before); scene = next; markDirty(); render(); return true; }
function updateHistory() { $('#undo-button').disabled = !undoStack.length; $('#redo-button').disabled = !redoStack.length; }
function resize() {
  if (!app.renderer) return;
  const width = stage.clientWidth, height = stage.clientHeight;
  if (!width || !height) return;
  app.renderer.resize(width, height);
  const scale = Math.min((width - 24) / 320, (height - 72) / 224);
  world.scale.set(scale);
  world.position.set((width - 320 * scale) / 2, (height + 40 - 224 * scale) / 2);
  app.render();
}
function box(g, x, y, width, height, color) { g.rect(x, y, width, height).fill(color); }
function drawItem(item) {
  const g = new Graphics(), { x, y, width: w, height: h } = item;
  if (item.kind === 'desk') {
    box(g, x, y, w, h, 0x343034);
    box(g, x + 2, y + 2, w - 4, h - 4, 0xB57957);
    box(g, x + 4, y + 4, w - 8, h - 8, 0xD49A6E);
    box(g, x + 8, y + h - 11, w / 3, 2, 0x343034);
    box(g, x + w * .57, y + h - 11, w / 3, 2, 0x343034);
  } else if (item.kind === 'sofa') {
    box(g, x, y, w, h, 0x343034);
    box(g, x + 2, y + 2, w - 4, h - 4, 0x547D82);
    box(g, x + 5, y + 5, w - 10, h - 10, 0x75A3A7);
    box(g, x + w / 2 - 1, y + 7, 2, h - 14, 0x343034);
  } else if (item.kind === 'rug') {
    box(g, x, y, w, h, 0x343034);
    box(g, x + 2, y + 2, w - 4, h - 4, 0xCB8168);
    box(g, x + 5, y + 5, w - 10, h - 10, 0xE6AE84);
    box(g, x + 7, y + 7, 8, 2, 0x343034);
    box(g, x + w - 15, y + 7, 8, 2, 0x343034);
    box(g, x + 7, y + h - 9, 8, 2, 0x343034);
    box(g, x + w - 15, y + h - 9, 8, 2, 0x343034);
  } else if (item.kind === 'plant') {
    box(g, x + w * .2, y + h * .60, w * .6, h * .4, 0x343034);
    box(g, x + w * .27, y + h * .63, w * .46, h * .3, 0x80624C);
    box(g, x + w * .27, y, w * .46, h * .55, 0x5E936B);
    box(g, x, y + h * .22, w, h * .2, 0x5E936B);
  }
  return g;
}
function render() {
  if (!app.renderer || !world.parent) { updateSidePanel(); updateHistory(); return; }
  for (const child of world.removeChildren()) child.destroy();
  const floor = new Graphics();
  floor.rect(0, 0, 320, 224).fill(0x162025);
  floor.poly(ROOM_POLYGON).fill(0xE8D7BA);
  world.addChild(floor);
  const ordered = orderedObjects();
  for (const item of ordered) {
    world.addChild(drawItem(item));
  }
  const selected = scene.objects.find(item => item.id === selectedId);
  if (selected) {
    const highlight = new Graphics().rect(selected.x - 3, selected.y - 3, selected.width + 6, selected.height + 6).stroke({ color: 0xE8A85E, width: 2 });
    world.addChild(highlight);
  }
  const wall = new Graphics();
  wall.poly(ROOM_POLYGON, true).stroke({ color: 0x604D46, width: 5 });
  world.addChild(wall);
  $('#room-sign strong').textContent = scene.companyName;
  if (document.activeElement !== $('#company-name')) $('#company-name').value = scene.companyName;
  $('#object-count').textContent = `${scene.objects.length} / 24`;
  $('#delete-selected').disabled = !selectedId;
  updateSidePanel(); updateHistory(); app.render();
}
const ROOM_POLYGON = fixture.room.polygon.flat();
function orderedObjects() { return [...scene.objects.filter(o => o.kind === 'rug'), ...scene.objects.filter(o => o.kind !== 'rug')]; }
function updateSidePanel() {
  const list = $('#object-list');
  const focusedId = list.contains(document.activeElement) ? document.activeElement.dataset.itemId : null;
  list.replaceChildren();
  const names = { desk: 'Desk', plant: 'Plant', sofa: 'Sofa', rug: 'Rug' };
  for (const item of scene.objects) {
    const button = document.createElement('button');
    button.className = `object-row${item.id === selectedId ? ' selected' : ''}`;
    button.dataset.itemId = item.id;
    button.role = 'option'; button.setAttribute('aria-selected', String(item.id === selectedId));
    button.innerHTML = `<span class="mini-furniture ${item.kind}-mini">${item.kind === 'plant' ? '♣' : item.kind === 'sofa' ? '▰' : item.kind === 'rug' ? '▧' : '▦'}</span><span>${names[item.kind]}</span><small>${item.x}, ${item.y}</small>`;
    button.addEventListener('click', () => { selectedId = item.id; render(); });
    list.append(button);
  }
  if (focusedId) list.querySelector(`[data-item-id="${CSS.escape(focusedId)}"]`)?.focus({ preventScroll: true });
  $('#delete-selected').disabled = !selectedId;
}
function showToast(message) {
  const toast = $('#toast'); toast.textContent = message; toast.classList.add('visible');
  clearTimeout(toastTimer); toastTimer = setTimeout(() => toast.classList.remove('visible'), 2800);
}
function canvasPoint(event) {
  const bounds = canvas.getBoundingClientRect(), width = stage.clientWidth, height = stage.clientHeight;
  const scale = Math.min((width - 24) / 320, (height - 72) / 224);
  return { x: (event.clientX - bounds.left - (width - 320 * scale) / 2) / scale, y: (event.clientY - bounds.top - (height + 40 - 224 * scale) / 2) / scale };
}
canvas.addEventListener('pointerdown', event => {
  const point = canvasPoint(event);
  const item = [...orderedObjects()].reverse().find(object => point.x >= object.x && point.x <= object.x + object.width && point.y >= object.y && point.y <= object.y + object.height);
  if (!item) { selectedId = null; render(); return; }
  selectedId = item.id; drag = { id: item.id, start: clone(scene), originX: item.x, originY: item.y, point };
  drag.dirtyBefore = isDirty; drag.statusBefore = $('#save-status').textContent; canvas.setPointerCapture(event.pointerId); render();
});
canvas.addEventListener('pointermove', event => {
  if (!drag) return;
  const point = canvasPoint(event), next = positionObject(scene, drag.id, drag.originX + point.x - drag.point.x, drag.originY + point.y - drag.point.y);
  if (next) { scene = next; markDirty(); render(); }
});
function finishDrag(commitMove) {
  if (!drag) return;
  const move = drag, start = move.start; drag = null;
  if (commitMove && JSON.stringify(start) !== JSON.stringify(scene)) remember(start);
  else if (!commitMove) { scene = start; isDirty = move.dirtyBefore; $('#save-status').textContent = move.statusBefore; render(); }
  updateHistory();
}
canvas.addEventListener('pointerup', () => finishDrag(true));
canvas.addEventListener('pointercancel', () => finishDrag(false));
canvas.addEventListener('lostpointercapture', () => finishDrag(false));
$('#company-name').addEventListener('focus', () => { companyEditStart = clone(scene); });
$('#company-name').addEventListener('input', event => {
  const input = event.currentTarget;
  const safe = input.value.replace(/[\u0000-\u001f\u007f-\u009f]/gu, '').slice(0, 60);
  if (safe !== input.value) input.value = safe;
  const trimmed = safe.trim();
  if (!trimmed) { input.setCustomValidity('Enter a company label.'); return; }
  input.setCustomValidity('');
  if (trimmed !== scene.companyName) { scene.companyName = trimmed; markDirty(); render(); }
});
$('#company-name').addEventListener('change', event => {
  const normalized = event.currentTarget.value.trim();
  if (normalized) scene.companyName = normalized;
  event.currentTarget.value = scene.companyName;
  if (companyEditStart && companyEditStart.companyName !== scene.companyName) remember(companyEditStart);
  companyEditStart = null; render();
});
for (const button of document.querySelectorAll('[data-add]')) button.addEventListener('click', () => {
  const next = createObject(scene, button.dataset.add);
  if (commit(next)) { selectedId = next.objects.at(-1).id; render(); } else showToast(scene.objects.length >= 24 ? 'This room already has 24 furniture pieces.' : 'There is no clear space for another piece here.');
});
$('#delete-selected').addEventListener('click', () => {
  if (!selectedId) return;
  const next = removeObject(scene, selectedId);
  if (commit(next)) { selectedId = next.objects.at(-1)?.id ?? null; render(); }
});
function nudge(dx, dy) { const next = moveObject(scene, selectedId, dx, dy); if (!commit(next)) showToast('That move would take furniture outside the room.'); }
document.querySelectorAll('[data-move]').forEach(button => button.addEventListener('click', () => {
  const delta = { up: [0, -4], down: [0, 4], left: [-4, 0], right: [4, 0] }[button.dataset.move]; nudge(...delta);
}));
document.addEventListener('keydown', event => {
  if (['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) return;
  const delta = { ArrowUp: [0, -4], ArrowDown: [0, 4], ArrowLeft: [-4, 0], ArrowRight: [4, 0] }[event.key];
  if (delta) { event.preventDefault(); nudge(...delta); }
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'z') { event.preventDefault(); (event.shiftKey ? redo : undo)(); }
  if (event.key === 'Delete' && selectedId) $('#delete-selected').click();
});
function undo() { if (!undoStack.length) return; redoStack.push(clone(scene)); scene = undoStack.pop(); selectedId = scene.objects.find(o => o.id === selectedId)?.id ?? scene.objects.at(-1)?.id ?? null; markDirty(); render(); }
function redo() { if (!redoStack.length) return; undoStack.push(clone(scene)); scene = redoStack.pop(); selectedId = scene.objects.find(o => o.id === selectedId)?.id ?? scene.objects.at(-1)?.id ?? null; markDirty(); render(); }
$('#undo-button').addEventListener('click', undo); $('#redo-button').addEventListener('click', redo);
$('#save-button').addEventListener('click', () => {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(validateScene(scene))); isDirty = false; $('#save-status').textContent = 'Saved just now on this device.'; showToast('Room saved on this device.'); }
  catch { $('#save-status').textContent = 'Could not save on this device.'; showToast('The browser could not save this room. Check storage settings and try again.'); }
});
try {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) { const restored = validateScene(JSON.parse(saved)); scene = restored; selectedId = scene.objects[0]?.id ?? null; isDirty = false; $('#save-status').textContent = 'Your saved room is ready.'; }
} catch { $('#save-status').textContent = 'Saved room could not be opened. Your starter room is ready.'; }
$('#export-button').addEventListener('click', () => {
  const blob = new Blob([`${JSON.stringify(validateScene(scene), null, 2)}\n`], { type: 'application/json' });
  const url = URL.createObjectURL(blob), link = document.createElement('a'); link.href = url; link.download = 'betherecentral-room.json'; link.click(); URL.revokeObjectURL(url); $('#file-status').textContent = 'Room JSON exported.';
});
$('#import-button').addEventListener('click', () => $('#import-file').click());
$('#import-file').addEventListener('change', async event => {
  const input = event.currentTarget, file = input.files?.[0]; if (!file) return;
  try {
    if (file.size > 256 * 1024) throw new Error('Room file is larger than the supported limit.');
    const source = await file.text();
    if (source.length > 65536) throw new Error('Room file is larger than 65,536 characters.');
    const parsed = JSON.parse(source), next = validateScene(parsed);
    if (commit(next)) { selectedId = next.objects[0]?.id ?? null; render(); }
    $('#file-status').textContent = 'Room JSON imported.'; showToast('Room file imported. Save it to keep it on this device.');
  } catch (error) { $('#file-status').textContent = error.message; showToast(`Could not import: ${error.message}`); }
  input.value = '';
});
$('#reset-button').addEventListener('click', () => {
  const next = makeScene(fixture);
  if (commit(next)) { selectedId = next.objects[0]?.id ?? null; render(); showToast('Starter room restored. Save to keep it on this device.'); }
});
window.addEventListener('resize', resize);
