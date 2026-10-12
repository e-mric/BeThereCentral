import { createHash } from 'node:crypto';
import { copyFile, mkdir, readFile, rm } from 'node:fs/promises';
import { dirname, join, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

const studioRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const repositoryRoot = resolve(studioRoot, '..');
const sourceRoot = resolve(repositoryRoot, 'content/community/assets');
const outputRoot = resolve(studioRoot, 'static/assets/community');
const catalogPath = join(sourceRoot, 'catalog-v2.json');
const catalog = JSON.parse(await readFile(catalogPath, 'utf8'));
const avatarPath = join(sourceRoot, 'avatar-parts-v1.json');
const avatarArt = JSON.parse(await readFile(avatarPath, 'utf8'));
if (avatarArt.version !== 1 || avatarArt.width !== 64 || avatarArt.height !== 96 || avatarArt.pixelScale !== 2 || avatarArt.parts?.length < 1) {
  throw new Error('The trusted avatar-parts-v1.json definition is missing its required 64×96 v1 frame contract.');
}

function safeAssetPath(relative) {
  if (typeof relative !== 'string' || relative.length === 0 || relative.startsWith('/') || relative.includes('\\')) {
    throw new Error(`Invalid catalog image path: ${relative}`);
  }
  const resolved = resolve(sourceRoot, relative);
  if (!resolved.startsWith(`${sourceRoot}${sep}`)) throw new Error(`Catalog image path escapes its source directory: ${relative}`);
  return resolved;
}

const images = new Map();
for (const prop of catalog.props) {
  const source = safeAssetPath(prop.image);
  if (images.has(prop.image)) continue;
  const bytes = await readFile(source);
  const digest = createHash('sha256').update(bytes).digest('hex');
  if (digest !== prop.sha256) throw new Error(`Asset hash mismatch for ${prop.image}`);
  images.set(prop.image, { source, digest });
}
for (const preset of Object.values(catalog.avatarPresets ?? {})) {
  const source = safeAssetPath(preset.image);
  const bytes = await readFile(source);
  const digest = createHash('sha256').update(bytes).digest('hex');
  if (digest !== preset.sha256) throw new Error(`Asset hash mismatch for avatar preset ${preset.image}`);
  images.set(preset.image, { source, digest });
}

await rm(outputRoot, { recursive: true, force: true });
await mkdir(outputRoot, { recursive: true });
for (const [relative, { source }] of images) {
  const target = resolve(outputRoot, relative);
  await mkdir(dirname(target), { recursive: true });
  await copyFile(source, target);
}
await copyFile(catalogPath, join(outputRoot, 'catalog-v2.json'));
await copyFile(avatarPath, join(outputRoot, 'avatar-parts-v1.json'));
console.log(`Synced ${images.size} checked prop/avatar images, avatar art v${avatarArt.version}, and catalog revision ${catalog.catalogRevision}.`);
