<script setup>
// 공통 컴포넌트: 카테고리 필터 버튼 (확정 #6) — 담당: 수연
// 선택 = 도메인 색 + 흰 글자 / 미선택 = 회색 + 진회색 글자, pill 형태 5등분
// 사용: <CategoryFilter v-model="selected" :categories="[{ code, label, theme }]" />
//   theme: roadmap | travel | rent | car | job (→ var(--theme-{theme}))
defineProps({
  modelValue: { type: String, default: '' },
  categories: { type: Array, default: () => [] }, // [{ code, label, theme }]
});

const emit = defineEmits(['update:modelValue']);
</script>

<template>
  <div class="category-filter">
    <button
      v-for="c in categories"
      :key="c.code"
      type="button"
      class="category-filter__btn"
      :style="
        modelValue === c.code
          ? { background: `var(--theme-${c.theme})`, color: '#fff' }
          : {}
      "
      @click="emit('update:modelValue', c.code)"
    >
      {{ c.label }}
    </button>
  </div>
</template>

<style scoped>
.category-filter {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 1fr; /* 항목 수만큼 균등 분할 */
  gap: 7px;
  width: 100%;
}

/* 미선택 = 보통 회색 + 진회색 글자 / 선택 = 도메인 색 + 흰글자(인라인 style) */
.category-filter__btn {
  padding: 5px 4px;
  border: 0;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  color: var(--btn-gray-text);
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
  cursor: pointer;
  font-family: inherit;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}
</style>
