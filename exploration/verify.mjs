import { stat, readFile } from 'node:fs/promises';
import { join } from 'node:path';
export const requiredFiles = ['index.html','viewer.js','scene-data.js','style.css','SCENE-LICENSE.txt','PLAYCANVAS-LICENSE.txt'];
export async function verifyBundle(directory) {
    for (const name of requiredFiles) {
        const file = join(directory,name);
        const info = await stat(file).catch(() => { throw new Error(`Missing viewer resource: ${name}`); });
        if (!info.isFile() || info.size === 0) throw new Error(`Empty viewer resource: ${name}`);
    }
    const html = await readFile(join(directory,'index.html'),'utf8');
    for (const resource of ['viewer.js','scene-data.js','style.css']) {
        if (!html.includes(`"${resource}"`)) throw new Error(`Unlinked resource: ${resource}`);
    }
}
