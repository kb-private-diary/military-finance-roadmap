<script setup>
// SCR-TRV-02 · step2) 여행 비용 계산
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { formatWon } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const loading = ref(true);
const loadError = ref('');
const cost = ref(null);
let scrollContainer = null;

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

const costItems = computed(() => [
  {
    label: '숙소비',
    value: toAmount(cost.value?.hotelCost),
    color: 'var(--chart-1)',
  },
  {
    label: '관광비',
    value: toAmount(cost.value?.livingCost),
    color: 'var(--chart-3)',
  },
  {
    label: '교통비',
    value: toAmount(cost.value?.flightCost),
    color: 'var(--chart-4)',
  },
]);

const chartTotal = computed(() =>
  costItems.value.reduce((sum, item) => sum + item.value, 0),
);

const percentOf = (value) => {
  if (!chartTotal.value) return 0;
  return Math.round((toAmount(value) / chartTotal.value) * 100);
};

onMounted(async () => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');

  try {
    if (route.query.recalculate === 'true') {
      await travelApi.createCost(goalId);
      const response = await travelApi.findCost(goalId);
      cost.value = unwrap(response);
      await router.replace({
        name: 'TravelCost',
        params: { goalId },
      });
    } else {
      try {
        const response = await travelApi.findCost(goalId);
        cost.value = unwrap(response);
      } catch (error) {
        if (error.response?.data?.code !== 'TRAVEL_006') {
          throw error;
        }

        await travelApi.createCost(goalId);
        const response = await travelApi.findCost(goalId);
        cost.value = unwrap(response);
      }
    }
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

const goPrevious = () => router.back();
const goNext = () =>
  router.push({ name: 'TravelPlaces', params: { goalId } });
</script>

<template>
  <div class="travel-cost">
    <RoadmapCharacterSlider :progress="34" label="여행 로드맵" />

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
      <h2 class="travel-cost__title text-title">
        총 예상 여행 비용입니다.
      </h2>
      <p class="travel-cost__total">{{ formatWon(cost.totalCost) }}</p>

      <BaseCard
        class="cost-card"
        padding="29px 22px 31px"
        aria-label="여행 비용 상세"
      >
        <div class="chart-area">
          <DonutChart
            :items="costItems"
            :size="174"
            :thickness="38"
            chart-label="숙소비, 관광비, 교통비 비율"
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
      </BaseCard>
    </template>

    <BottomButtonBar
      v-if="!loading && !loadError"
      primary-label="다 음"
      secondary-label="이 전"
      @primary-click="goNext"
      @secondary-click="goPrevious"
    />
  </div>
</template>

<style scoped>
.travel-cost {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.travel-cost :deep(.character-slider) {
  margin-bottom: 28px;
}

.travel-cost__title {
  margin: 0 0 14px;
  line-height: 1.35;
  text-align: center;
}

.travel-cost__total {
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
  grid-template-columns: 49px minmax(0, 1fr) 38px;
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
