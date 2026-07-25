<script setup>
// SCR-RENT-05 · step3) 자취 비용 계산  (담당: 수연 / 데모)
// 총 필요 자금 = 보증금 + (월세+관리비) x 거주개월. 보증금(반환)/월주거비(소멸) 분리.
import { useRoute, useRouter } from 'vue-router';
import StepIndicator from '@/components/common/StepIndicator.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();

// 데모 계산 예시 (부전현대 · 12개월)
const MONTHS = 12;
const deposit = 5000000;              // 보증금 (반환)
const monthlyTotal = (450000 + 100000) * MONTHS; // (월세+관리비) x 개월 (소멸)
const totalRequired = deposit + monthlyTotal;    // 11,600,000
const currentAsset = 20150000;        // 군적금 만기금

const items = [
  { label: '보증금', value: deposit, color: 'var(--chart-1)' },
  { label: '월세', value: 450000 * MONTHS, color: 'var(--chart-2)' },
  { label: '관리비', value: 100000 * MONTHS, color: 'var(--chart-3)' },
];

const won = (n) => n.toLocaleString('ko-KR') + '원';
const pct = (v) => Math.round((v / totalRequired) * 100) + '%';

const goNext = () => router.push({ name: 'RentProducts', params: { goalId: route.params.goalId } });
const save = () => goNext();
</script>

<template>
  <div class="page">
    <StepIndicator :current="3" :total="4" />

    <h2 class="page__title">총 필요 자금</h2>
    <p class="page__total">{{ won(totalRequired) }}</p>

    <div class="chart">
      <DonutChart :items="items" :size="180" />
    </div>

    <ul class="legend">
      <li v-for="it in items" :key="it.label">
        <span class="legend__dot" :style="{ backgroundColor: it.color }"></span>
        <span class="legend__label">{{ it.label }}</span>
        <span class="legend__val">{{ won(it.value) }}</span>
        <span class="legend__pct">{{ pct(it.value) }}</span>
      </li>
    </ul>

    <div class="split">
      <div class="split__box">
        <p class="split__k">보증금 (돌려받음)</p>
        <p class="split__v">{{ won(deposit) }}</p>
      </div>
      <div class="split__box split__box--spend">
        <p class="split__k">월 주거비 (소멸)</p>
        <p class="split__v">{{ won(monthlyTotal) }}</p>
      </div>
    </div>

    <div class="match">
      <span class="match__ok">✓</span>
      군적금 만기금 <b>{{ won(currentAsset) }}</b> 으로 충당 가능해요
    </div>
  </div>

  <BottomButtonBar
    primary-label="다음"
    secondary-label="저장"
    @primary-click="goNext"
    @secondary-click="save"
  />
</template>

<style scoped>
.page {
  padding: 8px 0 96px;
}
.page__title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-muted);
  text-align: center;
}
.page__total {
  margin: 0 0 20px;
  font-size: 28px;
  font-weight: 800;
  color: var(--text-strong);
  text-align: center;
}
.chart {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.legend {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.legend li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.legend__dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}
.legend__label {
  color: var(--text-body);
}
.legend__val {
  margin-left: auto;
  font-weight: 700;
  color: var(--text-strong);
}
.legend__pct {
  width: 40px;
  text-align: right;
  color: var(--text-hint);
}
.split {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.split__box {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  background-color: #f3f7ff;
}
.split__box--spend {
  background-color: #fff4f4;
}
.split__k {
  margin: 0 0 4px;
  font-size: 12px;
  color: var(--text-muted);
}
.split__v {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.match {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 12px;
  background-color: #fffdf3;
  border: 1px solid var(--kb-yellow);
  font-size: 13px;
  color: var(--kb-dark-gray);
}
.match__ok {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  border-radius: 50%;
  background-color: var(--kb-yellow-deep);
  color: #ffffff;
  font-size: 12px;
}
</style>
