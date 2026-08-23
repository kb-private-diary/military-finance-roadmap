<script setup>
// SCR-TRV-06 · 저장된 여행 로드맵 상세
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import calculatorImage from '@/assets/images/calculator.png';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import { formatDate, formatWon } from '@/util/format';

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
const budgetPlan = computed(() => detail.value?.budgetPlan ?? null);

const dateRange = computed(() => {
  if (!detail.value) return '';
  return `${formatDate(detail.value.startDate)}~${formatDate(detail.value.endDate)}`;
});

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

const costItems = computed(() => {
  const items = [
    {
      key: 'hotel',
      label: '숙박비',
      value: Number(cost.value.hotelCost) || 0,
      color: 'var(--chart-1)',
    },
    {
      key: 'transport',
      label: '교통비',
      value: Number(cost.value.flightCost) || 0,
      color: 'var(--chart-2)',
    },
    {
      key: 'living',
      label: '관광비',
      value: Number(cost.value.livingCost) || 0,
      color: 'var(--chart-3)',
    },
  ];
  const total = items.reduce((sum, item) => sum + item.value, 0);
  return items.map((item) => ({
    ...item,
    percentage: total ? Math.round((item.value / total) * 100) : 0,
  }));
});

const budgetDifference = computed(
  () =>
    (Number(detail.value?.totalBudget) || 0) -
    (Number(cost.value.totalCost) || 0),
);

const budgetUsageRate = computed(() => {
  const budget = Number(detail.value?.totalBudget) || 0;
  if (!budget) return 0;
  return Math.min(
    100,
    Math.round(((Number(cost.value.totalCost) || 0) / budget) * 100),
  );
});

const budgetStatus = computed(() => {
  const budget = Number(detail.value?.totalBudget) || 0;
  if (!budget) return { label: '예산 비교', rate: 0 };
  const rate = Math.round((Math.abs(budgetDifference.value) / budget) * 100);
  return {
    label: budgetDifference.value >= 0 ? '예산 여유' : '예산 초과',
    rate,
  };
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

const loadDetail = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const response = await travelApi.getGoalDetail(goalId);
    detail.value = unwrap(response);
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
      <PageHeader breadcrumb="저장한 로드맵" title="내가 그린 전역 작전" />
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
      <BaseCard padding="18px" class="detail-header">
        <h1 class="detail-header__title text-title">{{ detail.title }}</h1>
        <p class="detail-header__date">{{ dateRange }}</p>
        <p class="detail-header__route">
          {{ detail.departure }} → {{ detail.destination }}
        </p>
      </BaseCard>

      <BaseCard padding="18px" class="total-card">
        <div class="total-card__icon">
          <img :src="calculatorImage" alt="" />
        </div>
        <div>
          <p class="total-card__label">총 예상 준비 비용</p>
          <strong class="total-card__amount">
            {{ formatWon(cost.totalCost) }}
          </strong>
        </div>
      </BaseCard>

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
          <BaseCard padding="18px" class="cost-card">
            <h2 class="section-title text-label">예상 경비</h2>
            <div class="cost-bar" aria-label="예상 경비 비율">
              <span
                v-for="item in costItems"
                :key="item.key"
                :style="{
                  width: `${item.percentage}%`,
                  background: item.color,
                }"
              />
            </div>
            <ul class="cost-list">
              <li v-for="item in costItems" :key="item.key">
                <span
                  class="cost-list__dot"
                  :style="{ background: item.color }"
                />
                <span class="cost-list__label">{{ item.label }}</span>
                <strong>{{ formatWon(item.value) }}</strong>
                <span class="cost-list__rate">{{ item.percentage }}%</span>
              </li>
            </ul>
          </BaseCard>

          <BaseCard padding="18px" class="budget-card">
            <h2 class="section-title text-label">내 예산과 비교</h2>
            <div class="budget-card__summary">
              <dl>
                <div>
                  <dt>내가 입력한 예산</dt>
                  <dd>{{ formatWon(detail.totalBudget) }}</dd>
                </div>
                <div>
                  <dt>예상 여행 경비</dt>
                  <dd>{{ formatWon(cost.totalCost) }}</dd>
                </div>
              </dl>
              <div
                class="budget-card__status"
                :class="{ 'is-over': budgetDifference < 0 }"
              >
                <span>{{ budgetStatus.label }}</span>
                <strong>{{ budgetStatus.rate }}%</strong>
              </div>
            </div>
            <div class="budget-progress" aria-label="예산 사용률">
              <span :style="{ width: `${budgetUsageRate}%` }" />
            </div>
            <section
              v-if="budgetDifference < 0 && budgetPlan"
              class="budget-plan"
              aria-label="여행 자금 준비 안내"
            >
              <template v-if="budgetPlan.planType === 'IN_SERVICE'">
                <strong class="budget-plan__title">
                  복무 중 여행 자금 준비
                </strong>
                <p class="budget-plan__description">
                  현재 월급에서 납입 중인 군적금을 제외한 금액을 기준으로
                  계산했어요.
                </p>
                <dl class="budget-plan__details">
                  <div>
                    <dt>현재 월급</dt>
                    <dd>{{ formatWon(budgetPlan.monthlySalary) }}</dd>
                  </div>
                  <div>
                    <dt>월 군적금 납입액</dt>
                    <dd>{{ formatWon(budgetPlan.monthlySaving) }}</dd>
                  </div>
                  <div>
                    <dt>매달 준비 가능 금액</dt>
                    <dd>
                      {{ formatWon(budgetPlan.monthlyAvailableAmount) }}
                    </dd>
                  </div>
                </dl>
                <p
                  v-if="budgetPlan.requiredMonths"
                  class="budget-plan__result"
                >
                  부족한 {{ formatWon(budgetPlan.shortfall) }}을 마련하려면
                  약 <strong>{{ budgetPlan.requiredMonths }}개월</strong>이
                  필요해요.
                </p>
                <p v-else class="budget-plan__notice">
                  현재 월급과 군적금 납입액만으로는 추가 준비 기간을
                  계산하기 어려워요.
                </p>
              </template>

              <template v-else-if="budgetPlan.planType === 'AFTER_DISCHARGE'">
                <strong class="budget-plan__title">
                  전역 후 여행 자금 확인
                </strong>
                <p class="budget-plan__description">
                  전역 후 받을 군적금 만기 예상금까지 더해 여행 자금을
                  확인했어요.
                </p>
                <template v-if="budgetPlan.expectedMaturityAmount != null">
                  <dl class="budget-plan__details">
                    <div>
                      <dt>군적금 만기 예상금</dt>
                      <dd>
                        {{ formatWon(budgetPlan.expectedMaturityAmount) }}
                      </dd>
                    </div>
                  </dl>
                  <p
                    class="budget-plan__result"
                    :class="{
                      'is-short': budgetPlan.remainingAfterTravel < 0,
                    }"
                  >
                    <template v-if="budgetPlan.remainingAfterTravel >= 0">
                      여행 경비를 마련하고도
                      <strong>
                        {{ formatWon(budgetPlan.remainingAfterTravel) }}
                      </strong>
                      의 여유가 있어요.
                    </template>
                    <template v-else>
                      만기 예상금을 더해도
                      <strong>
                        {{
                          formatWon(
                            Math.abs(budgetPlan.remainingAfterTravel),
                          )
                        }}
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
            <div class="budget-card__guide">
              <strong>안내</strong>
              <p>· 계산 결과는 예상 금액이며 실제 비용과 다를 수 있습니다.</p>
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

.detail-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.detail-header__title {
  margin: 0;
}

.detail-header__date,
.detail-header__route {
  margin: 12px 0 0;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 500;
}

.detail-header__route {
  margin-top: 8px;
  font-size: 15px;
  font-weight: 700;
}

.total-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--kb-yellow-pale);
}

.total-card__icon {
  display: grid;
  width: 54px;
  height: 54px;
  flex: 0 0 54px;
  border-radius: 14px;
  background: var(--kb-yellow);
  place-items: center;
}

.total-card__icon img {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.total-card__label {
  margin: 0;
  color: var(--brand-gold);
  font-size: 13px;
  font-weight: 700;
}

.total-card__amount {
  display: block;
  margin-top: 4px;
  color: var(--kb-gray);
  font-size: 24px;
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
.cost-list,
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

.section-title {
  margin: 0;
}

.cost-bar {
  display: flex;
  height: 28px;
  margin-top: 28px;
  overflow: hidden;
  border-radius: 10px;
  background: var(--surface-muted);
}

.cost-bar span {
  height: 100%;
}

.cost-list {
  margin-top: 28px;
  gap: 12px;
}

.cost-list li {
  display: grid;
  grid-template-columns: 12px 1fr auto 38px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.cost-list__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.cost-list__rate {
  color: var(--brand-gold);
  font-weight: 700;
  text-align: right;
}

.budget-card__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
}

.budget-card__summary dl {
  display: grid;
  gap: 12px;
  margin: 0;
}

.budget-card__summary dt {
  color: var(--text-muted);
  font-size: 11px;
}

.budget-card__summary dd {
  margin: 4px 0 0;
  font-size: 13px;
  font-weight: 700;
}

.budget-card__status {
  display: grid;
  width: 92px;
  height: 92px;
  flex: 0 0 92px;
  border-radius: 14px;
  background: var(--kb-yellow-pale);
  color: var(--brand-gold);
  font-size: 12px;
  place-content: center;
  text-align: center;
}

.budget-card__status strong {
  margin-top: 4px;
  color: var(--kb-yellow-deep);
  font-size: 23px;
  font-weight: 500;
}

.budget-card__status.is-over,
.budget-card__status.is-over strong {
  color: var(--danger);
}

.budget-progress {
  height: 12px;
  margin-top: 24px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--surface-muted);
}

.budget-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--kb-yellow);
}

.budget-plan {
  margin-top: 22px;
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

.budget-card__guide {
  margin-top: 26px;
  color: var(--text-muted);
  font-size: 12px;
}

.budget-card__guide p {
  margin: 10px 0 0;
  line-height: 1.6;
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
