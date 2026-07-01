<template>
  <div class="section-card mb-4">
    <div class="flex items-center gap-2 mb-3">
      <div v-if="icon" class="w-6 h-6 bg-stone-100 rounded flex items-center justify-center text-stone-500 flex-shrink-0">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <template v-if="icon === 'user'"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></template>
          <template v-else-if="icon === 'file-text'"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></template>
          <template v-else-if="icon === 'zap'"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></template>
          <template v-else-if="icon === 'briefcase'"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path stroke-linecap="round" stroke-linejoin="round" d="M16 21V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v16"/></template>
          <template v-else-if="icon === 'code'"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></template>
          <template v-else-if="icon === 'book-open'"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></template>
          <template v-else-if="icon === 'award'"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></template>
          <template v-else-if="icon === 'palette'"><path stroke-linecap="round" stroke-linejoin="round" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"/></template>
        </svg>
      </div>
      <h3 class="text-xs font-semibold text-stone-500 uppercase tracking-wider">{{ title }}</h3>
    </div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: String,
  icon: String
})
</script>
