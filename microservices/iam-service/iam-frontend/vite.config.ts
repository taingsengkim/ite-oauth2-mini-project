import { fileURLToPath, URL } from 'node:url'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  base: '/dashboard/',
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    rolldownOptions: {
      input: { dashboard: fileURLToPath(new URL('./index.html', import.meta.url)), recovery: fileURLToPath(new URL('./recovery.html', import.meta.url)) },
    },
    outDir: '../iam-main/src/main/resources/static/dashboard',
    emptyOutDir: false,
  },
  server: {
    proxy: {
      '/logout': { target: 'http://localhost:9000', changeOrigin: true },
      '/login': { target: 'http://localhost:9000', changeOrigin: true },
      '/assets': { target: 'http://localhost:9000', changeOrigin: true },
      '/recovery-api': { target: 'http://localhost:9000', changeOrigin: true },
      '/account/identities': { target: 'http://localhost:9000', changeOrigin: true },
      '/oauth2/authorization': { target: 'http://localhost:9000', changeOrigin: true },
      '/dashboard-api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
      },
    },
  },
})
