import { defineStore } from 'pinia'
import api from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    loading: false
  }),
  actions: {
    async checkAuth() {
      const token = localStorage.getItem('jwt_token')
      if (!token) return
      try {
        this.loading = true
        const res = await api.get('/auth/me')
        this.user = res.data
        this.isAuthenticated = true
      } catch {
        localStorage.removeItem('jwt_token')
        this.user = null
        this.isAuthenticated = false
      } finally {
        this.loading = false
      }
    },
    loginWithGitHub() {
      window.location.href = '/oauth2/authorization/github'
    },
    setToken(token) {
      localStorage.setItem('jwt_token', token)
    },
    logout() {
      localStorage.removeItem('jwt_token')
      this.user = null
      this.isAuthenticated = false
      window.location.href = '/'
    }
  }
})
