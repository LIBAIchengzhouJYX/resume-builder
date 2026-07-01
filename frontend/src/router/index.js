import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { title: 'ResumeBuilder - 首页' }
  },
  {
    path: '/auth/callback',
    name: 'AuthCallback',
    component: () => import('@/views/AuthCallback.vue'),
    meta: { title: '登录中...' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true, title: '我的简历' }
  },
  {
    path: '/editor',
    name: 'Editor',
    component: () => import('@/views/EditorPage.vue'),
    meta: { requiresAuth: true, title: '编辑简历' }
  },
  {
    path: '/editor/:id',
    name: 'EditorEdit',
    component: () => import('@/views/EditorPage.vue'),
    meta: { requiresAuth: true, title: '编辑简历' }
  },
  {
    path: '/preview/:id',
    name: 'Preview',
    component: () => import('@/views/PreviewPage.vue'),
    meta: { requiresAuth: true, title: '预览简历' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
