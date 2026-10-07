export const clamp = (value, min, max) => Math.min(max, Math.max(min, value));

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
    // Lens zoom keeps the camera in the capture instead of dollying through
    // nearby splats and exposing the sparse outside of the sample.
    return {...state, fov: clamp(state.fov * factor, 35, 85)};
}
export function position(state) {
    const yaw = state.yaw * Math.PI / 180;
    const pitch = state.pitch * Math.PI / 180;
    return [state.target[0] + Math.sin(yaw) * Math.cos(pitch) * state.distance,
        state.target[1] + Math.sin(pitch) * state.distance,
        state.target[2] + Math.cos(yaw) * Math.cos(pitch) * state.distance];
}
