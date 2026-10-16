import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parsePackage } from '../index.js';

const here = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(here, '..');
const acceptance = JSON.parse(await readFile(path.join(root, 'acceptance.json'), 'utf8'));
const report = { valid: [], invalid: [] };

for (const name of acceptance.valid) {
  const source = await readFile(path.join(root, name), 'utf8');
  report.valid.push({ name, package: parsePackage(source) });
}
for (const name of acceptance.invalid) {
  const source = await readFile(path.join(root, 'fixtures/invalid', name), 'utf8');
  try {
    parsePackage(source);
    throw new Error(`Fixture unexpectedly validated: ${name}`);
  } catch (error) {
    if (error.message.startsWith('Fixture unexpectedly validated:')) throw error;
    report.invalid.push(name);
  }
}

process.stdout.write(`BTC_ACCEPTANCE_REPORT:${JSON.stringify(report)}\n`);
