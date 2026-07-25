<script setup>
// 공통 컴포넌트: 약관 동의 아코디언 (담당: 호빈)
// 체크박스로 동의 여부를 표시하고, 화살표를 눌러 약관 본문을 펼쳐볼 수 있다.
import { ref, computed } from 'vue';

const props = defineProps({
  terms: {
    type: Array, // [{ termsId, name, required, content, version }]
    default: () => [],
  },
  modelValue: {
    type: Array, // 동의한 termsId 목록
    default: () => [],
  },
});

const emit = defineEmits(['update:modelValue']);

const openIds = ref(new Set());

const requiredIds = computed(() =>
  props.terms.filter((term) => term.required).map((term) => term.termsId),
);

const isAgreed = (termsId) => props.modelValue.includes(termsId);

const isAllAgreed = computed(
  () => props.terms.length > 0 && props.terms.every((term) => isAgreed(term.termsId)),
);

const toggleTerm = (termsId) => {
  const next = isAgreed(termsId)
    ? props.modelValue.filter((id) => id !== termsId)
    : [...props.modelValue, termsId];
  emit('update:modelValue', next);
};

const toggleAll = () => {
  emit('update:modelValue', isAllAgreed.value ? [] : props.terms.map((term) => term.termsId));
};

const toggleOpen = (termsId) => {
  const next = new Set(openIds.value);
  next.has(termsId) ? next.delete(termsId) : next.add(termsId);
  openIds.value = next;
};

const isOpen = (termsId) => openIds.value.has(termsId);

defineExpose({ requiredIds });
</script>

<template>
  <div class="terms-accordion">
    <label class="terms-accordion__all">
      <input
        type="checkbox"
        class="terms-accordion__checkbox"
        :checked="isAllAgreed"
        @change="toggleAll"
      />
      <span class="terms-accordion__all-label">전체 동의합니다</span>
    </label>

    <ul class="terms-accordion__list list-unstyled mb-0">
      <li v-for="term in terms" :key="term.termsId" class="terms-accordion__item">
        <div class="terms-accordion__header">
          <label class="terms-accordion__row">
            <input
              type="checkbox"
              class="terms-accordion__checkbox"
              :checked="isAgreed(term.termsId)"
              @change="toggleTerm(term.termsId)"
            />
            <span class="terms-accordion__row-label">
              {{ term.name }}
              <span
                class="badge rounded-pill ms-1"
                :class="term.required ? 'text-bg-warning' : 'text-bg-secondary'"
              >
                {{ term.required ? '필수' : '선택' }}
              </span>
            </span>
          </label>
          <button
            type="button"
            class="terms-accordion__toggle"
            :aria-expanded="isOpen(term.termsId)"
            :aria-label="`${term.name} 본문 ${isOpen(term.termsId) ? '접기' : '펼치기'}`"
            @click="toggleOpen(term.termsId)"
          >
            <span class="terms-accordion__chevron" :class="{ 'is-open': isOpen(term.termsId) }">
              ▾
            </span>
          </button>
        </div>
        <div v-show="isOpen(term.termsId)" class="terms-accordion__content">
          {{ term.content }}
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
/* 체크박스: 브라우저 기본 렌더링을 끄고 크기·정렬을 직접 통일한다 (항목마다 다르게 보이던 문제 수정) */
.terms-accordion__checkbox {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  margin: 0;
  border: 1.5px solid var(--line-strong, #d0d0d0);
  border-radius: 5px;
  appearance: none;
  -webkit-appearance: none;
  background-color: #fff;
  cursor: pointer;
  position: relative;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

.terms-accordion__checkbox:checked {
  background-color: var(--kb-yellow-deep, #ffbc00);
  border-color: var(--kb-yellow-deep, #ffbc00);
}

.terms-accordion__checkbox:checked::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.terms-accordion__checkbox:focus-visible {
  outline: 2px solid var(--kb-yellow-deep, #ffbc00);
  outline-offset: 2px;
}

.terms-accordion__all {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.75rem 1rem;
  margin-bottom: 0.75rem;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.5rem;
  background-color: #fff8e6;
  cursor: pointer;
}

.terms-accordion__all-label {
  font-weight: 600;
}

.terms-accordion__item {
  border: 1px solid var(--bs-border-color);
  border-radius: 0.5rem;
  margin-bottom: 0.5rem;
  overflow: hidden;
}

.terms-accordion__header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
}

.terms-accordion__row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  cursor: pointer;
  flex-grow: 1;
  margin-bottom: 0;
}

.terms-accordion__toggle {
  border: none;
  background: transparent;
  padding: 0.25rem;
  line-height: 1;
  color: var(--bs-secondary-color);
}

.terms-accordion__chevron {
  display: inline-block;
  transition: transform 0.15s ease;
}

.terms-accordion__chevron.is-open {
  transform: rotate(180deg);
}

.terms-accordion__content {
  padding: 0 1rem 1rem 2.5rem;
  color: var(--bs-secondary-color);
  font-size: 0.9rem;
  white-space: pre-line;
}
</style>
