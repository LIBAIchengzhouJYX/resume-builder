import { defineStore } from 'pinia'
import api from '@/api'

export const useResumeStore = defineStore('resume', {
  state: () => ({
    resumes: [],
    currentResume: null,
    saving: false,
    lastSaved: null
  }),
  actions: {
    async loadResumes() {
      const res = await api.get('/resumes')
      this.resumes = res.data
    },
    async loadResume(id) {
      const res = await api.get(`/resumes/${id}`)
      this.currentResume = res.data
    },
    async saveResume(data) {
      this.saving = true
      try {
        let res
        if (data.id) {
          res = await api.put(`/resumes/${data.id}`, data)
        } else {
          res = await api.post('/resumes', data)
        }
        this.lastSaved = new Date()
        await this.loadResumes()
        return res.data
      } finally {
        this.saving = false
      }
    },
    async deleteResume(id) {
      await api.delete(`/resumes/${id}`)
      await this.loadResumes()
    },
    async exportPdf(id) {
      const res = await api.get(`/export/pdf/${id}`, {
        responseType: 'blob'
      })
      const url = URL.createObjectURL(res.data)
      const a = document.createElement('a')
      a.href = url
      a.download = 'resume.pdf'
      a.click()
      URL.revokeObjectURL(url)
    },
    as