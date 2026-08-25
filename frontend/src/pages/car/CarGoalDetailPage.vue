<script setup>
// SCR-CAR-05 · 자동차 목표 상세  (담당: 호빈)
// 저장된 자동차 로드맵 상세 — 선택한 목표 / 비용 계산 / 금융상품 3탭
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import dashboardApi from '@/api/dashboardApi';
import regretApi from '@/api/regretApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import GoalSummaryCard from '@/components/common/GoalSummaryCard.vue';
import EstimatedCostCard from '@/components/common/EstimatedCostCard.vue';
import BudgetGauge from '@/components/common/BudgetGauge.vue';
import carBudgetIcon from '@/assets/images/car-budget.png';
import carPriceIcon from '@/assets/images/car-price.png';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = computed(() => Number(route.params.goalId));

const TABS = [
  { key: 'savings', label: '저축진행률' },
  { key: 'cost', label: '비용계산' },
  { key: 'products', label: '금융상품' },
];
const activeTab = ref('savings');

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
// 전기차 보조금은 신차 구매에만 적용된다 (중고차는 지원 대상 아님).
const isElectric = computed(
  () => maintenanceCost.value?.fuelType === '전기' && goal.value?.isNew === true,
);
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

// 최근 1개월 후회소비 기반 비용 활용 분석 (진로 상세와 동일 패턴)
const formatAmount = (amount) => `${Number(amount ?? 0).toLocaleString()}원`;
const regretAnalysis = ref(null);

const loadRegretAnalysis = async () => {
  try {
    const spendings = await regretApi.findSpendings();

    const today = new Date();
    const oneMonthAgo = new Date(today);
    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);

    // 최근 1개월 내 후회소비(REGRET)만 합산
    const regretAmount = spendings
      .filter((spending) => {
        const spentAt = new Date(spending.spentAt);
        return (
          spentAt >= oneMonthAgo &&
          spentAt <= today &&
          spending.reviewType === 'REGRET'
        );
      })
      .reduce((total, spending) => total + Number(spending.amount ?? 0), 0);

    // 후회소비 금액으로 마련 가능한 자동차 유지비 항목 (만원 단위 → 원으로 환산)
    const prepItems = [
      { itemName: '한 달 자동차세', amount: monthlyTax.value * 10000, priority: 1 },
      { itemName: '한 달 정비비', amount: monthlyRepair.value * 10000, priority: 2 },
      { itemName: '한 달 보험료', amount: monthlyInsurance.value * 10000, priority: 3 },
      { itemName: '한 달 연료비', amount: monthlyFuel.value * 10000, priority: 4 },
    ].filter((item) => item.amount > 0);

    const affordableItems = prepItems
      .filter((item) => item.amount <= regretAmount)
      .sort((a, b) =>
        a.priority !== b.priority ? a.priority - b.priority : a.amount - b.amount,
      );

    let recommendationMessage = '';
    let targetItemName = null;
    let targetItemAmount = 0;

    if (regretAmount === 0) {
      recommendationMessage =
        '지금의 소비 습관을 유지하면서 자동차 자금을 모아보세요.';
    } else if (affordableItems.length > 0) {
      targetItemName = affordableItems[0].itemName;
      targetItemAmount = affordableItems[0].amount;
    } else {
      recommendationMessage =
        '작은 금액도 모이면 유지비에 보탬이 돼요.\n다음 달 유지비를 위해 조금씩 모아보는 건 어떨까요?';
    }

    regretAnalysis.value = {
      regretAmount,
      recommendationMessage,
      targetItemName,
      targetItemAmount,
    };
  } catch (error) {
    console.error('후회소비 자동차비용 활용 분석 실패:', error);
    regretAnalysis.value = null;
  }
};

// ── step3(CarCostPage) 비용 계산·요약 카드 이식 ──
const purchaseTotal = computed(
  () => (purchase.value?.price ?? 0) + (purchase.value?.tax ?? 0),
);
const MAINTENANCE_MONTHS = 36;
const CAT_COLORS = {
  tax: '#FFECBE',
  repair: '#DAC183',
  insurance: '#B39D89',
  fuel: '#6E6053',
};
const INK_DARK = '#5b4b2e';
const INK_LIGHT = '#ffffff';
const CAR_TYPE_LABEL = { 1: '경차', 2: '준중형', 3: 'SUV' };

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
  () => monthlyFuel.value + monthlyInsurance.value + monthlyRepair.value + monthlyTax.value,
);
const maint3yTotal = computed(() => monthlyTotal.value * MAINTENANCE_MONTHS);

// 탭① 한 달 기준
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

// 탭② 3년 기준
const yearlyData = computed(() => {
  if (!maintenanceCost.value || !purchase.value) return null;
  const buy = purchaseTotal.value;
  const keep = maint3yTotal.value;
  const total = buy + keep || 1;
  const pct = (v) => Math.round((v / total) * 100);
  const monthlyMin = monthlyFuel.value + monthlyOf(mc().insurancePremiumMin) + monthlyRepair.value + monthlyTax.value;
  const monthlyMax = monthlyFuel.value + monthlyOf(mc().insurancePremiumMax) + monthlyRepair.value + monthlyTax.value;
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

const COST_TABS = [
  { label: '한 달 기준', value: 'monthly' },
  { label: '3년 기준', value: 'yearly' },
];
const costTab = ref('monthly');
const activeCostState = computed(() =>
  costTab.value === 'monthly' ? monthlyData.value : yearlyData.value,
);

// 요약 카드 (step3와 동일)
const summaryChip = computed(() =>
  goal.value ? (goal.value.isNew ? '신차' : '중고') : '',
);
const summarySpecs = computed(() => {
  const g = goal.value;
  const p = purchase.value;
  if (!g || !p) return [];
  const specs = [];
  if (g.carTypeCode) specs.push({ label: '차종', value: CAR_TYPE_LABEL[g.carTypeCode] });
  if (!g.isNew && g.selectedYear) specs.push({ label: '연식', value: `${g.selectedYear}년식` });
  if (!g.isNew && g.selectedMileageKm) {
    specs.push({ label: '주행거리', value: `${g.selectedMileageKm.toLocaleString()}km` });
  }
  specs.push({ label: '시세', value: formatManwonUnit(p.price) });
  specs.push({ label: '취득세', value: formatManwonUnit(p.tax) });
  specs.push({ label: '공채매입', value: g.carTypeCode === 1 ? '면제 대상' : formatManwonUnit(p.bond ?? 0) });
  return specs;
});
const summaryCompare = computed(() => {
  const g = goal.value;
  const hasManual = !!g?.budget;
  const budget = hasManual ? g.budget : budgetStatus.value?.effectiveBudget;
  if (!g || !budget) return null;
  const price = Math.round(purchaseTotal.value);
  const diff = budget - price;
  return {
    left: { label: hasManual ? '내 예산' : '예상 만기금', value: `${budget.toLocaleString()}만`, icon: carBudgetIcon },
    right: { label: '실제 차값', value: `${price.toLocaleString()}만`, icon: carPriceIcon },
    badge:
      diff >= 0
        ? { text: `${diff.toLocaleString()}만 여유`, tone: 'good' }
        : { text: `${(-diff).toLocaleString()}만 필요`, tone: 'bad' },
  };
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

    if (isElectric.value) {
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

onMounted(async () => {
  await loadDetail();
  // 상세 로드 후 후회소비 분석 (보조 정보라 실패해도 화면엔 영향 없음)
  loadRegretAnalysis();
});

const goToList = () => {
  router.push({ name: 'CarRecommend', params: { goalId: goalId.value } });
};

const goToRoadmap = () => {
  router.push({ name: 'RoadmapMain' });
};
</script>

<template>
  <div class="car-detail">
    <!-- 헤더: 자취 상세와 동일하게 PageHeader + 도메인 태그 한 줄 -->
    <header class="car-detail__head">
      <PageHeader breadcrumb="저장한 로드맵" title="나의 자동차 작전" />
      <BaseTag label="자동차" variant="car" />
    </header>

    <div v-if="loading" class="car-detail__status text-caption">불러오는 중...</div>
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>

    <template v-else>
      <!-- 상단 요약 카드 (자동차 step3와 동일: 칩+모델+스펙+예산 vs 차값) -->
      <GoalSummaryCard
        v-if="goal"
        theme="car"
        :chip="summaryChip"
        :title="goal.selectedModelName || '자동차 목표'"
        :specs="summarySpecs"
        :compare="summaryCompare"
      />

      <p v-if="!hasSelectedModel" class="car-detail__empty text-caption">
        아직 선택한 차량이 없어요. 추천 목록에서 먼저 차량을 골라주세요.
      </p>

      <template v-else>
        <TabBar
          v-model="activeTab"
          :tabs="TABS.map((t) => ({ label: t.label, value: t.key }))"
        />

        <!-- 탭: 저축진행률 (여행과 동일한 반원 게이지) -->
        <div v-if="activeTab === 'savings'" class="tab-panel">
          <BaseCard v-if="savings" padding="18px 18px 14px" class="savings-card">
            <div class="savings-card__head">
              <span class="section-title">저축 진행률</span>
              <strong class="savings-card__rate">{{ savingsRate }}%</strong>
            </div>
            <div class="savings-card__gauge">
              <BudgetGauge
                :amount="Math.round(savings.currentTotalSavings / 10000)"
                :budget="budgetStatus?.effectiveBudget ?? 0"
                :center-value="savingsRate"
                center-unit="%"
                :show-pct="false"
                unit="만원"
              />
            </div>
            <p class="savings-card__desc">
              군적금 만기예상액
              {{ formatManwonUnit(Math.round(savings.expectedMaturityTotal / 10000)) }} 기준
            </p>
          </BaseCard>

          <p v-else class="car-detail__empty text-caption">
            아직 저축 진행률 정보가 없어요. 오픈뱅킹으로 군적금을 연동해보세요.
          </p>
        </div>

        <!-- 탭: 비용계산 (한 달 / 3년 물통, step3와 동일) -->
        <div v-else-if="activeTab === 'cost'" class="tab-panel">
          <div class="car-detail__cost-tabs">
            <TabBar variant="segment" v-model="costTab" :tabs="COST_TABS" />
          </div>
          <EstimatedCostCard
            v-if="activeCostState"
            :state="activeCostState"
            :dividers="false"
            note=""
          />

          <!-- 비용 활용 분석 (최근 1개월 후회소비 연결, 진로 상세와 동일 톤) -->
          <BaseCard padding="18px" class="cost-analysis-card">
            <div class="cost-analysis-card__header">
              <div class="cost-analysis-card__icon" aria-hidden="true">💡</div>
              <div>
                <h2 class="cost-analysis-card__title">유지비 활용 분석</h2>
                <p>소비 습관을 자동차 자금과 연결해봤어요.</p>
              </div>
            </div>

            <template v-if="regretAnalysis">
              <div class="cost-analysis-card__amount">
                <p v-if="regretAnalysis.regretAmount > 0">
                  최근 1개월간
                  <strong>{{ formatAmount(regretAnalysis.regretAmount) }}</strong>
                  을 후회소비로 사용했어요.
                </p>
                <p v-else>최근 1개월간 후회소비로 기록된 지출이 없어요.</p>
              </div>

              <div class="cost-analysis-card__result">
                <template v-if="regretAnalysis.targetItemName">
                  <p class="cost-analysis-card__result-label">
                    다음에는 후회소비 대신
                  </p>
                  <div class="cost-analysis-card__target">
                    <strong class="cost-analysis-card__target-name">
                      {{ regretAnalysis.targetItemName }}
                    </strong>
                    <strong class="cost-analysis-card__target-amount">
                      {{ formatAmount(regretAnalysis.targetItemAmount) }}
                    </strong>
                  </div>
                  <p class="cost-analysis-card__result-message">
                    을 마련해보는 건 어떨까요?
                  </p>
                </template>
                <p v-else class="cost-analysis-card__result-message">
                  {{ regretAnalysis.recommendationMessage }}
                </p>
              </div>
            </template>

            <div v-else class="cost-analysis-card__empty">
              후회소비 데이터를 불러오지 못했어요.
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

      <p class="car-detail__back text-caption" @click="goToList">추천 목록 다시 보기</p>
    </template>

    <BottomButtonBar
      primary-label="확인"
      @primary-click="goToRoadmap"
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

/* 비용계산 탭 안 한 달/3년 세그먼트 */
.car-detail__cost-tabs {
  margin-bottom: 14px;
}

/* 헤더: PageHeader + 도메인 태그 한 줄 (자취 상세와 동일) */
.car-detail__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
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

.savings-card__gauge {
  margin-top: 8px;
}

.savings-card__desc {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  text-align: center;
}

/* ── 유지비 활용 분석 (후회소비 연결, 진로 상세와 동일 톤) ── */
.cost-analysis-card__header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.cost-analysis-card__icon {
  display: flex;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  background: var(--surface-cream);
  border-radius: 8px;
}
.cost-analysis-card__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
}
.cost-analysis-card__header p {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 10px;
}
.cost-analysis-card__amount {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}
.cost-analysis-card__amount p {
  margin: 0;
  color: var(--text-body);
  font-size: 12px;
  line-height: 1.6;
}
.cost-analysis-card__amount strong {
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 700;
}
.cost-analysis-card__result {
  margin-top: 16px;
  padding: 16px;
  background: var(--surface-cream);
  border-radius: 10px;
}
.cost-analysis-card__result-label,
.cost-analysis-card__result-message {
  margin: 0;
  color: var(--text-body);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.6;
  white-space: pre-line;
}
.cost-analysis-card__target {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin: 8px 0;
}
.cost-analysis-card__target-name {
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
}
.cost-analysis-card__target-amount {
  flex-shrink: 0;
  color: var(--brand-gold);
  font-size: 17px;
  font-weight: 700;
}
.cost-analysis-card__empty {
  margin-top: 16px;
  padding: 14px;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.6;
  text-align: center;
}

.tab-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-body);
  display: block;
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
  margin: 4px 2px 0;
  color: var(--text-muted);
  text-align: right;
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
