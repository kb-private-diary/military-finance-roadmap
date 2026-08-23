<script setup>
// 공통 컴포넌트: 비용 구성 비율 막대그래프 + 범례 (담당: 수연)
// 자취 "N개월 거주 한다면?" 비율바 기반 (토글 제외). 항목별 색 막대 + 리스트(색점·이름·금액·%)
// 사용:
//   <CostBreakdown
//     title="비용 구성"
//     :items="[{ label: '숙소비', amount: '52만원', percent: 42, color: '#6b5b4d' }, ...]"
//     hint="예상 공과금까지 계산해 더 정확한 예산을 알려드려요" />
import BaseCard from '@/components/common/BaseCard.vue';

defineProps({
  title: { type: String, default: '' },
  // [{ label, amount(표시용 문자열), percent(숫자), color(css color) }]
  items: { type: Array, default: () => [] },
  hint: { type: String, default: '' },
});
</script>

<template>
  <div class="cost-breakdown">
    <BaseCard padding="14px 16px 16px">
      <p v-if="title" class="cost-breakdown__title">{{ title }}</p>

      <div class="cost-breakdown__bar">
        <div
          v-for="(it, i) in items"
          :key="i"
          class="cost-breakdown__seg"
          :style="{ width: `${it.percent}%`, background: it.color }"
        ></div>
      </div>

      <ul class="cost-breakdown__list">
        <li v-for="(it, i) in items" :key="i">
          <span class="cost-breakdown__dot" :style="{ background: it.color }"></span>
          <span class="cost-breakdown__name">{{ it.label }}</span>
          <span class="cost-breakdown__amt">{{ it.amount }}</span>
          <span class="cost-breakdown__pct">{{ it.percent }}%</span>
        </li>
      </ul>
    </BaseCard>

    <p v-if="hint" class="cost-breakdown__hint">💡 {{ hint }}</p>
  </div>
</template>

<style scoped>
.cost-breakdown__title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.cost-breakdown__bar {
  display: flex;
  width: 100%;
  height: 14px;
  border-radius: 999px;
  overflow: hidden;
  background: var(--kb-gray-pale);
}
.cost-breakdown__seg {
  height: 100%;
}
.cost-breakdown__list {
  list-style: none;
  margin: 14px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cost-breakdown__list li {
  display: flex;
  align-items: center;
  font-size: 13px;
}
.cost-breakdown__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;
  flex: none;
}
.cost-breakdown__name {
  color: var(--text-body);
}
.cost-breakdown__amt {
  margin-left: auto;
  font-weight: 700;
  color: var(--text-strong);
}
.cost-breakdown__pct {
  margin-left: 10px;
  min-width: 38px;
  font-weight: 700;
  color: #f0a500;
  text-align: right;
}
.cost-breakdown__hint {
  margin: 12px 0 0;
  text-align: center;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.6;
}
</style>
