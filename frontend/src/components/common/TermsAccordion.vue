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
  // '전체 동의합니다' 상단 박스 노출 여부 (약관이 1개면 숨기는 등)
  showSelectAll: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['update:modelValue']);

const openIds = ref(new Set());

const requiredIds = computed(() =>
  props.terms.filter((term) => term.required).map((term) => term.termsId),
);

// 필수 약관 먼저, 선택 약관 뒤로 묶어서 그룹 사이 간격을 준다
const requiredTerms = computed(() => props.terms.filter((term) => term.required));
const optionalTerms = computed(() => props.terms.filter((term) => !term.required));
const orderedTerms = computed(() => [...requiredTerms.value, ...optionalTerms.value]);
// 선택 그룹 첫 항목 - 위쪽에 간격을 주기 위한 표식
const firstOptionalId = computed(() => optionalTerms.value[0]?.termsId ?? null);

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
    <!-- 전체 동의 (맨 위 유지, showSelectAll=false면 숨김) -->
    <label v-if="showSelectAll" class="terms-accordion__all">
      <input
        type="checkbox"
        class="terms-accordion__checkbox"
        :checked="isAllAgreed"
        @change="toggleAll"
      />
      <span class="terms-accordion__all-label">전체 동의합니다</span>
    </label>

    <ul class="terms-accordion__list">
      <li
        v-for="term in orderedTerms"
        :key="term.termsId"
        class="terms-accordion__item"
        :class="{ 'terms-accordion__item--group-start': term.termsId === firstOptionalId }"
      >
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
                class="terms-accordion__tag"
                :class="term.required ? 'is-required' : 'is-optional'"
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
            <svg
              class="terms-accordion__chevron"
              :class="{ 'is-open': isOpen(term.termsId) }"
              viewBox="0 0 24 24"
              width="18"
              height="18"
              aria-hidden="true"
            >
              <path
                d="M7 10l5 5 5-5"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
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
.terms-accordion__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

/* 체크 표시: 동그란 원 + 체크. 미동의=회색 체크, 동의=노란 원+흰 체크 */
.terms-accordion__checkbox {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  margin: 0;
  border: 1.5px solid #cdd1d6;
  border-radius: 50%;
  appearance: none;
  -webkit-appearance: none;
  background-color: #fff;
  cursor: pointer;
  position: relative;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

/* 항상 체크(✓)를 보여주되, 미동의일 땐 옅은 회색 */
.terms-accordion__checkbox::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 5px;
  height: 9px;
  border: solid #9aa0a7;
  border-width: 0 2px 2px 0;
  transform: translate(-50%, -55%) rotate(45deg);
  transition: border-color 0.15s ease;
}

.terms-accordion__checkbox:checked {
  background-color: var(--kb-yellow-deep, #ffbc00);
  border-color: var(--kb-yellow-deep, #ffbc00);
}

.terms-accordion__checkbox:checked::after {
  border-color: #fff;
}

.terms-accordion__checkbox:focus-visible {
  outline: 2px solid var(--kb-yellow-deep, #ffbc00);
  outline-offset: 2px;
}

/* 전체 동의 - 맨 위 강조 행 (노란 테두리 + 흰 배경) */
.terms-accordion__all {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 6px;
  border: 1.5px solid var(--kb-yellow-deep, #ffbc00);
  border-radius: 12px;
  background-color: #fff;
  cursor: pointer;
}

.terms-accordion__all-label {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

/* 개별 항목: 헤더 밑에 full-width 구분선 */
.terms-accordion__item--group-start {
  margin-top: 36px;
}

.terms-accordion__header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  /* 왼쪽 여백을 전체동의 박스(테두리1.5+패딩16) 체크 위치와 맞춤 */
  padding: 21px 4px 21px 17px;
}

/* 밑줄: 양쪽 끝까지 full-width (좌우 균형) */
.terms-accordion__header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background-color: var(--kb-gold, #85714d);
}

.terms-accordion__row {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-grow: 1;
  margin-bottom: 0;
}

.terms-accordion__row-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-strong);
}

/* 필수/선택 - 알약 배지 대신 옅은 텍스트 */
.terms-accordion__tag {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 500;
}

.terms-accordion__tag.is-required {
  color: var(--kb-yellow-deep, #ffbc00);
}

.terms-accordion__tag.is-optional {
  color: var(--text-muted, #9a9ea4);
}

.terms-accordion__toggle {
  flex-shrink: 0;
  border: none;
  background: transparent;
  padding: 4px;
  line-height: 0;
  color: var(--text-muted);
  cursor: pointer;
}

.terms-accordion__chevron {
  display: block;
  transition: transform 0.2s ease;
}

.terms-accordion__chevron.is-open {
  transform: rotate(180deg);
}

.terms-accordion__content {
  padding: 12px 4px 14px 46px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-line;
}
</style>
