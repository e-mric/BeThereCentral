import { test } from 'node:test';
import assert from 'node:assert/strict';
import { disposeFailedApp } from '../src/lifecycle.mjs';

test('renderer failure stops drawing immediately and destroys after the callback', () => {
    const events = [];
    const pending = [];
    const app = {
        autoRender: true,
        destroy() { events.push('destroy'); },
    };
    disposeFailedApp(app, callback => pending.push(callback));
    assert.equal(app.autoRender, false);
    assert.deepEqual(events, []);
    assert.equal(pending.length, 1);
    pending[0]();
    assert.deepEqual(events, ['destroy']);
});
