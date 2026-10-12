import { parsePackage, serializePackage, validatePackage, MAX_PACKAGE_BYTES } from '../../../content/community/v1/index.js';
import sample from './sample-package.json' with { type: 'json' };

export type FurnitureKind = 'desk' | 'plant' | 'sofa' | 'rug';
export type Furniture = { id: string; kind: FurnitureKind; x: number; y: number; width: number; height: number };
export type RoomPackage = {
  packageVersion: 1;
  packageId: 'betherecentral.sample-room';
  contentRevision: number;
  features: ['rooms-v1'];
  assets: Array<{ id: string; type: string; path: string; mimeType: string; license: string; attribution: string }>;
  attribution: Array<{ id: string; text: string }>;
  room: { schemaVersion: 1; companyName: string; room: { id: string; width: number; height: number; polygon: number[][] }; objects: Furniture[] };
};
export const STARTER = validatePackage(sample) as RoomPackage;
export { MAX_PACKAGE_BYTES, parsePackage, serializePackage };
export const ROOM_WIDTH = 320;
export const ROOM_HEIGHT = 224;
export const KINDS: FurnitureKind[] = ['desk', 'plant', 'sofa', 'rug'];
export const LABELS: Record<FurnitureKind, string> = { desk: 'Desk', plant: 'Plant', sofa: 'Sofa', rug: 'Rug' };
export const STORAGE_KEY = 'betherecentral.community-studio.v1';

export function clonePackage(value: RoomPackage): RoomPackage { return structuredClone(value); }
export function valid(value: unknown): RoomPackage { return validatePackage(value) as RoomPackage; }
export function withRoom(value: RoomPackage, apply: (next: RoomPackage) => void): RoomPackage | null {
  const next = clonePackage(value);
  apply(next);
  try { return valid(next); } catch { return null; }
}
export function placeFurniture(value: RoomPackage, kind: FurnitureKind): RoomPackage | null {
  if (!KINDS.includes(kind) || value.room.objects.length >= 24) return null;
  const sizes: Record<FurnitureKind, [number, number]> = { desk: [72, 38], plant: [26, 30], sofa: [91, 43], rug: [78, 48] };
  const [width, height] = sizes[kind];
  const used = new Set(value.room.objects.map(item => item.id));
  let n = 1;
  while (used.has(`${kind}-${n}`)) n++;
  const id = `${kind}-${n}`;
  for (const [x, y] of [[128, 78], [80, 78], [190, 78], [128, 145], [76, 145], [195, 145], [128, 35], [32, 85], [250, 90]]) {
    const next = withRoom(value, p => p.room.objects.push({ id, kind, x, y, width, height }));
    if (next) return next;
  }
  return null;
}
export function moveFurniture(value: RoomPackage, id: string, x: number, y: number): RoomPackage | null {
  if (!value.room.objects.some(object => object.id === id)) return null;
  return withRoom(value, p => { const item = p.room.objects.find(object => object.id === id)!; item.x = Math.round(x); item.y = Math.round(y); });
}
export function removeFurniture(value: RoomPackage, id: string): RoomPackage | null {
  if (!value.room.objects.some(item => item.id === id)) return null;
  return withRoom(value, p => { p.room.objects = p.room.objects.filter(item => item.id !== id); });
}

/** Fit an atlas sprite inside its collision/placement box without changing its aspect ratio. */
export function fitSprite(item: Pick<Furniture, 'x' | 'y' | 'width' | 'height'>, sourceWidth: number, sourceHeight: number) {
  if (![item.x, item.y, item.width, item.height, sourceWidth, sourceHeight].every(Number.isFinite) || item.width <= 0 || item.height <= 0 || sourceWidth <= 0 || sourceHeight <= 0) return null;
  const scale = Math.min(item.width / sourceWidth, item.height / sourceHeight);
  const width = sourceWidth * scale;
  const height = sourceHeight * scale;
  return { x: item.x + (item.width - width) / 2, y: item.y + (item.height - height) / 2, width, height };
}
