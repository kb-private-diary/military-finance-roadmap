<script setup>
// SCR-TRV-02 · step2) 여행 비용 계산
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import takeoffIcon from '@/assets/images/takeoff.png';
import landingIcon from '@/assets/images/landing.png';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import GoalSummaryCard from '@/components/common/GoalSummaryCard.vue';
import EstimatedCostCard from '@/components/common/EstimatedCostCard.vue';
import BudgetGauge from '@/components/common/BudgetGauge.vue';
import { formatManwon } from '@/util/format';
import { findDomesticRegion } from './domesticRegions';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const loading = ref(true);
const loadError = ref('');
const saveError = ref('');
const cost = ref(null);
const goal = ref(null); // step1에서 입력한 여행 목표(제목·출발/도착·일정·예산)
const selectedStyle = ref('common');
const savedStyle = ref('common');
const savingStyle = ref(false);
let scrollContainer = null;

// ── 요약 카드용 파생값 ──────────────────────────────
const fmtMD = (iso) => {
  if (!iso) return '';
  const [, m, d] = iso.split('-');
  return `${Number(m)}.${Number(d)}`;
};
const tripDateRange = computed(() =>
  goal.value ? `${fmtMD(goal.value.startDate)} ~ ${fmtMD(goal.value.endDate)}` : '',
);
const tripNights = computed(() => {
  if (!goal.value?.startDate || !goal.value?.endDate) return '';
  const nights = Math.round(
    (new Date(goal.value.endDate) - new Date(goal.value.startDate)) / 86400000,
  );
  return `${nights}박 ${nights + 1}일`;
});
const tripDday = computed(() => {
  if (!goal.value?.startDate) return '';
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = Math.ceil((new Date(goal.value.startDate) - today) / 86400000);
  if (days > 0) return `D-${days}`;
  return days === 0 ? 'D-DAY' : `D+${-days}`;
});
const tripBudget = computed(() =>
  goal.value?.totalBudget
    ? `${Math.round(goal.value.totalBudget / 10000).toLocaleString()}만`
    : '',
);
// 도착지가 국내 지역이면 국내여행, 아니면 해외여행 (자취 오피스텔 칩처럼)
const tripScopeLabel = computed(() =>
  goal.value
    ? findDomesticRegion(goal.value.destination)
      ? '국내여행'
      : '해외여행'
    : '',
);

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

// 비용 구성(물통 + 리스트) — 숙소비 / 관광비 / 교통비. 자동차와 같은 4색 팔레트 계열.
// 위→아래 작은 것부터(물통 바닥에 큰 항목), 맨 아래 항목만 물통에 이름 표시.
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

const totalBudget = computed(() =>
  toAmount(displayedCost.value?.totalCost) +
  Number(displayedCost.value?.remainingBudget || 0),
);

// 게이지용 내 예산 (만원) — step1에서 입력한 고정 예산 우선, 없으면 파생값
const gaugeBudget = computed(() =>
  Math.round((goal.value?.totalBudget || totalBudget.value) / 10000),
);

onMounted(async () => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');

  // 요약 카드용 여행 목표 정보 (실패해도 비용 화면은 계속 표시)
  travelApi
    .findCurrentGoal()
    .then((response) => {
      goal.value = response.data?.data ?? null;
    })
    .catch(() => {});

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

    <PageHeader
      title="여행 로드맵의 예상 비용 내역입니다."
      description="원하는 여행 스타일에 따라 예상 비용이 달라집니다."
    />

    <!-- step1 입력값 기반 여행 목표 요약 카드 (공통 GoalSummaryCard) -->
    <GoalSummaryCard
      v-if="goal"
      accent="var(--kb-yellow)"
      class="travel-cost__summary"
      :chip="tripScopeLabel"
      :title="goal.title"
      :route="{
        from: { label: '출발지', value: goal.departure, icon: takeoffIcon },
        to: { label: '도착지', value: goal.destination, icon: landingIcon },
      }"
      :specs="[
        { label: '일정', value: tripDateRange },
        { label: '기간', value: tripNights },
        { label: '남은 기간', value: tripDday },
        { label: '예산', value: `${tripBudget}원` },
      ]"
    />

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
      <section class="style-selector" aria-label="여행 스타일 선택">
        <div class="style-selector__panel">
          <TabBar
            variant="segment"
            v-model="selectedStyle"
            :tabs="STYLE_OPTIONS"
          />
        </div>
        <p class="style-selector__description text-caption">
          {{ selectedStyleOption.description }}
        </p>
      </section>

      <EstimatedCostCard
        v-if="travelState"
        class="travel-cost__cost"
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

      <p v-if="saveError" class="save-error text-caption" role="alert">
        {{ saveError }}
      </p>
    </template>

    <BottomButtonBar
      v-if="!loading && !loadError"
      :primary-label="savingStyle ? '저장 중...' : '다음'"
      secondary-label="이전"
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

/* step1 목표 요약 카드 — 제목/슬라이더와의 간격을 다른 로드맵(20px)에 맞춤 */
.travel-cost__summary {
  margin-top: 20px;
  margin-bottom: 20px;
}

.travel-cost :deep(.character-slider) {
  margin-bottom: 20px;
}

.style-selector {
  margin-bottom: 28px;
}

/* 크림 패널 제거 → 학교/지역 선택처럼 평평하게 (밑줄 탭만) */
.style-selector__panel {
  padding: 0;
}


.style-selector__description {
  min-height: 44px;
  margin: 8px 4px 0;
  color: var(--text-body);
  line-height: 1.55;
  white-space: pre-line;
  word-break: keep-all;
}

.travel-cost__cost {
  margin-bottom: 28px;
}
/* 카드 안: 제목 → 게이지 → 선 하나 → 표 */
.travel-cost__cost-title {
  margin: 0;
  padding: 15px 16px 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.travel-cost__gauge {
  padding: 16px 16px 14px;
}
.travel-cost__mid {
  height: 1px;
  margin: 0 16px; /* 밑에 표(좌우 16px 여백) 너비에 맞춤 */
  background: #e7e9ec; /* divider-thin(#dde0e3)보다 연하게 */
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
