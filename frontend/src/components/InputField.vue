<template>
  <div class="field">
    <label v-if="label" class="block text-xs font-medium text-stone-500 mb-1">{{ label }}</label>
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :placeholder="placeholder"
      class="input-field"
    />
  </div>
</template>

<script setup>
defineProps({
  modelValue: String,
  label: String,
  placeholder: String
})
defineEmits(['update:modelValue'])
</script>
