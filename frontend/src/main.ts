// sockjs-client 依赖 Node 风格的 global 变量，在浏览器里没有会报 “global is not defined”
// 这里做一个简单 polyfill，兼容 Vite 构建后的运行环境
if (typeof globalThis !== 'undefined' && !(globalThis as any).global) {
  ;(globalThis as any).global = globalThis
}

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
