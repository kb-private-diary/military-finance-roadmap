<script setup>
// 공통 컴포넌트: 로드맵 목표 요약 카드 (담당: 수연)
// 상단 도메인색 바 + (아이콘) 칩 + 제목 + (수정) / 서브타이틀 / 알약태그 / (여행)비행경로 or (자동차)예산비교 / 스펙그리드
// 필요한 부분만 넘기면 나머지는 자동 생략 → 도메인별 재사용
import { computed } from 'vue';
import BaseCard from '@/components/common/BaseCard.vue';

const props = defineProps({
  title: { type: String, default: '' },
  chip: { type: String, default: '' }, // 회색 칩 (예: 중고 / 해외여행)
  theme: { type: String, default: 'travel' }, // 상단 바 색 var(--theme-{theme})
  accent: { type: String, default: '' }, // 색 직접 지정 — theme보다 우선
  icon: { type: String, default: '' }, // 앞 아이콘 이미지 src
  subtitle: { type: String, default: '' }, // 제목 아래 한 줄 (예: 준중형 · 경기도 · 운전경력 2년)
  tags: { type: Array, default: () => [] }, // 알약 태그 문자열 배열 (예: ['2021년식', '36,000km'])
  showEdit: { type: Boolean, default: false }, // '수정' 링크 표시
  route: { type: Object, default: null }, // 여행: { from, to, icon }
  // 자동차: { left: {label, value}, right: {label, value}, badge: {text, tone: 'good'|'bad'} }
  compare: { type: Object, default: null },
  specs: { type: Array, default: () => [] }, // [{ label, value }]
});
const emit = defineEmits(['edit']);

const accentColor = computed(() => props.accent || `var(--theme-${props.theme})`);
</script>

<template>
  <BaseCard
    class="goal-summary"
    padding="16px"
    :style="{ '--gs-accent': accentColor }"
  >
    <div class="goal-summary__head">
      <img v-if="icon" class="goal-summary__icon" :src="icon" alt="" />
      <div class="goal-summary__headmain">
        <div class="goal-summary__titlerow">
          <span v-if="chip" class="goal-summary__chip">{{ chip }}</span>
          <span class="goal-summary__title">{{ title }}</span>
        </div>
        <p v-if="subtitle" class="goal-summary__subtitle">{{ subtitle }}</p>
      </div>
      <button
        v-if="showEdit"
        type="button"
        class="goal-summary__edit"
        @click="emit('edit')"
      >
        수정
      </button>
    </div>

    <div v-if="tags.length" class="goal-summary__tags">
      <span v-for="(t, i) in tags" :key="i" class="goal-summary__tag">{{ t }}</span>
    </div>

    <!-- 여행: 비행 경로 패널 -->
    <div v-if="route" class="goal-summary__route">
      <div class="goal-summary__place">
        <img
          v-if="route.from.icon"
          class="goal-summary__place-icon"
          :src="route.from.icon"
          alt=""
        />
        <span class="goal-summary__place-label">{{ route.from.label }}</span>
        <span class="goal-summary__place-value">{{ route.from.value }}</span>
      </div>
      <div class="goal-summary__path" aria-hidden="true">
        <span v-if="route.icon" class="goal-summary__path-icon">{{
          route.icon
        }}</span>
      </div>
      <div class="goal-summary__place">
        <img
          v-if="route.to.icon"
          class="goal-summary__place-icon"
          :src="route.to.icon"
          alt=""
        />
        <span class="goal-summary__place-label">{{ route.to.label }}</span>
        <span class="goal-summary__place-value">{{ route.to.value }}</span>
      </div>
    </div>

    <div v-if="specs.length" class="goal-summary__specs">
      <div v-for="(s, i) in specs" :key="i" class="goal-summary__spec">
        <span class="goal-summary__spec-label">{{ s.label }}</span>
        <span class="goal-summary__spec-value">{{ s.value }}</span>
      </div>
    </div>

    <!-- 자동차: 예산 vs 실제값 비교 (스펙 아래, 배경 없이) + 여유/부족 텍스트 -->
    <template v-if="compare">
      <div class="goal-summary__compare">
        <div class="goal-summary__cmp-side">
          <img
            v-if="compare.left.icon"
            class="goal-summary__cmp-icon"
            :src="compare.left.icon"
            alt=""
          />
          <span class="goal-summary__cmp-label">{{ compare.left.label }}</span>
          <span class="goal-summary__cmp-value">{{ compare.left.value }}</span>
        </div>
        <div class="goal-summary__cmp-mid">
          <span
            v-if="compare.badge"
            class="goal-summary__note"
            :class="`goal-summary__note--${compare.badge.tone}`"
            >{{ compare.badge.text }}</span
          >
          <span class="goal-summary__cmp-sep" aria-hidden="true">vs</span>
        </div>
        <div class="goal-summary__cmp-side">
          <img
            v-if="compare.right.icon"
            class="goal-summary__cmp-icon"
            :src="compare.right.icon"
            alt=""
          />
          <span class="goal-summary__cmp-label">{{ compare.right.label }}</span>
          <span
            class="goal-summary__cmp-value"
            :class="
              compare.badge ? `goal-summary__cmp-value--${compare.badge.tone}` : ''
            "
            >{{ compare.right.value }}</span
          >
        </div>
      </div>
    </template>
  </BaseCard>
</template>

<style scoped>
.goal-summary {
  border-top: 4px solid var(--gs-accent);
  overflow: hidden;
}
.goal-summary__head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.goal-summary__icon {
  flex: none;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  object-fit: contain;
  background: var(--kb-yellow-pale);
  padding: 6px;
}
.goal-summary__headmain {
  flex: 1;
  min-width: 0;
}
.goal-summary__titlerow {
  display: flex;
  align-items: center;
  gap: 8px;
}
.goal-summary__chip {
  flex: none;
  padding: 3px 9px;
  border-radius: 6px;
  background: #9d9d9d;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}
.goal-summary__title {
  min-width: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.goal-summary__subtitle {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--text-hint);
}
.goal-summary__edit {
  flex: none;
  align-self: flex-start;
  border: 0;
  background: transparent;
  color: var(--kb-yellow-deep);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}
.goal-summary__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}
.goal-summary__tag {
  padding: 5px 11px;
  border-radius: 8px;
  background: var(--kb-yellow-pale);
  color: var(--kb-dark-gray);
  font-size: 13px;
  font-weight: 600;
}
.goal-summary__route {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  background: linear-gradient(180deg, #eaf6fe 0%, #fcfeff 100%);
}
.goal-summary__place {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.goal-summary__place-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
  margin-bottom: 2px;
}
.goal-summary__place-label {
  font-size: 12px;
  color: var(--text-hint);
}
.goal-summary__place-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.goal-summary__path {
  position: relative;
  flex: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 22px;
}
.goal-summary__path::before {
  content: '';
  position: absolute;
  left: 2px;
  right: 2px;
  top: 50%;
  border-top: 2px dashed var(--tag-gray-mid);
  transform: translateY(-50%);
}
.goal-summary__path-icon {
  position: relative;
  z-index: 1;
  padding: 0 6px;
  font-size: 17px;
  background: var(--surface-default);
}
/* 자동차: 예산 vs 실제값 비교 (스펙 아래, 배경 없이 · 위 구분선) */
.goal-summary__compare {
  display: flex;
  align-items: stretch;
  gap: 8px;
  margin-top: 16px;
  padding: 16px 16px 0;
  border-top: 1px solid var(--divider-thin);
}
.goal-summary__cmp-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
/* 가운데 열: 여유(위·아이콘 높이) + vs(아래·값 높이) */
.goal-summary__cmp-mid {
  flex: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}
.goal-summary__cmp-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
  margin-bottom: 4px;
}
.goal-summary__cmp-label {
  font-size: 12px;
  color: var(--text-hint);
}
.goal-summary__cmp-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
/* 실제 차값: 여유=초록 / 부족=빨강 */
.goal-summary__cmp-value--good {
  color: #1a7a54;
}
.goal-summary__cmp-value--bad {
  color: #e08828;
}
.goal-summary__cmp-sep {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 700;
}
/* 예산 여유/부족 — 아이콘 사이(가운데 위). 굵은 컬러 글자 */
.goal-summary__note {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}
/* 여유: 초록 */
.goal-summary__note--good {
  color: #1a7a54;
}
/* 부족: 주황 (게이지와 동일 톤) */
.goal-summary__note--bad {
  color: #e08828;
}
.goal-summary__specs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 14px;
  padding: 0 16px;
}
.goal-summary__spec {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.goal-summary__spec-label {
  font-size: 12px;
  color: var(--text-hint);
}
.goal-summary__spec-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
</style>
