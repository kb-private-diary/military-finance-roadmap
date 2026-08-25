<script setup>
// 공통 컴포넌트: 예산 대비 반원 게이지 (담당: 수연)
// 가운데 금액 + 남음/부족(게이지색) + 0~예산 축.
import { computed } from 'vue';
import moneyLeftIcon from '@/assets/images/money-left.png';
import moneyNeedIcon from '@/assets/images/money-need.png';

const props = defineProps({
  amount: { type: Number, required: true }, // 예상 금액 (만원)
  budget: { type: Number, required: true }, // 내 예산 (만원)
  unit: { type: String, default: '만원' },
  // 중앙 큰 숫자·단위를 직접 지정 (null이면 amount·unit 사용). 예: 저축진행률(33 / %)
  centerValue: { type: [Number, String], default: null },
  centerUnit: { type: String, default: null },
  // 남음/필요 줄 표시 여부
  showDiff: { type: Boolean, default: true },
  // '예산의 X%' 줄 표시 여부
  showPct: { type: Boolean, default: true },
});

const ARC_LEN = Math.PI * 100; // 반원 호 길이 (r=100)

const usage = computed(() =>
  props.budget ? Math.round((props.amount / props.budget) * 100) : 0,
);
const fillLen = computed(() => (Math.min(Math.max(usage.value, 0), 100) / 100) * ARC_LEN);
const diff = computed(() => props.budget - props.amount); // +남음 / -부족
const isOver = computed(() => diff.value < 0);
const diffPct = computed(() =>
  props.budget ? Math.round((Math.abs(diff.value) / props.budget) * 100) : 0,
);
const diffText = computed(() =>
  isOver.value
    ? `${Math.abs(diff.value).toLocaleString()}${props.unit} 필요`
    : `${diff.value.toLocaleString()}${props.unit} 남음`,
);
</script>

<template>
  <div class="gauge">
    <div class="gauge__arc">
      <svg viewBox="0 0 240 130" role="img" :aria-label="`예산의 ${usage}% 사용`">
        <path
          class="gauge__track"
          d="M 20 120 A 100 100 0 0 1 220 120"
          fill="none"
          stroke-width="18"
          stroke-linecap="round"
        />
        <path
          class="gauge__fill"
          :class="{ 'gauge__fill--over': isOver }"
          d="M 20 120 A 100 100 0 0 1 220 120"
          fill="none"
          stroke-width="18"
          stroke-linecap="round"
          :stroke-dasharray="`${fillLen} ${ARC_LEN}`"
        />
      </svg>
      <div class="gauge__center">
        <div class="gauge__amt">
          {{ centerValue != null ? centerValue : amount.toLocaleString()
          }}<span>{{ centerUnit != null ? centerUnit : unit }}</span>
        </div>
        <div
          v-if="showDiff"
          class="gauge__sub"
          :class="{ 'gauge__sub--over': isOver }"
        >
          <img
            class="gauge__sub-icon"
            :src="isOver ? moneyNeedIcon : moneyLeftIcon"
            alt=""
          />
          <span>{{ diffText }}</span>
        </div>
        <div v-if="showPct" class="gauge__pct">예산의 {{ diffPct }}%</div>
      </div>
    </div>

    <div class="gauge__axis">
      <span>0{{ unit }}</span>
      <span>{{ budget.toLocaleString() }}{{ unit }}</span>
    </div>
  </div>
</template>

<style scoped>
.gauge {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.gauge__arc {
  position: relative;
  width: 100%;
  max-width: 260px;
}
.gauge__arc svg {
  display: block;
  width: 100%;
  height: auto;
}
.gauge__track {
  stroke: var(--kb-gray-pale);
}
.gauge__fill {
  stroke: var(--kb-yellow-deep);
  transition: stroke-dasharray 0.4s ease;
}
.gauge__fill--over {
  stroke: #f0a030;
}
.gauge__center {
  position: absolute;
  left: 0;
  right: 0;
  top: 45%;
  text-align: center;
}
.gauge__amt {
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-strong);
}
.gauge__amt span {
  margin-left: 2px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-muted);
}
/* 남음/필요 — 아이콘 + 진한 골드(남음) / 주황(필요) */
.gauge__sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #c8890a;
}
.gauge__sub-icon {
  width: 16px;
  height: 16px;
  object-fit: contain;
}
.gauge__sub--over {
  color: #e08828;
}
/* 예산 % — 회색, 남음 밑 */
.gauge__pct {
  margin-top: 3px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
}
.gauge__axis {
  display: flex;
  justify-content: space-between;
  width: 100%;
  max-width: 260px;
  margin-top: 1px;
  padding: 0 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
}
</style>
