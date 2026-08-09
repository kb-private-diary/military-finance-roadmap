<script setup>
// SCR-CAR-03 · step3) 자동차 비용 계산  (담당: 호빈)
// step3 - 취득세 포함 구매비용 + 연간 유지비 + 3년 총비용
// UI는 여행 로드맵 비용 페이지(TravelCostPage)와 동일한 형식(도넛차트+범례+막대그래프)으로 맞춤
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = computed(() => Number(route.params.goalId));

const currentStep = 3;

const goal = ref(null);
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
const maintenanceTotalMin = computed(() => maintenanceCost.value?.totalMaintenanceCostMin ?? 0);
const maintenanceTotalMax = computed(() => maintenanceCost.value?.totalMaintenanceCostMax ?? 0);
const threeYearTotalMin = computed(() => purchaseTotal.value + maintenanceTotalMin.value * 3);
const threeYearTotalMax = computed(() => purchaseTotal.value + maintenanceTotalMax.value * 3);

// 도넛차트는 하나의 값만 받을 수 있어 보험료는 min~max 중간값으로 대표한다.
const insuranceMidAnnual = computed(() => {
  const min = maintenanceCost.value?.insurancePremiumMin ?? 0;
  const max = maintenanceCost.value?.insurancePremiumMax ?? 0;
  return (min + max) / 2;
});
const fuelTaxAnnual = computed(
  () =>
    (maintenanceCost.value?.estimatedFuelCostAnnual ?? 0) +
    (maintenanceCost.value?.annualVehicleTaxAfterDiscount ?? 0),
);

const costItems = computed(() => [
  { label: '구매비용', value: purchaseTotal.value, color: 'var(--chart-1)' },
  { label: '세금·연료비', value: fuelTaxAnnual.value * 3, color: 'var(--chart-3)' },
  { label: '보험료', value: insuranceMidAnnual.value * 3, color: 'var(--chart-4)' },
]);

const chartTotal = computed(() =>
  costItems.value.reduce((sum, item) => sum + item.value, 0),
);

const percentOf = (value) => {
  if (!chartTotal.value) return 0;
  return Math.round((value / chartTotal.value) * 100);
};

const loadCost = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const goalResult = await carApi.findGoalDetail(goalId.value);
    goal.value = unwrap(goalResult);

    const maintenanceResult = await carApi.findMaintenanceCost(goalId.value);
    maintenanceCost.value = unwrap(maintenanceResult);

    if (goal.value.isNew) {
      const taxResult = await carApi.findAcquisitionTax(goalId.value);
      const tax = unwrap(taxResult);
      purchase.value = { price: tax.vehiclePrice, tax: tax.acquisitionTaxAmount };
    } else {
      const usedResult = await carApi.findUsedPrice(goalId.value);
      const used = unwrap(usedResult);
      purchase.value = { price: used.estimatedUsedPrice, tax: used.acquisitionTaxAmount };
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
      <h2 class="car-cost__title text-title">
        3년간 예상되는<br />총 준비 비용입니다.
      </h2>
      <p class="car-cost__total">
        {{ formatManwonUnit(threeYearTotalMin) }}~{{ formatManwonUnit(threeYearTotalMax) }}
      </p>

      <BaseCard
        class="cost-card"
        padding="29px 22px 31px"
        aria-label="자동차 비용 상세"
      >
        <div class="chart-area">
          <DonutChart
            :items="costItems"
            :size="174"
            :thickness="38"
            chart-label="구매비용, 세금·연료비, 보험료 비율"
          />

          <ul class="legend">
            <li v-for="item in costItems" :key="item.label">
              <span
                class="legend__swatch"
                :style="{ backgroundColor: item.color }"
              />
              <span class="text-caption">{{ item.label }}</span>
            </li>
          </ul>
        </div>

        <ul class="breakdown">
          <li v-for="item in costItems" :key="item.label">
            <span class="breakdown__label text-caption">{{ item.label }}</span>
            <ProgressBar
              :value="item.value"
              :total="chartTotal"
              :color="item.color"
              :height="6"
            />
            <strong>{{ percentOf(item.value) }}%</strong>
          </li>
        </ul>

        <p class="cost-card__hint">구매 비용 + 3년치 세금·연료비·보험료 기준</p>
        <p class="cost-card__notice">상세 요금은 저장을 완료하면 보여줍니다.</p>
      </BaseCard>
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

.car-cost__title {
  margin: 0 0 14px;
  line-height: 1.35;
  text-align: center;
}

.car-cost__total {
  margin: 0 0 18px;
  color: var(--kb-gray);
  font-size: 27px;
  font-weight: 500;
  line-height: 1.25;
  text-align: center;
}

.cost-card {
  border-radius: 13px;
}

.chart-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 23px;
  margin-bottom: 36px;
}

.legend,
.breakdown {
  margin: 0;
  padding: 0;
  list-style: none;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend li {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-body);
  font-size: 11px;
  white-space: nowrap;
}

.legend__swatch {
  width: 12px;
  height: 12px;
  flex: 0 0 12px;
}

.breakdown {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.breakdown li {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr) 38px;
  align-items: center;
  gap: 9px;
  color: var(--kb-gray);
  font-size: 12px;
}

.breakdown__label {
  font-weight: 500;
}

.breakdown strong {
  color: var(--text-body);
  font-size: 12px;
  font-weight: 600;
  text-align: right;
}

.cost-card__hint {
  margin: 18px 0 0;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  color: var(--text-hint);
  font-size: 11px;
  text-align: center;
}

.cost-card__notice {
  margin: 4px 0 0;
  color: var(--text-hint);
  font-size: 10px;
  text-align: center;
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
