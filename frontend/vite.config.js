import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // Windows image editors can lock assets and crash native file watchers.
  server: { watch: { usePolling: true, interval: 500 } },
})
