<script setup>
defineProps({
  primaryLabel: { type: String, required: true, default: '다음' },
  secondaryLabel: { type: String, required: false, default: '' },
  primaryDisabled: { type: Boolean, required: false, default: false },
  primaryVariant: { type: String, required: false, default: 'primary' },
});

defineEmits(['primary-click', 'secondary-click']);
</script>

<template>
  <div class="bottom-button-bar">
    <button
      v-if="secondaryLabel"
      type="button"
      class="bar-button secondary"
      @click="$emit('secondary-click')"
    >
      {{ secondaryLabel }}
    </button>
    <button
      type="button"
      class="bar-button"
      :class="primaryVariant"
      :disabled="primaryDisabled"
      @click="$emit('primary-click')"
    >
      {{ primaryLabel }}
    </button>
  </div>
</template>

<style scoped>
/* 화면 하단 고정. 프레임 폭(아이폰 16 기준 393px)에 맞춰 가운데 정렬한다. */
.bottom-button-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  width: 100%;
  max-width: 393px;
  background-color: #ffffff;
  padding-bottom: env(safe-area-inset-bottom);
}

.bar-button {
  height: 56px;
  border: 0;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.secondary {
  flex: 4;
  background-color: var(--kb-gray-pale);
  color: var(--kb-dark-gray);
}

.primary {
  flex: 6;
  background-color: var(--kb-yellow);
  color: var(--text-strong);
}

.bar-button:disabled {
  background-color: var(--kb-gray-pale);
  color: var(--text-disabled);
  cursor: not-allowed;
}

.danger {
  flex: 6;
  background-color: var(--danger);
  color: #ffffff;
}
</style>
