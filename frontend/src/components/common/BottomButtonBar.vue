<script setup>
defineProps({
  // primaryLabel 을 비워두면 primary 버튼을 렌더하지 않는다.
  // (매물 리스트 step2 처럼 '이전'만 필요한 back-only 바 용도)
  primaryLabel: { type: String, required: false, default: '' },
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
      v-if="primaryLabel"
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
  z-index: 100;
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
  height: 48px;
  border: 0;
  font-size: 14px;
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
