<template>
  <div class="min-h-screen bg-stone-50 flex flex-col">
    <!-- Toolbar -->
    <nav class="flex items-center justify-between px-6 py-3 bg-white border-b border-stone-200 flex-shrink-0 z-30">
      <div class="flex items-center gap-4">
        <router-link to="/dashboard" class="text-stone-400 hover:text-stone-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/></svg>
        </router-link>
        <input v-model="resumeTitle" placeholder="简历标题（如：张三-后端开发-2026）"
          class="text-sm font-medium bg-transparent border-none outline-none w-64 text-gray-700 placeholder-stone-300" />
        <span class="text-xs text-stone-400">
          {{ resumeStore.saving ? '保存中...' : resumeStore.lastSaved ? '已保存 ' + formatTime(resumeStore.lastSaved) : '' }}
        </span>
      </div>
      <div class="flex items-center gap-3">
        <!-- AI buttons -->
        <div class="flex items-center gap-1.5 mr-1">
          <button @click="openAiPanel('GENERATE')"
            class="flex items-center gap-1 text-xs px-3 py-2 rounded-lg border border-purple-200 bg-purple-50 text-purple-700 hover:bg-purple-100 transition-colors font-medium"
            title="AI 一键生成简历">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
            AI 生成
          </button>
          <button @click="openAiPanel('ANALYZE')"
            class="flex items-center gap-1 text-xs px-3 py-2 rounded-lg border border-purple-200 bg-purple-50 text-purple-700 hover:bg-purple-100 transition-colors font-medium"
            title="AI 分析优化建议">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
            分析
          </button>
          <button @click="aiMode = 'settings'; aiVisible = true"
            class="flex items-center gap-1 text-xs px-2 py-2 rounded-lg border border-stone-200 bg-white text-stone-500 hover:text-stone-700 hover:border-stone-300 transition-colors"
            title="AI 设置">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          </button>
        </div>
        <select v-model="currentTheme" class="text-xs border border-stone-200 rounded-lg px-3 py-2 bg-white">
          <option value="modern">Modern 现代</option>
          <option value="minimal">Minimal 极简</option>
          <option value="sidebar">Sidebar 侧边栏</option>
          <option value="timeline">Timeline 时间轴</option>
          <option value="classic">Classic 经典</option>
          <option value="bold">Bold 粗犷</option>
        </select>
        <select v-model="currentLanguage" class="text-xs border border-stone-200 rounded-lg px-3 py-2 bg-white">
          <option value="bilingual">中英双语</option>
          <option value="zh">仅中文</option>
          <option value="en">English Only</option>
        </select>
        <button @click="saveResume" class="btn-secondary text-xs px-4 py-2" :disabled="resumeStore.saving">
          {{ resumeStore.saving ? '保存中...' : '保存' }}
        </button>
        <button @click="exportPdf" class="btn-primary text-xs px-4 py-2">导出 PDF</button>
      </div>
    </nav>

    <!-- Main: Left Editor + Right Preview -->
    <div class="flex flex-1 overflow-hidden">
      <!-- Left: Editor Panel -->
      <div class="w-[460px] min-w-[400px] flex-shrink-0 bg-white border-r border-stone-200 overflow-y-auto p-5">
        <!-- Personal Info -->
        <SectionCard title="基本信息 / Personal Info" icon="user">
          <div class="grid grid-cols-2 gap-3">
            <InputField label="姓名" v-model="form.nameCN" placeholder="张三" />
            <InputField label="English Name" v-model="form.nameEN" placeholder="San Zhang" />
            <InputField label="手机 / Phone" v-model="form.phone" placeholder="+86 138-0000-0000" />
            <InputField label="邮箱 / Email" v-model="form.email" placeholder="zhangsan@email.com" />
            <InputField label="GitHub" v-model="form.github" placeholder="github.com/zhangsan" />
            <InputField label="LinkedIn / 网站" v-model="form.website" placeholder="linkedin.com/in/zhangsan" />
            <InputField label="所在地 / Location" v-model="form.location" placeholder="北京 / Beijing" class="col-span-2" />
          </div>
        </SectionCard>

        <!-- Summary — freestyle Markdown -->
        <SectionCard title="个人概述 / Summary" icon="file-text">
          <MarkdownEditor v-model="form.summaryCN" label="中文概述" showAiButton
            @ai-polish="openAiForField('summaryCN', $event)"
            placeholder="写 2-3 句概述你的技术背景、核心能力和职业方向。支持 **加粗**、*斜体*、- 列表等 Markdown 语法。" :rows="3" />
          <MarkdownEditor v-model="form.summaryEN" label="English Summary" showAiButton
            @ai-polish="openAiForField('summaryEN', $event)"
            placeholder="2-3 sentences about your background, strengths, and career direction. Supports **bold**, *italic*, - lists, etc."
            :rows="3" class="mt-3" />
        </SectionCard>

        <!-- Skills — structured inputs for labels, freestyle for body -->
        <SectionCard title="技能 / Skills" icon="zap">
          <div class="mb-3">
            <MarkdownEditor label="自由描述（支持 Markdown）" v-model="form.skillsFreestyle"
              placeholder="你可以自由排版技能部分，例如：&#10;&#10;**编程语言**&#10;Python, JavaScript, Java, C, SQL&#10;&#10;**框架 & 工具**&#10;Spring Boot, Vue, Docker, Git&#10;&#10;**安全**&#10;OWASP Top 10, Burp Suite, Wireshark" :rows="5" />
          </div>
          <p class="text-xs text-stone-400 mb-2">或者使用下方的结构化字段（选其一即可）：</p>
          <InputField label="编程语言 / Languages" v-model="form.skillLangs" placeholder="Python, JavaScript, C, SQL, Java" />
          <InputField label="框架 & 工具 / Frameworks" v-model="form.skillTools" placeholder="React, Node.js, Spring Boot, Docker, Git" class="mt-2" />
          <InputField label="安全 / Security" v-model="form.skillSecurity" placeholder="OWASP Top 10, Burp Suite, Wireshark, Nmap" class="mt-2" />
          <InputField label="其他 / Other" v-model="form.skillOther" placeholder="Linux, 英语(CET-6), 技术写作, 项目管理" class="mt-2" />
        </SectionCard>

        <!-- Work Experience — structured header + freestyle desc -->
        <SectionCard title="工作经历 / Experience" icon="briefcase">
          <div v-for="(exp, i) in form.workExperience" :key="i"
            class="mb-4 p-3 bg-stone-50 rounded-lg relative border border-stone-100">
            <button @click="removeWork(i)" class="absolute top-2 right-2 w-6 h-6 flex items-center justify-center text-stone-400 hover:text-red-500 rounded-full hover:bg-red-50 transition-colors z-10">&times;</button>
            <div class="grid grid-cols-2 gap-2">
              <InputField label="职位 (CN)" v-model="exp.titleCN" placeholder="后端开发实习生" />
              <InputField label="Position (EN)" v-model="exp.titleEN" placeholder="Backend Dev Intern" />
              <InputField label="公司 (CN)" v-model="exp.companyCN" placeholder="XX科技" />
              <InputField label="Company (EN)" v-model="exp.companyEN" placeholder="XX Tech" />
              <InputField label="开始" v-model="exp.startDate" placeholder="2024.06" />
              <InputField label="结束" v-model="exp.endDate" placeholder="至今 / Present" />
            </div>
            <MarkdownEditor label="工作描述 (CN)" v-model="exp.descCN" showAiButton
              @ai-polish="openAiForField(`workExperience.${i}.descCN`, $event)"
              placeholder="用 Markdown 自由描述工作内容，例如：&#10;&#10;使用 **Python + Django** 开发内部自动化平台&#10;&#10;- 设计了 RESTful API 架构，日均处理 10万+ 请求&#10;- 集成 Redis 缓存，响应时间降低 60%&#10;&#10;参与 **Web 安全测试**&#10;&#10;- 使用 Burp Suite 发现 5 个 XSS 漏洞并修复" :rows="5" class="mt-2" />
            <MarkdownEditor label="Description (EN)" v-model="exp.descEN" showAiButton
              @ai-polish="openAiForField(`workExperience.${i}.descEN`, $event)"
              placeholder="Describe your work using Markdown. Supports **bold**, lists, headings, etc." :rows="4" class="mt-2" />
          </div>
          <button @click="addWork" class="flex items-center gap-1.5 text-sm text-blue-600 hover:text-blue-800 font-medium mt-1">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
            添加经历
          </button>
        </SectionCard>

        <!-- Projects -->
        <SectionCard title="项目经历 / Projects" icon="code">
          <div v-for="(proj, i) in form.projects" :key="i"
            class="mb-4 p-3 bg-stone-50 rounded-lg relative border border-stone-100">
            <button @click="removeProject(i)" class="absolute top-2 right-2 w-6 h-6 flex items-center justify-center text-stone-400 hover:text-red-500 rounded-full hover:bg-red-50 transition-colors z-10">&times;</button>
            <div class="grid grid-cols-2 gap-2">
              <InputField label="项目名 (CN)" v-model="proj.nameCN" placeholder="Web 漏洞扫描器" />
              <InputField label="Project (EN)" v-model="proj.nameEN" placeholder="Web Vuln Scanner" />
              <InputField label="技术栈" v-model="proj.tech" placeholder="Python, Django, SQLite, Docker" />
              <InputField label="链接" v-model="proj.link" placeholder="github.com/xxx/project" />
            </div>
            <MarkdownEditor label="项目描述 (CN)" v-model="proj.descCN" showAiButton
              @ai-polish="openAiForField(`projects.${i}.descCN`, $event)"
              placeholder="用 Markdown 描述项目亮点，例如：&#10;&#10;独立设计并实现了一个 Web 漏洞扫描器&#10;&#10;- 支持 **SQL 注入**、**XSS**、**CSRF** 三种漏洞检测&#10;- 集成自动化报告生成，输出 PDF 格式漏洞报告&#10;- 使用多线程技术，扫描速度提升 3 倍" :rows="4" class="mt-2" />
            <MarkdownEditor label="Description (EN)" v-model="proj.descEN" showAiButton
              @ai-polish="openAiForField(`projects.${i}.descEN`, $event)"
              placeholder="Describe the project with Markdown." :rows="3" class="mt-2" />
          </div>
          <button @click="addProject" class="flex items-center gap-1.5 text-sm text-blue-600 hover:text-blue-800 font-medium mt-1">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
            添加项目
          </button>
        </SectionCard>

        <!-- Education -->
        <SectionCard title="教育背景 / Education" icon="book-open">
          <div v-for="(edu, i) in form.education" :key="i"
            class="mb-4 p-3 bg-stone-50 rounded-lg relative border border-stone-100">
            <button @click="removeEducation(i)" class="absolute top-2 right-2 w-6 h-6 flex items-center justify-center text-stone-400 hover:text-red-500 rounded-full hover:bg-red-50 transition-colors z-10">&times;</button>
            <div class="grid grid-cols-2 gap-2">
              <InputField label="学校 (CN)" v-model="edu.schoolCN" placeholder="XX大学" />
              <InputField label="School (EN)" v-model="edu.schoolEN" placeholder="XX University" />
              <InputField label="专业 (CN)" v-model="edu.majorCN" placeholder="计算机科学与技术" />
              <InputField label="Major (EN)" v-model="edu.majorEN" placeholder="Computer Science" />
              <InputField label="学位" v-model="edu.degree" placeholder="本科 / Bachelor" />
              <InputField label="时间" v-model="edu.dateRange" placeholder="2022.09 - 2026.06" />
              <InputField label="GPA / 荣誉 (可选)" v-model="edu.honors" placeholder="GPA 3.5/4.0 · 校级奖学金" class="col-span-2" />
            </div>
          </div>
          <button @click="addEducation" class="flex items-center gap-1.5 text-sm text-blue-600 hover:text-blue-800 font-medium mt-1">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
            添加教育经历
          </button>
        </SectionCard>

        <!-- Certifications — freestyle -->
        <SectionCard title="证书 & 培训 / Certifications" icon="award">
          <MarkdownEditor v-model="form.certifications" showAiButton
            @ai-polish="openAiForField('certifications', $event)"
            placeholder="支持 Markdown 排版，例如：&#10;&#10;- **CISP** 注册信息安全专业人员 (2024)&#10;- **CET-6** 英语六级 580分 (2023)&#10;- **AWS Certified Cloud Practitioner** (2025)" :rows="4" />
        </SectionCard>

        <!-- Custom CSS -->
        <SectionCard title="自定义 CSS (可选)" icon="palette">
          <p class="text-xs text-stone-400 mb-2">覆盖默认样式变量，打造独一无二的简历配色</p>
          <textarea v-model="customCss"
            placeholder=":root { --accent: #2563eb; --font-display: 'Georgia', serif; }"
            class="input-field font-mono text-xs"
            rows="4"
          ></textarea>
        </SectionCard>
      </div>

      <!-- Right: Live Preview -->
      <div class="flex-1 overflow-y-auto bg-stone-200 p-8 flex justify-center">
        <div class="resume-preview-page" :class="'theme-' + currentTheme">
          <div v-html="computedPreviewHtml"></div>
        </div>
      </div>
    </div>

    <!-- AI Modal -->
    <AiModal
      :visible="aiVisible"
      :mode="aiMode"
      :sourceText="aiSourceText"
      :resumeContent="aiResumeContent"
      @close="aiVisible = false"
      @apply="onAiApply"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResumeStore } from '@/stores/resume'
import { marked } from 'marked'
import InputField from '@/components/InputField.vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import SectionCard from '@/components/SectionCard.vue'
import AiModal from '@/components/AiModal.vue'

const route = useRoute()
const router = useRouter()
const resumeStore = useResumeStore()

const resumeTitle = ref('未命名简历')
const currentTheme = ref('modern')
const currentLanguage = ref('bilingual')
const customCss = ref('')
const resumeId = ref(null)

// ═══ FORM DATA ═══
const emptyWork = () => ({ titleCN: '', titleEN: '', companyCN: '', companyEN: '', startDate: '', endDate: '', descCN: '', descEN: '' })
const emptyProject = () => ({ nameCN: '', nameEN: '', tech: '', link: '', descCN: '', descEN: '' })
const emptyEducation = () => ({ schoolCN: '', schoolEN: '', majorCN: '', majorEN: '', degree: '', dateRange: '', honors: '' })

const form = reactive({
  nameCN: '', nameEN: '', phone: '', email: '', github: '', website: '', location: '',
  summaryCN: '', summaryEN: '',
  skillLangs: '', skillTools: '', skillSecurity: '', skillOther: '',
  skillsFreestyle: '',
  workExperience: [],
  projects: [],
  education: [],
  certifications: ''
})

function formatTime(date) {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  const id = route.params.id
  if (id) {
    resumeId.value = parseInt(id)
    try {
      await resumeStore.loadResume(id)
      const r = resumeStore.currentResume
      if (r) {
        resumeTitle.value = r.title
        currentTheme.value = r.theme || 'modern'
        currentLanguage.value = r.language || 'bilingual'
        customCss.value = r.customCss || ''
        if (r.content) {
          const data = JSON.parse(r.content)
          Object.assign(form, {
            nameCN: '', nameEN: '', phone: '', email: '', github: '', website: '', location: '',
            summaryCN: '', summaryEN: '',
            skillLangs: '', skillTools: '', skillSecurity: '', skillOther: '',
            skillsFreestyle: '',
            workExperience: [],
            projects: [],
            education: [],
            certifications: '',
            ...data
          })
        }
      }
    } catch { /* new resume */ }
  }
  if (form.education.length === 0) addEducation()
  if (form.workExperience.length === 0) addWork()
  if (form.projects.length === 0) addProject()
})

function addWork() { form.workExperience.push(emptyWork()) }
function removeWork(i) { form.workExperience.splice(i, 1) }
function addProject() { form.projects.push(emptyProject()) }
function removeProject(i) { form.projects.splice(i, 1) }
function addEducation() { form.education.push(emptyEducation()) }
function removeEducation(i) { form.education.splice(i, 1) }

// ═══ SAVE ═══
async function saveResume() {
  const result = await resumeStore.saveResume({
    id: resumeId.value,
    title: resumeTitle.value || '未命名简历',
    content: JSON.stringify({ ...form }),
    customCss: customCss.value,
    theme: currentTheme.value,
    language: currentLanguage.value
  })
  if (result && result.id && !resumeId.value) {
    resumeId.value = result.id
    router.replace(`/editor/${result.id}`)
  }
}

async function exportPdf() {
  if (!resumeId.value) await saveResume()
  if (resumeId.value) {
    // Build the full HTML with theme and custom CSS
    const bodyHtml = computedPreviewHtml.value
    const themeClass = 'theme-' + currentTheme.value
    const ccss = customCss.value || ''

    const fullHtml = [
      '<!DOCTYPE html><html lang="zh"><head><meta charset="UTF-8">',
      '<style>',
      getBaseCss(),
      ccss,
      '</style></head><body>',
      '<div class="resume-preview-page ' + themeClass + '">',
      bodyHtml,
      '</div></body></html>'
    ].join('\n')

    await resumeStore.exportPdfFromHtml(fullHtml)
  }
}

// ═══ PREVIEW: render Markdown → HTML for the resume ═══
const esc = s => (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')

function mdToHtml(text) {
  if (!text) return ''
  return marked(text || '', { breaks: true })
}

const computedPreviewHtml = computed(() => {
  const f = form
  const showEN = currentLanguage.value !== 'zh'
  const showCN = currentLanguage.value !== 'en'

  let h = ''

  // ── Header ──
  const name = showCN ? f.nameCN : f.nameEN
  const name2 = showEN && showCN && f.nameEN ? f.nameEN : ''
  const contacts = [f.phone, f.email, f.github, f.website, f.location].filter(Boolean)

  h += `<div class="r-header">`
  h += `<div class="r-name">${esc(name)}${name2 ? ' <span style="font-size:14pt;font-weight:400;color:#888">/</span> '+esc(name2) : ''}</div>`
  if (contacts.length) h += `<div class="r-contact">${contacts.map(c=>`<span>${esc(c)}</span>`).join('')}</div>`
  h += `</div>`

  // ── Summary (Markdown rendered) ──
  const sCN = showCN && f.summaryCN
  const sEN = showEN && f.summaryEN
  if (sCN || sEN) {
    h += `<div class="r-section"><div class="r-section-title">${showCN?'个人概述':'Summary'}${showCN&&showEN?' / Summary':''}</div>`
    if (sCN) h += `<div class="r-summary">${mdToHtml(f.summaryCN)}</div>`
    if (sCN && sEN) h += `<hr class="r-bilingual-divider">`
    if (sEN) h += `<div class="r-summary">${mdToHtml(f.summaryEN)}</div>`
    h += `</div>`
  }

  // ── Skills ──
  const skillsData = [f.skillLangs, f.skillTools, f.skillSecurity, f.skillOther].filter(Boolean)
  if (f.skillsFreestyle || skillsData.length) {
    h += `<div class="r-section"><div class="r-section-title">${showCN?'技能':'Skills'}${showCN&&showEN?' / Skills':''}</div>`
    if (f.skillsFreestyle) {
      h += `<div class="r-item-desc">${mdToHtml(f.skillsFreestyle)}</div>`
    } else if (skillsData.length) {
      const labels = [
        '编程语言' + (showEN ? ' / Languages' : ''),
        '框架 & 工具' + (showEN ? ' / Frameworks' : ''),
        '安全' + (showEN ? ' / Security' : ''),
        '其他' + (showEN ? ' / Other' : '')
      ]
      const all = [f.skillLangs, f.skillTools, f.skillSecurity, f.skillOther]
      all.forEach((v, i) => {
        if (v) h += `<p class="r-skill-line"><strong>${labels[i]}:</strong> ${esc(v)}</p>`
      })
    }
    h += `</div>`
  }

  // ── Work Experience (Markdown desc) ──
  const workItems = f.workExperience.filter(e => e.titleCN || e.titleEN)
  if (workItems.length) {
    h += `<div class="r-section"><div class="r-section-title">${showCN?'工作经历':'Work Experience'}${showCN&&showEN?' / Work Experience':''}</div>`
    workItems.forEach(e => {
      const title = showCN ? e.titleCN : e.titleEN
      const title2 = showEN && showCN && e.titleEN ? e.titleEN : ''
      const company = showCN ? e.companyCN : e.companyEN
      const company2 = showEN && showCN && e.companyEN ? e.companyEN : ''
      const date = [e.startDate, e.endDate || '至今'].filter(Boolean).join(' — ')
      const descCN = showCN && e.descCN ? mdToHtml(e.descCN) : ''
      const descEN = showEN && e.descEN ? mdToHtml(e.descEN) : ''

      h += `<div class="r-item">`
      h += `<div class="r-item-header"><span class="r-item-title">${esc(title)}${title2?' / '+esc(title2):''}</span><span class="r-item-date">${esc(date)}</span></div>`
      h += `<div class="r-item-sub">${esc(company)}${company2?' / '+esc(company2):''}</div>`
      if (descCN || descEN) {
        h += `<div class="r-item-desc">`
        if (descCN) h += descCN
        if (descCN && descEN) h += `<hr class="r-bilingual-divider">`
        if (descEN) h += descEN
        h += `</div>`
      }
      h += `</div>`
    })
    h += `</div>`
  }

  // ── Projects (Markdown desc) ──
  const projItems = f.projects.filter(p => p.nameCN || p.nameEN)
  if (projItems.length) {
    h += `<div class="r-section"><div class="r-section-title">${showCN?'项目经历':'Projects'}${showCN&&showEN?' / Projects':''}</div>`
    projItems.forEach(p => {
      const name = showCN ? p.nameCN : p.nameEN
      const name2 = showEN && showCN && p.nameEN ? p.nameEN : ''
      h += `<div class="r-item">`
      h += `<div class="r-item-header"><span class="r-item-title">${esc(name)}${name2?' / '+esc(name2):''}</span>${p.link?`<span class="r-item-date">${esc(p.link)}</span>`:''}</div>`
      if (p.tech) h += `<div class="r-item-sub">${esc(p.tech)}</div>`
      const pDescCN = showCN && p.descCN ? mdToHtml(p.descCN) : ''
      const pDescEN = showEN && p.descEN ? mdToHtml(p.descEN) : ''
      if (pDescCN || pDescEN) {
        h += `<div class="r-item-desc">`
        if (pDescCN) h += pDescCN
        if (pDescCN && pDescEN) h += `<hr class="r-bilingual-divider">`
        if (pDescEN) h += pDescEN
        h += `</div>`
      }
      h += `</div>`
    })
    h += `</div>`
  }

  // ── Education ──
  const eduItems = f.education.filter(e => e.schoolCN || e.schoolEN)
  if (eduItems.length) {
    h += `<div class="r-section"><div class="r-section-title">${showCN?'教育背景':'Education'}${showCN&&showEN?' / Education':''}</div>`
    eduItems.forEach(e => {
      const school = showCN ? e.schoolCN : e.schoolEN
      const school2 = showEN && showCN && e.schoolEN ? e.schoolEN : ''
      const major = showCN ? e.majorCN : e.majorEN
      const major2 = showEN && showCN && e.majorEN ? e.majorEN : ''
      h += `<div class="r-item">`
      h += `<div class="r-item-header"><span class="r-item-title">${esc(school)}${school2?' / '+esc(school2):''}</span><span class="r-item-date">${esc(e.dateRange)}</span></div>`
      h += `<div class="r-item-sub">${esc(major)}${major2?' / '+esc(major2):''} · ${esc(e.degree)}</div>`
      if (e.honors) h += `<div class="r-item-desc">${esc(e.honors)}</div>`
      h += `</div>`
    })
    h += `</div>`
  }

  // ── Certifications (Markdown) ──
  if (f.certifications.trim()) {
    h += `<div class="r-section"><div class="r-section-title">${showCN?'证书 & 培训':'Certifications'}${showCN&&showEN?' / Certifications':''}</div>`
    h += `<div class="r-item-desc">${mdToHtml(f.certifications)}</div>`
    h += `</div>`
  }

  return h
})

// Auto-save
let saveTimeout = null
watch([form, resumeTitle, currentTheme, currentLanguage, customCss], () => {
  clearTimeout(saveTimeout)
  saveTimeout = setTimeout(() => {
    if (resumeId.value) saveResume()
  }, 2000)
}, { deep: true })

// ═══ AI 功能 ═══
const aiVisible = ref(false)
const aiMode = ref('settings') // 'settings' | 'panel'
const aiSourceText = ref('')
const aiResumeContent = ref('')
const aiCurrentTarget = ref(null) // 当前 AI 操作的目标字段路径，如 'summaryCN'

function openAiPanel(action) {
  // 先检查是否已配置 API Key
  const apiKey = localStorage.getItem('ai_apikey')
  if (!apiKey) {
    aiMode.value = 'settings'
    aiVisible.value = true
    return
  }

  aiMode.value = 'panel'
  aiSourceText.value = ''
  aiCurrentTarget.value = null

  if (action === 'GENERATE') {
    // 生成模式 — 不需要源文本，传关键词即可
    aiSourceText.value = ''
  } else if (action === 'ANALYZE') {
    // 分析全文
    aiResumeContent.value = JSON.stringify({ ...form })
  }

  aiVisible.value = true
}

/**
 * 从 MarkdownEditor/textarea 打开 AI 面板 — 传入选中文本
 * target: 目标字段路径，如 'summaryCN', 'workExperience.0.descCN'
 */
function openAiForField(target, text) {
  const apiKey = localStorage.getItem('ai_apikey')
  if (!apiKey) {
    aiMode.value = 'settings'
    aiVisible.value = true
    return
  }

  aiMode.value = 'panel'
  aiSourceText.value = text || ''
  aiCurrentTarget.value = target
  aiResumeContent.value = JSON.stringify({ ...form })
  aiVisible.value = true
}

function onAiApply({ action, text }) {
  if (!aiCurrentTarget.value || !text) return

  if (aiCurrentTarget.value === 'fullResume') {
    // 应用整个简历（AI 生成模式）
    try {
      const generated = JSON.parse(text)
      Object.assign(form, generated)
    } catch {
      // 如果不是 JSON，尝试总结/分析文本不做自动填充
    }
    return
  }

  // 嵌套路径处理：如 'workExperience.0.descCN'
  const path = aiCurrentTarget.value.split('.')
  let obj = form
  for (let i = 0; i < path.length - 1; i++) {
    obj = obj[path[i]]
    if (!obj) return
  }
  const lastKey = path[path.length - 1]

  if (action === 'IMPROVE') {
    // 润色直接替换
    obj[lastKey] = text
  } else if (action === 'TRANSLATE') {
    obj[lastKey] = text
  } else if (action === 'GENERATE') {
    obj[lastKey] = text
  } else if (action === 'ANALYZE') {
    // 分析结果不自动填充内容，仅做参考
  }
}

// 暴露 openAiForField 给子组件
function getAiFieldOpener(target) {
  return (text) => openAiForField(target, text)
}

// ═══ PDF Export: build base CSS to pass to backend ═══
function getBaseCss() {
  return `
    .resume-preview-page {
      width: 210mm; min-height: 297mm; background: #fff;
      padding: 20mm 18mm; font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif;
      font-size: 10.5pt; line-height: 1.6; color: #222;
    }
    .r-header { text-align: center; margin-bottom: 14pt; }
    .r-name { font-size: 22pt; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 4pt; }
    .r-contact { font-size: 9pt; color: #555; display: flex; flex-wrap: wrap; justify-content: center; gap: 4px 14px; }
    .r-section { margin-bottom: 12pt; page-break-inside: avoid; }
    .r-section-title { font-size: 11pt; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; border-bottom: 1.5px solid #333; padding-bottom: 3pt; margin-bottom: 7pt; }
    .r-item { margin-bottom: 7pt; }
    .r-item-header { display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; }
    .r-item-title { font-weight: 600; font-size: 10.5pt; }
    .r-item-sub { font-size: 9pt; color: #555; }
    .r-item-date { font-size: 8.5pt; color: #777; white-space: nowrap; }
    .r-item-desc { font-size: 9.5pt; margin-top: 2pt; color: #444; }
    .r-item-desc ul { padding-left: 14pt; margin: 2pt 0; }
    .r-item-desc li { margin-bottom: 1pt; }
    .r-item-desc p { margin: 2pt 0; }
    .r-item-desc strong { font-weight: 600; }
    .r-summary { font-size: 10pt; color: #444; line-height: 1.7; }
    .r-summary p { margin: 3pt 0; }
    .r-bilingual-divider { border: none; border-top: 1px dashed #ccc; margin: 8pt 0; }
    .r-skill-line { font-size: 9.5pt; margin-bottom: 2pt; }
    h3 { font-size: 10.5pt; font-weight: 600; margin: 6pt 0 2pt; }
    h4 { font-size: 10pt; font-weight: 600; margin: 4pt 0 1pt; }
    blockquote { border-left: 2px solid #ddd; padding-left: 8pt; color: #666; margin: 4pt 0; }
    code { background: #f0f0f0; padding: 1px 3px; border-radius: 2px; font-size: 9pt; }
    hr { border: none; border-top: 1px solid #e5e5e5; margin: 6pt 0; }
    /* Themes */
    .theme-modern .r-section-title { border-bottom-color: #2563eb; color: #2563eb; }
    .theme-minimal { font-family: 'Helvetica Neue', 'Noto Sans SC', sans-serif; font-size: 10pt; }
    .theme-minimal .r-name { font-weight: 300; font-size: 26pt; letter-spacing: 0.08em; text-transform: uppercase; }
    .theme-minimal .r-section-title { border-bottom: 1px solid #ddd; font-weight: 400; letter-spacing: 0.12em; font-size: 9pt; color: #999; }
    .theme-timeline .r-section-title { border-bottom: 2px solid #d97706; color: #d97706; }
    .theme-timeline .r-item { border-left: 2px solid #e5e7eb; padding-left: 12pt; }
    .theme-classic { font-family: 'Times New Roman', 'Songti SC', serif; font-size: 11pt; }
    .theme-classic .r-name { font-variant: small-caps; }
    .theme-classic .r-section-title { border-bottom: 1px solid #000; color: #000; text-align: center; }
    .theme-bold { font-family: 'Helvetica Neue', 'Noto Sans SC', sans-serif; font-size: 10pt; }
    .theme-bold .r-header { background: #1a1a1a; color: #fff; padding: 18pt 24pt; margin-left: -18mm; margin-right: -18mm; margin-top: -20mm; margin-bottom: 16pt; }
    .theme-bold .r-name { color: #fff; }
    .theme-bold .r-contact { color: #aaa; }
    .theme-bold .r-section-title { background: #f0f0f0; padding: 4pt 8pt; border-bottom: none; font-size: 10pt; }
  `
}
</script>

<style scoped>
.resume-preview-page {
  width: 210mm;
  min-height: 297mm;
  background: #fff;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 20mm 18mm;
  font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', serif;
  font-size: 10.5pt;
  line-height: 1.6;
  color: #222;
  flex-shrink: 0;
  transition: box-shadow 0.3s;
}

/* Resume inner styles */
.resume-preview-page :deep(.r-header) { text-align: center; margin-bottom: 14pt; }
.resume-preview-page :deep(.r-name) { font-size: 22pt; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 4pt; }
.resume-preview-page :deep(.r-contact) { font-size: 9pt; color: #555; display: flex; flex-wrap: wrap; justify-content: center; gap: 4px 14px; }
.resume-preview-page :deep(.r-section) { margin-bottom: 12pt; }
.resume-preview-page :deep(.r-section-title) { font-size: 11pt; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; border-bottom: 1.5px solid #333; padding-bottom: 3pt; margin-bottom: 7pt; }
.resume-preview-page :deep(.r-item) { margin-bottom: 7pt; }
.resume-preview-page :deep(.r-item-header) { display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; }
.resume-preview-page :deep(.r-item-title) { font-weight: 600; font-size: 10.5pt; }
.resume-preview-page :deep(.r-item-sub) { font-size: 9pt; color: #555; }
.resume-preview-page :deep(.r-item-date) { font-size: 8.5pt; color: #777; white-space: nowrap; }
.resume-preview-page :deep(.r-item-desc) { font-size: 9.5pt; margin-top: 2pt; color: #444; }
.resume-preview-page :deep(.r-item-desc ul) { padding-left: 14pt; margin: 2pt 0; }
.resume-preview-page :deep(.r-item-desc li) { margin-bottom: 1pt; }
.resume-preview-page :deep(.r-item-desc p) { margin: 2pt 0; }
.resume-preview-page :deep(.r-item-desc strong) { font-weight: 600; }
.resume-preview-page :deep(.r-summary) { font-size: 10pt; color: #444; line-height: 1.7; }
.resume-preview-page :deep(.r-summary p) { margin: 3pt 0; }
.resume-preview-page :deep(.r-bilingual-divider) { border: none; border-top: 1px dashed #ccc; margin: 8pt 0; }
.resume-preview-page :deep(.r-skill-line) { font-size: 9.5pt; margin-bottom: 2pt; }

/* ── THEMES ── */
.theme-modern { font-family: 'Georgia', 'Noto Serif SC', serif; }
.theme-modern :deep(.r-section-title) { border-bottom-color: #2563eb; color: #2563eb; }

.theme-minimal { font-family: 'Helvetica Neue', 'Noto Sans SC', sans-serif; font-size: 10pt; }
.theme-minimal :deep(.r-name) { font-weight: 300; font-size: 26pt; letter-spacing: 0.08em; text-transform: uppercase; }
.theme-minimal :deep(.r-section-title) { border-bottom: 1px solid #ddd; font-weight: 400; letter-spacing: 0.12em; font-size: 9pt; color: #999; }

.theme-sidebar {
  display: grid; grid-template-columns: 200px 1fr; gap: 24px; padding: 0;
  font-family: 'Inter', 'Noto Sans SC', sans-serif; font-size: 9.5pt;
}
.theme-sidebar :deep(.r-header) { text-align: left; }
.theme-sidebar :deep(.r-name) { font-size: 18pt; }
.theme-sidebar :deep(.r-section-title) { border-bottom: none; font-size: 9pt; color: #555; letter-spacing: 0.08em; padding-bottom: 0; margin-bottom: 5pt; }

.theme-timeline { font-family: 'Georgia', 'Noto Serif SC', serif; }
.theme-timeline :deep(.r-section-title) { border-bottom: 2px solid #d97706; color: #d97706; }
.theme-timeline :deep(.r-item) { border-left: 2px solid #e5e7eb; padding-left: 12pt; position: relative; margin-bottom: 10pt; }
.theme-timeline :deep(.r-item::before) { content: ''; position: absolute; left: -5px; top: 4px; width: 8px; height: 8px; background: #d97706; border-radius: 50%; }

.theme-classic { font-family: 'Times New Roman', 'Songti SC', serif; font-size: 11pt; }
.theme-classic :deep(.r-name) { font-variant: small-caps; }
.theme-classic :deep(.r-section-title) { border-bottom: 1px solid #000; color: #000; text-align: center; }

.theme-bold { font-family: 'Helvetica Neue', 'Noto Sans SC', sans-serif; font-size: 10pt; }
.theme-bold :deep(.r-header) { background: #1a1a1a; color: #fff; padding: 18pt 24pt; margin: -20mm -18mm 16pt -18mm; }
.theme-bold :deep(.r-name) { color: #fff; }
.theme-bold :deep(.r-contact) { color: #aaa; }
.theme-bold :deep(.r-section-title) { background: #f0f0f0; padding: 4pt 8pt; border-bottom: none; font-size: 10pt; }

/* Markdown in preview */
.resume-preview-page :deep(h3) { font-size: 10.5pt; font-weight: 600; margin: 6pt 0 2pt; }
.resume-preview-page :deep(h4) { font-size: 10pt; font-weight: 600; margin: 4pt 0 1pt; }
.resume-preview-page :deep(blockquote) { border-left: 2px solid #ddd; padding-left: 8pt; color: #666; margin: 4pt 0; font-style: italic; }
.resume-preview-page :deep(code) { background: #f0eff0; padding: 1px 3px; border-radius: 2px; font-size: 9pt; }
.resume-preview-page :deep(hr) { border: none; border-top: 1px solid #e5e3dc; margin: 6pt 0; }
.resume-preview-page :deep(a) { color: #2563eb; }
.resume-preview-page :deep(del) { color: #aaa; }
</style>
