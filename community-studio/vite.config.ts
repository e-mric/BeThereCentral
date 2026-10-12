import { fileURLToPath } from 'node:url';
import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

const lib = fileURLToPath(new URL('./src/lib', import.meta.url));

export default defineConfig({
  resolve: { alias: { '#lib': lib } },
  plugins: [sveltekit({ adapter: adapter({ fallback: '200.html' }), preprocess: vitePreprocess() })]
});
