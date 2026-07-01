<template>
  <div class="field">
    <label v-if="label" class="block text-xs font-medium text-stone-500 mb-1">{{ label }}</label>
    <textarea
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :placeholder="placeholder"
      :rows="rows"
      class="input-field"
    ></textarea>
  </div>
</template>

<script setup>
defineProps({
  modelValue: String,
  label: String,
  placeholder: String,
  rows: { type: Number, default: 3 }
})
defineEmits(['update:modelValue'])
</script>
