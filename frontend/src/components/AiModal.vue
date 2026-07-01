<template>
  <Teleport to="body">
    <div v-if="visible" class="ai-modal-overlay" @click.self="close">
      <div class="ai-modal" :class="{ 'ai-modal-wide': mode === 'panel' }">
        <!-- Header -->
        <div class="ai-modal-header">
          <h3 class="ai-modal-title">AI 助手</h3>
          <button @click="close" class="ai-close-btn">&times;</button>
        </div>

        <!-- Settings mode -->
        <div v-if="mode === 'settings'" class="ai-settings-body">
          <div class="ai-field">
            <label>模型选择</label>
            <select v-model="localModel" class="ai-select">
              <option v-for="m in presetModels" :key="m.model" :value="m.model">
                {{ m.name }}
              </option>
              <option value="__custom__">自定义 / Custom</option>
            </select>
            <p v-if="selectedPreset" class="ai-hint">{{ selectedPreset.description }}</p>
          </div>

          <div class="ai-field">
            <label>API 地址（Endpoint）</label>
            <input v-model="localEndpoint" class="ai-input" placeholder="https://api.deepseek.com" />
            <p class="ai-hint">兼容 OpenAI 协议的 API 地址</p>
          </div>

          <div class="ai-field">
            <label>API Key</label>
            <input v-model="localModelName" v-if="localModel === '__custom__'" class="ai-input mb-2" placeholder="模型名称，如 deepseek-chat" />
            <input v-model="localApiKey" type="password" class="ai-input" placeholder="sk-xxxxxxxxxxxxxxxx" />
            <p class="ai-hint">密钥仅保存在浏览器本地存储，不会上传到服务器</p>
          </div>

          <div class="ai-modal-footer">
            <button @click="close" class="ai-btn-secondary">取消</button>
            <button @click="saveAndClose" class="ai-btn-primary">保存</button>
          </div>
        </div>

        <!-- Action Panel mode -->
        <div v-else-if="mode === 'panel'" class="ai-panel-body">
          <!-- Quick actions -->
          <div class="ai-quick-actions">
            <button @click="runAction('IMPROVE')" :disabled="aiRunning"
              class="ai-action-btn" :class="{ active: currentAction === 'IMPROVE' }">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
              润色优化
            </button>
            <button @click="runAction('GENERATE')" :disabled="aiRunning"
              class="ai-action-btn" :class="{ active: currentAction === 'GENERATE' }">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
              生成内容
            </button>
            <button @click="runAction('ANALYZE')" :disabled="aiRunning"
              class="ai-action-btn" :class="{ active: currentAction === 'ANALYZE' }">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
              分析建议
            </button>
            <button @click="runAction('TRANSLATE')" :disabled="aiRunning"
              class="ai-action-btn" :class="{ active: currentAction === 'TRANSLATE' }">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/></svg>
              翻译
            </button>
          </div>

          <!-- Generate extra inputs -->
          <div v-if="currentAction === 'GENERATE'" class="ai-generate-extra">
            <div class="ai-field">
              <label>目标职位</label>
              <input v-model="genJobTitle" class="ai-input" placeholder="如：后端开发工程师" />
            </div>
            <div class="ai-field">
              <label>关键词 / 技能</label>
              <input v-model="genKeywords" class="ai-input" placeholder="如：Spring Boot, Python, Docker" />
            </div>
            <div class="ai-field">
              <label>个人背景（可选）</label>
              <textarea v-model="genBackground" class="ai-textarea" placeholder="如：3年Java开发经验，参与过微服务项目..." rows="2"></textarea>
            </div>
          </div>

          <!-- Translate direction -->
          <div v-if="currentAction === 'TRANSLATE'" class="ai-translate-extra">
            <div class="ai-field">
              <label>翻译方向</label>
              <select v-model="translateDir" class="ai-select">
                <option value="zh2en">中文 → English</option>
                <option value="en2zh">English → 中文</option>
              </select>
            </div>
          </div>

          <!-- Input text preview -->
          <div v-if="sourceText && currentAction !== 'GENERATE'" class="ai-source-preview">
            <div class="ai-source-label">输入文本</div>
            <div class="ai-source-text">{{ sourceText.substring(0, 300) }}{{ sourceText.length > 300 ? '...' : '' }}</div>
          </div>

          <!-- Loading -->
          <div v-if="aiRunning" class="ai-loading">
            <svg class="ai-spin" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v4m0 12v4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83M2 12h4m12 0h4M4.93 19.07l2.83-2.83m8.48-8.48l2.83-2.83"/></svg>
            <span>{{ loadingText }}</span>
          </div>

          <!-- Result -->
          <div v-if="aiResult && !aiRunning" class="ai-result-area">
            <div class="ai-result-actions">
              <span class="ai-result-label">AI 结果</span>
              <button @click="applyResult" class="ai-apply-btn">应用结果</button>
            </div>
            <div class="ai-result-content" v-html="renderedResult"></div>
          </div>

          <!-- Error -->
          <div v-if="aiError" class="ai-error">
            <svg class="w-4 h-4 text-red-500 flex-shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <span>{{ aiError }}</span>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { marked } from 'marked'
import api from '@/api'

const props = defineProps({
  visible: { type: Boolean, default: false },
  mode: { type: String, default: 'settings' }, // 'settings' | 'panel'
  sourceText: { type: String, default: '' },   // 当前选中的文本（用于润色/翻译）
  resumeContent: { type: String, default: '' }, // 全文（用于分析）
})

const emit = defineEmits(['close', 'apply'])

// ── Preset models ──
const presetModels = [
  { name: 'DeepSeek V3', model: 'deepseek-chat', endpoint: 'https://api.deepseek.com', description: 'DeepSeek 最新通用模型，性价比极高' },
  { name: 'DeepSeek R1', model: 'deepseek-reasoner', endpoint: 'https://api.deepseek.com', description: 'DeepSeek 推理模型，适合复杂分析' },
  { name: 'GLM-4', model: 'glm-4', endpoint: 'https://open.bigmodel.cn/api/paas/v4', description: '智谱 GLM-4 旗舰模型，中文能力强' },
  { name: 'GLM-4 Flash', model: 'glm-4-flash', endpoint: 'https://open.bigmodel.cn/api/paas/v4', description: 'GLM-4 轻量版，速度更快' },
  { name: 'OpenAI GPT-4o', model: 'gpt-4o', endpoint: 'https://api.openai.com', description: 'OpenAI 最强模型' },
  { name: 'OpenAI GPT-4o Mini', model: 'gpt-4o-mini', endpoint: 'https://api.openai.com', description: 'OpenAI 轻量模型' },
]

// ── Local state ──
const localModel = ref('')
const localEndpoint = ref('')
const localApiKey = ref('')
const localModelName = ref('')
const currentAction = ref(null)
const genKeywords = ref('')
const genJobTitle = ref('')
const genBackground = ref('')
const translateDir = ref('zh2en')
const aiRunning = ref(false)
const aiResult = ref('')
const aiError = ref('')

const selectedPreset = computed(() => presetModels.find(m => m.model === localModel.value))

// ── Load saved from localStorage ──
function loadSettings() {
  localModel.value = localStorage.getItem('ai_model') || 'deepseek-chat'
  localEndpoint.value = localStorage.getItem('ai_endpoint') || 'https://api.deepseek.com'
  localApiKey.value = localStorage.getItem('ai_apikey') || ''
  localModelName.value = localStorage.getItem('ai_custom_model') || ''
}

loadSettings()
watch(() => props.visible, (v) => { if (v) loadSettings() })

// If user picks a preset, auto-fill endpoint
watch(localModel, (m) => {
  if (m === '__custom__') return
  const p = presetModels.find(x => x.model === m)
  if (p) localEndpoint.value = p.endpoint
})

function saveAndClose() {
  localStorage.setItem('ai_model', localModel.value)
  localStorage.setItem('ai_endpoint', localEndpoint.value)
  localStorage.setItem('ai_apikey', localApiKey.value)
  localStorage.setItem('ai_custom_model', localModelName.value)
  emit('close')
}

function close() {
  aiResult.value = ''
  aiError.value = ''
  currentAction.value = null
  emit('close')
}

// ── Run AI action ──
const loadingText = computed(() => {
  switch (currentAction.value) {
    case 'IMPROVE': return '正在润色优化中...'
    case 'GENERATE': return '正在生成内容...'
    case 'ANALYZE': return '正在分析简历...'
    case 'TRANSLATE': return '正在翻译...'
    default: return '处理中...'
  }
})

async function runAction(action) {
  currentAction.value = action
  aiRunning.value = true
  aiResult.value = ''
  aiError.value = ''

  const model = localModel.value === '__custom__' ? localModelName.value : localModel.value
  const headers = {
    'X-AI-Endpoint': localEndpoint.value,
    'X-AI-ApiKey': localApiKey.value,
    'X-AI-Model': model,
  }

  const body = {
    action,
    text: props.sourceText || '',
    resumeContent: props.resumeContent || '',
    translateDirection: translateDir.value,
    keywords: genKeywords.value,
    jobTitle: genJobTitle.value,
    background: genBackground.value,
  }

  try {
    const res = await api.post('/ai', body, { headers })
    if (res.data?.result) {
      aiResult.value = res.data.result
    } else if (res.data?.error) {
      aiError.value = res.data.error
    } else {
      aiError.value = 'AI 返回了空结果'
    }
  } catch (e) {
    if (e.response?.data?.error) {
      aiError.value = e.response.data.error
    } else if (e.response?.status === 401) {
      aiError.value = '认证失败，请检查 API Key'
    } else if (e.response?.status === 429) {
      aiError.value = '请求太频繁，请稍后再试'
    } else {
      aiError.value = e.message || '请求失败，请检查网络和 API 配置'
    }
  } finally {
    aiRunning.value = false
  }
}

// ── Apply result ──
function applyResult() {
  emit('apply', {
    action: currentAction.value,
    text: aiResult.value,
  })
  close()
}

// Render result as Markdown
const renderedResult = computed(() => {
  if (!aiResult.value) return ''
  return marked(aiResult.value, { breaks: true })
})
</script>

<style scoped>
.ai-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999; backdrop-filter: blur(2px);
}
.ai-modal {
  background: #fff; border-radius: 12px; width: 480px; max-width: 95vw;
  max-height: 85vh; display: flex; flex-direction: column; overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2); animation: aiFadeIn 0.2s ease-out;
}
.ai-modal-wide { width: 620px; }
@keyframes aiFadeIn { from { opacity: 0; transform: scale(0.96) translateY(8px); } to { opacity: 1; transform: scale(1) translateY(0); } }

.ai-modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #e5e7eb;
}
.ai-modal-title { font-size: 16px; font-weight: 600; color: #1f2937; }
.ai-close-btn { font-size: 22px; color: #9ca3af; background: none; border: none; cursor: pointer; line-height: 1; padding: 0 4px; }
.ai-close-btn:hover { color: #374151; }

/* Settings */
.ai-settings-body, .ai-panel-body {
  padding: 16px 20px; overflow-y: auto; flex: 1;
}

.ai-field { margin-bottom: 14px; }
.ai-field label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 4px; }
.ai-field.mb-2 { margin-bottom: 8px; }
.ai-input, .ai-select, .ai-textarea {
  width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 8px;
  font-size: 13px; outline: none; transition: border-color 0.15s; box-sizing: border-box;
  background: #fff; color: #1f2937;
}
.ai-input:focus, .ai-select:focus, .ai-textarea:focus { border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.ai-textarea { resize: vertical; font-family: inherit; }
.ai-hint { font-size: 11px; color: #9ca3af; margin-top: 2px; }

.ai-modal-footer {
  display: flex; gap: 8px; justify-content: flex-end; padding-top: 8px;
}
.ai-btn-primary, .ai-btn-secondary {
  padding: 8px 18px; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; border: none;
}
.ai-btn-primary { background: #6366f1; color: #fff; }
.ai-btn-primary:hover { background: #5558e6; }
.ai-btn-secondary { background: #f3f4f6; color: #374151; }
.ai-btn-secondary:hover { background: #e5e7eb; }

/* Panel quick actions */
.ai-quick-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 16px; }
.ai-action-btn {
  display: flex; align-items: center; gap: 6px; padding: 10px 14px;
  border: 1px solid #e5e7eb; border-radius: 10px; background: #fff; color: #374151;
  font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.15s;
}
.ai-action-btn:hover:not(:disabled) { border-color: #6366f1; background: #f5f3ff; color: #4f46e5; }
.ai-action-btn.active { border-color: #6366f1; background: #eef2ff; color: #4f46e5; }
.ai-action-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.ai-generate-extra, .ai-translate-extra {
  padding: 12px; background: #f9fafb; border-radius: 8px; margin-bottom: 14px;
}

.ai-source-preview { margin-bottom: 14px; }
.ai-source-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 4px; }
.ai-source-text {
  font-size: 12px; color: #6b7280; background: #f9fafb; border-radius: 6px;
  padding: 8px 10px; max-height: 80px; overflow-y: auto; white-space: pre-wrap; word-break: break-all;
}

.ai-loading {
  display: flex; align-items: center; gap: 10px; padding: 20px;
  color: #6366f1; font-size: 14px; justify-content: center;
}
.ai-spin { animation: spin 1.4s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.ai-result-area {
  margin-top: 8px; border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden;
}
.ai-result-actions {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; background: #f9fafb; border-bottom: 1px solid #e5e7eb;
}
.ai-result-label { font-size: 11px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; }
.ai-apply-btn {
  font-size: 12px; font-weight: 500; color: #fff; background: #10b981; border: none;
  border-radius: 6px; padding: 4px 12px; cursor: pointer;
}
.ai-apply-btn:hover { background: #059669; }
.ai-result-content {
  padding: 12px; font-size: 13px; line-height: 1.7; color: #374151;
  max-height: 300px; overflow-y: auto; white-space: pre-wrap;
}
.ai-result-content :deep(p) { margin: 4px 0; }
.ai-result-content :deep(ul), .ai-result-content :deep(ol) { padding-left: 18px; margin: 4px 0; }
.ai-result-content :deep(strong) { font-weight: 600; }
.ai-result-content :deep(code) { background: #f0eff0; padding: 1px 3px; border-radius: 3px; font-size: 12px; }

.ai-error {
  display: flex; align-items: flex-start; gap: 8px; padding: 10px 14px;
  background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px;
  color: #dc2626; font-size: 13px; margin-top: 12px;
}
</style>
