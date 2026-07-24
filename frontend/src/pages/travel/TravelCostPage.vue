<script setup>
// SCR-TRV-02 · step2) 여행 비용 계산
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const loading = ref(true);
const loadError = ref('');
const cost = ref(null);
let scrollContainer = null;

const readErrorMessage = (error, fallback) =>
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
    color: '#6e5f52',
  },
  {
    label: '식비',
    label: '관광비',
    value: toAmount(cost.value?.livingCost),
    color: '#d8bd76',
  },
  {
    label: '교통비',
    value: toAmount(cost.value?.flightCost),
    color: '#ffebba',
  },
]);

const chartTotal = computed(() =>
  costItems.value.reduce((sum, item) => sum + item.value, 0),
);

const segments = computed(() => {
  if (!chartTotal.value) return [];

  let accumulated = 0;
  return costItems.value.map((item) => {
    const start = (accumulated / chartTotal.value) * 100;
    accumulated += item.value;
    const end = (accumulated / chartTotal.value) * 100;
    return `${item.color} ${start}% ${end}%`;
  });
});

const donutStyle = computed(() => ({
  background: segments.value.length
    ? `conic-gradient(${segments.value.join(', ')})`
    : '#ececec',
}));

const percentOf = (value) => {
  if (!chartTotal.value) return 0;
  return Math.round((toAmount(value) / chartTotal.value) * 100);
};

const formatWon = (value) =>
  `${toAmount(value).toLocaleString('ko-KR')}원`;

onMounted(async () => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');

  try {
    await api.post(`/api/travel/goals/${goalId}/costs`, {});
    const response = await api.get(`/api/travel/goals/${goalId}/costs`);
    cost.value = unwrap(response);
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
    <section class="roadmap-step" aria-label="여행 로드맵 2단계">
      <p class="roadmap-step__label">여행 로드맵</p>
      <div class="roadmap-step__progress">
        <span class="roadmap-step__number">2</span>
        <span class="roadmap-step__line">
          <span class="roadmap-step__line-fill" />
        </span>
      </div>
    </section>

    <div v-if="loading" class="status-box" role="status">
      예상 비용을 계산하고 있습니다.
    </div>

    <div v-else-if="loadError" class="status-box status-box--error" role="alert">
      <p>{{ loadError }}</p>
      <button type="button" @click="goPrevious">이전 화면으로</button>
    </div>

    <template v-else-if="cost">
      <h2 class="travel-cost__title">총 예상 여행 비용입니다.</h2>
      <p class="travel-cost__total">{{ formatWon(cost.totalCost) }}</p>

      <section class="cost-card" aria-label="여행 비용 상세">
        <div class="chart-area">
          <div
            class="donut-chart"
            :style="donutStyle"
            role="img"
            aria-label="숙소비, 식비, 교통비 비율을 나타내는 도넛 차트"
          >
            <span class="donut-chart__hole" />
          </div>

          <ul class="legend">
            <li v-for="item in costItems" :key="item.label">
              <span
                class="legend__swatch"
                :style="{ backgroundColor: item.color }"
              />
              <span>{{ item.label }}</span>
            </li>
          </ul>
        </div>

        <ul class="breakdown">
          <li v-for="item in costItems" :key="item.label">
            <span class="breakdown__label">{{ item.label }}</span>
            <span class="breakdown__bar">
              <span
                class="breakdown__fill"
                :style="{
                  width: `${percentOf(item.value)}%`,
                  backgroundColor: item.color,
                }"
              />
            </span>
            <strong>{{ percentOf(item.value) }}%</strong>
          </li>
        </ul>
      </section>
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
  color: #111;
}

.roadmap-step {
  margin-bottom: 34px;
}

.roadmap-step__label {
  margin: 0 0 15px;
  color: #565656;
  font-size: 12px;
}

.roadmap-step__progress {
  position: relative;
  display: flex;
  align-items: center;
  height: 22px;
}

.roadmap-step__number {
  position: relative;
  z-index: 2;
  display: grid;
  width: 22px;
  height: 22px;
  margin-left: 25%;
  place-items: center;
  border: 2px solid #657052;
  border-radius: 50%;
  background: #fff;
  color: #293020;
  font-size: 12px;
  font-weight: 700;
  transform: translateX(-50%);
}

.roadmap-step__line {
  position: absolute;
  right: 10px;
  left: 10px;
  height: 4px;
  overflow: hidden;
  background: #dedede;
}

.roadmap-step__line-fill {
  display: block;
  width: 25%;
  height: 100%;
  background: #657052;
}

.travel-cost__title {
  margin: 0 0 14px;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.35;
  text-align: center;
}

.travel-cost__total {
  margin: 0 0 18px;
  color: #74695c;
  font-size: 27px;
  font-weight: 500;
  line-height: 1.25;
  text-align: center;
}

.cost-card {
  padding: 29px 22px 31px;
  border: 1px solid #cfcfcf;
  border-radius: 13px;
}

.chart-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 23px;
  margin-bottom: 36px;
}

.donut-chart {
  position: relative;
  display: grid;
  width: 174px;
  height: 174px;
  flex: 0 0 174px;
  place-items: center;
  border-radius: 50%;
  transform: rotate(-8deg);
}

.donut-chart__hole {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  background: #fff;
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
  color: #4f4b45;
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
  color: #58534c;
  font-size: 12px;
}

.breakdown__label {
  font-weight: 500;
}

.breakdown__bar {
  height: 6px;
  overflow: hidden;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #ddd;
  background: #fff;
}

.breakdown__fill {
  display: block;
  height: 100%;
  min-width: 0;
}

.breakdown strong {
  color: #333;
  font-size: 12px;
  font-weight: 600;
  text-align: right;
}

.status-box {
  margin-top: 80px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

.status-box--error {
  color: #d34b4b;
}

.status-box p {
  margin: 0 0 16px;
}

.status-box button {
  padding: 9px 16px;
  border: 0;
  background: #657052;
  color: #fff;
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
  background: #fff;
}

.travel-cost :deep(.bottom-button-bar .bar-button.secondary) {
  background: #e8e8e8;
  color: #333;
}

.travel-cost :deep(.bottom-button-bar .bar-button.primary) {
  background: #ffcc00;
  color: #111;
}
</style>
