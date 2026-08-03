<script setup>
defineProps({
  variant: {
    type: String,
    required: false,
    default: 'oval-yellow',
  },
  active: { type: Boolean, required: false, default: false },
  label: { type: String, required: true },
  icon: { type: String, required: false, default: '' },
});
const emit = defineEmits(['click']);
</script>

<template>
  <button
    :class="[
      'category-btn',
      `category-btn--${variant}`,
      { 'is-active': active },
    ]"
    @click="emit('click')"
    type="button"
  >
    <span v-if="icon" class="category-btn__icon">{{ icon }}</span>
    <span class="category-btn__label">{{ label }}</span>
  </button>
</template>

<style scoped>
.category-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  width: 100%;
  gap: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  font-family: inherit;
  font-weight: 500;
  outline: none;
  white-space: nowrap;
  transition: all 0.2s ease;
}
.category-btn:active {
  transform: scale(0.96);
}

/* ── 각 Variant 스타일 ── */
.category-btn--oval-yellow {
  padding: clamp(8px, 2vw, 10px) clamp(10px, 3vw, 16px);
  border-radius: 999px;
  font-size: clamp(14px, 3.8vw, 16px);
  font-weight: 500;
  background-color: var(--gray-pale-bg);
  color: var(--gray-mid);
}
.category-btn--oval-yellow.is-active {
  background-color: var(--kb-yellow);
  color: var(--kb-gray);
  font-weight: 600;
}

/* 4. oval-brown */
.category-btn--oval-brown {
  padding: clamp(7px, 2vw, 9px) clamp(10px, 3vw, 16px);
  border-radius: 999px;
  font-size: clamp(14px, 3.8vw, 16px);
  background-color: var(--brown-pale-bg);
  color: var(--brown-text);
}
.category-btn--oval-brown.is-active {
  background-color: var(--brown-active-bg);
  color: var(--surface-default);
  font-weight: 600;
}

/* 여행 스타일 선택 */
.category-btn--oval-green {
  padding: clamp(8px, 2.5vw, 10px) clamp(12px, 4vw, 20px);
  border-radius: 999px;
  font-size: clamp(13px, 3.8vw, 15px);
  background-color: var(--military-green-light);
  color: var(--military-green);
}

.category-btn--oval-green.is-active {
  background-color: var(--military-green);
  color: var(--surface-default);
  font-weight: 700;
}

/* 2. square-yellow */
.category-btn--square-yellow {
  padding: clamp(8px, 2vw, 10px) clamp(10px, 3vw, 16px);
  border-radius: 12px;
  font-size: clamp(14px, 3.8vw, 16px);
  font-weight: 500;
  background-color: var(--gray-pale-bg);
  color: var(--gray-mid);
}
.category-btn--square-yellow.is-active {
  background-color: var(--kb-yellow);
  color: var(--kb-gray);
  font-weight: 600;
}

/* 5. 파스텔 공통 (로드맵 등) */
.category-btn--pastel-purple,
.category-btn--pastel-blue,
.category-btn--pastel-yellow,
.category-btn--pastel-green,
.category-btn--pastel-pink {
  padding: 6px 1.5vw;
  border-radius: 999px;
  font-size: clamp(11px, 2.8vw, 14px);
  letter-spacing: -0.5px;
  font-weight: 600;
  background-color: var(--pastel-inactive-bg);
  color: var(--surface-default);
  border: none;
}
.category-btn--pastel-purple.is-active {
  background-color: var(--pastel-purple);
}
.category-btn--pastel-blue.is-active {
  background-color: var(--pastel-blue);
}
.category-btn--pastel-yellow.is-active {
  background-color: var(--pastel-yellow);
}
.category-btn--pastel-green.is-active {
  background-color: var(--pastel-green);
}
.category-btn--pastel-pink.is-active {
  background-color: var(--pastel-pink);
}

.category-btn__icon {
  font-size: 15px;
  line-height: 1;
}
</style>
