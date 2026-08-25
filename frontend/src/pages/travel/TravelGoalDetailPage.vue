<script setup>
// SCR-TRV-06 · 저장된 여행 로드맵 상세
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import regretApi from '@/api/regretApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import EstimatedCostCard from '@/components/common/EstimatedCostCard.vue';
import GoalSummaryCard from '@/components/common/GoalSummaryCard.vue';
import BudgetGauge from '@/components/common/BudgetGauge.vue';
import takeoffIcon from '@/assets/images/takeoff.png';
import landingIcon from '@/assets/images/landing.png';
import { findDomesticRegion } from './domesticRegions';
import { formatManwon } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const TABS = [
  { key: 'goal', label: '선택한 목표' },
  { key: 'cost', label: '비용 계산' },
  { key: 'products', label: '금융상품' },
];

const activeTab = ref('goal');
const detail = ref(null);
const loading = ref(true);
const loadError = ref('');
let scrollContainer = null;

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const cost = computed(() => detail.value?.cost ?? {});
const products = computed(() => detail.value?.products ?? {});
const selectedItems = computed(() => {
  const places = (detail.value?.places ?? []).map((place, index) => ({
    key: `${place.type}:${place.name}:${index}`,
    name: place.name,
    info: place.info,
    image: place.image,
    typeLabel: place.type === 'food' ? '맛집' : '관광지',
  }));
  const selectedPackage = detail.value?.selectedPackage;
  if (selectedPackage) {
    places.push({
      key: `package:${selectedPackage.packageId}`,
      name: selectedPackage.name,
      info:
        selectedPackage.description ||
        selectedPackage.departurePeriod ||
        '선택한 패키지 상품',
      image: selectedPackage.imageUrl,
      typeLabel: '패키지',
    });
  }
  return places;
});

// 여행 step3와 동일한 물통 + 예산 게이지 (저장된 스타일 금액 기준)
const toAmount = (value) => Math.max(Number(value) || 0, 0);
const displayedCost = computed(() => cost.value || null);

const travelState = computed(() => {
  if (!displayedCost.value) return null;
  const base = [
    { label: '교통비', amount: toAmount(displayedCost.value.flightCost), color: '#DAC183', ink: '#5b4b2e' },
    { label: '관광비', amount: toAmount(displayedCost.value.livingCost), color: '#B39D89', ink: '#ffffff' },
    { label: '숙소비', amount: toAmount(displayedCost.value.hotelCost), color: '#6E6053', ink: '#ffffff' },
  ];
  const sum = base.reduce((s, it) => s + it.amount, 0) || 1;
  const items = [...base]
    .sort((a, b) => a.amount - b.amount)
    .map((it, idx, arr) => ({
      label: it.label,
      amountText: formatManwon(it.amount),
      percent: Math.round((it.amount / sum) * 100),
      color: it.color,
      ink: it.ink,
      showName: idx === arr.length - 1,
    }));
  return {
    headLabel: '총 예상 여행 경비',
    amount: Math.round(toAmount(displayedCost.value.totalCost) / 10000),
    unit: '만원',
    items,
  };
});

const gaugeBudget = computed(() =>
  detail.value?.totalBudget
    ? detail.value.totalBudget
    : Math.round(toAmount(displayedCost.value?.totalCost) / 10000),
);

// 복무 중 / 전역 후 여행 자금 준비 계획 (백엔드 제공) + 예산 초과 여부
const budgetPlan = computed(() => detail.value?.budgetPlan ?? null);
const isOverBudget = computed(
  () => !!travelState.value && travelState.value.amount > gaugeBudget.value,
);

// 상단 요약 카드(GoalSummaryCard) — 여행 step3와 동일 구성
const tripDday = computed(() => {
  if (!detail.value?.startDate) return '';
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = Math.ceil((new Date(detail.value.startDate) - today) / 86400000);
  if (days > 0) return `D-${days}`;
  return days === 0 ? 'D-DAY' : `D+${-days}`;
});
const tripBudget = computed(() =>
  detail.value?.totalBudget
    ? `${detail.value.totalBudget.toLocaleString()}만`
    : '-',
);
const tripScopeLabel = computed(() =>
  detail.value
    ? findDomesticRegion(detail.value.destination)
      ? '국내여행'
      : '해외여행'
    : '',
);
// 일정+기간 한 줄: "27년 9월 20일 ~ 27년 9월 23일 (3박 4일)"
const tripSchedule = computed(() => {
  const s = detail.value?.startDate;
  const e = detail.value?.endDate;
  if (!s || !e) return '-';
  const start = new Date(s);
  const end = new Date(e);
  const nights = Math.round((end - start) / 86400000);
  const fmt = (d) =>
    `${String(d.getFullYear()).slice(2)}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;
  return `${fmt(start)} ~ ${fmt(end)} (${nights}박 ${nights + 1}일)`;
});

const productGroups = computed(() => [
  {
    key: 'cards',
    title: '관련 카드상품',
    items: products.value.cards ?? [],
  },
  {
    key: 'savings',
    title: '관련 적금상품',
    items: products.value.savings ?? [],
  },
  {
    key: 'insurances',
    title: '관련 보험상품',
    items: products.value.insurances ?? [],
  },
]);

const hasProducts = computed(() =>
  productGroups.value.some(({ items }) => items.length > 0),
);

const formatAmount = (amount) => `${Number(amount ?? 0).toLocaleString()}원`;

// ─────────────────────────────────────────────
// 최근 1개월 후회소비 기반 여행 경비 활용 분석 (진로 상세와 동일 패턴)
// ─────────────────────────────────────────────
const regretAnalysis = ref(null);

const loadRegretAnalysis = async () => {
  try {
    // 로그인 사용자의 전체 지출 내역 조회
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

    // 후회소비 금액으로 마련 가능한 여행 경비 항목 구성 (교통 → 숙소 → 관광 순)
    const c = displayedCost.value ?? {};
    const prepItems = [
      { itemName: '교통비', amount: toAmount(c.flightCost), priority: 1 },
      { itemName: '숙소비', amount: toAmount(c.hotelCost), priority: 2 },
      { itemName: '관광비', amount: toAmount(c.livingCost), priority: 3 },
    ].filter((item) => item.amount > 0);

    // 후회소비 금액으로 전액 마련 가능한 항목만, 우선순위·금액순 정렬
    const affordableItems = prepItems
      .filter((item) => item.amount <= regretAmount)
      .sort((a, b) =>
        a.priority !== b.priority ? a.priority - b.priority : a.amount - b.amount,
      );

    let recommendationMessage = '';
    let targetItemName = null;
    let targetItemAmount = 0;

    // ① 최근 3개월 후회소비가 없는 경우
    if (regretAmount === 0) {
      recommendationMessage =
        '지금의 소비 습관을 유지하면서 여행 자금을 모아보세요.';
    }
    // ② 후회소비 금액으로 여행 경비 항목 하나 이상 마련 가능한 경우
    else if (affordableItems.length > 0) {
      const targetItem = affordableItems[0];
      targetItemName = targetItem.itemName;
      targetItemAmount = targetItem.amount;
    }
    // ③ 후회소비는 있지만 경비 항목 금액보다 적은 경우
    else {
      recommendationMessage =
        '작은 금액도 모이면 여행 경비에 보탬이 돼요.\n다음 여행을 위해 조금씩 모아보는 건 어떨까요?';
    }

    regretAnalysis.value = {
      regretAmount,
      recommendationMessage,
      targetItemName,
      targetItemAmount,
    };
  } catch (error) {
    console.error('후회소비 여행경비 활용 분석 실패:', error);
    regretAnalysis.value = null;
  }
};

const loadDetail = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const response = await travelApi.getGoalDetail(goalId);
    detail.value = unwrap(response);
    // 상세(비용) 로드 후 후회소비 분석 (보조 정보라 실패해도 화면엔 영향 없음)
    loadRegretAnalysis();
  } catch (error) {
    loadError.value = readErrorMessage(
      error,
      '여행 목표 상세 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const goToRoadmap = () => router.push({ name: 'RoadmapMain' });

const goToRecommend = () =>
  router.push({ name: 'TravelPlaces', params: { goalId } });

onMounted(() => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');
  loadDetail();
});

onBeforeUnmount(() => {
  scrollContainer?.classList.remove('travel-scrollbar-hidden');
});
</script>

<template>
  <div class="travel-detail">
    <!-- 헤더: 자취 상세와 동일하게 PageHeader + 도메인 태그 한 줄 -->
    <header class="travel-detail__head">
      <PageHeader breadcrumb="저장한 로드맵" title="나의 여행 작전" />
      <BaseTag label="여행" variant="travel" />
    </header>

    <div v-if="loading" class="status-box text-caption" role="status">
      여행 로드맵을 불러오고 있습니다.
    </div>

    <div
      v-else-if="loadError"
      class="status-box status-box--error text-caption"
      role="alert"
    >
      <p>{{ loadError }}</p>
      <button type="button" @click="loadDetail">다시 시도</button>
    </div>

    <template v-else-if="detail">
      <!-- 상단 요약 카드 (여행 step3와 동일: 칩 + route + 일정/기간/남은기간/예산) -->
      <GoalSummaryCard
        theme="travel"
        :chip="tripScopeLabel"
        :title="detail.title"
        :route="{
          from: { label: '출발지', value: detail.departure, icon: takeoffIcon },
          to: { label: '도착지', value: detail.destination, icon: landingIcon },
        }"
        :specs="[
          { label: '일정', value: tripSchedule, full: true },
          { label: '남은 기간', value: tripDday },
          { label: '예산', value: tripBudget },
        ]"
      />


      <section class="detail-content">
        <TabBar
          v-model="activeTab"
          :tabs="TABS.map((tab) => ({ label: tab.label, value: tab.key }))"
        />

        <div v-if="activeTab === 'goal'" class="tab-panel">
          <ul v-if="selectedItems.length" class="selected-list">
            <li v-for="item in selectedItems" :key="item.key">
              <BaseCard padding="12px" class="selected-card">
                <div class="selected-card__image">
                  <span aria-hidden="true">이미지</span>
                  <img
                    v-if="item.image"
                    :src="item.image"
                    :alt="`${item.name} 이미지`"
                    @error="$event.currentTarget.style.display = 'none'"
                  />
                </div>
                <div class="selected-card__content">
                  <span class="selected-card__type">{{ item.typeLabel }}</span>
                  <strong class="selected-card__name">{{ item.name }}</strong>
                  <p v-if="item.info" class="selected-card__info">
                    {{ item.info }}
                  </p>
                </div>
              </BaseCard>
            </li>
          </ul>
          <EmptyState
            v-else
            title="선택한 여행 목표가 없습니다."
            description="관광지·맛집과 패키지는 선택하지 않아도 저장할 수 있어요."
          />
        </div>

        <div v-else-if="activeTab === 'cost'" class="tab-panel">
          <EstimatedCostCard
            v-if="travelState"
            :state="travelState"
            :dividers="false"
            note=""
          >
            <template #head>
              <p class="travel-cost__cost-title">세부 비용 내역</p>
              <div class="travel-cost__gauge">
                <BudgetGauge
                  :amount="travelState.amount"
                  :budget="gaugeBudget"
                  unit="만원"
                />
              </div>
              <div class="travel-cost__mid"></div>
            </template>
          </EstimatedCostCard>

          <!-- 복무 중 / 전역 후 여행 자금 준비 (예산 초과 시) -->
          <section
            v-if="isOverBudget && budgetPlan"
            class="budget-plan"
            aria-label="여행 자금 준비 안내"
          >
            <template v-if="budgetPlan.planType === 'IN_SERVICE'">
              <strong class="budget-plan__title">복무 중 여행 자금 준비</strong>
              <p class="budget-plan__description">
                현재 월급에서 납입 중인 군적금을 제외한 금액을 기준으로
                계산했어요.
              </p>
              <dl class="budget-plan__details">
                <div>
                  <dt>현재 월급</dt>
                  <dd>{{ formatAmount(budgetPlan.monthlySalary) }}</dd>
                </div>
                <div>
                  <dt>월 군적금 납입액</dt>
                  <dd>{{ formatAmount(budgetPlan.monthlySaving) }}</dd>
                </div>
                <div>
                  <dt>매달 준비 가능 금액</dt>
                  <dd>{{ formatAmount(budgetPlan.monthlyAvailableAmount) }}</dd>
                </div>
              </dl>
              <p v-if="budgetPlan.requiredMonths" class="budget-plan__result">
                부족한 {{ formatAmount(budgetPlan.shortfall) }}을 마련하려면 약
                <strong>{{ budgetPlan.requiredMonths }}개월</strong>이 필요해요.
              </p>
              <p v-else class="budget-plan__notice">
                현재 월급과 군적금 납입액만으로는 추가 준비 기간을 계산하기
                어려워요.
              </p>
            </template>

            <template v-else-if="budgetPlan.planType === 'AFTER_DISCHARGE'">
              <strong class="budget-plan__title">전역 후 여행 자금 확인</strong>
              <p class="budget-plan__description">
                전역 후 받을 군적금 만기 예상금까지 더해 여행 자금을
                확인했어요.
              </p>
              <template v-if="budgetPlan.expectedMaturityAmount != null">
                <dl class="budget-plan__details">
                  <div>
                    <dt>군적금 만기 예상금</dt>
                    <dd>
                      {{ formatAmount(budgetPlan.expectedMaturityAmount) }}
                    </dd>
                  </div>
                </dl>
                <p
                  class="budget-plan__result"
                  :class="{ 'is-short': budgetPlan.remainingAfterTravel < 0 }"
                >
                  <template v-if="budgetPlan.remainingAfterTravel >= 0">
                    여행 경비를 마련하고도
                    <strong>
                      {{ formatAmount(budgetPlan.remainingAfterTravel) }}
                    </strong>
                    의 여유가 있어요.
                  </template>
                  <template v-else>
                    만기 예상금을 더해도
                    <strong>
                      {{ formatAmount(Math.abs(budgetPlan.remainingAfterTravel)) }}
                    </strong>
                    이 더 필요해요.
                  </template>
                </p>
              </template>
              <p v-else class="budget-plan__notice">
                군적금 가입 정보가 없어 만기 예상금을 계산할 수 없어요.
              </p>
            </template>
          </section>

          <!-- 여행 경비 활용 분석 (최근 3개월 후회소비 연결) -->
          <BaseCard padding="18px" class="cost-analysis-card">
            <div class="cost-analysis-card__header">
              <div class="cost-analysis-card__icon" aria-hidden="true">💡</div>
              <div>
                <h2 class="cost-analysis-card__title">여행 경비 활용 분석</h2>
                <p>소비 습관을 여행 자금과 연결해봤어요.</p>
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

        <div v-else class="tab-panel">
          <div v-if="hasProducts" class="product-groups">
            <section
              v-for="group in productGroups"
              v-show="group.items.length"
              :key="group.key"
              class="product-group"
            >
              <h2 class="product-group__title text-label">
                {{ group.title }}
              </h2>
              <ul class="product-list">
                <li
                  v-for="product in group.items"
                  :key="`${product.type}:${product.productId}`"
                >
                  <BaseCard padding="16px" class="product-card">
                    <div class="product-card__content">
                      <strong class="product-card__name">
                        {{ product.name }}
                      </strong>
                      <p v-if="product.description" class="product-card__desc">
                        {{ product.description }}
                      </p>
                    </div>
                    <a
                      v-if="product.url"
                      class="product-card__external"
                      :href="product.url"
                      target="_blank"
                      rel="noopener noreferrer"
                      :aria-label="`${product.name} 외부 상품 페이지 열기`"
                    >
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path
                          d="M14 4h6v6M20 4l-9 9M18 13v5a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h5"
                        />
                      </svg>
                    </a>
                  </BaseCard>
                </li>
              </ul>
            </section>
          </div>
          <EmptyState
            v-else
            title="추천 금융상품이 없습니다."
            description="상품 정보가 등록되면 이 화면에서 확인할 수 있어요."
          />
        </div>
      </section>

      <p class="travel-detail__back text-caption" @click="goToRecommend">
        추천 목록 다시 보기
      </p>
    </template>

    <BottomButtonBar
      v-if="!loading && !loadError && detail"
      primary-label="확인"
      @primary-click="goToRoadmap"
    />
  </div>
</template>

<style scoped>
.travel-detail {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 18px;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

/* 헤더: PageHeader + 도메인 태그 한 줄 (자취 상세와 동일) */
.travel-detail__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.status-box {
  padding: 48px 12px;
  color: var(--text-muted);
  text-align: center;
}

.status-box p {
  margin: 0;
}

.status-box--error {
  color: var(--danger);
}

.status-box button {
  margin-top: 12px;
  padding: 8px 14px;
  border: 0;
  background: var(--travel-primary);
  color: var(--surface-default);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.tab-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.selected-list,
.product-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.selected-card {
  position: relative;
  display: flex;
  min-height: 112px;
  align-items: center;
  gap: 14px;
  border-color: var(--kb-yellow-deep);
}

.selected-card__image {
  position: relative;
  display: grid;
  width: 104px;
  height: 88px;
  flex: 0 0 104px;
  overflow: hidden;
  background: var(--surface-muted);
  color: var(--text-muted);
  font-size: 12px;
  place-items: center;
}

.selected-card__image img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-card__content {
  min-width: 0;
}

.selected-card__type {
  color: var(--text-muted);
  font-size: 11px;
}

.selected-card__name {
  display: -webkit-box;
  margin-top: 4px;
  overflow: hidden;
  font-size: 14px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.selected-card__info {
  display: -webkit-box;
  margin: 6px 0 0;
  overflow: hidden;
  color: var(--text-muted);
  font-size: 11px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-groups {
  display: grid;
  gap: 26px;
}

.product-group__title {
  margin: 0 0 10px;
}

.product-list {
  gap: 10px;
}

.product-card {
  position: relative;
  min-height: 90px;
}

.product-card__content {
  padding-right: 38px;
}

.product-card__name {
  display: block;
  font-size: 14px;
}

.product-card__desc {
  display: -webkit-box;
  margin: 6px 0 0;
  overflow: hidden;
  color: var(--text-muted);
  font-size: 11px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-card__external {
  position: absolute;
  top: 50%;
  right: 14px;
  display: grid;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: var(--text-strong);
  transform: translateY(-50%);
  place-items: center;
}

.product-card__external:hover,
.product-card__external:focus-visible {
  background: var(--surface-muted);
}

.product-card__external svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 2;
}

/* 여행 step3와 동일: 물통 head의 세부 비용 내역 제목 + 예산 게이지 */
.travel-cost__cost-title {
  margin: 0;
  padding: 15px 16px 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.travel-cost__gauge {
  padding: 16px 16px 14px;
}
.travel-cost__mid {
  height: 1px;
  margin: 0 16px;
  background: #e7e9ec;
}

/* ── 복무 중 / 전역 후 여행 자금 준비 (예산 초과 시) ── */
.budget-plan {
  margin-top: 14px;
  padding: 16px;
  border-radius: 12px;
  background: var(--surface-subtle);
}

.budget-plan__title {
  display: block;
  font-size: 13px;
}

.budget-plan__description,
.budget-plan__notice {
  margin: 8px 0 0;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.6;
}

.budget-plan__details {
  display: grid;
  gap: 8px;
  margin: 14px 0 0;
}

.budget-plan__details div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.budget-plan__details dt {
  color: var(--text-muted);
  font-size: 11px;
}

.budget-plan__details dd {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
}

.budget-plan__result {
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  color: var(--travel-primary-dark);
  font-size: 12px;
  line-height: 1.6;
}

.budget-plan__result strong {
  color: var(--travel-primary);
}

.budget-plan__result.is-short,
.budget-plan__result.is-short strong {
  color: var(--danger);
}

/* 추천 목록 다시 보기 (오른쪽 정렬) */
.travel-detail__back {
  margin: 4px 2px 0;
  color: var(--text-muted);
  text-align: right;
  text-decoration: underline;
  cursor: pointer;
}

/* ── 여행 경비 활용 분석 (후회소비 연결, 진로 상세와 동일 톤) ── */
.cost-analysis-card {
  margin-bottom: 14px;
}

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

:global(.app-content.travel-scrollbar-hidden) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

:global(.app-content.travel-scrollbar-hidden::-webkit-scrollbar) {
  display: none;
  width: 0;
  height: 0;
}
</style>
