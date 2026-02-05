import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // Listen on all addresses
    allowedHosts: true // Allow any hostname (e.g. app1.local) - Vite 6+ maybe? Or just works.
    // Actually for older Vite, just 'host: true' is enough to bind. 
    // But 'allowedHosts' is newer security feature. 
    // Let's safe bet: host: '0.0.0.0'
  }
})
