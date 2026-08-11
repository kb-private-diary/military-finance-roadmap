<script setup>
// SCR-TRV-02 · step2) 여행 비용 계산
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import calculatorImage from '@/assets/images/calculator.png';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { formatWon } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const loading = ref(true);
const loadError = ref('');
const saveError = ref('');
const cost = ref(null);
const selectedStyle = ref('common');
const savedStyle = ref('common');
const savingStyle = ref(false);
let scrollContainer = null;

const STYLE_OPTIONS = [
  {
    value: 'saving',
    label: '최저가',
    description:
      '가성비 위주의 실속 있는 여행입니다.\n합리적인 교통편과 숙소를 중심으로 부담을 낮춘 예상 경비를 안내합니다.',
  },
  {
    value: 'common',
    label: '일반',
    description:
      '편안함과 비용의 균형을 고려한 표준 여행입니다.\n대중적으로 선택하는 교통편과 숙소를 기준으로 예상 경비를 안내합니다.',
  },
  {
    value: 'premium',
    label: '로열티',
    description:
      '편안함과 여유를 우선한 풍성한 여행입니다.\n이동과 숙박에 여유를 더해 넉넉한 예상 경비를 안내합니다.',
  },
];

const readErrorMessage = (error, fallback) =>
  (error.code === 'ECONNABORTED'
    ? '여행 비용 조회가 지연되고 있습니다. 잠시 후 다시 시도해주세요.'
    : null) ||
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const unwrap = (response) => response.data?.data;
const toAmount = (value) => Math.max(Number(value) || 0, 0);

const applyCostResponse = (response) => {
  const data = unwrap(response);
  cost.value = data;
  selectedStyle.value = data?.selectedStyle || 'common';
  savedStyle.value = selectedStyle.value;
};

const selectedStyleOption = computed(
  () =>
    STYLE_OPTIONS.find(
      ({ value }) => value === selectedStyle.value,
    ) || STYLE_OPTIONS[1],
);

const displayedCost = computed(() => {
  if (!cost.value) return null;

  const styleCost = cost.value.styleCosts?.find(
    ({ style }) => style === selectedStyle.value,
  );
  return styleCost
    ? { ...cost.value, ...styleCost }
    : cost.value;
});

const costItems = computed(() => [
  {
    label: '숙소비',
    value: toAmount(displayedCost.value?.hotelCost),
    color: 'var(--chart-1)',
  },
  {
    label: '관광비',
    value: toAmount(displayedCost.value?.livingCost),
    color: 'var(--chart-2)',
  },
  {
    label: '교통비',
    value: toAmount(displayedCost.value?.flightCost),
    color: 'var(--chart-3)',
  },
]);

const chartTotal = computed(() =>
  costItems.value.reduce((sum, item) => sum + item.value, 0),
);

const percentOf = (value) => {
  if (!chartTotal.value) return 0;
  return Math.round((toAmount(value) / chartTotal.value) * 100);
};

const totalBudget = computed(() =>
  toAmount(displayedCost.value?.totalCost) +
  Number(displayedCost.value?.remainingBudget || 0),
);

const budgetDifference = computed(
  () => totalBudget.value - toAmount(displayedCost.value?.totalCost),
);

const budgetUsageRate = computed(() => {
  if (!totalBudget.value) return 0;
  return Math.min(
    100,
    Math.round(
      (toAmount(displayedCost.value?.totalCost) / totalBudget.value) * 100,
    ),
  );
});

const budgetStatus = computed(() => {
  if (!totalBudget.value) return { label: '예산 비교', rate: 0 };
  return {
    label: budgetDifference.value >= 0 ? '절약 가능' : '예산 초과',
    rate: Math.round(
      (Math.abs(budgetDifference.value) / totalBudget.value) * 100,
    ),
  };
});

onMounted(async () => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');

  try {
    if (route.query.recalculate === 'true') {
      await travelApi.createCost(goalId);
      const response = await travelApi.findCost(goalId);
      applyCostResponse(response);
      await router.replace({
        name: 'TravelCost',
        params: { goalId },
      });
    } else {
      try {
        const response = await travelApi.findCost(goalId);
        applyCostResponse(response);
      } catch (error) {
        if (error.response?.data?.code !== 'TRAVEL_006') {
          throw error;
        }

        await travelApi.createCost(goalId);
        const response = await travelApi.findCost(goalId);
        applyCostResponse(response);
      }
    }
    void travelApi.prefetchPackages(goalId);
  } catch (error) {
    loadError.value = readErrorMessage(
      error,
      '예상 여행 비용을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
});

onBeforeUnmount(() => {
  scrollContainer?.classList.remove('travel-scrollbar-hidden');
});

const goPrevious = () => router.push({ name: 'TravelGoalCreate' });
const goNext = async () => {
  if (savingStyle.value) return;

  savingStyle.value = true;
  saveError.value = '';
  try {
    if (selectedStyle.value !== savedStyle.value) {
      await travelApi.updateCostStyle(goalId, selectedStyle.value);
      savedStyle.value = selectedStyle.value;
    }
    await router.push({ name: 'TravelPlaces', params: { goalId } });
  } catch (error) {
    saveError.value = readErrorMessage(
      error,
      '선택한 여행 스타일을 저장하지 못했습니다.',
    );
  } finally {
    savingStyle.value = false;
  }
};
</script>

<template>
  <div class="travel-cost">
    <RoadmapCharacterSlider :step="2" label="여행 로드맵" />

    <div v-if="loading" class="status-box text-caption" role="status">
      예상 비용을 계산하고 있습니다.
    </div>

    <div
      v-else-if="loadError"
      class="status-box status-box--error text-caption"
      role="alert"
    >
      <p>{{ loadError }}</p>
      <button type="button" @click="goPrevious">이전 화면으로</button>
    </div>

    <template v-else-if="cost">
      <section class="style-selector" aria-labelledby="style-title">
        <div class="style-selector__panel">
          <h2 id="style-title" class="style-selector__title text-label">
            여행 스타일
          </h2>
          <div class="style-selector__buttons">
            <CategoryButton
              v-for="option in STYLE_OPTIONS"
              :key="option.value"
              variant="oval-yellow"
              :label="option.label"
              :active="selectedStyle === option.value"
              @click="selectedStyle = option.value"
            />
          </div>
        </div>
        <p class="style-selector__description text-caption">
          {{ selectedStyleOption.description }}
        </p>
      </section>

      <section class="total-summary" aria-label="총 예상 여행 경비">
        <div class="total-summary__icon">
          <img :src="calculatorImage" alt="" />
        </div>
        <div>
          <h2 class="total-summary__label">총 예상 여행 경비</h2>
          <p class="total-summary__amount">
            {{ formatWon(displayedCost.totalCost) }}
          </p>
        </div>
      </section>

      <BaseCard
        class="cost-card"
        padding="22px 18px"
        aria-label="여행 비용 상세"
      >
        <h2 class="section-title text-label">비용 구성</h2>
        <div class="cost-bar" aria-label="여행 비용 구성 비율">
          <span
            v-for="item in costItems"
            :key="item.label"
            :style="{
              width: `${percentOf(item.value)}%`,
              background: item.color,
            }"
          />
        </div>

        <ul class="cost-list">
          <li v-for="item in costItems" :key="item.label">
            <span
              class="cost-list__dot"
              :style="{ background: item.color }"
            />
            <span class="cost-list__label">{{ item.label }}</span>
            <strong>{{ formatWon(item.value) }}</strong>
            <span class="cost-list__rate">{{ percentOf(item.value) }}%</span>
          </li>
        </ul>
      </BaseCard>

      <BaseCard padding="22px 18px" class="budget-card">
        <h2 class="section-title text-label">내 예산과 비교</h2>
        <div class="budget-card__summary">
          <dl>
            <div>
              <dt>내가 입력한 예산</dt>
              <dd>{{ formatWon(totalBudget) }}</dd>
            </div>
            <div>
              <dt>예상 여행 경비</dt>
              <dd>{{ formatWon(displayedCost.totalCost) }}</dd>
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
        <div
          class="budget-progress"
          :class="{ 'is-over': budgetDifference < 0 }"
          aria-label="예산 사용률"
        >
          <span :style="{ width: `${budgetUsageRate}%` }" />
        </div>
      </BaseCard>

      <section class="cost-guide text-caption" aria-label="예상 비용 안내">
        <strong>안내</strong>
        <p>· 계산 결과는 예상 금액이며 실제 비용과 다를 수 있습니다.</p>
      </section>

      <p v-if="saveError" class="save-error text-caption" role="alert">
        {{ saveError }}
      </p>
    </template>

    <BottomButtonBar
      v-if="!loading && !loadError"
      :primary-label="savingStyle ? '저장 중...' : '다 음'"
      secondary-label="이 전"
      :primary-disabled="savingStyle"
      @primary-click="goNext"
      @secondary-click="goPrevious"
    />
  </div>
</template>

<style scoped>
.travel-cost {
  min-height: 100%;
  padding: 18px 0 96px;
  color: var(--text-strong);
}

.travel-cost :deep(.character-slider) {
  margin-bottom: 26px;
}

.style-selector {
  margin-bottom: 28px;
}

.style-selector__panel {
  padding: 14px 8px 16px;
  border-radius: 12px;
  background: var(--kb-yellow-pale);
}

.style-selector__title {
  margin: 0 0 10px;
}

.style-selector__buttons {
  display: flex;
  gap: 10px;
  padding: 8px;
  background: var(--surface-subtle);
}

.style-selector__buttons :deep(.category-btn) {
  min-height: 46px;
  padding: 8px 10px;
  font-size: 14px;
}

.style-selector__description {
  min-height: 44px;
  margin: 8px 4px 0;
  color: var(--text-body);
  line-height: 1.55;
  white-space: pre-line;
  word-break: keep-all;
}

.total-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 26px;
  margin: 0 0 32px;
}

.total-summary__icon {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: 10px;
  background: var(--kb-yellow-pale);
  place-items: center;
}

.total-summary__icon img {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.total-summary__label {
  margin: 0 0 8px;
  color: var(--brand-gold);
  font-size: 14px;
  font-weight: 500;
}

.total-summary__amount {
  margin: 0;
  color: var(--kb-gray);
  font-size: 29px;
  font-weight: 500;
  line-height: 1.15;
}

.cost-card,
.budget-card {
  margin-bottom: 28px;
}

.section-title {
  margin: 0;
}

.cost-bar {
  display: flex;
  height: 30px;
  margin-top: 24px;
  overflow: hidden;
  border-radius: 10px;
  background: var(--surface-muted);
}

.cost-bar span {
  display: flex;
  height: 100%;
  min-width: 0;
  align-items: center;
  justify-content: center;
}

.cost-list {
  display: grid;
  gap: 12px;
  margin: 26px 0 0;
  padding: 0;
  list-style: none;
}

.cost-list li {
  display: grid;
  grid-template-columns: 12px 1fr auto 36px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.cost-list__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.cost-list__label,
.cost-list strong {
  color: var(--text-strong);
  font-weight: 600;
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
  margin-top: 22px;
}

.budget-card__summary dl {
  display: grid;
  gap: 14px;
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
  width: 88px;
  height: 88px;
  flex: 0 0 88px;
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
  margin-top: 26px;
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

.budget-progress.is-over span {
  background: var(--danger);
}

.cost-guide {
  margin-bottom: 16px;
  padding: 18px;
  border-radius: 12px;
  background: var(--surface-subtle);
  color: var(--text-muted);
}

.cost-guide strong {
  font-size: 13px;
  font-weight: 500;
}

.cost-guide p {
  margin: 10px 0 0;
  line-height: 1.6;
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
  background: var(--travel-primary);
  color: var(--surface-default);
  font-family: inherit;
  font-size: 12px;
}

.save-error {
  margin: 0 0 12px;
  color: var(--danger);
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

.travel-cost :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.travel-cost :deep(.bottom-button-bar .bar-button.secondary) {
  background: var(--kb-gray-pale);
  color: var(--text-body);
}

.travel-cost :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}
</style>
