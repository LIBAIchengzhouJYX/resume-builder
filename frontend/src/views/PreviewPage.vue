<template>
  <div class="min-h-screen bg-stone-50 flex flex-col">
    <nav class="flex items-center justify-between px-6 py-3 bg-white border-b border-stone-200 flex-shrink-0">
      <div class="flex items-center gap-4">
        <router-link to="/dashboard" class="text-stone-400 hover:text-stone-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/></svg>
        </router-link>
        <h1 class="text-sm font-semibold text-gray-700" v-if="resume">{{ resume.title }}</h1>
      </div>
      <div class="flex items-center gap-3">
        <router-link :to="`/editor/${resumeId}`" class="btn-secondary text-xs">编辑</router-link>
        <button @click="exportPdf" class="btn-primary text-xs">下载 PDF</button>
      </div>
    </nav>
    <div class="flex-1 overflow-y-auto bg-stone-200 p-8 flex justify-center">
      <div class="resume-page" :class="'theme-' + (resume?.theme || 'modern')">
        <div v-html="renderedHtml"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useResumeStore } from '@/stores/resume'
import { marked } from 'marked'

const route = useRoute()
const resumeStore = useResumeStore()
const resume = ref(null)
const resumeId = computed(() => route.params.id)

onMounted(async () => {
  await resumeStore.loadResume(resumeId.value)
  resume.value = resumeStore.currentResume
})

const esc = s => (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')

function mdToHtml(text) {
  if (!text) return ''
  return marked(text || '', { breaks: true })
}

const renderedHtml = computed(() => {
  if (!resume.value?.content) return '<p class="text-stone-400 text-center mt-20">暂无内容</p>'
  const data = JSON.parse(resume.value.content)
  const f = { nameCN:'',nameEN:'',phone:'',email:'',github:'',website:'',location:'',
    summaryCN:'',summaryEN:'',skillLangs:'',skillTools:'',skillSecurity:'',skillOther:'',
    skillsFreestyle:'',workExperience:[],projects:[],education:[],certifications:'',...data }

  let h = ''
  // Header
  const n = f.nameCN || f.nameEN
  const n2 = f.nameEN && f.nameCN ? f.nameEN : ''
  h += `<div class="r-header"><div class="r-name">${esc(n)}${n2?' <span style="font-size:14pt;font-weight:400;color:#888">/</span> '+esc(n2):''}</div>`
  const ct = [f.phone,f.email,f.github,f.website,f.location].filter(Boolean)
  if (ct.length) h += `<div class="r-contact">${ct.map(c=>`<span>${esc(c)}</span>`).join('')}</div>`
  h += `</div>`

  if (f.summaryCN||f.summaryEN){h+=`<div class="r-section"><div class="r-section-title">个人概述 / Summary</div>`
    if(f.summaryCN)h+=`<div class="r-summary">${mdToHtml(f.summaryCN)}</div>`
    if(f.summaryCN&&f.summaryEN)h+=`<hr class="r-b-divider">`
    if(f.summaryEN)h+=`<div class="r-summary">${mdToHtml(f.summaryEN)}</div>`;h+=`</div>`}

  if(f.skillsFreestyle||[f.skillLangs,f.skillTools,f.skillSecurity,f.skillOther].filter(Boolean).length){
    h+=`<div class="r-section"><div class="r-section-title">技能 / Skills</div>`
    if(f.skillsFreestyle)h+=`<div class="r-item-desc">${mdToHtml(f.skillsFreestyle)}</div>`
    else{
      const ls=['编程语言 / Languages','框架 & 工具 / Frameworks','安全 / Security','其他 / Other']
      ;[f.skillLangs,f.skillTools,f.skillSecurity,f.skillOther].forEach((v,i)=>{if(v)h+=`<p style="font-size:9.5pt;margin-bottom:2pt"><strong>${ls[i]}:</strong> ${esc(v)}</p>`})
    };h+=`</div>`}

  f.workExperience.filter(e=>e.titleCN||e.titleEN).forEach(e=>{
    h+=`<div class="r-section"><div class="r-section-title">工作经历 / Work Experience</div>`
    const t=e.titleCN||e.titleEN,t2=e.titleEN&&e.titleCN?e.titleEN:''
    const c=e.companyCN||e.companyEN,c2=e.companyEN&&e.companyCN?e.companyEN:''
    const d=[e.startDate,e.endDate||'至今'].filter(Boolean).join(' — ')
    h+=`<div class="r-item"><div class="r-item-header"><span class="r-item-title">${esc(t)}${t2?' / '+esc(t2):''}</span><span class="r-item-date">${esc(d)}</span></div>`
    h+=`<div class="r-item-sub">${esc(c)}${c2?' / '+esc(c2):''}</div>`
    if(e.descCN||e.descEN){h+=`<div class="r-item-desc">`
      if(e.descCN)h+=mdToHtml(e.descCN);if(e.descCN&&e.descEN)h+=`<hr class="r-b-divider">`
      if(e.descEN)h+=mdToHtml(e.descEN);h+=`</div>`}
    h+=`</div></div>`
  })

  f.projects.filter(p=>p.nameCN||p.nameEN).forEach(p=>{
    h+=`<div class="r-section"><div class="r-section-title">项目经历 / Projects</div>`
    const n=p.nameCN||p.nameEN,n2=p.nameEN&&p.nameCN?p.nameEN:''
    h+=`<div class="r-item"><div class="r-item-header"><span class="r-item-title">${esc(n)}${n2?' / '+esc(n2):''}</span>${p.link?`<span class="r-item-date">${esc(p.link)}</span>`:''}</div>`
    if(p.tech)h+=`<div class="r-item-sub">${esc(p.tech)}</div>`
    if(p.descCN||p.descEN){h+=`<div class="r-item-desc">`
      if(p.descCN)h+=mdToHtml(p.descCN);if(p.descCN&&p.descEN)h+=`<hr class="r-b-divider">`
      if(p.descEN)h+=mdToHtml(p.descEN);h+=`</div>`}
    h+=`</div></div>`
  })

  f.education.filter(e=>e.schoolCN||e.schoolEN).forEach(e=>{
    h+=`<div class="r-section"><div class="r-section-title">教育背景 / Education</div>`
    const s=e.schoolCN||e.schoolEN,s2=e.schoolEN&&e.schoolCN?e.schoolEN:''
    const m=e.majorCN||e.majorEN,m2=e.majorEN&&e.majorCN?e.majorEN:''
    h+=`<div class="r-item"><div class="r-item-header"><span class="r-item-title">${esc(s)}${s2?' / '+esc(s2):''}</span><span class="r-item-date">${esc(e.dateRange)}</span></div>`
    h+=`<div class="r-item-sub">${esc(m)}${m2?' / '+esc(m2):''} · ${esc(e.degree)}</div>`
    if(e.honors)h+=`<div class="r-item-desc">${esc(e.honors)}</div>`
    h+=`</div></div>`
  })

  if(f.certifications.trim()){h+=`<div class="r-section"><div class="r-section-title">证书 & 培训 / Certifications</div>`
    h+=`<div class="r-item-desc">${mdToHtml(f.certifications)}</div></div>`}

  return h
})

async function exportPdf() {
  await resumeStore.exportPdf(resumeId.value)
}
</script>

<style scoped>
.resume-page {
  width: 210mm; min-height: 297mm;
  background: #fff; box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 20mm 18mm;
  font-family: 'Georgia', 'Noto Serif SC', serif;
  font-size: 10.5pt; line-height: 1.6; color: #222;
}
.resume-page :deep(.r-header){text-align:center;margin-bottom:14pt}
.resume-page :deep(.r-name){font-size:22pt;font-weight:700;letter-spacing:.04em;margin-bottom:4pt}
.resume-page :deep(.r-contact){font-size:9pt;color:#555;display:flex;flex-wrap:wrap;justify-content:center;gap:4px 14px}
.resume-page :deep(.r-section){margin-bottom:12pt}
.resume-page :deep(.r-section-title){font-size:11pt;font-weight:700;letter-spacing:.06em;text-transform:uppercase;border-bottom:1.5px solid #333;padding-bottom:3pt;margin-bottom:7pt}
.resume-page :deep(.r-item){margin-bottom:7pt}
.resume-page :deep(.r-item-header){display:flex;justify-content:space-between;align-items:baseline}
.resume-page :deep(.r-item-title){font-weight:600;font-size:10.5pt}
.resume-page :deep(.r-item-sub){font-size:9pt;color:#555}
.resume-page :deep(.r-item-date){font-size:8.5pt;color:#777;white-space:nowrap}
.resume-page :deep(.r-item-desc){font-size:9.5pt;margin-top:2pt;color:#444}
.resume-page :deep(.r-item-desc ul,.r-item-desc ol){padding-left:14pt;margin:2pt 0}
.resume-page :deep(.r-item-desc li){margin-bottom:1pt}
.resume-page :deep(.r-item-desc p){margin:2pt 0}
.resume-page :deep(.r-item-desc strong){font-weight:600}
.resume-page :deep(.r-summary){font-size:10pt;color:#444;line-height:1.7}
.resume-page :deep(.r-b-divider){border:none;border-top:1px dashed #ccc;margin:8pt 0}

.theme-modern :deep(.r-section-title){border-bottom-color:#2563eb;color:#2563eb}
.theme-minimal{font-family:'Helvetica Neue','Noto Sans SC',sans-serif;font-size:10pt}
.theme-minimal :deep(.r-name){font-weight:300;font-size:26pt;letter-spacing:.08em;text-transform:uppercase}
.theme-minimal :deep(.r-section-title){border-bottom:1px solid #ddd;font-weight:400;letter-spacing:.12em;font-size:9pt;color:#999}
.theme-sidebar{display:grid;grid-template-columns:200px 1fr;gap:24px;padding:0;font-family:'Inter','Noto Sans SC',sans-serif;font-size:9.5pt}
.theme-sidebar :deep(.r-header){text-align:left}
.theme-sidebar :deep(.r-name){font-size:18pt}
.theme-timeline :deep(.r-section-title){border-bottom:2px solid #d97706;color:#d97706}
.theme-timeline :deep(.r-item){border-left:2px solid #e5e7eb;padding-left:12pt;position:relative;margin-bottom:10pt}
.theme-timeline :deep(.r-item::before){content:'';position:absolute;left:-5px;top:4px;width:8px;height:8px;background:#d97706;border-radius:50%}
.theme-classic{font-family:'Times New Roman','Songti SC',serif;font-size:11pt}
.theme-classic :deep(.r-section-title){border-bottom:1px solid #000;color:#000;text-align:center}
.theme-bold{font-family:'Helvetica Neue','Noto Sans SC',sans-serif;font-size:10pt}
.theme-bold :deep(.r-header){background:#1a1a1a;color:#fff;padding:18pt 24pt;margin:-20mm -18mm 16pt -18mm}
.theme-bold :deep(.r-name){color:#fff}
.theme-bold :deep(.r-contact){color:#aaa}
</style>
