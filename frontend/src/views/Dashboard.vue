<template>
  <div class="min-h-screen bg-stone-50">
    <!-- Nav -->
    <nav class="flex items-center justify-between px-8 py-4 bg-white border-b border-stone-100">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-gray-900 rounded-lg flex items-center justify-center">
          <span class="text-white font-bold text-sm">R</span>
        </div>
        <span class="font-semibold text-lg">ResumeBuilder</span>
      </div>
      <div class="flex items-center gap-4">
        <router-link to="/editor" class="btn-primary text-sm">+ 新建简历</router-link>
        <div class="flex items-center gap-2 cursor-pointer" @click="authStore.logout()">
          <img v-if="authStore.user?.avatarUrl" :src="authStore.user?.avatarUrl"
            class="w-7 h-7 rounded-full" />
          <span class="text-sm text-stone-500 hover:text-stone-700">{{ authStore.user?.login }}</span>
        </div>
      </div>
    </nav>

    <!-- Content -->
    <main class="max-w-4xl mx-auto px-6 py-12">
      <div class="flex items-center justify-between mb-8">
        <h1 class="text-2xl font-bold text-gray-900">我的简历</h1>
        <span class="text-sm text-stone-400">{{ resumes.length }} 份简历</span>
      </div>

      <!-- Empty state -->
      <div v-if="resumes.length === 0 && !resumeStore.loading" class="section-card text-center py-16">
        <div class="w-16 h-16 bg-stone-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-stone-400" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m3.75 9v6m3-3H9m1.5-12H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/>
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-700 mb-1">还没有简历</h3>
        <p class="text-stone-400 mb-6">点击下方按钮创建你的第一份专业简历</p>
        <router-link to="/editor" class="btn-primary inline-flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
          创建简历
        </router-link>
      </div>

      <!-- Resume list -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="resume in resumes" :key="resume.id"
          class="section-card group hover:shadow-md transition-all cursor-pointer">
          <div class="flex items-start justify-between mb-3">
            <div>
              <h3 class="font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">{{ resume.title }}</h3>
              <p class="text-xs text-stone-400 mt-0.5">
                模板: {{ resume.theme || 'modern' }} · {{ resume.language === 'bilingual' ? '中英双语' : resume.language === 'zh' ? '中文' : 'English' }}
              </p>
            </div>
            <span v-if="resume.isPublic" class="px-2 py-0.5 bg-emerald-50 text-emerald-600 text-xs rounded-full">公开</span>
          </div>
          <p v-if="resume.description" class="text-sm text-stone-500 mb-3 line-clamp-2">{{ resume.description }}</p>
          <div class="flex items-center justify-between text-xs text-stone-400">
            <span>更新于 {{ resume.updatedAt || resume.createdAt }}</span>
            <div class="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <router-link :to="`/editor/${resume.id}`" class="text-blue-500 hover:text-blue-700">编辑</router-link>
              <button @click.stop="handleDelete(resume.id)" class="text-red-400 hover:text-red-600">删除</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useResumeStore } from '@/stores/resume'

const authStore = useAuthStore()
const resumeStore = useResumeStore()
const resumes = computed(() => resumeStore.resumes)

onMounted(async () => {
  await resumeStore.loadResumes()
})

async function handleDelete(id) {
  if (confirm('确定删除这份简历吗？此操作不可恢复。')) {
    await resumeStore.deleteResume(id)
  }
}
</script>
