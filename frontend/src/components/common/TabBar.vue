<script setup>
// 공통 컴포넌트: 탭 / 카테고리 토글 (담당: 수연)
// ① 스타일 확정: 각진 카드 탭 + 하단 노랑 + 그림자 (여백O · 선O · 노랑O · 그림자O)
// 상세보기 탭(선택한 목표/비용/금융상품), 큰 분류 선택(학교/지역) 등 "여러 개 중 하나 고르기" 전부 이걸로.
// 사용:
//   <TabBar v-model="activeIdx" :tabs="['선택한 목표', '비용 계산', '금융상품']" />
//   <TabBar v-model="mode" :tabs="[{ label: '학교 근처', value: 'school' }, { label: '지역으로', value: 'region' }]" />
const props = defineProps({
  modelValue: { type: [String, Number], default: 0 },
  // ['라벨', ...] 또는 [{ label, value }, ...]
  tabs: { type: Array, default: () => [] },
  // 'underline' = ① 각진 카드 + 하단 노랑 밑줄 (기본)
  // 'fill'      = ③ 주노랑 채움 · 각진(선택 시 통 노랑, 조금 작게)
  // 'segment'   = 알약형 세그먼트 (회색 트랙 + 흰 알약 활성)
  variant: { type: String, default: 'underline' },
});

const emit = defineEmits(['update:modelValue']);

const valueOf = (tab, idx) =>
  typeof tab === 'object' && tab !== null ? tab.value : idx;
const labelOf = (tab) =>
  typeof tab === 'object' && tab !== null ? tab.label : tab;
</script>

<template>
  <div :class="['tab-bar', `tab-bar--${variant}`]">
    <button
      v-for="(tab, idx) in tabs"
      :key="idx"
      type="button"
      class="tab-bar__tab"
      :class="{ 'is-active': modelValue === valueOf(tab, idx) }"
      @click="emit('update:modelValue', valueOf(tab, idx))"
    >
      {{ labelOf(tab) }}
    </button>
  </div>
</template>

<style scoped>
/* ① 각진 탭 + 하단 노랑 (여백O · 선O · 그림자O) */
.tab-bar {
  display: flex;
  gap: 6px;
}

.tab-bar__tab {
  flex: 1;
  padding: 11px 4px;
  border: 1px solid var(--kb-gray-pale);
  border-bottom: 3px solid transparent;
  border-radius: 8px 8px 4px 4px;
  background: var(--kb-gray-pale); /* 비활성 = 보통 회색 */
  color: var(--btn-gray-text); /* 비활성 글자색 */
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}

.tab-bar__tab.is-active {
  background: var(--surface-default, #fff); /* 활성 = 흰 배경 */
  color: var(--text-strong);
  border-bottom-color: var(--kb-yellow); /* 하단 주 노랑 */
  font-weight: 700;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

/* ③ 주노랑 채움 · 각진(10px) — 선택 시 통 노랑, 밑줄X, 조금 작게 */
.tab-bar--fill .tab-bar__tab {
  padding: 8px 4px;
  border: 0;
  border-radius: 10px;
  font-size: 14px;
}
.tab-bar--fill .tab-bar__tab.is-active {
  background: var(--kb-yellow);
  color: var(--kb-dark-gray);
  border-bottom-color: transparent;
  box-shadow: none;
  font-weight: 700;
}

/* 알약형 세그먼트 — 회색 트랙 + 흰 알약 활성 */
.tab-bar--segment {
  gap: 4px;
  padding: 4px;
  border-radius: 999px;
  background: var(--bg-gray);
}
.tab-bar--segment .tab-bar__tab {
  padding: 9px 4px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  box-shadow: none;
}
.tab-bar--segment .tab-bar__tab.is-active {
  background: var(--surface-default, #fff);
  color: var(--text-strong);
  border-bottom-color: transparent;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
  font-weight: 700;
}
</style>
