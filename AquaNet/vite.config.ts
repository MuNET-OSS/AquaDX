import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import UnoCSS from '@unocss/svelte-scoped/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    UnoCSS(),
    svelte()
  ],
  define: {
    APP_VERSION: JSON.stringify(process.env.npm_package_version),
  },
  build: {
    rollupOptions: {
      output: {
        entryFileNames: `x_angelkawaii_x/x_[hash:11]_x.js`,
        assetFileNames: 'x_angelkawaii_x/x_[hash:11]_x[extname]',
        chunkFileNames: 'x_angelkawaii_x/x_[hash:11]_x.js',
        compact: true,
      },
    },
  },
})
