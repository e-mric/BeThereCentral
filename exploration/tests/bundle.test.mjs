import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFile, mkdtemp, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { requiredFiles, verifyBundle } from '../verify.mjs';
test('redistributed sample is the reviewed 650000-splat derivative',async()=>{
    const data = await readFile(new URL('../public/engine-room.compressed.ply',import.meta.url));
    assert.equal(createHash('sha256').update(data).digest('hex'),'def08d55dd968491f8940398379c0592490c239bfaa144db8d679d30dc3d97f1');
    assert.match(data.subarray(0,2048).toString(),/element vertex 650000/);
});
test('packaging rejects missing renderer, scene or licence',async()=>{
    const dir = await mkdtemp(join(tmpdir(),'bethere-bundle-'));
    try {
        for (const missing of ['viewer.js','scene-data.js','SCENE-LICENSE.txt']) {
            for (const name of requiredFiles) {
                if(name === missing) await rm(join(dir,name),{force:true});
                else await writeFile(join(dir,name),'nonempty');
            }
            await assert.rejects(verifyBundle(dir),new RegExp(missing.replaceAll('.','\\.')));
        }
    } finally { await rm(dir,{recursive:true,force:true}); }
});
test('built deliverable includes all local resources and attribution',async()=>{
    await verifyBundle(new URL('../dist/',import.meta.url).pathname);
});
