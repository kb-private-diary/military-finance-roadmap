<script setup>
// SCR-CAR-03 · step3) 자동차 비용 계산  (담당: 호빈)
// step3 - 취득세 포함 구매비용 + 연간 유지비 + 3년 총비용
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
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

    <h2 class="car-cost__title text-title">비용 계산</h2>

    <div v-if="loading" class="car-cost__status text-caption">불러오는 중...</div>
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>

    <template v-else>
      <section class="purchase-summary">
        <div class="purchase-summary__icon" aria-hidden="true">&#128176;</div>
        <div class="purchase-summary__content">
          <p class="purchase-summary__label">구매 비용</p>
          <strong class="purchase-summary__amount">{{ formatManwonUnit(purchaseTotal) }}</strong>
          <p class="purchase-summary__desc">
            {{ formatManwonUnit(purchase?.price) }} + 취득세 {{ formatManwonUnit(purchase?.tax) }}
          </p>
        </div>
      </section>

      <BaseCard v-if="maintenanceCost" padding="16px">
        <h3 class="section-title">연간 유지비 구성</h3>
        <ul class="cost-list">
          <li class="cost-row">
            <span class="cost-row__name">연료비</span>
            <strong>{{ formatManwonUnit(maintenanceCost.estimatedFuelCostAnnual) }}</strong>
          </li>
          <li class="cost-row">
            <span class="cost-row__name">자동차세</span>
            <strong>{{ formatManwonUnit(maintenanceCost.annualVehicleTaxAfterDiscount) }}</strong>
          </li>
          <li class="cost-row">
            <span class="cost-row__name">보험료</span>
            <strong>
              {{ formatManwonUnit(maintenanceCost.insurancePremiumMin) }}~{{
                formatManwonUnit(maintenanceCost.insurancePremiumMax)
              }}
            </strong>
          </li>
        </ul>
        <div class="cost-list__total">
          연간 합계 {{ formatManwonUnit(maintenanceTotalMin) }}~{{ formatManwonUnit(maintenanceTotalMax) }}
        </div>
      </BaseCard>

      <BaseCard padding="18px" class="total-card">
        <h3 class="section-title">3년 총비용</h3>
        <p class="total-card__desc">구매 비용 + 연간 유지비 × 3년 기준</p>
        <strong class="total-card__amount">
          {{ formatManwonUnit(threeYearTotalMin) }}~{{ formatManwonUnit(threeYearTotalMax) }}
        </strong>
      </BaseCard>
    </template>

    <BottomButtonBar
      primary-label="다음"
      secondary-label="이전"
      :primary-disabled="loading || !!loadError"
      @primary-click="handleNext"
      @secondary-click="handlePrev"
    />
  </div>
</template>

<style scoped>
.car-cost {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px 0 96px;
  color: var(--text-strong);
}

.car-cost__title {
  margin: 0;
}

.car-cost__status {
  padding: 40px 0;
  text-align: center;
  color: var(--text-muted);
}

.form-error {
  margin: 0;
  color: var(--danger);
}

.purchase-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 16px;
  background: var(--kb-yellow-pale);
}

.purchase-summary__icon {
  display: flex;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--kb-yellow);
  font-size: 22px;
}

.purchase-summary__label {
  margin: 0;
  color: var(--brand-gold);
  font-size: 13px;
  font-weight: 600;
}

.purchase-summary__amount {
  display: block;
  margin-top: 4px;
  font-size: 20px;
  font-weight: 800;
}

.purchase-summary__desc {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-body);
  display: block;
}

.cost-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.cost-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.cost-row__name {
  color: var(--text-muted);
}

.cost-list__total {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  color: var(--text-hint);
  font-size: 12px;
  text-align: center;
}

.total-card {
  text-align: center;
}

.total-card__desc {
  margin: 0 0 10px;
  color: var(--text-muted);
  font-size: 12px;
}

.total-card__amount {
  font-size: 20px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}

.car-cost :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.car-cost :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}

.car-cost :deep(.bottom-button-bar .bar-button.primary:disabled) {
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  cursor: not-allowed;
}
</style>
