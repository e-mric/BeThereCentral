import { AppBase, AppOptions, Asset, CameraComponentSystem, Color, Entity,
    FILLMODE_FILL_WINDOW, GSplatComponentSystem, GSplatHandler,
    RESOLUTION_AUTO, TextureHandler, createGraphicsDevice, GSPLAT_RENDERER_RASTER_CPU_SORT } from 'playcanvas';
import { orbit, overview, position, zoom } from './camera.mjs';
import { disposeFailedApp } from './lifecycle.mjs';

const $ = id => document.getElementById(id);
const canvas = $('scene');
if (location.protocol === 'file:' || location.hostname === 'appassets.androidplatform.net') {
    document.documentElement.classList.add('embedded-app');
}
const controls = ['reset', 'zoom-in', 'zoom-out', 'tour'];
let app, camera, asset, state, ready = false, disposed = false, failed = false, automatic = false;
let phase = 'startup';
let firstFrameMs = null, frames = 0, recentFrames = 0, lastMeasure = performance.now(), fps = 0;
const started = performance.now();
const pointers = new Map();
let pinch;
const statusDetail = $('status-detail');
function stage(next) {
    phase = next;
    console.info('BETHERE_EXPLORE', JSON.stringify({event:'stage', phase}));
}

function fail(message, diagnostic = message) {
    if (disposed || failed) return;
    failed = true;
    ready = false;
    clearTimeout(timeout);
    stopTour();
    pointers.clear(); pinch = null;
    controls.forEach(id => $(id).disabled = true);
    $('status').hidden = false;
    $('status').querySelector('.spinner').hidden = true;
    $('status').querySelector('h1').textContent = 'This scene couldn’t open';
    statusDetail.textContent = `${message} If you opened this from BeThereCentral, return to its Map tab to continue.`;
    $('status').dataset.state = 'error';
    document.body.dataset.state = 'error';
    $('retry').hidden = false;
    canvas.dataset.state = 'error';
    if (app) {
        const failedApp = app;
        app = null;
        disposeFailedApp(failedApp);
    }
    console.error('BETHERE_EXPLORE', JSON.stringify({event:'error', phase, message:sanitize(diagnostic)}));
}
function sanitize(message) {
    return String(message).replace(/[\u0000-\u001f\u007f]/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 160);
}
const timeout = setTimeout(() => fail('The viewer took too long to load.'), 45000);
$('retry').onclick = () => location.reload();
$('about').onclick = () => {
    stopTour();
    $('metrics').textContent = firstFrameMs == null ? 'Scene not rendered yet.' : `First scene frame: ${(firstFrameMs / 1000).toFixed(2)} s · Recent rendering: ${fps} fps · 10.58 MB scene. Browser/simulator results do not predict your phone’s performance.`;
    $('details').showModal();
};
$('close-about').onclick = () => $('details').close();
function stopTour() { automatic = false; $('tour').setAttribute('aria-pressed', 'false'); $('tour').textContent = 'Look around'; }
function updateCamera() {
    if (!camera || !state) return;
    camera.setPosition(...position(state));
    camera.lookAt(...state.target);
    camera.camera.fov = state.fov;
    if (app) app.renderNextFrame = true;
}
function reset() { if (!ready) return; stopTour(); state = overview(); updateCamera(); }
$('reset').onclick = reset;
$('zoom-in').onclick = () => { stopTour(); state = zoom(state, 0.8); updateCamera(); };
$('zoom-out').onclick = () => { stopTour(); state = zoom(state, 1.25); updateCamera(); };
$('tour').onclick = () => { automatic = !automatic; $('tour').setAttribute('aria-pressed', String(automatic)); $('tour').textContent = automatic ? 'Pause' : 'Look around'; };
function pan(dx, dy) {
    const scale = state.distance * 0.0015;
    const right = camera.right, up = camera.up;
    state.target = state.target.map((v, i) => v - [right.x,right.y,right.z][i] * dx * scale + [up.x,up.y,up.z][i] * dy * scale);
}
canvas.addEventListener('contextmenu', e => e.preventDefault());
canvas.addEventListener('pointerdown', e => {
    if (!ready) return;
    stopTour(); canvas.focus(); canvas.setPointerCapture(e.pointerId);
    pointers.set(e.pointerId, {x:e.clientX,y:e.clientY}); pinch = null;
});
canvas.addEventListener('pointermove', e => {
    if (!ready || !pointers.has(e.pointerId)) return;
    const previous = pointers.get(e.pointerId), dx = e.clientX - previous.x, dy = e.clientY - previous.y;
    pointers.set(e.pointerId, {x:e.clientX,y:e.clientY});
    if (pointers.size === 2) {
        const [a,b] = [...pointers.values()];
        const distance = Math.hypot(a.x-b.x,a.y-b.y);
        if (pinch && distance > 0) state = zoom(state,pinch/distance);
        pinch = distance; pan(dx/2,dy/2);
    } else if (e.buttons === 2 || e.shiftKey) pan(dx,dy);
    else state = orbit(state,dx,dy);
    updateCamera();
});
for (const event of ['pointerup','pointercancel','lostpointercapture']) canvas.addEventListener(event,e => { pointers.delete(e.pointerId); pinch = null; });
canvas.addEventListener('wheel', e => { if (!ready) return; e.preventDefault(); stopTour(); state = zoom(state,Math.exp(Math.max(-200,Math.min(200,e.deltaY))*0.003)); updateCamera(); },{passive:false});
canvas.addEventListener('keydown', e => {
    if (!ready) return;
    const actions = {ArrowLeft:()=>state=orbit(state,-35,0),ArrowRight:()=>state=orbit(state,35,0),ArrowUp:()=>state=orbit(state,0,-35),ArrowDown:()=>state=orbit(state,0,35),'+':()=>state=zoom(state,.8),'-':()=>state=zoom(state,1.25),'r':reset,'R':reset};
    if (actions[e.key]) { e.preventDefault(); stopTour(); actions[e.key](); updateCamera(); }
});
canvas.addEventListener('webglcontextlost', e => { e.preventDefault(); fail('Graphics memory became unavailable.'); });
document.addEventListener('visibilitychange', () => {
    stopTour(); pointers.clear(); pinch = null;
    if (app) app.autoRender = !document.hidden;
});
window.addEventListener('pagehide', () => {
    disposed = true; clearTimeout(timeout); stopTour();
    if (app) { app.destroy(); app = null; }
});
window.addEventListener('resize', () => { if (app) { app.resizeCanvas(); app.renderNextFrame = true; } });

async function start() {
    stage('startup');
    if (!window.BETHERE_SCENE_BASE64) throw new Error('The bundled sample is missing.');
    stage('graphics-device');
    const device = await createGraphicsDevice(canvas,{deviceTypes:['webgl2'],antialias:false,alpha:true,powerPreference:'high-performance'});
    if (disposed || failed) { device.destroy(); return; }
    device.maxPixelRatio = Math.min(devicePixelRatio, 1.5);
    stage('app-init');
    const options = new AppOptions(); options.graphicsDevice = device;
    options.componentSystems = [CameraComponentSystem,GSplatComponentSystem];
    options.resourceHandlers = [TextureHandler,GSplatHandler];
    app = new AppBase(canvas); app.init(options);
    app.setCanvasFillMode(FILLMODE_FILL_WINDOW); app.setCanvasResolution(RESOLUTION_AUTO);
    app.scene.gsplat.renderer = GSPLAT_RENDERER_RASTER_CPU_SORT;
    camera = new Entity('Sample camera');
    camera.addComponent('camera',{clearColor:new Color(12/255,17/255,20/255),fov:75,nearClip:.03,farClip:300});
    app.root.addChild(camera);
    // GPU readbacks used while decoding splat data need the device's frame loop running.
    app.start();
    statusDetail.textContent = 'Loading 650,000 Gaussian splats…';
    stage('scene-load');
    const binary = atob(window.BETHERE_SCENE_BASE64); delete window.BETHERE_SCENE_BASE64;
    const bytes = Uint8Array.from(binary,c=>c.charCodeAt(0));
    asset = new Asset('Sample engine room','gsplat',{url:'engine-room.compressed.ply',contents:new Response(bytes)});
    app.assets.add(asset);
    await new Promise((resolve,reject) => { asset.once('load',resolve); asset.once('error',reject); app.assets.load(asset); });
    if (disposed || failed || !app) return;
    const entity = new Entity('Independent sample scene');
    entity.addComponent('gsplat',{asset}); entity.setEulerAngles(180,0,0); app.root.addChild(entity);
    // Photographic sample coordinates: deliberately unrelated to the sample building.
    state = overview(); updateCamera();
    statusDetail.textContent = 'Drawing your first view…';
    app.on('update',dt => { if (ready && automatic && !document.hidden) { state.yaw += Math.min(dt,.05)*7; updateCamera(); } });
    stage('first-frame');
    app.on('postrender',() => {
        frames++; recentFrames++;
        const now = performance.now();
        if (now-lastMeasure>1000) { fps = Math.round(recentFrames*1000/(now-lastMeasure)); recentFrames=0; lastMeasure=now; }
        // Wait for renderer/sorting to have produced visible splats, not just an HTML load.
        if (!ready && !failed && frames>3 && app.stats.frame.gsplats>0) {
            ready=true; clearTimeout(timeout); firstFrameMs=now-started;
            $('status').hidden=true; canvas.dataset.state='ready';
            controls.forEach(id=>$(id).disabled=false);
            phase = 'ready';
            console.info('BETHERE_EXPLORE', JSON.stringify({event:'first-frame', firstFrameMs:Math.round(firstFrameMs)}));
            console.info('BETHERE_EXPLORE', JSON.stringify({event:'ready', phase, firstFrameMs:Math.round(firstFrameMs)}));
        }
    });
}
start().catch(error => {
    const message = sanitize(error?.message || 'Unknown rendering error');
    fail(phase === 'graphics-device' ? 'This device could not start the graphics renderer.' : 'This device could not render the sample.', message);
});
