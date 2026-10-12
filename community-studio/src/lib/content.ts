import * as contract from '../../../content/community/v2/index.js';
import { STARTER as LEGACY_STARTER } from './room.ts';

export type Prop = {
  id: string; label: string; category: string; image: string; crop: [number, number, number, number];
  defaultSize: [number, number]; minSize: [number, number]; maxSize: [number, number];
  drawOrder: 'floor' | 'prop'; collision: [number, number, number, number] | null; interaction: string | null; sha256: string;
};
export type RoomObject = { id: string; kind: string; x: number; y: number; width: number; height: number };
export type PaintActor = { id: string; x: number; y: number };
export type PaintElement = { type: 'prop'; item: RoomObject; id: string; floor: boolean; depth: number; insertion: number } | { type: 'character'; actor: PaintActor; id: string; floor: false; depth: number; insertion: number };
export type Character = {
  id: string; displayName: string; introduction: string;
  appearance: { bodyPalette: string; hair: string; hairPalette: string; top: string; topPalette: string; trousersPalette: string; accessory: string } | { presetId: string };
};
export type Clue = { id: string; title: string; text: string };
export type Answer = { id: string; text: string };
export type Quest = {
  id: string; template: 'inspect-two-answer-one-v1'; title: string; learningObjective: string; introduction: string;
  clues: [Clue, Clue]; question: string; answers: [Answer, Answer, Answer]; correctAnswerId: string;
  hint: string; explanation: string; reward: { id: string; label: string };
};
export type GamePoint = { x: number; y: number };
export type Gameplay = {
  playerCharacterId: string; spawn: GamePoint; npc: { characterId: string; x: number; y: number };
  clues: [{ clueId: string; x: number; y: number }, { clueId: string; x: number; y: number }];
};
export type StudioPackage = {
  packageVersion: 2; packageId: 'betherecentral.sample-room'; contentRevision: number;
  features: ['rooms-v2'] | ['rooms-v2', 'play-learn-v1'];
  assets: { catalogId: string; catalogRevision: number }; attribution: string[];
  room: { schemaVersion: 2; companyName: string; room: { id: string; width: number; height: number; polygon: number[][] }; objects: RoomObject[] };
  characters: Character[]; quest: Quest | null; gameplay: Gameplay | null;
};

const validateV2 = contract.validatePackage as (value: unknown) => StudioPackage;
const MAX_PACKAGE_BYTES = contract.MAX_PACKAGE_BYTES as number;
const serializeV2 = contract.serializePackage as (value: unknown) => string;
const upgradeLegacy = contract.upgradeV1Package as (value: unknown) => StudioPackage;
export const propCatalog = contract.propCatalog as () => Prop[];
export const getProp = contract.getProp as (id: string) => Prop | null;
export const getAvatarPreset = contract.getAvatarPreset as unknown as (id: string) => import('./avatar-art.ts').AvatarPreset | null;
export const avatarPresetCatalog = contract.avatarPresetCatalog as unknown as () => Record<string, import('./avatar-art.ts').AvatarPreset>;
export function getCharacterRules(): Record<string, string[]> {
  const value = (contract.getCharacterRules as () => Record<string, unknown>)();
  return Object.fromEntries(Object.entries(value).filter(([, options]) => Array.isArray(options) && options.every(option => typeof option === 'string'))) as Record<string, string[]>;
}
export const ROOM = (contract.ROOM as StudioPackage['room']['room']);
export const FEATURES_PLAY = ['rooms-v2', 'play-learn-v1'] as const;
export const STARTER = upgradeLegacy(LEGACY_STARTER);

// Package data is deliberately JSON-only. A JSON round trip also accepts the
// Svelte $state proxies passed by editor controls, which structuredClone rejects.
export function clonePackage(value: StudioPackage): StudioPackage { return JSON.parse(JSON.stringify(value)) as StudioPackage; }
export function validatePackage(value: unknown): StudioPackage { return validateV2(value); }
export function serializePackage(value: StudioPackage): string { return serializeV2(clonePackage(value)); }
export function parsePackage(source: string): StudioPackage {
  if (typeof source !== 'string' || new TextEncoder().encode(source).byteLength > MAX_PACKAGE_BYTES) {
    throw new Error(`Package must be at most ${MAX_PACKAGE_BYTES} UTF-8 bytes.`);
  }
  let value: unknown;
  try { value = JSON.parse(source); } catch { throw new Error('Package must be valid JSON.'); }
  if (value !== null && typeof value === 'object' && 'packageVersion' in value && value.packageVersion === 1) return upgradeLegacy(source);
  return validateV2(value);
}
export function updatePackage(value: StudioPackage, update: (draft: StudioPackage) => void): StudioPackage {
  const next = clonePackage(value); update(next); return validateV2(next);
}
export function addProp(value: StudioPackage, kind: string): StudioPackage | null {
  if (value.room.objects.length >= 24) return null;
  const prop = getProp(kind); if (!prop) return null;
  const used = new Set(value.room.objects.map(object => object.id));
  let index = 1, id = `${kind}-${index}`;
  while (used.has(id)) id = `${kind}-${++index}`;
  const candidates = [[128, 76], [80, 76], [192, 76], [128, 140], [72, 140], [204, 140], [132, 32], [32, 88], [252, 88], [168, 100]];
  for (const [x, y] of candidates) {
    try { return updatePackage(value, draft => draft.room.objects.push({ id, kind, x, y, width: prop.defaultSize[0], height: prop.defaultSize[1] })); }
    catch { /* Try the next fully validated location. */ }
  }
  return null;
}
export function moveProp(value: StudioPackage, id: string, x: number, y: number): StudioPackage | null {
  if (!value.room.objects.some(object => object.id === id)) return null;
  try { return updatePackage(value, draft => { const item = draft.room.objects.find(object => object.id === id)!; item.x = Math.round(x); item.y = Math.round(y); }); }
  catch { return null; }
}
export function resizeProp(value: StudioPackage, id: string, axis: 'width' | 'height', size: number): StudioPackage | null {
  if (!value.room.objects.some(object => object.id === id)) return null;
  try { return updatePackage(value, draft => { const item = draft.room.objects.find(object => object.id === id)!; item[axis] = Math.round(size); }); }
  catch { return null; }
}
export function removeProp(value: StudioPackage, id: string): StudioPackage | null {
  if (!value.room.objects.some(object => object.id === id)) return null;
  try { return updatePackage(value, draft => { draft.room.objects = draft.room.objects.filter(item => item.id !== id); }); }
  catch { return null; }
}
export function paintElements(objects: RoomObject[], actors: PaintActor[] = []): PaintElement[] {
  const elements: PaintElement[] = [
    ...objects.map((item, insertion) => ({ type: 'prop' as const, item, id: item.id, floor: getProp(item.kind)?.drawOrder === 'floor', depth: item.y + item.height, insertion })),
    ...actors.map((actor, index) => ({ type: 'character' as const, actor, id: actor.id, floor: false as const, depth: actor.y, insertion: objects.length + index }))
  ];
  return elements.sort((a, b) => Number(b.floor) - Number(a.floor) || a.depth - b.depth || (a.id < b.id ? -1 : a.id > b.id ? 1 : 0) || a.insertion - b.insertion);
}
export function paintOrder(objects: RoomObject[]): RoomObject[] {
  return paintElements(objects).flatMap(element => element.type === 'prop' ? [element.item] : []);
}
export function hitTestObject(objects: RoomObject[], x: number, y: number): RoomObject | null {
  return paintOrder(objects).reverse().find(item => x >= item.x && x <= item.x + item.width && y >= item.y && y <= item.y + item.height) ?? null;
}
export function makePlayPackage(value: StudioPackage): StudioPackage {
  if (value.features.length === 2) return clonePackage(value);
  const characters: Character[] = [
    { id: 'player', displayName: 'Alex', introduction: 'I like learning how things work and making small improvements.', appearance: { presetId: 'founder-v1' } },
    { id: 'guide', displayName: 'Mira', introduction: 'I can help you explore the room and think through the challenge.', appearance: { presetId: 'patch-v1' } }
  ];
  const quest: Quest = {
    id: 'room-review', template: 'inspect-two-answer-one-v1', title: 'Make a room change clear',
    learningObjective: 'Practice checking a small open-source content change.',
    introduction: 'A room file is easier to share when its contents are clear and checked.',
    clues: [
      { id: 'clue-room-file', title: 'The room file', text: 'The room keeps a fixed outline and lists each piece with a safe name and position.' },
      { id: 'clue-preview', title: 'The preview', text: 'The preview helps someone check the change before sharing the file.' }
    ],
    question: 'What is a helpful final check before sharing a room change?',
    answers: [
      { id: 'answer-preview', text: 'Open the preview and check the room.' },
      { id: 'answer-skip', text: 'Skip the preview and hope it looks right.' },
      { id: 'answer-path', text: 'Add a file path from the internet.' }
    ],
    correctAnswerId: 'answer-preview',
    hint: 'Look at the clue about checking the room before sharing.',
    explanation: 'A preview makes the result visible, and package validation checks the room data before the game loads it.',
    reward: { id: 'room-reviewer', label: 'Room reviewer' }
  };
  const gameplay: Gameplay = {
    playerCharacterId: 'player', spawn: { x: 160, y: 104 }, npc: { characterId: 'guide', x: 224, y: 104 },
    clues: [{ clueId: 'clue-room-file', x: 112, y: 112 }, { clueId: 'clue-preview', x: 208, y: 112 }]
  };
  return updatePackage(value, draft => { draft.features = [...FEATURES_PLAY]; draft.characters = characters; draft.quest = quest; draft.gameplay = gameplay; });
}
export function updatePlayable(value: StudioPackage, update: (draft: StudioPackage) => void): StudioPackage { return updatePackage(value, update); }
