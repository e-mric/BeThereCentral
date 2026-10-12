#!/usr/bin/env node
// Copy the trusted community catalog and its pinned images into the Godot project.
import { createHash } from 'node:crypto';
import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const toolsDir = path.dirname(fileURLToPath(import.meta.url));
const gameDir = path.resolve(toolsDir, '..');
const repository = path.resolve(gameDir, '..');
const sourceDir = path.join(repository, 'content/community/assets');
const targetDir = path.join(gameDir, 'assets/community');
const manifestPath = path.join(sourceDir, 'catalog-v2.json');
const avatarPartsPath = path.join(sourceDir, 'avatar-parts-v1.json');
const catalog = JSON.parse(await readFile(manifestPath, 'utf8'));

if (catalog.catalogId !== 'betherecentral-community' || catalog.catalogRevision !== 2) {
  throw new Error('Unexpected community asset catalog identity.');
}
await mkdir(targetDir, { recursive: true });
await copyFile(manifestPath, path.join(targetDir, 'catalog-v2.json'));

const avatarPartsData = await readFile(avatarPartsPath);
const avatarPartsHash = createHash('sha256').update(avatarPartsData).digest('hex');
if (avatarPartsHash !== '7522eb485878f943b8ef9ee15f0a3e61cb11e1634613a952b4363552e332d093') {
  throw new Error('Trusted avatar-parts-v1 definition hash mismatch.');
}
await writeFile(path.join(targetDir, 'avatar-parts-v1.json'), avatarPartsData);

for (const prop of catalog.props) {
  const relative = prop.image;
  if (path.isAbsolute(relative) || relative.split(/[\\/]/).includes('..')) {
    throw new Error(`Unsafe trusted catalog image path: ${relative}`);
  }
  const source = path.resolve(sourceDir, relative);
  if (!source.startsWith(`${path.resolve(sourceDir)}${path.sep}`)) {
    throw new Error(`Catalog image escapes the trusted source directory: ${relative}`);
  }
  const data = await readFile(source);
  const digest = createHash('sha256').update(data).digest('hex');
  if (digest !== prop.sha256) throw new Error(`Catalog image hash mismatch: ${relative}`);
  const target = path.join(targetDir, relative);
  await mkdir(path.dirname(target), { recursive: true });
  await writeFile(target, data);
}
for (const [presetId, preset] of Object.entries(catalog.avatarPresets ?? {})) {
  if (!['founder-v1', 'patch-v1', 'ninon-v1', 'morgane-v1', 'architect-v1'].includes(presetId) || !preset.image || path.isAbsolute(preset.image) || preset.image.split(/[\\/]/).includes('..')) {
    throw new Error(`Unexpected trusted avatar preset: ${presetId}`);
  }
  const source = path.resolve(sourceDir, preset.image);
  if (!source.startsWith(`${path.resolve(sourceDir)}${path.sep}`)) {
    throw new Error(`Avatar preset image escapes the trusted source directory: ${preset.image}`);
  }
  const data = await readFile(source);
  const digest = createHash('sha256').update(data).digest('hex');
  if (digest !== preset.sha256) throw new Error(`Avatar preset image hash mismatch: ${preset.image}`);
  const target = path.join(targetDir, preset.image);
  await mkdir(path.dirname(target), { recursive: true });
  await writeFile(target, data);
}
console.log(`Synced and verified ${catalog.props.length} props and ${Object.keys(catalog.avatarPresets ?? {}).length} avatar presets into ${targetDir}`);
