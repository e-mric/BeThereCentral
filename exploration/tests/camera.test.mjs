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
test('lens zoom is reversible and invalid factors cannot corrupt the camera',()=>{
    const initial = overview();
    const reversed = zoom(zoom(initial,.8),1.25);
    assert.ok(Math.abs(reversed.fov-initial.fov)<1e-10);
    assert.ok(Math.abs(reversed.distance-initial.distance)<1e-10);
    assert.deepEqual(position(zoom(initial,.8)),position(initial));
    assert.deepEqual(zoom(initial,0),initial);
    assert.deepEqual(zoom(initial,-1),initial);
    assert.deepEqual(zoom(initial,Number.NaN),initial);
    assert.deepEqual(zoom(initial,Number.POSITIVE_INFINITY),initial);
});
test('wide zoom moves the camera outward after the FOV cap and reverses across the threshold',()=>{
    const initial = overview();
    const nearCap = zoom(initial,1.25);
    const farther = zoom(nearCap,1.25);
    const farthest = zoom(farther,1.25);
    assert.equal(nearCap.fov,85);
    assert.ok(nearCap.distance>initial.distance);
    assert.ok(farther.distance>nearCap.distance);
    assert.ok(farthest.distance>farther.distance);
    assert.notDeepEqual(position(nearCap),position(initial));
    for (const state of [nearCap,farther,farthest]) {
        const reversed = zoom(state,.8);
        const roundTrip = zoom(reversed,1.25);
        assert.ok(Math.abs(roundTrip.distance-state.distance)<1e-8);
        assert.ok(Math.abs(roundTrip.fov-state.fov)<1e-8);
    }
});
test('zoom-out and zoom-in stay within camera and lens bounds',()=>{
    let far = overview();
    let near = overview();
    for (let i=0;i<100;i++) { far=zoom(far,1.25); near=zoom(near,.8); }
    assert.equal(far.fov,85); assert.equal(far.distance,35);
    assert.equal(near.fov,35); assert.equal(near.distance,2.5);
});
test('camera position respects target and viewing distance',()=>{
    assert.deepEqual(position(home),[1,2,13]);
    const point=position({...home,yaw:90}); assert.ok(Math.abs(point[0]-11)<1e-8);
});
test('overview returns an independent canonical reset pose',()=>{
    const initial = overview();
    const changed = orbit(zoom(zoom(initial,1.25),1.25),40,20);
    changed.target[0] = 3;
    assert.deepEqual(overview(),{target:[0,.3,4],distance:2.5,yaw:180,pitch:15,fov:75});
    assert.notDeepEqual(changed,overview());
});
