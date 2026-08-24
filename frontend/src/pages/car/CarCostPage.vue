<script setup>
// SCR-CAR-03 · step3) 자동차 비용 계산  (담당: 호빈)
// step3 - 취득세 포함 구매비용 + 연간 유지비 + 3년 총비용
// 예상 비용 내역: 탭(한 달 기준 / 3년 기준) + 물통 + 리스트 (EstimatedCostCard)
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import EstimatedCostCard from '@/components/common/EstimatedCostCard.vue';
import GoalSummaryCard from '@/components/common/GoalSummaryCard.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import carBudgetIcon from '@/assets/images/car-budget.png';
import carPriceIcon from '@/assets/images/car-price.png';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = computed(() => Number(route.params.goalId));

const currentStep = 3;

const goal = ref(null);
const budgetStatus = ref(null); // 만기금 기준 예산(수동 예산 없을 때 폴백)
const purchase = ref(null); // { price, tax }
const maintenanceCost = ref(null);
const loading = ref(true);
const loadError = ref('');

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const purchaseTotal = computed(() => (purchase.value?.price ?? 0) + (purchase.value?.tax ?? 0));

const MAINTENANCE_MONTHS = 36;

// 4색(사용자 지정): 자동차세 → 정비 → 보험료 → 연료비 (연할수록 작은 항목)
const CAT_COLORS = {
  tax: '#FFECBE',
  repair: '#DAC183',
  insurance: '#B39D89',
  fuel: '#6E6053',
};
const INK_DARK = '#5b4b2e';
const INK_LIGHT = '#ffffff';

const mc = () => maintenanceCost.value || {};
const insuranceMidAnnual = computed(() => {
  const min = mc().insurancePremiumMin ?? 0;
  const max = mc().insurancePremiumMax ?? 0;
  return (min + max) / 2;
});
const monthlyOf = (annual) => Math.round((annual ?? 0) / 12);
const monthlyFuel = computed(() => monthlyOf(mc().estimatedFuelCostAnnual));
const monthlyInsurance = computed(() => monthlyOf(insuranceMidAnnual.value));
const monthlyRepair = computed(() => monthlyOf(mc().estimatedRepairCostAnnual));
const monthlyTax = computed(() => monthlyOf(mc().annualVehicleTaxAfterDiscount));
const monthlyTotal = computed(
  () =>
    monthlyFuel.value + monthlyInsurance.value + monthlyRepair.value + monthlyTax.value,
);
const maint3yTotal = computed(() => monthlyTotal.value * MAINTENANCE_MONTHS);

// 탭① 한 달 기준 — 유지비 4개 항목 (위→아래: 작은 것부터, 물통 바닥에 큰 항목)
const monthlyData = computed(() => {
  if (!maintenanceCost.value) return null;
  const g = goal.value || {};
  const carType = CAR_TYPE_LABEL[g.carTypeCode] || '';
  const total = monthlyTotal.value || 1;
  const pct = (m) => Math.round((m / total) * 100);
  const items = [
    { label: '자동차세', sub: `${carType} 기준`, monthly: monthlyTax.value, color: CAT_COLORS.tax, ink: INK_DARK },
    { label: '정비', sub: '소모품·정기점검', monthly: monthlyRepair.value, color: CAT_COLORS.repair, ink: INK_DARK },
    { label: '보험료', sub: `경력 ${g.experienceYears ?? 0}년 · ${g.region ?? ''}`, monthly: monthlyInsurance.value, color: CAT_COLORS.insurance, ink: INK_LIGHT },
    { label: '연료비', sub: mc().fuelType ? `${mc().fuelType} 기준` : '', monthly: monthlyFuel.value, color: CAT_COLORS.fuel, ink: INK_LIGHT, showName: true },
  ].map((it) => ({
    ...it,
    percent: pct(it.monthly),
    amountText: formatManwonUnit(it.monthly),
    showName: it.showName || false,
  }));
  return {
    headLabel: '월 예상 유지비',
    amount: monthlyTotal.value,
    unit: '만원',
    subNote: `${carType} · 운전경력 ${g.experienceYears ?? 0}년 기준`,
    items,
  };
});

// 탭② 3년 기준 — 유지비 + 구매 비용 2개 층 (차값·취득세는 들여쓰기)
const yearlyData = computed(() => {
  if (!maintenanceCost.value || !purchase.value) return null;
  const buy = purchaseTotal.value;
  const keep = maint3yTotal.value;
  const total = buy + keep || 1;
  const pct = (v) => Math.round((v / total) * 100);
  const monthlyMin =
    monthlyFuel.value + monthlyOf(mc().insurancePremiumMin) + monthlyRepair.value + monthlyTax.value;
  const monthlyMax =
    monthlyFuel.value + monthlyOf(mc().insurancePremiumMax) + monthlyRepair.value + monthlyTax.value;
  return {
    headLabel: '3년 총 예상 비용',
    amount: Math.round(total),
    unit: '만원',
    range: `${formatManwonUnit(buy + monthlyMin * MAINTENANCE_MONTHS)} ~ ${formatManwonUnit(buy + monthlyMax * MAINTENANCE_MONTHS)}`,
    items: [
      { label: '유지비', sub: `월 ${monthlyTotal.value}만원 × ${MAINTENANCE_MONTHS}개월`, percent: pct(keep), color: 'var(--kb-yellow)', ink: 'var(--kb-dark-gray)', showName: true, amountText: formatManwonUnit(keep) },
      { label: '구매 비용', sub: '살 때 딱 한 번', percent: pct(buy), color: CAT_COLORS.fuel, ink: INK_LIGHT, showName: true, amountText: formatManwonUnit(buy) },
    ],
    nested: [
      { label: '시세', value: formatManwonUnit(purchase.value.price) },
      { label: '취득세', value: formatManwonUnit(purchase.value.tax) },
    ],
  };
});

// 탭(카드 밖): 한 달 기준 / 3년 기준
const COST_TABS = [
  { label: '한 달 기준', value: 'monthly' },
  { label: '3년 기준', value: 'yearly' },
];
const costTab = ref('monthly');
const activeCostState = computed(() =>
  costTab.value === 'monthly' ? monthlyData.value : yearlyData.value,
);

// 목표 요약 카드 (GoalSummaryCard)
const CAR_TYPE_LABEL = { 1: '경차', 2: '준중형', 3: 'SUV' };
const summaryChip = computed(() =>
  goal.value ? (goal.value.isNew ? '신차' : '중고') : '',
);
// 차 정보 스펙 — 차종 · 연식 · 주행거리 · 시세 · 취득세 · 공채매입 (경차는 공채 면제)
const summarySpecs = computed(() => {
  const g = goal.value;
  const p = purchase.value;
  if (!g || !p) return [];
  const specs = [];
  if (g.carTypeCode) {
    specs.push({ label: '차종', value: CAR_TYPE_LABEL[g.carTypeCode] });
  }
  if (!g.isNew && g.selectedYear) {
    specs.push({ label: '연식', value: `${g.selectedYear}년식` });
  }
  if (!g.isNew && g.selectedMileageKm) {
    specs.push({
      label: '주행거리',
      value: `${g.selectedMileageKm.toLocaleString()}km`,
    });
  }
  specs.push({ label: '시세', value: formatManwonUnit(p.price) });
  specs.push({ label: '취득세', value: formatManwonUnit(p.tax) });
  specs.push({
    label: '공채매입',
    value: g.carTypeCode === 1 ? '면제 대상' : formatManwonUnit(p.bond ?? 0),
  });
  return specs;
});
// 예산 비교: 내 예산 vs 실제 차값(구매비용) → 여유/부족 뱃지
//   수동 예산(g.budget)이 있으면 그 값, 없으면 군적금 만기금(effectiveBudget)을 예산 기준으로 사용
const summaryCompare = computed(() => {
  const g = goal.value;
  const hasManual = !!g?.budget;
  const budget = hasManual ? g.budget : budgetStatus.value?.effectiveBudget;
  if (!g || !budget) return null;
  const price = Math.round(purchaseTotal.value);
  const diff = budget - price;
  return {
    left: {
      label: hasManual ? '내 예산' : '예상 만기금',
      value: `${budget.toLocaleString()}만`,
      icon: carBudgetIcon,
    },
    right: {
      label: '실제 차값',
      value: `${price.toLocaleString()}만`,
      icon: carPriceIcon,
    },
    badge:
      diff >= 0
        ? { text: `${diff.toLocaleString()}만 여유`, tone: 'good' }
        : { text: `${(-diff).toLocaleString()}만 필요`, tone: 'bad' },
  };
});

const loadCost = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const goalResult = await carApi.findGoalDetail(goalId.value);
    goal.value = unwrap(goalResult);

    // 예산 비교 카드용: 수동 예산이 없으면 군적금 만기금(effectiveBudget)을 예산 기준으로 사용
    try {
      const budgetResult = await carApi.findBudgetStatus(goalId.value);
      budgetStatus.value = unwrap(budgetResult);
    } catch {
      budgetStatus.value = null; // 만기금 조회 실패해도 비용 화면은 계속
    }

    const maintenanceResult = await carApi.findMaintenanceCost(goalId.value);
    maintenanceCost.value = unwrap(maintenanceResult);

    if (goal.value.isNew) {
      const taxResult = await carApi.findAcquisitionTax(goalId.value);
      const tax = unwrap(taxResult);
      purchase.value = {
        price: tax.vehiclePrice,
        tax: tax.acquisitionTaxAmount,
        bond: tax.bondPurchaseAmount,
      };
    } else {
      const usedResult = await carApi.findUsedPrice(goalId.value);
      const used = unwrap(usedResult);
      purchase.value = {
        price: used.estimatedUsedPrice,
        tax: used.acquisitionTaxAmount,
        bond: used.bondPurchaseAmount,
      };
    }
  } catch (error) {
    loadError.value = readErrorMessage(error, '비용 정보를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

onMounted(loadCost);

const handleNext = () => {
  router.push({ name: 'CarProducts', params: { goalId: goalId.value } });
};

const handlePrev = () => {
  router.back();
};
</script>

<template>
  <div class="car-cost">
    <RoadmapCharacterSlider :step="currentStep" label="자동차 로드맵" />

    <div v-if="loading" class="status-box text-caption" role="status">
      예상 비용을 계산하고 있습니다.
    </div>

    <div
      v-else-if="loadError"
      class="status-box status-box--error text-caption"
      role="alert"
    >
      <p>{{ loadError }}</p>
      <button type="button" @click="handlePrev">이전 화면으로</button>
    </div>

    <template v-else>
      <PageHeader
        title="자동차 로드맵의 예상 비용 내역입니다."
        description="자동차 구매 시, 3년 간 유지비를 분석해드립니다."
      />

      <GoalSummaryCard
        v-if="goal"
        accent="var(--kb-yellow)"
        class="car-cost__summary"
        :chip="summaryChip"
        :title="goal.selectedModelName || '자동차 목표'"
        :compare="summaryCompare"
        :specs="summarySpecs"
      />

      <div class="car-cost__tabs">
        <TabBar variant="segment" v-model="costTab" :tabs="COST_TABS" />
      </div>
      <EstimatedCostCard
        v-if="activeCostState"
        class="car-cost__cost"
        :state="activeCostState"
        :dividers="false"
        note=""
      />
    </template>

    <BottomButtonBar
      v-if="!loading && !loadError"
      primary-label="다음"
      secondary-label="이전"
      @primary-click="handleNext"
      @secondary-click="handlePrev"
    />
  </div>
</template>

<style scoped>
.car-cost {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.car-cost :deep(.character-slider) {
  margin-bottom: 28px;
}

.car-cost__summary {
  margin: 20px 0;
}

.car-cost__tabs {
  margin-bottom: 16px;
}
.car-cost__cost {
  margin-top: 4px;
}

.status-box {
  margin-top: 80px;
  color: var(--text-muted);
  text-align: center;
}

.status-box--error {
  color: var(--danger);
}

.status-box p {
  margin: 0 0 16px;
}

.status-box button {
  padding: 9px 16px;
  border: 0;
  background: var(--military-green);
  color: var(--surface-default);
  font-family: inherit;
  font-size: 12px;
}

.car-cost :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.car-cost :deep(.bottom-button-bar .bar-button.secondary) {
  background: var(--kb-gray-pale);
  color: var(--text-body);
}

.car-cost :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}
</style>
