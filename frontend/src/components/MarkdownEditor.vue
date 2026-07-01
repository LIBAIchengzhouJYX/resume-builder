<template>
  <div class="md-editor" :class="{ 'md-editor--focused': isFocused }">
    <label v-if="label" class="block text-xs font-medium text-stone-500 mb-1.5">{{ label }}</label>

    <!-- Toolbar -->
    <div class="md-toolbar" v-if="showToolbar">
      <button @click="wrapText('**')" title="加粗 / Bold" class="md-tb-btn font-bold">B</button>
      <button @click="wrapText('*')" title="斜体 / Italic" class="md-tb-btn italic">I</button>
      <button @click="wrapText('~~')" title="删除线 / Strikethrough" class="md-tb-btn line-through">S</button>
      <span class="md-tb-divider"></span>
      <button @click="insertPrefix('- ')" title="无序列表 / Bullet List" class="md-tb-btn">•</button>
      <button @click="insertPrefix('1. ')" title="有序列表 / Numbered List" class="md-tb-btn">1.</button>
      <span class="md-tb-divider"></span>
      <button @click="wrapText('[]()', { open: '[', close: '](url)' })" title="链接 / Link" class="md-tb-btn">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
      </button>
      <button @click="wrapText('`')" title="行内代码 / Inline Code" class="md-tb-btn">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"/></svg>
      </button>
      <button @click="insertBlock('> ')" title="引用 / Blockquote" class="md-tb-btn">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8 4v12a4 4 0 01-4 4H3m0-8h5M16 4v12a4 4 0 01-4 4h-1m0-8h5"/></svg>
      </button>
      <span class="md-tb-divider"></span>
      <button @click="insertHeading(3)" title="三级标题 / H3" class="md-tb-btn text-xs">H3</button>
      <button @click="insertDivider()" title="分隔线 / Divider" class="md-tb-btn">—</button>
      <span v-if="showAiButton" class="md-tb-divider"></span>
      <button v-if="showAiButton" @click="$emit('ai-polish', props.modelValue)" title="AI 润色 / AI Polish" class="md-tb-btn md-tb-btn-ai">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
      </button>
    </div>

    <!-- Editor area -->
    <div class="md-editor-area">
      <div class="md-editor-lines" v-if="lineNumbers">
        <span v-for="n in lineCount" :key="n" class="md-line-num">{{ n }}</span>
      </div>
      <textarea
        ref="textareaRef"
        :value="modelValue"
        @input="onInput"
        @focus="isFocused = true"
        @blur="isFocused = false"
        @keydown="onKeydown"
        :placeholder="placeholder"
        :rows="rows"
        :class="['md-textarea', { 'md-textarea--numbered': lineNumbers }]"
        spellcheck="false"
      ></textarea>
    </div>

    <!-- Toggle: raw / preview -->
    <div class="md-footer" v-if="showPreview">
      <label class="md-preview-toggle">
        <input type="checkbox" v-model="previewMode" />
        <span>预览 / Preview</span>
      </label>
    </div>

    <!-- Preview -->
    <div v-if="previewMode && showPreview" class="md-preview prose" v-html="renderedHtml"></div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  rows: { type: Number, default: 5 },
  showToolbar: { type: Boolean, default: true },
  showPreview: { type: Boolean, default: true },
  lineNumbers: { type: Boolean, default: false },
  showAiButton: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'ai-polish'])

const textareaRef = ref(null)
const isFocused = ref(false)
const previewMode = ref(false)

const lineCount = computed(() => {
  return Math.max(props.rows, (props.modelValue || '').split('\n').length)
})

const renderedHtml = computed(() => {
  if (!props.modelValue) return ''
  return marked(props.modelValue, { breaks: true })
})

function onInput(e) {
  emit('update:modelValue', e.target.value)
}

function onKeydown(e) {
  // Tab inserts spaces
  if (e.key === 'Tab') {
    e.preventDefault()
    const ta = textareaRef.value
    if (!ta) return
    const start = ta.selectionStart
    const end = ta.selectionEnd
    const text = props.modelValue || ''
    const newText = text.substring(0, start) + '  ' + text.substring(end)
    emit('update:modelValue', newText)
    nextTick(() => {
      ta.selectionStart = ta.selectionEnd = start + 2
    })
  }
}

function wrapText(wrapper, opts = {}) {
  const ta = textareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const text = props.modelValue || ''
  const selected = text.substring(start, end)

  let open, close
  if (opts.open) {
    open = opts.open
    close = opts.close || opts.open
  } else {
    open = wrapper
    close = wrapper
  }

  // If nothing selected, insert placeholder
  const replacement = selected ? open + selected + close : open + (opts.open ? 'text' : 'bold') + close
  const newText = text.substring(0, start) + replacement + text.substring(end)
  emit('update:modelValue', newText)

  nextTick(() => {
    if (selected) {
      ta.selectionStart = start
      ta.selectionEnd = end + open.length + close.length
    } else {
      const sel = start + open.length
      const selEnd = sel + (opts.open ? 4 : 4)
      ta.selectionStart = sel
      ta.selectionEnd = selEnd
    }
    ta.focus()
  })
}

function insertPrefix(prefix) {
  const ta = textareaRef.value
  if (!ta) return
  const text = props.modelValue || ''
  // Find start of current line
  let lineStart = ta.selectionStart
  while (lineStart > 0 && text[lineStart - 1] !== '\n') lineStart--
  const before = text.substring(0, lineStart)
  const after = text.substring(lineStart)
  const newText = before + prefix + after
  emit('update:modelValue', newText)
  nextTick(() => {
    ta.selectionStart = ta.selectionEnd = lineStart + prefix.length
    ta.focus()
  })
}

function insertBlock(prefix) {
  insertPrefix(prefix)
}

function insertHeading(level) {
  insertPrefix('#'.repeat(level) + ' ')
}

function insertDivider() {
  insertPrefix('---\n')
}
</script>

<style scoped>
.md-editor {
  border: 1px solid #e2e0d8;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #fafaf8;
}
.md-editor--focused {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37,99,235,0.08);
  background: #fff;
}

.md-toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 8px;
  background: #f5f4f0;
  border-bottom: 1px solid #e2e0d8;
  flex-wrap: wrap;
}
.md-tb-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #555;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.12s;
}
.md-tb-btn:hover {
  background: #e8e6de;
  color: #1a1a1a;
}
.md-tb-divider {
  width: 1px;
  height: 18px;
  background: #d5d3cc;
  margin: 0 4px;
}

.md-tb-btn-ai {
  color: #7c3aed;
}
.md-tb-btn-ai:hover {
  background: #ede9fe;
  color: #6d28d9;
}

.md-editor-area {
  display: flex;
}
.md-editor-lines {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 8px 4px 8px 8px;
  background: #f5f4f0;
  border-right: 1px solid #e2e0d8;
  user-select: none;
  min-width: 32px;
}
.md-line-num {
  font-size: 11px;
  line-height: 1.6;
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  color: #c5c3bb;
}

.md-textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: vertical;
  padding: 8px 10px;
  font-family: 'SF Mono', 'Cascadia Code', 'Noto Sans SC', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #1a1a1a;
  background: transparent;
  min-height: 40px;
}
.md-textarea::placeholder {
  color: #b5b3ad;
}
.md-textarea--numbered {
  resize: vertical;
}

.md-footer {
  padding: 4px 8px;
  border-top: 1px solid #e2e0d8;
  background: #fafaf8;
}
.md-preview-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #888;
  cursor: pointer;
}
.md-preview-toggle input {
  width: 14px;
  height: 14px;
}

/* Rendered preview */
.md-preview {
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #333;
  border-top: 1px solid #e2e0d8;
  max-height: 240px;
  overflow-y: auto;
  background: #fff;
}
.md-preview :deep(h3) { font-size: 15px; font-weight: 600; margin: 8px 0 4px; }
.md-preview :deep(h4) { font-size: 14px; font-weight: 600; margin: 6px 0 3px; }
.md-preview :deep(p) { margin: 0 0 6px; }
.md-preview :deep(ul), .md-preview :deep(ol) { padding-left: 18px; margin: 4px 0; }
.md-preview :deep(li) { margin-bottom: 2px; }
.md-preview :deep(strong) { font-weight: 600; }
.md-preview :deep(code) { background: #f0efe8; padding: 1px 4px; border-radius: 3px; font-size: 12px; }
.md-preview :deep(blockquote) { border-left: 3px solid #d5d3cc; padding-left: 10px; color: #777; margin: 6px 0; }
.md-preview :deep(hr) { border: none; border-top: 1px solid #e5e3dc; margin: 8px 0; }
.md-preview :deep(a) { color: #2563eb; }
.md-preview :deep(del) { color: #aaa; }
</style>
