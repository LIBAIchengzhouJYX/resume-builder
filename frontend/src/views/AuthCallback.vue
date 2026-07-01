<template>
  <div class="min-h-screen flex items-center justify-center bg-stone-50">
    <div class="text-center">
      <div class="w-12 h-12 border-4 border-stone-200 border-t-blue-500 rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-stone-500">正在登录...</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  const token = route.query.token
  if (token) {
    authStore.setToken(token)
    authStore.checkAuth().then(() => {
      router.push('/dashboard')
    })
  } else {
    router.push('/')
  }
})
</script>
