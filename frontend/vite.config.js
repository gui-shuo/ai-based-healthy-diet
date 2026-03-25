import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath } from 'url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    
    // 自动导入Vue API
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
      eslintrc: {
        enabled: true
      }
    }),
    
    // 自动导入组件
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  
  resolve: {
    alias: {
      '@': path.resolve(path.dirname(fileURLToPath(import.meta.url)), 'src')
    }
  },
  
  server: {
    port: 3000,
    open: true,
    watch: {
      ignored: ['**/src/auto-imports.d.ts', '**/src/components.d.ts']
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  },
  
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser', // 使用terser获得更好的压缩
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除console
        drop_debugger: true // 移除debugger
      }
    },
    rollupOptions: {
      output: {
        // 更细粒度的代码分割
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // Element Plus组件库
            if (id.includes('element-plus')) {
              return 'element-plus'
            }
            // ECharts图表库
            if (id.includes('echarts')) {
              return 'echarts'
            }
            // Vue核心库
            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) {
              return 'vue-vendor'
            }
            // 其他第三方库
            return 'vendor'
          }
        },
        // 静态资源文件名
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    },
    chunkSizeWarningLimit: 1000, // 降低警告阈值
    cssCodeSplit: true, // CSS代码分割
    reportCompressedSize: false, // 禁用压缩大小报告以加快构建
    assetsInlineLimit: 4096 // 小于4kb的资源内联为base64
  },
  
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus/es',
      'element-plus/es/components/menu/style/css',
      'element-plus/es/components/menu-item/style/css',
      'element-plus/es/components/avatar/style/css',
      'element-plus/es/components/skeleton/style/css',
      'element-plus/es/components/alert/style/css',
      'element-plus/es/components/row/style/css',
      'element-plus/es/components/col/style/css',
      'element-plus/es/components/input-number/style/css',
      'element-plus/es/components/progress/style/css',
      'element-plus/es/components/image/style/css',
      'element-plus/es/components/dialog/style/css',
      'element-plus/es/components/slider/style/css',
      'element-plus/es/components/radio-group/style/css',
      'element-plus/es/components/radio-button/style/css',
      'element-plus/es/components/switch/style/css',
      'element-plus/es/components/select/style/css',
      'element-plus/es/components/option/style/css',
      'element-plus/es/components/divider/style/css',
      'element-plus/es/components/upload/style/css',
      'element-plus/es/components/drawer/style/css',
      'element-plus/es/components/timeline/style/css',
      'element-plus/es/components/timeline-item/style/css',
      'element-plus/es/components/card/style/css',
      'element-plus/es/components/radio/style/css',
      'element-plus/es/components/collapse-transition/style/css',
      'element-plus/es/components/space/style/css',
      'element-plus/es/components/date-picker/style/css',
      'element-plus/es/components/descriptions/style/css',
      'element-plus/es/components/descriptions-item/style/css',
      'element-plus/es/components/scrollbar/style/css',
      'element-plus/es/components/table/style/css',
      'element-plus/es/components/table-column/style/css',
      'element-plus/es/components/loading/style/css',
      'element-plus/es/components/pagination/style/css',
      'element-plus/es/components/tag/style/css',
      'element-plus/es/components/empty/style/css',
      'element-plus/es/components/form/style/css',
      'element-plus/es/components/link/style/css',
      'element-plus/es/components/checkbox/style/css',
      'element-plus/es/components/form-item/style/css',
      'element-plus/es/components/input/style/css',
      'element-plus/es/components/message/style/css',
      'element-plus/es/components/dropdown/style/css',
      'element-plus/es/components/dropdown-menu/style/css',
      'element-plus/es/components/dropdown-item/style/css',
      'element-plus/es/components/button/style/css',
      'element-plus/es/components/icon/style/css',
      'element-plus/es/components/base/style/css',
      'element-plus/es/components/tabs/style/css',
      'element-plus/es/components/tab-pane/style/css',
      'element-plus/es/components/tooltip/style/css',
      'element-plus/es/components/popover/style/css',
      'element-plus/es/components/badge/style/css',
      'element-plus/es/components/breadcrumb/style/css',
      'element-plus/es/components/breadcrumb-item/style/css',
      'element-plus/es/components/message-box/style/css',
      'element-plus/es/components/notification/style/css',
      'element-plus/es/components/text/style/css',
      'element-plus/es/components/container/style/css',
      'element-plus/es/components/header/style/css',
      'element-plus/es/components/main/style/css',
      'element-plus/es/components/aside/style/css',
      'element-plus/es/components/footer/style/css'
    ]
  },

  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['legacy-js-api'],
        // 使用@use替代@import，不再需要additionalData
        // additionalData: `@import "@/styles/variables.scss";`
      }
    }
  }
})
