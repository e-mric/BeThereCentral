// Defer destruction out of WebGL and PlayCanvas callbacks that may be in progress.
export function disposeFailedApp(app, defer = queueMicrotask) {
    app.autoRender = false;
    defer(() => app.destroy());
}
