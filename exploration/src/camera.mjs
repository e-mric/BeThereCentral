export const clamp = (value, min, max) => Math.min(max, Math.max(min, value));

const MIN_FOV = 35;
const MAX_FOV = 85;
const BASE_DISTANCE = 2.5;
const BASE_FOV = 75;
const MAX_DISTANCE = 35;
const radians = degrees => degrees * Math.PI / 180;
const degrees = radians => radians * 180 / Math.PI;
const baseFrame = BASE_DISTANCE * Math.tan(radians(BASE_FOV / 2));
const minFrameScale = Math.tan(radians(MIN_FOV / 2)) / Math.tan(radians(BASE_FOV / 2));
const maxFrameScale = MAX_DISTANCE * Math.tan(radians(MAX_FOV / 2)) / baseFrame;

// Camera state is independent from building coordinates and physical location.
// Curated interior view of the bundled sample. Keep the proven camera position
// near x 0, y 1, z 1.5; aim down to show machinery instead of empty ceiling.
export function overview() {
    return {target:[0,0.3,4],distance:2.5,yaw:180,pitch:15,fov:75};
}
export function orbit(state, dx, dy) {
    return {...state, yaw: state.yaw - dx * 0.25, pitch: clamp(state.pitch + dy * 0.25, -80, 80)};
}
export function zoom(state, factor) {
    if (!Number.isFinite(factor) || factor <= 0) return state;

    // Keep one reversible framing scale. Use lens changes while possible,
    // then dolly outward once the wide-FOV limit is reached.
    const currentFrame = state.distance * Math.tan(radians(state.fov / 2));
    const frameScale = clamp(currentFrame / baseFrame * factor, minFrameScale, maxFrameScale);
    const desiredFrame = baseFrame * frameScale;
    const lensFov = degrees(2 * Math.atan(desiredFrame / BASE_DISTANCE));
    if (lensFov <= MAX_FOV) {
        return {...state, distance: BASE_DISTANCE, fov: clamp(lensFov, MIN_FOV, MAX_FOV)};
    }
    return {
        ...state,
        distance: frameScale === maxFrameScale
            ? MAX_DISTANCE
            : clamp(desiredFrame / Math.tan(radians(MAX_FOV / 2)), BASE_DISTANCE, MAX_DISTANCE),
        fov: MAX_FOV,
    };
}
export function position(state) {
    const yaw = state.yaw * Math.PI / 180;
    const pitch = state.pitch * Math.PI / 180;
    return [state.target[0] + Math.sin(yaw) * Math.cos(pitch) * state.distance,
        state.target[1] + Math.sin(pitch) * state.distance,
        state.target[2] + Math.cos(yaw) * Math.cos(pitch) * state.distance];
}

// Bounds of the bundled capture after the display-only 180° X rotation.
// They are scene coordinates, not surveyed building metres or floor positions.
const SAMPLE_CENTER = [0.0762, 0.5186, 3.3131];
const SAMPLE_HALF_EXTENTS = [3.9858, 1.0907, 6.7738];
export function topView(aspect = 1) {
    const ratio = Number.isFinite(aspect) && aspect > 0 ? aspect : 1;
    const pitch = 60;
    const tilt = radians(pitch);
    const back = [0, Math.sin(tilt), -Math.cos(tilt)];
    const up = [0, Math.cos(tilt), Math.sin(tilt)];
    // Keep the capture clear of the right-hand rail, with matching breathing room.
    const vertical = Math.tan(radians(MAX_FOV / 2)) * .84;
    const horizontal = Math.tan(radians(MAX_FOV / 2)) * ratio * .72;
    let distance = BASE_DISTANCE;
    for (const x of [-SAMPLE_HALF_EXTENTS[0], SAMPLE_HALF_EXTENTS[0]]) {
        for (const y of [-SAMPLE_HALF_EXTENTS[1], SAMPLE_HALF_EXTENTS[1]]) {
            for (const z of [-SAMPLE_HALF_EXTENTS[2], SAMPLE_HALF_EXTENTS[2]]) {
                const depth = y * back[1] + z * back[2];
                distance = Math.max(distance, depth + Math.max(Math.abs(x) / horizontal,
                    Math.abs(y * up[1] + z * up[2]) / vertical));
            }
        }
    }
    return {target: [...SAMPLE_CENTER], distance: clamp(distance + .8, BASE_DISTANCE, MAX_DISTANCE),
        yaw: 180, pitch, fov: MAX_FOV};
}
