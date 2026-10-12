export type AvatarDirection = 'front' | 'back' | 'left' | 'right';
export type AvatarPose = 'idle' | 'walk-a' | 'walk-b' | 'walk-c' | 'walk-d' | 'walk-e' | 'walk-f' | 'walk-g' | 'walk-h';
export type AvatarAtlasWalkPose = 'walk1' | 'walk2' | 'walk3' | 'walk4' | 'walk5' | 'walk6' | 'walk7' | 'walk8';
type Rgba = [number, number, number, number];
type Run = [number, number, number, string];
type AvatarFrame = { direction: AvatarDirection; pose: AvatarPose; offset: [number, number]; runs: Run[] };
type AvatarPart = { id: string; select: Record<string, string>; frames: AvatarFrame[] };
export type AvatarDefinition = {
  version: number; width: number; height: number; worldFrameSize: [number, number]; pixelScale: number; pivot: [number, number]; frameDurationMs: number;
  draw: { runFormat: string; order: string; coordinates?: string };
  palettes: Record<string, Record<string, Rgba>>;
  appearanceRamps: Record<string, Record<string, string>>;
  parts: AvatarPart[];
};
export type AvatarManualAppearance = { bodyPalette: string; hair: string; hairPalette: string; top: string; topPalette: string; trousersPalette: string; accessory: string };
export type AvatarAppearance = AvatarManualAppearance | { presetId: string };
export type AvatarPreset = {
  label: string; image: string; sha256: string; frameSize: [number, number]; worldSize: [number, number];
  footPivot: [number, number]; frameDurationMs: number; sourceScale: number;
  realPerson?: boolean;
  frames: Record<AvatarDirection, { idle: { crop: [number, number, number, number]; sourcePivot: [number, number] } } & Partial<Record<AvatarAtlasWalkPose, { crop: [number, number, number, number]; sourcePivot: [number, number] }>>>;
  attribution: string;
};
export type AvatarWorldRect = { x: number; y: number; width: number; height: number };

/** Places a 64×96 source frame by its normalized pivot, independent of atlas sampling scale. */
export function avatarWorldRect(feetX: number, feetY: number, frameSize: [number, number], worldSize: [number, number], pivot: [number, number]): AvatarWorldRect {
  const scaleX = worldSize[0] / frameSize[0], scaleY = worldSize[1] / frameSize[1];
  return { x: feetX - pivot[0] * scaleX, y: feetY - pivot[1] * scaleY, width: worldSize[0], height: worldSize[1] };
}

/** Centers a compact nameplate above the visible sprite box, leaving its face unobstructed. */
export function avatarNameplateAnchor(rect: AvatarWorldRect, gap = 4): { x: number; y: number } {
  return { x: rect.x + rect.width / 2, y: rect.y - gap };
}

const MANUAL_WALK_POSES: AvatarPose[] = ['walk-a', 'walk-b', 'walk-c', 'walk-d'];
const ATLAS_WALK_POSES: AvatarAtlasWalkPose[] = ['walk1', 'walk2', 'walk3', 'walk4', 'walk5', 'walk6', 'walk7', 'walk8'];

/** Returns only walk frames present in the selected direction; manual art retains its four shared poses. */
export function avatarWalkSequence(preset?: AvatarPreset | null, direction: AvatarDirection = 'front'): AvatarPose[] {
  if (!preset) return [...MANUAL_WALK_POSES];
  const frames = preset.frames[direction];
  const poseMap: AvatarPose[] = ['walk-a', 'walk-b', 'walk-c', 'walk-d', 'walk-e', 'walk-f', 'walk-g', 'walk-h'];
  const sequence = ATLAS_WALK_POSES.flatMap((pose, index) => frames[pose] ? [poseMap[index]] : []);
  return sequence.length ? sequence : [...MANUAL_WALK_POSES];
}

/** Preserves the front-view cycle duration when a direction has fewer authored frames. */
export function avatarWalkFrameDuration(preset?: AvatarPreset | null, direction: AvatarDirection = 'front', manualDurationMs = 160): number {
  if (!preset) return manualDurationMs;
  const referenceCount = avatarWalkSequence(preset, 'front').length;
  const directionCount = avatarWalkSequence(preset, direction).length;
  return Math.max(1, preset.frameDurationMs * referenceCount / directionCount);
}

export function advanceAvatarWalkFrame(index: number, sequence: readonly AvatarPose[]): number {
  return sequence.length ? ((index + 1) % sequence.length) : 0;
}

/** Advances on elapsed time without losing a partial frame to timer-poll jitter. */
export function advanceAvatarWalkClock(index: number, elapsedMs: number, frameDurationMs: number, frameCount: number): { index: number; elapsedMs: number } {
  if (frameCount < 1) return { index: 0, elapsedMs: 0 };
  if (!Number.isFinite(frameDurationMs) || frameDurationMs <= 0) return { index: index % frameCount, elapsedMs: 0 };
  const elapsed = Math.max(0, Number.isFinite(elapsedMs) ? elapsedMs : 0);
  const steps = Math.floor(elapsed / frameDurationMs);
  return { index: ((index + steps) % frameCount + frameCount) % frameCount, elapsedMs: elapsed - steps * frameDurationMs };
}

const CACHE_LIMIT = 128;
const cache = new Map<string, Uint8Array>();
const rampGroupByEnum: Record<string, string> = {
  bodyPalette: 'skin', hairPalette: 'hair', topPalette: 'top', trousersPalette: 'trousers'
};

function cacheKey(definition: AvatarDefinition, appearance: AvatarManualAppearance, direction: AvatarDirection, pose: AvatarPose) {
  const values = appearance as unknown as Record<string, string>;
  const completeAppearance = Object.keys(values).sort().map(key => `${key}=${values[key]}`).join('&');
  return `manual|${definition.version}|${completeAppearance}|${direction}|${pose}`;
}

function colorFor(definition: AvatarDefinition, appearance: AvatarManualAppearance, key: string): Rgba {
  if (key.startsWith('@')) {
    const match = /^@([A-Za-z][A-Za-z0-9]*)\.([A-Za-z][A-Za-z0-9]*)$/.exec(key);
    if (!match) throw new Error(`Invalid avatar palette reference: ${key}`);
    const [, enumKey, swatch] = match;
    const group = rampGroupByEnum[enumKey];
    const selected = (appearance as unknown as Record<string, string>)[enumKey];
    const paletteName = group && selected ? definition.appearanceRamps[group]?.[selected] : undefined;
    const color = paletteName ? definition.palettes[paletteName]?.[swatch] : undefined;
    if (!color) throw new Error(`Unknown avatar palette reference: ${key}`);
    return color;
  }
  const fixedKey = key.startsWith('fixed:') ? key.slice('fixed:'.length) : key;
  const fixed = definition.palettes.fixed?.[fixedKey];
  if (!fixed) throw new Error(`Unknown avatar color: ${key}`);
  return fixed;
}

function matches(selectors: Record<string, string>, appearance: AvatarManualAppearance): boolean {
  const values = appearance as unknown as Record<string, string>;
  return Object.entries(selectors).every(([key, value]) => value === '*' || values[key] === value);
}

/** Rasterizes the shared, trusted run definition to the exact native RGBA byte grid. */
export function renderAvatarRgba(
  definition: AvatarDefinition,
  appearance: AvatarManualAppearance,
  direction: AvatarDirection,
  pose: AvatarPose
): Uint8ClampedArray {
  const key = cacheKey(definition, appearance, direction, pose);
  const cached = cache.get(key);
  if (cached) { cache.delete(key); cache.set(key, cached); return new Uint8ClampedArray(cached); }
  const rgba = new Uint8ClampedArray(definition.width * definition.height * 4);
  for (const part of definition.parts) {
    if (!matches(part.select, appearance)) continue;
    const frame = part.frames.find(item => item.direction === direction && item.pose === pose);
    if (!frame) continue;
    const [dx, dy] = frame.offset;
    for (const [x, y, length, colorKey] of frame.runs) {
      const px = x + dx, py = y + dy;
      if (!Number.isInteger(px) || !Number.isInteger(py) || !Number.isInteger(length) || length < 1 || px < 0 || py < 0 || px + length > definition.width || py >= definition.height) {
        throw new Error(`Avatar run is outside the ${definition.width}×${definition.height} frame in part ${part.id}.`);
      }
      const color = colorFor(definition, appearance, colorKey);
      let offset = (py * definition.width + px) * 4;
      for (let i = 0; i < length; i += 1, offset += 4) rgba.set(color, offset);
    }
  }
  cache.set(key, new Uint8Array(rgba));
  if (cache.size > CACHE_LIMIT) cache.delete(cache.keys().next().value!);
  return rgba;
}

const presetPose: Record<AvatarPose, 'idle' | AvatarAtlasWalkPose> = {
  idle: 'idle', 'walk-a': 'walk1', 'walk-b': 'walk2', 'walk-c': 'walk3', 'walk-d': 'walk4',
  'walk-e': 'walk5', 'walk-f': 'walk6', 'walk-g': 'walk7', 'walk-h': 'walk8'
};

/** Samples one curated crop from the trusted atlas into the same native RGBA grid as manual art. */
export function renderAvatarPresetRgba(
  presetId: string, preset: AvatarPreset, atlas: Uint8Array, atlasWidth: number, atlasHeight: number,
  direction: AvatarDirection, pose: AvatarPose
): Uint8Array {
  const key = `preset|${presetId}|${preset.sha256}|${direction}|${pose}`;
  const cached = cache.get(key);
  if (cached) { cache.delete(key); cache.set(key, cached); return cached; }
  const [width, height] = preset.frameSize;
  if (atlas.length !== atlasWidth * atlasHeight * 4) throw new Error('Trusted avatar atlas pixels have an unexpected size.');
  const frame = preset.frames[direction][presetPose[pose]];
  if (!frame) throw new Error(`Avatar preset ${presetId} does not include ${presetPose[pose]} for ${direction}.`);
  const [cropX, cropY, cropWidth, cropHeight] = frame.crop;
  const [pivotX, pivotY] = frame.sourcePivot;
  const output = new Uint8Array(width * height * 4);
  for (let y = 0; y < height; y += 1) {
    const sourceY = Math.floor((y - preset.footPivot[1]) / preset.sourceScale + pivotY);
    if (sourceY < 0 || sourceY >= cropHeight) continue;
    for (let x = 0; x < width; x += 1) {
      const sourceX = Math.floor((x - preset.footPivot[0]) / preset.sourceScale + pivotX);
      if (sourceX < 0 || sourceX >= cropWidth) continue;
      const sourceOffset = ((cropY + sourceY) * atlasWidth + cropX + sourceX) * 4;
      const targetOffset = (y * width + x) * 4;
      output[targetOffset] = atlas[sourceOffset];
      output[targetOffset + 1] = atlas[sourceOffset + 1];
      output[targetOffset + 2] = atlas[sourceOffset + 2];
      output[targetOffset + 3] = atlas[sourceOffset + 3];
    }
  }
  cache.set(key, output);
  if (cache.size > CACHE_LIMIT) cache.delete(cache.keys().next().value!);
  return output;
}

export function avatarRasterCacheSize(): number { return cache.size; }
export function clearAvatarRasterCache(): void { cache.clear(); }
