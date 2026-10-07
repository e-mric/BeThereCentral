import { test } from 'node:test';
import assert from 'node:assert/strict';
import { orbit, overview, position, zoom } from '../src/camera.mjs';
const home = {target:[1,2,3],yaw:0,pitch:0,distance:10,fov:75};
test('orbit changes perspective without moving the independent scene target',()=>{
    const next = orbit(home,100,2000);
    assert.equal(next.pitch,80); assert.equal(next.yaw,-25);
    assert.deepEqual(next.target,home.target); assert.notDeepEqual(position(next),position(home));
    assert.equal(home.yaw,0);
});
test('lens zoom is reversible within bounds without moving through the capture',()=>{
    assert.equal(zoom(zoom(home,.8),1.25).fov,75);
    assert.equal(zoom(home,0).fov,35); assert.equal(zoom(home,100).fov,85);
    assert.deepEqual(position(zoom(home,.8)),position(home));
});
test('camera position respects target and viewing distance',()=>{
    assert.deepEqual(position(home),[1,2,13]);
    const point=position({...home,yaw:90}); assert.ok(Math.abs(point[0]-11)<1e-8);
});
test('overview returns an independent canonical reset pose',()=>{
    const initial = overview();
    const changed = orbit(zoom(initial,.8),40,20);
    changed.target[0] = 3;
    assert.deepEqual(overview(),{target:[0,.3,4],distance:2.5,yaw:180,pitch:15,fov:75});
    assert.notDeepEqual(changed,overview());
});
