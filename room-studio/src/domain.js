export const ROOM = Object.freeze({
  id: 'studio-demo', width: 320, height: 224,
  polygon: [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]
});
export const STORAGE_KEY = 'betherecentral.room-studio.v1';
export const KINDS = Object.freeze(['desk', 'plant', 'sofa', 'rug']);
export const DEFAULT_SIZE = Object.freeze({ desk: [64, 36], plant: [26, 30], sofa: [88, 42], rug: [78, 48] });
const ROOT_KEYS = ['schemaVersion', 'companyName', 'room', 'objects'];
const ROOM_KEYS = ['id', 'width', 'height', 'polygon'];
const OBJECT_KEYS = ['id', 'kind', 'x', 'y', 'width', 'height'];

const isRecord = value => value !== null && typeof value === 'object' && !Array.isArray(value);
const exactKeys = (value, keys) => isRecord(value) && Object.keys(value).length === keys.length && keys.every(key => Object.hasOwn(value, key));
const finite = value => typeof value === 'number' && Number.isFinite(value);
const sameRoom = room => exactKeys(room, ROOM_KEYS) && room.id === ROOM.id && room.width === ROOM.width && room.height === ROOM.height && JSON.stringify(room.polygon) === JSON.stringify(ROOM.polygon);

export function pointInPolygon(point, polygon = ROOM.polygon) {
  let sign = 0;
  for (let i = 0; i < polygon.length; i += 1) {
    const a = polygon[i], b = polygon[(i + 1) % polygon.length];
    const cross = (b[0] - a[0]) * (point[1] - a[1]) - (b[1] - a[1]) * (point[0] - a[0]);
    if (Math.abs(cross) < 1e-8) continue;
    const next = Math.sign(cross);
    if (sign && sign !== next) return false;
    sign = next;
  }
  return true;
}

export function objectFits(object) {
  if (![object.x, object.y, object.width, object.height].every(finite)) return false;
  if (object.width < 16 || object.height < 16 || object.width > ROOM.width || object.height > ROOM.height) return false;
  if (object.x < 0 || object.y < 0 || object.x + object.width > ROOM.width || object.y + object.height > ROOM.height) return false;
  return [[object.x, object.y], [object.x + object.width, object.y], [object.x, object.y + object.height], [object.x + object.width, object.y + object.height]].every(point => pointInPolygon(point));
}

export function validateScene(value) {
  if (!exactKeys(value, ROOT_KEYS)) throw new Error('Room file must contain exactly schemaVersion, companyName, room, and objects.');
  if (value.schemaVersion !== 1) throw new Error('This room file version is not supported.');
  if (typeof value.companyName !== 'string' || value.companyName.trim() !== value.companyName || value.companyName.length < 1 || value.companyName.length > 60 || /[\u0000-\u001f\u007f-\u009f]/u.test(value.companyName)) throw new Error('Company label must be 1–60 trimmed characters with no control characters.');
  if (!sameRoom(value.room)) throw new Error('Room geometry must match the trusted Studio demo room.');
  if (!Array.isArray(value.objects) || value.objects.length > 24) throw new Error('A room can contain at most 24 furniture pieces.');
  const ids = new Set();
  for (const object of value.objects) {
    if (!exactKeys(object, OBJECT_KEYS)) throw new Error('Each furniture item must contain exactly id, kind, x, y, width, and height.');
    if (typeof object.id !== 'string' || !/^[A-Za-z0-9_-]{1,40}$/.test(object.id) || ids.has(object.id)) throw new Error('Furniture IDs must be unique, safe names of up to 40 characters.');
    ids.add(object.id);
    if (!KINDS.includes(object.kind)) throw new Error('This furniture type is not available in Room Studio.');
    if (!objectFits(object)) throw new Error('A furniture item must fit fully inside the room boundary.');
  }
  return structuredClone(value);
}

export function makeScene(fixture) { return validateScene(fixture); }
export function snapshot(scene) { return structuredClone(scene); }
export function positionObject(scene, id, x, y) {
  const next = snapshot(scene);
  const item = next.objects.find(object => object.id === id);
  if (!item) return null;
  item.x = Math.round(x); item.y = Math.round(y);
  try { return validateScene(next); } catch { return null; }
}
export function createObject(scene, kind) {
  if (!KINDS.includes(kind) || scene.objects.length >= 24) return null;
  const [width, height] = DEFAULT_SIZE[kind];
  let index = 1, id;
  const used = new Set(scene.objects.map(object => object.id));
  do { id = `${kind}-${index++}`; } while (used.has(id));
  const candidates = [[128, 78], [80, 78], [190, 78], [128, 145], [76, 145], [195, 145], [128, 35], [32, 85], [250, 90]];
  for (const [x, y] of candidates) {
    const next = snapshot(scene);
    next.objects.push({ id, kind, x, y, width, height });
    try { return validateScene(next); } catch { /* Try the next known in-bounds spot. */ }
  }
  return null;
}
export function removeObject(scene, id) {
  const next = snapshot(scene), length = next.objects.length;
  next.objects = next.objects.filter(object => object.id !== id);
  return next.objects.length === length ? null : validateScene(next);
}
export function moveObject(scene, id, dx, dy) {
  const item = scene.objects.find(object => object.id === id);
  return item ? positionObject(scene, id, item.x + dx, item.y + dy) : null;
}
