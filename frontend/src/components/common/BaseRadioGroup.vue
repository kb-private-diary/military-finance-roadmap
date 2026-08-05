<script setup>
defineProps({
  modelValue: { type: [String, Boolean, Number], required: true },
  options: { type: Array, required: true }, // [{ label, value }]
  name: { type: String, required: true },
  label: { type: String, required: false, default: '' },
});
defineEmits(['update:modelValue']);
</script>

<template>
  <div class="base-radio-group">
    <p v-if="label" class="base-radio-group__label">{{ label }}</p>
    <div class="base-radio-group__options">
      <label
        v-for="opt in options"
        :key="String(opt.value)"
        class="base-radio-group__option"
      >
        <input
          type="radio"
          class="base-radio-group__input"
          :name="name"
          :checked="modelValue === opt.value"
          @change="$emit('update:modelValue', opt.value)"
        />
        <span class="base-radio-group__dot" />
        <span class="base-radio-group__text">{{ opt.label }}</span>
      </label>
    </div>
  </div>
</template>

<style scoped>
.base-radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.base-radio-group__label {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-body);
}

.base-radio-group__options {
  display: flex;
  gap: 20px;
}

.base-radio-group__option {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.base-radio-group__input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.base-radio-group__dot {
  position: relative;
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid var(--line-strong);
  border-radius: 50%;
  transition: border-color 0.2s ease;
}

.base-radio-group__dot::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background-color: var(--kb-yellow-deep);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.15s ease;
}

.base-radio-group__input:checked + .base-radio-group__dot {
  border-color: var(--kb-yellow-deep);
}

.base-radio-group__input:checked + .base-radio-group__dot::after {
  transform: translate(-50%, -50%) scale(1);
}

.base-radio-group__text {
  font-size: 14px;
  color: var(--text-body);
}
</style>
