import { validateScene } from '../../../room-studio/src/domain.js';

export const MAX_PACKAGE_BYTES = 65_536;
const ROOT_KEYS = ['packageVersion', 'packageId', 'contentRevision', 'features', 'assets', 'attribution', 'room'];
const ASSET_KEYS = ['id', 'type', 'path', 'mimeType', 'license', 'attribution'];
const ATTRIBUTION_KEYS = ['id', 'text'];
const ASSET_CATALOG = [{
  id: 'campus-props-atlas', type: 'image-atlas',
  path: 'composeApp/src/commonMain/composeResources/drawable/campus_props.png',
  mimeType: 'image/png', license: 'MIT',
  attribution: 'BeThereCentral project; original generated pixel-art atlas. See docs/design/generation-prompts.md and docs/assets/PIXEL_ART.md.'
}];
const ATTRIBUTION = [{
  id: 'betherecentral-sample-room',
  text: 'Fictional BeThereCentral Room Studio sample. Room layout and company label are illustrative and are not mapped to a real tenant room.'
}];
const isRecord = value => value !== null && typeof value === 'object' && !Array.isArray(value);
const exactKeys = (value, keys) => isRecord(value) && Object.keys(value).length === keys.length && keys.every(key => Object.hasOwn(value, key));
const sameJSON = (a, b) => JSON.stringify(a) === JSON.stringify(b);
const sameFields = (value, expected, keys) => exactKeys(value, keys) && keys.every(key => value[key] === expected[key]);

export function validatePackage(value) {
  if (!exactKeys(value, ROOT_KEYS)) throw new Error('Package must contain exactly the documented v1 fields.');
  if (value.packageVersion !== 1 || value.packageId !== 'betherecentral.sample-room' || !Number.isSafeInteger(value.contentRevision) || value.contentRevision < 1) throw new Error('Unsupported package version, ID, or content revision.');
  if (!Array.isArray(value.features) || !sameJSON(value.features, ['rooms-v1'])) throw new Error('Unsupported or missing required package feature.');
  if (!Array.isArray(value.assets) || value.assets.length !== ASSET_CATALOG.length || !value.assets.every((asset, i) => sameFields(asset, ASSET_CATALOG[i], ASSET_KEYS))) throw new Error('Asset catalog must match the trusted bundled asset catalog.');
  if (!Array.isArray(value.attribution) || value.attribution.length !== ATTRIBUTION.length || !value.attribution.every((entry, i) => sameFields(entry, ATTRIBUTION[i], ATTRIBUTION_KEYS))) throw new Error('Package attribution is missing or changed.');
  validateScene(value.room);
  return structuredClone(value);
}

export function parsePackage(source) {
  if (typeof source !== 'string' || new TextEncoder().encode(source).byteLength > MAX_PACKAGE_BYTES) throw new Error(`Package must be at most ${MAX_PACKAGE_BYTES} UTF-8 bytes.`);
  let value;
  try { value = JSON.parse(source); } catch { throw new Error('Package must be valid JSON.'); }
  return validatePackage(value);
}

export function serializePackage(value) {
  return JSON.stringify(validatePackage(value), null, 2);
}
