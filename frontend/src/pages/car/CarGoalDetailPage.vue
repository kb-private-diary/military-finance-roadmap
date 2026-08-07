<script setup>
// SCR-CAR-05 · 자동차 목표 상세  (담당: 호빈)
// 저장된 자동차 로드맵 상세 — 선택한 목표 / 비용 계산 / 금융상품 3탭
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import dashboardApi from '@/api/dashboardApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = computed(() => Number(route.params.goalId));

const TABS = [
  { key: 'goal', label: '선택한 목표' },
  { key: 'cost', label: '비용 계산' },
  { key: 'products', label: '금융상품' },
];
const activeTab = ref('goal');

const goal = ref(null);
const purchase = ref(null); // { price, tax, total }
const maintenanceCost = ref(null);
const budgetStatus = ref(null);
const evSubsidy = ref(null);
const savings = ref(null); // 오픈뱅킹 군적금 연동 안 돼 있으면 null 유지
const loading = ref(true);
const loadError = ref('');

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const hasSelectedModel = computed(() => !!goal.value?.selectedModelId);
const isElectric = computed(() => maintenanceCost.value?.fuelType === '전기');
const showLoanProduct = computed(() => budgetStatus.value?.withinBudget === false);
const overBudgetAmount = computed(() => {
  if (!budgetStatus.value) return 0;
  return budgetStatus.value.purchaseTotal - budgetStatus.value.effectiveBudget;
});

// 저축 진행률: 현재 납입액 / 기준 예산(수동입력 또는 만기예상액)
const savingsRate = computed(() => {
  if (!savings.value || !budgetStatus.value?.effectiveBudget) return 0;
  const currentManwon = savings.value.currentTotalSavings / 10_000;
  return Math.min(100, Math.round((currentManwon / budgetStatus.value.effectiveBudget) * 100));
});

const loadDetail = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const goalResult = await carApi.findGoalDetail(goalId.value);
    goal.value = unwrap(goalResult);

    if (!goal.value?.selectedModelId) {
      return; // 아직 차량 선택 전 — 비용/금융상품 관련 API는 호출하지 않음
    }

    const [maintenanceResult, budgetResult] = await Promise.all([
      carApi.findMaintenanceCost(goalId.value),
      carApi.findBudgetStatus(goalId.value),
    ]);
    maintenanceCost.value = unwrap(maintenanceResult);
    budgetStatus.value = unwrap(budgetResult);

    if (goal.value.isNew) {
      const taxResult = await carApi.findAcquisitionTax(goalId.value);
      const tax = unwrap(taxResult);
      purchase.value = { price: tax.vehiclePrice, tax: tax.acquisitionTaxAmount };
    } else {
      const usedResult = await carApi.findUsedPrice(goalId.value);
      const used = unwrap(usedResult);
      purchase.value = { price: used.estimatedUsedPrice, tax: used.acquisitionTaxAmount };
    }

    if (maintenanceCost.value?.fuelType === '전기') {
      const evResult = await carApi.findEvSubsidy(goalId.value);
      evSubsidy.value = unwrap(evResult);
    }

    try {
      savings.value = await dashboardApi.findSavingsStatus();
    } catch {
      savings.value = null; // 오픈뱅킹 군적금 미연동 — 저축 진행률 섹션은 그냥 숨김
    }
  } catch (error) {
    loadError.value = readErrorMessage(error, '목표 상세 정보를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

onMounted(loadDetail);

const selectTab = (key) => {
  activeTab.value = key;
};

const goToList = () => {
  router.push({ name: 'CarRecommend', params: { goalId: goalId.value } });
};
</script>

<template>
  <div class="car-detail">
    <div v-if="loading" class="car-detail__status text-caption">불러오는 중...</div>
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>

    <template v-else>
      <div class="car-detail__header">
        <h2 class="car-detail__title text-title">
          {{ goal.selectedModelName ? `${goal.selectedModelName} 목표` : '자동차 목표' }}
        </h2>
        <BaseTag
          v-if="goal.status === 'SELECTED'"
          label="선택 완료"
          variant="yellow"
        />
      </div>
      <p class="car-detail__subtitle text-caption">
        목표 구매 시기 {{ goal.targetDate }} · {{ goal.region }}
      </p>

      <p v-if="!hasSelectedModel" class="car-detail__empty text-caption">
        아직 선택한 차량이 없어요. 추천 목록에서 먼저 차량을 골라주세요.
      </p>

      <template v-else>
        <!-- 저축 진행률 -->
        <BaseCard v-if="savings" padding="18px" class="savings-card">
          <div class="savings-card__head">
            <span class="section-title">저축 진행률</span>
            <strong class="savings-card__rate">{{ savingsRate }}%</strong>
          </div>
          <div class="savings-progress">
            <div class="savings-progress__bar" :style="{ width: `${savingsRate}%` }"></div>
          </div>
          <p class="savings-card__desc">
            {{ formatManwonUnit(Math.round(savings.currentTotalSavings / 10000)) }} /
            {{ formatManwonUnit(budgetStatus?.effectiveBudget) }} 달성
            (군적금 만기예상액 {{ formatManwonUnit(Math.round(savings.expectedMaturityTotal / 10000)) }})
          </p>
        </BaseCard>

        <div class="tab-row">
          <button
            v-for="tab in TABS"
            :key="tab.key"
            type="button"
            class="tab-button"
            :class="{ 'is-active': activeTab === tab.key }"
            @click="selectTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- 탭: 선택한 목표 -->
        <div v-if="activeTab === 'goal'" class="tab-panel">
          <BaseCard padding="18px" class="applied-card">
            <div class="applied-card__check" aria-hidden="true">&#10003;</div>
            <div class="applied-card__content">
              <strong class="applied-card__name">
                {{ goal.selectedModelName }} · {{ goal.isNew ? '신차' : '중고' }}
                <template v-if="!goal.isNew && goal.selectedYear">({{ goal.selectedYear }}년식)</template>
              </strong>
              <p class="applied-card__desc">목표 구매 시기 {{ goal.targetDate }}</p>
              <p class="applied-card__amount">
                {{ formatManwonUnit(purchase?.price) }} + 취득세 {{ formatManwonUnit(purchase?.tax) }}
              </p>
            </div>
          </BaseCard>

          <dl class="info-list">
            <div class="info-list__row">
              <dt>기준 예산</dt>
              <dd>{{ formatManwonUnit(budgetStatus?.effectiveBudget) }}</dd>
            </div>
            <div class="info-list__row">
              <dt>운전 경력</dt>
              <dd>{{ goal.experienceYears }}년</dd>
            </div>
            <div class="info-list__row">
              <dt>거주 지역</dt>
              <dd>{{ goal.region }}</dd>
            </div>
          </dl>
        </div>

        <!-- 탭: 비용 계산 -->
        <div v-else-if="activeTab === 'cost'" class="tab-panel">
          <section class="purchase-summary">
            <div class="purchase-summary__icon" aria-hidden="true">&#128176;</div>
            <div class="purchase-summary__content">
              <p class="purchase-summary__label">구매 비용</p>
              <strong class="purchase-summary__amount">
                {{ formatManwonUnit((purchase?.price ?? 0) + (purchase?.tax ?? 0)) }}
              </strong>
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
              연간 합계 {{ formatManwonUnit(maintenanceCost.totalMaintenanceCostMin) }}~{{
                formatManwonUnit(maintenanceCost.totalMaintenanceCostMax)
              }}
            </div>
          </BaseCard>
        </div>

        <!-- 탭: 금융상품 -->
        <div v-else class="tab-panel">
          <section class="car-detail__section">
            <h3 class="section-title">관련 보험상품</h3>
            <BaseCard padding="18px" class="product-card">
              <div class="product-card__icon" aria-hidden="true">&#128737;</div>
              <div class="product-card__content">
                <strong class="product-card__name">KB 손해보험 다이렉트 자동차보험</strong>
                <p class="product-card__desc">
                  {{ goal.experienceYears }}년 운전경력 · {{ goal.selectedModelName }} 기준 추천
                </p>
                <p class="product-card__amount">
                  예상 연 보험료 {{ formatManwonUnit(maintenanceCost?.insurancePremiumMin) }}~{{
                    formatManwonUnit(maintenanceCost?.insurancePremiumMax)
                  }}
                </p>
              </div>
              <a
                class="product-card__external"
                href="https://direct.kbinsure.co.kr/"
                target="_blank"
                rel="noopener noreferrer"
                aria-label="KB 손해보험 다이렉트 자동차보험 홈페이지 열기"
              >
                <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
                  <path d="M14 4h6v6M20 4l-9 9M18 13v5a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h5" />
                </svg>
              </a>
            </BaseCard>
          </section>

          <section v-if="isElectric" class="car-detail__section">
            <h3 class="section-title">전기차 보조금</h3>
            <BaseCard padding="18px" class="product-card">
              <div class="product-card__icon" aria-hidden="true">&#9889;</div>
              <div class="product-card__content">
                <strong class="product-card__name">{{ evSubsidy?.region }} 전기차 보조금</strong>
                <p class="product-card__amount">
                  보조금 적용가 {{ formatManwonUnit(evSubsidy?.finalPrice) }}
                </p>
              </div>
            </BaseCard>
          </section>

          <section v-if="showLoanProduct" class="car-detail__section">
            <h3 class="section-title">관련 대출상품</h3>
            <BaseCard padding="18px" class="product-card">
              <div class="product-card__icon" aria-hidden="true">&#128176;</div>
              <div class="product-card__content">
                <strong class="product-card__name">
                  KB 매직카대출({{ goal.isNew ? '신차' : '중고차' }})
                </strong>
                <p class="product-card__desc">
                  선택한 차량이 준비 가능한 금액보다 {{ formatManwonUnit(overBudgetAmount) }} 더 필요해요.
                </p>
              </div>
            </BaseCard>
          </section>
        </div>
      </template>

      <p class="car-detail__back text-caption" @click="goToList">목록으로</p>
    </template>

    <BottomButtonBar
      primary-label="확인"
      secondary-label="삭제"
      @primary-click="goToList"
    />
  </div>
</template>

<style scoped>
.car-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px 0 96px;
  color: var(--text-strong);
}

.car-detail__status,
.car-detail__empty {
  padding: 40px 0;
  text-align: center;
  color: var(--text-muted);
}

.form-error {
  margin: 0;
  color: var(--danger);
}

.car-detail__header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.car-detail__title {
  margin: 0;
}

.car-detail__subtitle {
  margin: -8px 0 0;
  color: var(--text-muted);
}

.savings-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.savings-card__rate {
  font-size: 18px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}

.savings-progress {
  width: 100%;
  height: 10px;
  margin-top: 14px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--surface-muted);
}

.savings-progress__bar {
  height: 100%;
  border-radius: inherit;
  background: var(--kb-yellow);
  transition: width 0.3s ease;
}

.savings-card__desc {
  margin: 10px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.tab-row {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--line);
}

.tab-button {
  flex: 1;
  padding: 12px 0;
  border: 0;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}

.tab-button.is-active {
  color: var(--text-strong);
  border-bottom-color: var(--kb-yellow-deep);
}

.tab-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.applied-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.applied-card__check {
  display: flex;
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--military-green);
  color: var(--surface-default);
  font-size: 13px;
}

.applied-card__name {
  display: block;
  font-size: 15px;
  font-weight: 700;
}

.applied-card__desc {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.applied-card__amount {
  margin: 8px 0 0;
  font-size: 15px;
  font-weight: 800;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
}

.info-list__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-list__row dt {
  color: var(--text-muted);
  font-size: 13px;
}

.info-list__row dd {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
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

.car-detail__section + .car-detail__section {
  margin-top: 4px;
}

.product-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.product-card__external {
  position: absolute;
  top: 50%;
  right: 16px;
  display: grid;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: var(--text-strong);
  text-decoration: none;
  transform: translateY(-50%);
  place-items: center;
}

.product-card__external:hover,
.product-card__external:focus-visible {
  background: var(--surface-muted);
}

.product-card__external svg {
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
}

.product-card__icon {
  display: flex;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--kb-yellow-pale);
  font-size: 22px;
}

.product-card__content {
  min-width: 0;
  padding-right: 38px;
}

.product-card__name {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-body);
}

.product-card__desc {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.product-card__amount {
  margin: 8px 0 0;
  font-size: 15px;
  font-weight: 800;
}

.car-detail__back {
  margin: 4px 0 0;
  color: var(--text-muted);
  text-align: center;
  text-decoration: underline;
  cursor: pointer;
}

.car-detail :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.car-detail :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}
</style>
