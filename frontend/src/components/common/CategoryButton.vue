<script setup>
defineProps({
  variant: {
    type: String,
    required: false,
    default: 'oval-yellow',
  },
  active: { type: Boolean, required: false, default: false },
  label: { type: String, required: true, default: '버튼' },
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
  padding: clamp(10px, 2.5vw, 12px) clamp(12px, 4vw, 20px);
  border-radius: 999px;
  font-size: clamp(13px, 3.8vw, 15px);
  font-weight: 500;
  background-color: #f5f5f5;
  color: #9e9e9e;
}
.category-btn--oval-yellow.is-active {
  background-color: #ffbc00; /* KB Yellow Positive */
  color: #60584c; /* KB Gray */
  font-weight: 600;
}

/* 3. oval-green */
.category-btn--oval-green {
  padding: clamp(6px, 2vw, 8px) clamp(4px, 2.5vw, 10px);
  border-radius: 999px;
  font-size: clamp(11px, 3.5vw, 14px);
  letter-spacing: -0.5px;
  background-color: #e0e6dc;
  color: #536349; /* 국방색 */
}
.category-btn--oval-green.is-active {
  background-color: #536349; /* 국방색 */
  color: #ffffff;
  font-weight: 600;
}

/* 4. oval-brown */
.category-btn--oval-brown {
  padding: clamp(8px, 2.5vw, 10px) clamp(12px, 4vw, 20px);
  border-radius: 999px;
  font-size: clamp(13px, 3.8vw, 15px);
  background-color: #f1ede1;
  color: #8d6e52;
}
.category-btn--oval-brown.is-active {
  background-color: #c9a97a;
  color: #ffffff;
  font-weight: 600;
}

/* 2. square-yellow */
.category-btn--square-yellow {
  padding: clamp(10px, 2.5vw, 12px) clamp(12px, 4vw, 20px);
  border-radius: 12px;
  font-size: clamp(13px, 3.8vw, 15px);
  font-weight: 500;
  background-color: #f5f5f5;
  color: #9e9e9e;
}
.category-btn--square-yellow.is-active {
  background-color: #ffbc00; /* KB Yellow Positive */
  color: #60584c; /* KB Gray */
  font-weight: 600;
}

/* 5. 파스텔 공통 (로드맵 등) */
.category-btn--pastel-purple,
.category-btn--pastel-blue,
.category-btn--pastel-yellow,
.category-btn--pastel-green,
.category-btn--pastel-pink {
  padding: 8px 2vw;
  border-radius: 999px;
  font-size: clamp(10px, 2.8vw, 13px);
  letter-spacing: -0.5px;
  font-weight: 600;
  background-color: #c2c2c2;
  color: #ffffff;
  border: none;
}
.category-btn--pastel-purple.is-active {
  background-color: #e2b2fe;
}
.category-btn--pastel-blue.is-active {
  background-color: #5bc3ff;
}
.category-btn--pastel-yellow.is-active {
  background-color: #fbd55b;
}
.category-btn--pastel-green.is-active {
  background-color: #9cd495;
}
.category-btn--pastel-pink.is-active {
  background-color: #f8a5a5;
}

.category-btn__icon {
  font-size: 15px;
  line-height: 1;
}
</style>
