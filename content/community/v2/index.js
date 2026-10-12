import catalog from '../assets/catalog-v2.json' with { type: 'json' };
import { parsePackage as parseV1, validatePackage as validateV1 } from '../v1/index.js';

export const MAX_PACKAGE_BYTES = 65_536;
export const MAX_OBJECTS = 24;
export const ROOM = Object.freeze({
  id: 'studio-demo', width: 320, height: 224,
  polygon: [[24, 0], [296, 0], [320, 24], [320, 200], [296, 224], [24, 224], [0, 200], [0, 24]]
});
export const FEATURES_ROOM = Object.freeze(['rooms-v2']);
export const FEATURES_PLAY = Object.freeze(['rooms-v2', 'play-learn-v1']);
const ROOT_KEYS = ['packageVersion', 'packageId', 'contentRevision', 'features', 'assets', 'attribution', 'room', 'characters', 'quest', 'gameplay'];
const ASSET_KEYS = ['catalogId', 'catalogRevision'];
const ATTRIBUTION_ID = catalog.attributionId;
const ROOM_KEYS = ['schemaVersion', 'companyName', 'room', 'objects'];
const GEOMETRY_KEYS = ['id', 'width', 'height', 'polygon'];
const OBJECT_KEYS = ['id', 'kind', 'x', 'y', 'width', 'height'];
const CHARACTER_KEYS = ['id', 'displayName', 'introduction', 'appearance'];
const APPEARANCE_KEYS = ['bodyPalette', 'hair', 'hairPalette', 'top', 'topPalette', 'trousersPalette', 'accessory'];
const QUEST_KEYS = ['id', 'template', 'title', 'learningObjective', 'introduction', 'clues', 'question', 'answers', 'correctAnswerId', 'hint', 'explanation', 'reward'];
const CLUE_KEYS = ['id', 'title', 'text'];
const ANSWER_KEYS = ['id', 'text'];
const GAMEPLAY_KEYS = ['playerCharacterId', 'spawn', 'npc', 'clues'];
const POINT_KEYS = ['x', 'y'];
const NPC_KEYS = ['characterId', 'x', 'y'];
const GAME_CLUE_KEYS = ['clueId', 'x', 'y'];
const REWARD_KEYS = ['id', 'label'];
const props = new Map(catalog.props.map(prop => [prop.id, prop]));
const characterRules = catalog.character;

const record = value => value !== null && typeof value === 'object' && !Array.isArray(value);
const exact = (value, keys) => record(value) && Object.keys(value).length === keys.length && keys.every(key => Object.hasOwn(value, key));
const idOK = value => typeof value === 'string' && /^[A-Za-z0-9_-]{1,40}$/.test(value);
const textOK = (value, min, max) => typeof value === 'string' && value.trim() === value && value.length >= min && value.length <= max && !/[\u0000-\u001f\u007f-\u009f]/u.test(value);
const integer = value => Number.isSafeInteger(value);
const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
const sameRecord = (value, expected) => exact(value, Object.keys(expected)) && Object.keys(expected).every(key => value[key] === expected[key]);
const uniqueIds = (items, label) => {
  const seen = new Set();
  for (const item of items) {
    if (!idOK(item.id) || seen.has(item.id)) throw new Error(`${label} IDs must be unique safe names of up to 40 characters.`);
    seen.add(item.id);
  }
  return seen;
};

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

function validatesInRoom(object) {
  return [[object.x, object.y], [object.x + object.width, object.y], [object.x, object.y + object.height], [object.x + object.width, object.y + object.height]].every(point => pointInPolygon(point));
}

function validateRoom(value) {
  if (!exact(value, ROOM_KEYS) || value.schemaVersion !== 2) throw new Error('Room must be the documented schema version 2 shape.');
  if (!textOK(value.companyName, 1, 60)) throw new Error('Company label must be 1–60 trimmed characters with no control characters.');
  const shape = value.room;
  if (!exact(shape, GEOMETRY_KEYS) || shape.id !== ROOM.id || shape.width !== ROOM.width || shape.height !== ROOM.height || !same(shape.polygon, ROOM.polygon)) throw new Error('Room geometry must match the trusted 320 × 224 chamfered room.');
  if (!Array.isArray(value.objects) || value.objects.length > MAX_OBJECTS) throw new Error(`A room can contain at most ${MAX_OBJECTS} props.`);
  uniqueIds(value.objects, 'Prop');
  let secretDoors = 0;
  for (const object of value.objects) {
    if (!exact(object, OBJECT_KEYS)) throw new Error('Each prop must contain exactly id, kind, x, y, width, and height.');
    const prop = props.get(object.kind);
    if (!prop) throw new Error(`Unknown catalog prop: ${String(object.kind)}.`);
    if (![object.x, object.y, object.width, object.height].every(integer) || object.width < prop.minSize[0] || object.height < prop.minSize[1] || object.width > prop.maxSize[0] || object.height > prop.maxSize[1]) throw new Error(`Prop ${object.id} must use integer placement and size within catalog bounds.`);
    if (object.x < 0 || object.y < 0 || object.x + object.width > ROOM.width || object.y + object.height > ROOM.height || !validatesInRoom(object)) throw new Error(`Prop ${object.id} must fit inside the trusted room boundary.`);
    if (prop.interaction === 'white-construct') secretDoors += 1;
  }
  if (secretDoors > 1) throw new Error('A room can contain at most one secret bookshelf door.');
}

function validateCharacters(characters) {
  if (!Array.isArray(characters) || characters.length !== 2) throw new Error('A playable package must contain exactly two characters.');
  const ids = uniqueIds(characters, 'Character');
  for (const character of characters) {
    if (!exact(character, CHARACTER_KEYS) || !textOK(character.displayName, 1, 40) || !textOK(character.introduction, 0, 160)) throw new Error('Character fields or text limits are invalid.');
    const appearance = character.appearance;
    if (exact(appearance, ['presetId'])) {
      if (typeof appearance.presetId !== 'string' || !Object.hasOwn(catalog.avatarPresets ?? {}, appearance.presetId)) throw new Error(`Unknown trusted character preset: ${String(appearance.presetId)}.`);
    } else {
      if (!exact(appearance, APPEARANCE_KEYS)) throw new Error('Character appearance must be exactly a manual appearance or a trusted preset reference.');
      for (const key of APPEARANCE_KEYS) if (!characterRules[key].includes(appearance[key])) throw new Error(`Unknown character appearance option: ${key}.`);
    }
  }
  return ids;
}

function validateQuest(quest) {
  if (!exact(quest, QUEST_KEYS) || quest.template !== 'inspect-two-answer-one-v1') throw new Error('Quest must use the supported inspect-two-answer-one-v1 template.');
  if (!idOK(quest.id) || !textOK(quest.title, 1, 80) || !textOK(quest.learningObjective, 1, 160) || !textOK(quest.introduction, 1, 600) || !textOK(quest.question, 1, 320) || !textOK(quest.hint, 1, 800) || !textOK(quest.explanation, 1, 800)) throw new Error('Quest IDs or text fields exceed their documented limits.');
  if (!Array.isArray(quest.clues) || quest.clues.length !== 2 || !Array.isArray(quest.answers) || quest.answers.length !== 3) throw new Error('A quest must have exactly two clues and three answers.');
  const clueIds = uniqueIds(quest.clues, 'Clue');
  for (const clue of quest.clues) if (!exact(clue, CLUE_KEYS) || !textOK(clue.title, 1, 60) || !textOK(clue.text, 1, 600)) throw new Error('Clue fields or text limits are invalid.');
  const answerIds = uniqueIds(quest.answers, 'Answer');
  for (const answer of quest.answers) if (!exact(answer, ANSWER_KEYS) || !textOK(answer.text, 1, 160)) throw new Error('Answer fields or text limits are invalid.');
  if (!answerIds.has(quest.correctAnswerId)) throw new Error('Correct answer must reference one of the three answers.');
  if (!exact(quest.reward, REWARD_KEYS) || !idOK(quest.reward.id) || !textOK(quest.reward.label, 1, 80)) throw new Error('Quest reward fields are invalid.');
  return clueIds;
}

function circleInsideRoom(x, y, radius = 6) {
  for (let i = 0; i < ROOM.polygon.length; i += 1) {
    const a = ROOM.polygon[i], b = ROOM.polygon[(i + 1) % ROOM.polygon.length];
    const dx = b[0] - a[0], dy = b[1] - a[1];
    const signedArea = dx * (y - a[1]) - dy * (x - a[0]);
    if (signedArea < radius * Math.hypot(dx, dy)) return false;
  }
  return true;
}

function blockedByProp(x, y, object, radius = 6) {
  const prop = props.get(object.kind);
  if (!prop.collision) return false;
  const [rx, ry, rw, rh] = prop.collision;
  const left = object.x + rx * object.width - radius;
  const top = object.y + ry * object.height - radius;
  const right = object.x + (rx + rw) * object.width + radius;
  const bottom = object.y + (ry + rh) * object.height + radius;
  return x >= left && x <= right && y >= top && y <= bottom;
}

function validateGameplay(gameplay, characters, clueIds, objects) {
  if (!exact(gameplay, GAMEPLAY_KEYS) || !Array.isArray(gameplay.clues) || gameplay.clues.length !== 2) throw new Error('Gameplay must declare the player, spawn, guide, and two clue positions.');
  const characterIds = new Set(characters.map(character => character.id));
  if (!characterIds.has(gameplay.playerCharacterId) || !exact(gameplay.spawn, POINT_KEYS) || !exact(gameplay.npc, NPC_KEYS) || !characterIds.has(gameplay.npc.characterId) || gameplay.npc.characterId === gameplay.playerCharacterId) throw new Error('Gameplay character references are invalid.');
  const targets = [gameplay.spawn, gameplay.npc, ...gameplay.clues];
  const seenClues = new Set();
  for (const clue of gameplay.clues) {
    if (!exact(clue, GAME_CLUE_KEYS) || !clueIds.has(clue.clueId) || seenClues.has(clue.clueId)) throw new Error('Gameplay clue references must uniquely match both quest clues.');
    seenClues.add(clue.clueId);
  }
  if (seenClues.size !== clueIds.size) throw new Error('Gameplay must place both quest clues.');
  for (const point of targets) {
    if (!integer(point.x) || !integer(point.y) || point.x % 4 !== 0 || point.y % 4 !== 0 || !circleInsideRoom(point.x, point.y) || objects.some(object => blockedByProp(point.x, point.y, object))) throw new Error('Gameplay positions must be reachable standing points on the 4-pixel grid.');
  }
  const pointKey = (x, y) => `${x},${y}`;
  const visited = new Set([pointKey(gameplay.spawn.x, gameplay.spawn.y)]);
  const queue = [[gameplay.spawn.x, gameplay.spawn.y]];
  for (let cursor = 0; cursor < queue.length; cursor += 1) {
    const [x, y] = queue[cursor];
    for (const [nx, ny] of [[x - 4, y], [x + 4, y], [x, y - 4], [x, y + 4]]) {
      const key = pointKey(nx, ny);
      if (!visited.has(key) && nx >= 0 && ny >= 0 && nx <= ROOM.width && ny <= ROOM.height && circleInsideRoom(nx, ny) && !objects.some(object => blockedByProp(nx, ny, object))) {
        visited.add(key); queue.push([nx, ny]);
      }
    }
  }
  for (const point of targets.slice(1)) if (!visited.has(pointKey(point.x, point.y))) throw new Error('Gameplay points must be reachable from the spawn around trusted prop collision footprints.');
  for (const object of objects.filter(item => ['white-construct', 'debug-duck'].includes(props.get(item.kind).interaction))) {
    if (props.get(object.kind).interaction === 'white-construct') {
      const approach = {
        x: Math.round((object.x + object.width / 2) / 4) * 4,
        y: Math.ceil((object.y + object.height + 8) / 4) * 4
      };
      if (!circleInsideRoom(approach.x, approach.y) || objects.some(item => blockedByProp(approach.x, approach.y, item)) || !visited.has(pointKey(approach.x, approach.y))) {
        throw new Error(`Secret bookshelf ${object.id} must have a reachable front approach point.`);
      }
      continue;
    }
    const near = [...visited].some(key => {
      const [x, y] = key.split(',').map(Number);
      const dx = Math.max(object.x - x, 0, x - object.x - object.width);
      const dy = Math.max(object.y - y, 0, y - object.y - object.height);
      return Math.hypot(dx, dy) <= 24;
    });
    if (!near) throw new Error(`Built-in interaction ${object.id} must have a reachable approach point within 24 pixels.`);
  }
}

export function validatePackage(value) {
  if (!exact(value, ROOT_KEYS)) throw new Error('Package must contain exactly the documented v2 fields.');
  if (value.packageVersion !== 2 || value.packageId !== 'betherecentral.sample-room' || !Number.isSafeInteger(value.contentRevision) || value.contentRevision < 1) throw new Error('Unsupported package version, ID, or content revision.');
  if (!sameRecord(value.assets, { catalogId: catalog.catalogId, catalogRevision: catalog.catalogRevision })) throw new Error('Package must reference the trusted bundled asset catalog revision.');
  if (!Array.isArray(value.attribution) || !same(value.attribution, [ATTRIBUTION_ID])) throw new Error('Package attribution must identify the required original asset catalog.');
  validateRoom(value.room);
  if (same(value.features, FEATURES_ROOM)) {
    if (!Array.isArray(value.characters) || value.characters.length !== 0 || value.quest !== null || value.gameplay !== null) throw new Error('Room-only packages cannot include character or quest content.');
  } else if (same(value.features, FEATURES_PLAY)) {
    const characterIds = validateCharacters(value.characters);
    const clueIds = validateQuest(value.quest);
    validateGameplay(value.gameplay, value.characters, clueIds, value.room.objects);
    if (!characterIds.has(value.gameplay.playerCharacterId)) throw new Error('Player character reference is invalid.');
  } else throw new Error('Unsupported or missing v2 package feature.');
  return structuredClone(value);
}

export function parsePackage(source) {
  if (typeof source !== 'string' || new TextEncoder().encode(source).byteLength > MAX_PACKAGE_BYTES) throw new Error(`Package must be at most ${MAX_PACKAGE_BYTES} UTF-8 bytes.`);
  let value;
  try { value = JSON.parse(source); } catch { throw new Error('Package must be valid JSON.'); }
  return validatePackage(value);
}

export function serializePackage(value) { return JSON.stringify(validatePackage(value), null, 2); }

const LEGACY_KIND = Object.freeze({ desk: 'desk', plant: 'plant', sofa: 'sofa', rug: 'rug' });
export function upgradeV1Package(valueOrSource) {
  const legacy = typeof valueOrSource === 'string' ? parseV1(valueOrSource) : validateV1(valueOrSource);
  const upgraded = {
    packageVersion: 2, packageId: legacy.packageId, contentRevision: legacy.contentRevision,
    features: [...FEATURES_ROOM], assets: { catalogId: catalog.catalogId, catalogRevision: catalog.catalogRevision },
    attribution: [ATTRIBUTION_ID],
    room: {
      schemaVersion: 2, companyName: legacy.room.companyName, room: structuredClone(ROOM),
      objects: legacy.room.objects.map(object => ({ id: object.id, kind: LEGACY_KIND[object.kind], x: object.x, y: object.y, width: object.width, height: object.height }))
    },
    characters: [], quest: null, gameplay: null
  };
  try { return validatePackage(upgraded); }
  catch (error) { throw new Error(`Validated v1 package cannot be upgraded losslessly to v2: ${error.message}`); }
}

export const propCatalog = () => structuredClone(catalog.props);
export const getProp = id => props.has(id) ? structuredClone(props.get(id)) : null;
export const getCharacterRules = () => structuredClone(characterRules);
export const avatarPresetCatalog = () => structuredClone(catalog.avatarPresets ?? {});
export const getAvatarPreset = id => typeof id === 'string' && Object.hasOwn(catalog.avatarPresets ?? {}, id) ? structuredClone(catalog.avatarPresets[id]) : null;

export function createProgress(quest) {
  validateQuest(quest);
  return { questId: quest.id, foundClueIds: [], attempts: 0, correctAnswerAccepted: false, completed: false, rewardIds: [] };
}

export function validateProgress(quest, value) {
  validateQuest(quest);
  const keys = ['questId', 'foundClueIds', 'attempts', 'correctAnswerAccepted', 'completed', 'rewardIds'];
  if (!exact(value, keys) || value.questId !== quest.id || !Array.isArray(value.foundClueIds) || !Array.isArray(value.rewardIds) || !Number.isSafeInteger(value.attempts) || value.attempts < 0 || typeof value.correctAnswerAccepted !== 'boolean' || typeof value.completed !== 'boolean') throw new Error('Saved quest progress has an invalid shape or value.');
  const clueIds = new Set(quest.clues.map(clue => clue.id));
  if (value.foundClueIds.some(id => !clueIds.has(id)) || new Set(value.foundClueIds).size !== value.foundClueIds.length) throw new Error('Saved quest progress has invalid or repeated clues.');
  const shouldBeComplete = value.correctAnswerAccepted && value.foundClueIds.length === clueIds.size;
  if (value.completed !== shouldBeComplete || value.rewardIds.length !== (shouldBeComplete ? 1 : 0) || (shouldBeComplete && value.rewardIds[0] !== quest.reward.id)) throw new Error('Saved quest completion and reward state do not match the collected clues and answer.');
  if (value.correctAnswerAccepted && value.attempts === 0) throw new Error('Saved quest progress cannot accept an answer before an attempt.');
  return structuredClone(value);
}

export function serializeProgress(quest, value) { return JSON.stringify(validateProgress(quest, value)); }

export function parseProgress(quest, source) {
  if (typeof source !== 'string' || new TextEncoder().encode(source).byteLength > 4096) throw new Error('Saved quest progress must be at most 4096 UTF-8 bytes.');
  let value;
  try { value = JSON.parse(source); } catch { throw new Error('Saved quest progress must be valid JSON.'); }
  return validateProgress(quest, value);
}

export function observeClue(quest, progress, clueId) {
  validateQuest(quest);
  const next = validateProgress(quest, progress);
  if (!quest.clues.some(clue => clue.id === clueId)) throw new Error('Progress or clue reference is invalid.');
  if (!next.foundClueIds.includes(clueId)) next.foundClueIds.push(clueId);
  return validateProgress(quest, updateCompletion(quest, next).progress);
}

export function answerQuestion(quest, progress, answerId) {
  validateQuest(quest);
  const next = validateProgress(quest, progress);
  if (!quest.answers.some(answer => answer.id === answerId)) throw new Error('Progress or answer reference is invalid.');
  next.attempts += 1;
  const correct = answerId === quest.correctAnswerId;
  if (correct) next.correctAnswerAccepted = true;
  const result = updateCompletion(quest, next);
  return { progress: validateProgress(quest, result.progress), correct, completedNow: result.completedNow };
}

function updateCompletion(quest, progress) {
  const wasCompleted = progress.completed;
  if (progress.correctAnswerAccepted && progress.foundClueIds.length === 2 && progress.foundClueIds.every(id => quest.clues.some(clue => clue.id === id))) {
    progress.completed = true;
    if (!progress.rewardIds.includes(quest.reward.id)) progress.rewardIds.push(quest.reward.id);
  }
  return { progress, completedNow: !wasCompleted && progress.completed };
}

export function resetProgress(quest) { return createProgress(quest); }
