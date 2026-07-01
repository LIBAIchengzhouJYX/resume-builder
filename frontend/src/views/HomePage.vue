<template>
  <div class="min-h-screen flex flex-col">
    <!-- Nav -->
    <nav class="flex items-center justify-between px-8 py-5 border-b border-stone-100 bg-white/80 backdrop-blur sticky top-0 z-50">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 bg-gray-900 rounded-lg flex items-center justify-center">
          <span class="text-white font-bold text-sm">R</span>
        </div>
        <span class="font-semibold text-lg tracking-tight">Resume<span class="text-blue-500">Builder</span></span>
      </div>
      <div class="flex items-center gap-4">
        <a href="https://github.com" target="_blank" class="text-stone-400 hover:text-stone-600 transition-colors">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
        </a>
        <span v-if="authStore.loading" class="text-sm text-stone-400">加载中...</span>
        <button v-else-if="!authStore.isAuthenticated" @click="authStore.loginWithGitHub"
          class="btn-primary flex items-center gap-2">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
          GitHub 登录
        </button>
        <router-link v-else to="/dashboard" class="flex items-center gap-2 group">
          <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl"
            class="w-8 h-8 rounded-full ring-2 ring-stone-100 group-hover:ring-blue-200 transition-all" />
          <span class="text-sm font-medium">{{ authStore.user?.name || authStore.user?.login }}</span>
        </router-link>
      </div>
    </nav>

    <!-- Hero -->
    <main class="flex-1 flex flex-col items-center justify-center px-6 text-center max-w-4xl mx-auto py-20">
      <div class="inline-flex items-center gap-2 px-4 py-1.5 bg-blue-50 text-blue-700 text-xs font-medium rounded-full mb-8">
        <span class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse"></span>
        开源 · 免费 · 无广告
      </div>
      <h1 class="text-5xl md:text-6xl lg:text-7xl font-display font-semibold leading-tight tracking-tight text-gray-900 mb-6">
        打造令人印象深刻的<br/>
        <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-violet-600">专业简历</span>
      </h1>
      <p class="text-lg text-stone-500 max-w-2xl mb-10 leading-relaxed">
        面向技术人才的开源简历制作工具。支持中英双语、多套精选模板、
        实时预览和高质量 PDF 导出。用 GitHub 账号一键登录，所有数据云端保存。
      </p>
      <div class="flex items-center gap-4">
        <button @click="authStore.loginWithGitHub" class="btn-primary text-base px-8 py-3.5 flex items-center gap-3">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
          使用 GitHub 开始制作
        </button>
        <a href="https://github.com" target="_blank" class="btn-secondary text-base px-8 py-3.5">
          Star on GitHub
        </a>
      </div>

      <!-- Features grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-24 w-full">
        <div class="section-card text-left group hover:shadow-md transition-shadow">
          <div class="w-10 h-10 bg-violet-50 text-violet-600 rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>
          </div>
          <h3 class="font-semibold text-gray-900 mb-1.5">多套精选模板</h3>
          <p class="text-sm text-stone-500">经典、极简、侧边栏、时间轴等 6 套布局，每种都支持自定义 CSS 变量，做出独一无二的风格。</p>
        </div>

        <div class="section-card text-left group hover:shadow-md transition-shadow">
          <div class="w-10 h-10 bg-emerald-50 text-emerald-600 rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"/></svg>
          </div>
          <h3 class="font-semibold text-gray-900 mb-1.5">中英双语</h3>
          <p class="text-sm text-stone-500">每条经历同时支持中文和英文输入，自动排版生成双语对照简历，一键切换语言版本。</p>
        </div>

        <div class="section-card text-left group hover:shadow-md transition-shadow">
          <div class="w-10 h-10 bg-amber-50 text-amber-600 rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z"/></svg>
          </div>
          <h3 class="font-semibold text-gray-900 mb-1.5">高质量 PDF 导出</h3>
          <p class="text-sm text-stone-500">wkhtmltopdf 渲染引擎，A4 标准排版，保持矢量文字和完美排版，打印效果与预览一致。</p>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="border-t border-stone-100 py-8 text-center text-sm text-stone-400">
      <p>MIT License · 开源项目 · 欢迎贡献代码</p>
    </footer>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
