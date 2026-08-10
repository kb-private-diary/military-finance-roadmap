<script setup>
// SCR-MAIN-01 · 메인 요약 화면 (담당: 지원)
// 복무 정보·관심 로드맵 자금 현황·다가오는 일정·후회소비·전우들·추천 상품 조회

import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import { useRouter } from 'vue-router';

import mainApi from '@/api/mainApi';

import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import { formatManwon, formatWon } from '@/util/format';

import bearSalute from '@/assets/images/bear-salute.png';
import calendarIcon from '@/assets/images/calendar.png';

const router = useRouter();

// 메인 API 응답 데이터
const summary = ref(null);

// API 조회 상태
const isLoading = ref(false);
const hasError = ref(false);

// 추천 상품 배너 상태
const currentProductIndex = ref(0);
let productTimer = null;

const isFundInfoOpen = ref(false);

const toggleFundInfo = () => {
  isFundInfoOpen.value = !isFundInfoOpen.value;
};

// 로드맵 카테고리별 화면 표시 정보
const CATEGORY_MAP = {
  1: {
    code: 'TRAVEL',
    label: '여행',
    cardClass: 'roadmap-card--travel',
    tagVariant: 'pastel-blue',
  },
  2: {
    code: 'JOB',
    label: '진로',
    cardClass: 'roadmap-card--job',
    tagVariant: 'pastel-yellow',
  },
  3: {
    code: 'CAR',
    label: '자동차',
    cardClass: 'roadmap-card--car',
    tagVariant: 'pastel-green',
  },
  4: {
    code: 'RENT',
    label: '자취',
    cardClass: 'roadmap-card--rent',
    tagVariant: 'pastel-pink',
  },
};

// 관심 등록한 로드맵 최대 4개
const favoriteRoadmaps = computed(() =>
  (summary.value?.favoriteRoadmaps ?? []).slice(0, 4),
);

// 여행·진로 중 가장 가까운 일정 최대 2개
const upcomingSchedules = computed(() =>
  (summary.value?.upcomingSchedules ?? []).slice(0, 2),
);

// 관심 등록한 로드맵 목표 금액 합계
// amount가 null인 목표는 0원으로 계산
const plannedAmount = computed(() =>
  favoriteRoadmaps.value.reduce(
    (sum, roadmap) => sum + Number(roadmap.amount ?? 0),
    0,
  ),
);

// 군적금 예상 만기 수령액
const maturityAmount = computed(() =>
  Number(summary.value?.savings?.expectedMaturityTotal ?? 0),
);

// 사용 예정 금액이 만기 예상금에서 차지하는 비율
const usageRate = computed(() => {
  if (maturityAmount.value <= 0) {
    return 0;
  }

  return Math.min(
    Math.round((plannedAmount.value / maturityAmount.value) * 100),
    100,
  );
});

// 관심 로드맵 예상 비용이 만기 예상금을 초과했는지 여부
const isOverBudget = computed(
  () => maturityAmount.value > 0 && plannedAmount.value > maturityAmount.value,
);

// 만기 예상금 대비 남거나 부족한 금액
const budgetDifference = computed(() =>
  Math.abs(maturityAmount.value - plannedAmount.value),
);

// 도넛 차트 데이터
const roadmapFundChartItems = computed(() => {
  if (maturityAmount.value <= 0) {
    return [
      {
        label: '금액 정보 없음',
        value: 1,
        color: 'var(--line)',
      },
    ];
  }

  const usedAmount = Math.min(plannedAmount.value, maturityAmount.value);

  const remainingAmount = Math.max(maturityAmount.value - usedAmount, 0);

  return [
    {
      label: '사용 예정 금액',
      value: usedAmount,
      color: 'var(--kb-yellow)',
    },
    {
      label: '남은 금액',
      value: remainingAmount,
      color: 'var(--line)',
    },
  ];
});

// 전역일까지 남은 기본 복무일수
const dischargeDday = computed(() => {
  const basicInfo = summary.value?.basicInfo;

  if (!basicInfo) {
    return 0;
  }

  return Math.max(
    Number(basicInfo.totalServiceDays ?? 0) -
      Number(basicInfo.currentServiceDays ?? 0),
    0,
  );
});

// 전역 여부
const isDischarged = computed(() => {
  const dischargeDate = summary.value?.basicInfo?.dischargeDate;

  if (!dischargeDate) {
    return false;
  }

  const today = new Date();
  const discharge = new Date(`${dischargeDate}T00:00:00`);

  today.setHours(0, 0, 0, 0);

  return today >= discharge;
});

// 복무 중 날짜별 응원 문구
const ACTIVE_DAILY_MESSAGES = [
  '오늘도 고생하셨습니다!',
  '전역까지 한 걸음 더!',
  '오늘 하루도 힘내세요!',
  '오늘도 무사히 파이팅!',
  '오늘도 힘찬 하루!',
  '전역이 가까워지고 있어요!',
  '오늘도 좋은 하루 보내세요!',
];

// 전역 후 날짜별 응원 문구
const DISCHARGED_DAILY_MESSAGES = [
  '새로운 시작을 응원합니다!',
  '오늘도 힘찬 하루 보내세요!',
  '앞으로의 날들을 응원합니다!',
  '새로운 목표를 향해 출발!',
  '오늘도 좋은 하루 보내세요!',
  '전역 후 계획도 차근차근!',
];

// 같은 날짜에는 같은 문구 노출
const dailyMessage = computed(() => {
  const messages = isDischarged.value
    ? DISCHARGED_DAILY_MESSAGES
    : ACTIVE_DAILY_MESSAGES;

  const today = new Date();

  const dateKey = Math.floor(
    new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime() /
      (1000 * 60 * 60 * 24),
  );

  return messages[dateKey % messages.length];
});

// 현재 배너에 노출할 적금 상품
const currentProduct = computed(() => {
  const products = summary.value?.recommendedProducts ?? [];

  if (products.length === 0) {
    return null;
  }

  return products[currentProductIndex.value];
});

// 현재 추천 적금 상품 상세 페이지 이동
const goProductDetail = () => {
  if (!currentProduct.value?.productId) {
    return;
  }

  router.push({
    name: 'SavingProductDetail',
    params: {
      productId: currentProduct.value.productId,
    },
  });
};

// 카테고리별 화면 정보 반환
const getCategoryInfo = (categoryId) =>
  CATEGORY_MAP[categoryId] ?? {
    code: '',
    label: '로드맵',
    emoji: '🎯',
    cardClass: '',
    tagVariant: 'gray',
  };

// 관심 로드맵 상세 페이지 이동
const goRoadmapDetail = (roadmap) => {
  const categoryCode = getCategoryInfo(roadmap.categoryId).code;

  switch (categoryCode) {
    case 'TRAVEL':
      router.push({
        name: 'TravelGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    case 'JOB':
      router.push({
        name: 'JobGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    case 'CAR':
      router.push({
        name: 'CarGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    case 'RENT':
      router.push({
        name: 'RentGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    default:
      console.warn('지원하지 않는 카테고리입니다.', roadmap);
  }
};

// 금리 표시
const formatRate = (rate) => {
  const numberRate = Number(rate ?? 0);

  return Number.isInteger(numberRate)
    ? numberRate.toFixed(0)
    : numberRate.toFixed(2);
};

// 추천 상품 자동 전환 시작
const startProductSlider = () => {
  stopProductSlider();

  const products = summary.value?.recommendedProducts ?? [];

  if (products.length <= 1) {
    return;
  }

  productTimer = window.setInterval(() => {
    currentProductIndex.value =
      (currentProductIndex.value + 1) % products.length;
  }, 4000);
};

// 추천 상품 자동 전환 종료
const stopProductSlider = () => {
  if (productTimer === null) {
    return;
  }

  window.clearInterval(productTimer);
  productTimer = null;
};

// 사용자가 선택한 상품으로 이동
const selectProduct = (index) => {
  currentProductIndex.value = index;
  startProductSlider();
};

// 메인 요약 정보 조회
const fetchSummary = async () => {
  isLoading.value = true;
  hasError.value = false;

  try {
    summary.value = await mainApi.findSummary();
    currentProductIndex.value = 0;

    startProductSlider();
  } catch (error) {
    console.error('메인 요약 조회 실패:', error);

    summary.value = null;
    hasError.value = true;
  } finally {
    isLoading.value = false;
  }
};

onMounted(async () => {
  await fetchSummary();
});

onBeforeUnmount(() => {
  stopProductSlider();
});
</script>

<template>
  <main class="home-page">
    <!-- 조회 중 -->
    <div v-if="isLoading" class="home-state">
      <p class="text-caption">메인 정보를 불러오는 중입니다.</p>
    </div>

    <template v-else-if="summary">
      <!-- 전역 D-Day 카드 -->
      <section class="discharge-card">
        <div class="discharge-card__dday">
          <template v-if="!isDischarged">
            <span>전역까지</span>

            <strong>D-{{ dischargeDday }}</strong>
          </template>

          <template v-else>
            <span>복무 완료</span>

            <strong class="discharge-card__complete"> 전역 완료 </strong>
          </template>
        </div>

        <div class="discharge-card__divider"></div>

        <div class="discharge-card__message">
          <p>{{ dailyMessage }}</p>

          <strong v-if="!isDischarged">
            {{ summary.basicInfo?.name }}
            {{ summary.basicInfo?.rankName }}님
          </strong>

          <strong v-else> {{ summary.basicInfo?.name }}님 </strong>
        </div>

        <!-- 군인 캐릭터 -->
        <img :src="bearSalute" alt="" class="discharge-card__character" />
      </section>

      <!-- 관심 로드맵 자금 현황 -->
      <div class="fund-section-heading">
        <h2 class="text-title">관심 로드맵 자금 현황</h2>

        <button
          type="button"
          class="info-icon"
          aria-label="관심 로드맵 자금 현황 설명"
          :aria-expanded="isFundInfoOpen"
          @click="toggleFundInfo"
        >
          i
        </button>

        <div v-if="isFundInfoOpen" class="fund-info__tooltip" role="tooltip">
          관심 로드맵의 예상 비용과 군적금 예상 만기 수령액을 비교합니다.
        </div>
      </div>

      <BaseCard padding="14px 16px 16px" class="fund-card">
        <!-- 자금 현황 -->
        <div class="fund-summary-layout">
          <!-- 도넛 차트 -->
          <div class="chart-area">
            <DonutChart
              :items="roadmapFundChartItems"
              :size="118"
              :thickness="14"
              chart-label="군적금 만기 예상금 대비 관심 로드맵 사용 예정 금액"
            />

            <div class="chart-center">
              <template v-if="!isOverBudget">
                <div class="chart-center__rate">
                  <strong>{{ usageRate }}</strong>
                  <span>%</span>
                </div>

                <small>예정 사용률</small>
              </template>

              <template v-else>
                <span class="chart-center__warning">!</span>
                <strong class="chart-center__over-text">예산 초과</strong>
              </template>
            </div>
          </div>

          <!-- 금액 요약 -->
          <div class="amount-summary">
            <div class="amount-summary__item">
              <span
                class="amount-summary__icon amount-summary__icon--maturity"
                aria-hidden="true"
              >
                💰
              </span>

              <div class="amount-summary__content">
                <p>만기 예상금</p>

                <strong>
                  {{ formatWon(maturityAmount) }}
                </strong>
              </div>
            </div>

            <div class="amount-summary__divider"></div>

            <div class="amount-summary__item">
              <span
                class="amount-summary__icon amount-summary__icon--planned"
                aria-hidden="true"
              >
                💳
              </span>

              <div class="amount-summary__content">
                <p>로드맵 예상 비용</p>

                <strong>{{ formatWon(plannedAmount) }}</strong>

                <span v-if="isOverBudget" class="amount-summary__shortage">
                  {{ formatWon(budgetDifference) }} 부족
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 관심 등록 현황 -->
        <div class="favorite-section">
          <div class="favorite-heading">
            <span class="favorite-count">
              {{ favoriteRoadmaps.length }}/4
            </span>

            <p>
              카테고리별 1개까지 등록 가능
              <span class="info-icon">i</span>
            </p>
          </div>

          <!-- 관심 등록한 로드맵이 있을 때 -->
          <div v-if="favoriteRoadmaps.length > 0" class="roadmap-grid">
            <article
              v-for="roadmap in favoriteRoadmaps"
              :key="`${roadmap.categoryId}-${roadmap.goalId}`"
              class="roadmap-card"
              :class="getCategoryInfo(roadmap.categoryId).cardClass"
              @click="goRoadmapDetail(roadmap)"
            >
              <div class="roadmap-card__content">
                <BaseTag
                  :label="getCategoryInfo(roadmap.categoryId).label"
                  :variant="getCategoryInfo(roadmap.categoryId).tagVariant"
                />

                <strong class="roadmap-card__title">
                  {{ roadmap.title }}
                </strong>

                <p v-if="roadmap.amount != null" class="roadmap-card__amount">
                  {{ formatManwon(roadmap.amount) }}
                </p>

                <p
                  v-else
                  class="roadmap-card__amount roadmap-card__amount--pending"
                >
                  금액 계산 전
                </p>
              </div>
            </article>
          </div>

          <!-- 관심 등록한 로드맵이 없을 때 -->
          <div v-else class="favorite-empty">
            <span class="favorite-empty__icon" aria-hidden="true"> 🤍 </span>

            <p class="text-caption">관심 등록한 로드맵이 아직 없습니다.</p>
          </div>
        </div>
      </BaseCard>

      <!-- 다가오는 일정 -->
      <section
        v-if="upcomingSchedules.length > 0"
        class="schedule-section"
        :class="{
          'schedule-section--single': upcomingSchedules.length === 1,
        }"
      >
        <article
          v-for="schedule in upcomingSchedules"
          :key="`${schedule.category}-${schedule.title}-${schedule.scheduleDate}`"
          class="schedule-item"
        >
          <img
            :src="calendarIcon"
            alt=""
            class="schedule-item__icon"
            aria-hidden="true"
          />

          <div class="schedule-item__content">
            <span
              class="schedule-item__dday"
              :class="{
                'schedule-item__dday--travel': schedule.category === 'TRAVEL',
              }"
            >
              D-{{ schedule.dday }}
            </span>

            <strong class="schedule-item__title">
              {{ schedule.title }}
            </strong>
          </div>
        </article>
      </section>

      <div v-else class="schedule-empty">
        <p class="text-caption">다가오는 일정이 없습니다.</p>
      </div>

      <!-- 후회소비 -->
      <BaseCard
        padding="0 18px"
        class="summary-link-card"
        @click="router.push({ name: 'RegretDashboard' })"
      >
        <span class="summary-link-card__icon" aria-hidden="true">💸</span>
        <p class="summary-link-card__message">소비 점호 하러 가시겠습니까?</p>

        <span class="summary-link-card__arrow" aria-hidden="true"> › </span>
      </BaseCard>

      <!-- 전우들 -->
      <BaseCard
        padding="0 18px"
        class="summary-link-card"
        @click="router.push({ name: 'Social' })"
      >
        <span class="summary-link-card__icon" aria-hidden="true"> 👥 </span>

        <p class="summary-link-card__message">전우들과 비교해 보시겠습니까?</p>

        <span class="summary-link-card__arrow" aria-hidden="true"> › </span>
      </BaseCard>

      <!-- 추천 상품 배너 -->
      <section
        v-if="currentProduct"
        class="product-banner"
        @click="goProductDetail"
        @mouseenter="stopProductSlider"
        @mouseleave="startProductSlider"
      >
        <div class="product-banner__content">
          <strong>
            {{ currentProduct.productName }}
          </strong>

          <span>
            {{ currentProduct.saveTrm }}개월 기준, 최대 연
            {{ formatRate(currentProduct.maxRate) }}% 금리
          </span>

          <div
            v-if="(summary.recommendedProducts ?? []).length > 1"
            class="product-banner__dots"
            @click.stop
          >
            <button
              v-for="(_, index) in summary.recommendedProducts"
              :key="index"
              type="button"
              :class="{
                'product-banner__dot--active': currentProductIndex === index,
              }"
              :aria-label="`${index + 1}번째 상품 보기`"
              @click="selectProduct(index)"
            ></button>
          </div>
        </div>

        <div class="product-banner__image" aria-hidden="true">
          <span class="product-banner__card">💳</span>
          <span class="product-banner__coins">🪙</span>
        </div>
      </section>
    </template>

    <!-- 조회 실패 -->
    <div v-else class="home-state">
      <p class="text-caption">
        {{
          hasError
            ? '메인 정보를 불러오지 못했습니다.'
            : '표시할 정보가 없습니다.'
        }}
      </p>

      <button type="button" class="home-state__retry" @click="fetchSummary">
        다시 시도
      </button>
    </div>
  </main>
</template>

<style scoped>
.home-page {
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 18px;
  padding: 18px 16px 100px;
  box-sizing: border-box;
}

.home-state {
  padding: 80px 20px;
  text-align: center;
}

.home-state p {
  margin: 0;
}

.home-state__retry {
  margin-top: 16px;
  padding: 9px 18px;
  color: var(--text-body);
  font-family: inherit;
  font-weight: 500;
  background: var(--kb-yellow);
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

/* ── 전역 D-Day ── */

.discharge-card {
  position: relative;
  display: grid;
  min-height: 104px;
  grid-template-columns: 92px 1px minmax(0, 1fr);
  align-items: center;
  overflow: hidden;
  padding: 16px 88px 16px 20px;
  color: var(--surface-default);
  background: linear-gradient(
    135deg,
    var(--travel-primary) 0%,
    var(--travel-primary-dark) 100%
  );
  border-radius: 16px;
  box-shadow: 0 6px 18px rgb(41 78 31 / 18%);
  box-sizing: border-box;
}

.discharge-card__dday {
  display: flex;
  flex-direction: column;
}

.discharge-card__dday .discharge-card__complete {
  font-size: 18px;
  line-height: 1.2;
  white-space: nowrap;
}

.discharge-card__dday span {
  margin-bottom: 5px;
  font-size: 12px;
  font-weight: 400;
}

.discharge-card__dday strong {
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
}

.discharge-card__divider {
  width: 1px;
  height: 48px;
  background: var(--overlay-white-30);
}

.discharge-card__message {
  min-width: 0;
  padding-left: 16px;
}

.discharge-card__message p {
  margin: 0;
  color: var(--kb-yellow);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  word-break: keep-all;
}

.discharge-card__message strong {
  display: block;
  margin-top: 2px;
  color: var(--kb-yellow);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
  white-space: nowrap;
}

.discharge-card__character {
  position: absolute;
  right: -1px;
  bottom: -14px;
  width: 98px;
  height: auto;
  object-fit: contain;
  pointer-events: none;
  user-select: none;
}

/* ── 자금 현황 ── */

.fund-card {
  width: 100%;
  box-sizing: border-box;
}

.fund-section-heading {
  position: relative;
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
}

.fund-section-heading h2 {
  margin: 0;
}

/* 정보 아이콘 */
.info-icon {
  display: inline-flex;
  width: 17px;
  height: 17px;
  padding: 0;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  color: var(--text-hint);
  font-family: inherit;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;

  background: transparent;
  border: 1px solid var(--line-strong);
  border-radius: 50%;
  box-sizing: border-box;
  cursor: pointer;
}

/* 정보 툴팁 */
.fund-info__tooltip {
  position: absolute;
  z-index: 20;
  top: calc(100% + 10px);
  left: 0;

  width: min(280px, calc(100vw - 64px));
  padding: 10px 12px;
  box-sizing: border-box;

  color: var(--text-body);
  font-size: 11px;
  font-weight: 400;
  line-height: 1.5;
  word-break: keep-all;

  background: var(--surface-default);
  border: 1px solid var(--line);
  border-radius: 10px;
  box-shadow: 0 4px 12px var(--shadow-dropdown);
}

/*
 * 도넛 영역을 118px로 줄이고
 * 오른쪽 금액 영역에 최대한 많은 너비를 배정한다.
 */
.fund-summary-layout {
  display: grid;
  width: 100%;
  grid-template-columns: 118px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  box-sizing: border-box;
}

/* 도넛 차트 */

.chart-area {
  position: relative;
  display: flex;
  width: 118px;
  height: 118px;
  align-items: center;
  justify-content: center;
}

.chart-area :deep(svg) {
  display: block;
  width: 118px;
  height: 118px;
  flex-shrink: 0;
  overflow: visible;
}

.chart-center {
  position: absolute;
  top: 50%;
  left: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translate(-50%, -50%);
}

.chart-center__rate {
  display: flex;
  align-items: baseline;
}

.chart-center__rate strong {
  color: var(--text-strong);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.chart-center__rate span {
  margin-left: 2px;
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
}

.chart-center small {
  margin-top: 5px;
  padding: 3px 6px;
  color: var(--brand-gold);
  font-size: 8px;
  font-weight: 700;
  background: var(--kb-yellow-pale);
  border-radius: 999px;
  white-space: nowrap;
}

.chart-center__warning {
  display: flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;

  color: var(--surface-default);
  font-size: 14px;
  font-weight: 700;

  background: var(--warning-strong);
  border-radius: 50%;
}

.chart-center__over-text {
  color: var(--danger);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.amount-summary__shortage {
  display: block;
  margin-top: 4px;

  color: var(--danger);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

/* 금액 요약 */

.amount-summary {
  display: flex;
  width: 100%;
  min-width: 0;
  flex-direction: column;
}

.amount-summary__item {
  display: grid;
  width: 100%;
  min-width: 0;
  grid-template-columns: 32px minmax(0, 1fr);
  align-items: center;
  gap: 6px;
}

.amount-summary__icon {
  display: flex;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  font-size: 15px;
  background: var(--surface-default);
  border: 1px solid var(--line);
  border-radius: 50%;
  box-sizing: border-box;
}

.amount-summary__icon--maturity,
.amount-summary__icon--planned {
  background: var(--surface-default);
}

.amount-summary__content {
  width: 100%;
  min-width: 0;
}

.amount-summary__content p {
  margin: 0 0 4px;
  color: var(--text-muted);
  font-size: 10px;
  line-height: 1.2;
  white-space: nowrap;
}

/*
 * 금액에 overflow, text-overflow를 사용하지 않는다.
 * API 금액을 무조건 한 줄로 전부 표시한다.
 */
.amount-summary__content strong {
  display: block;
  width: 100%;
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: -1.2px;
  line-height: 1.2;
  white-space: nowrap;
}

.amount-summary__divider {
  width: 100%;
  height: 1px;
  margin: 12px 0;
  background: var(--line);
}

/* ── 관심 로드맵 ── */

.favorite-section {
  padding-top: 12px;
  border-top: 1px dashed var(--line-strong);
}

.favorite-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 13px;
}

.favorite-count {
  flex-shrink: 0;
  padding: 2px 6px;
  color: var(--military-green);
  font-size: 11px;
  font-weight: 600;
  background: var(--military-green-light);
  border-radius: 6px;
  white-space: nowrap;
}

.favorite-heading > p {
  display: flex;
  max-width: 116px;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin: 0;
  color: var(--text-hint);
  font-size: 9px;
  line-height: 1.4;
  text-align: right;
}

.roadmap-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 9px;
}

.roadmap-card {
  position: relative;
  display: flex;
  min-width: 0;
  min-height: 86px;
  align-items: stretch;
  overflow: hidden;
  padding: 10px 12px;

  color: inherit;
  text-decoration: none;
  cursor: pointer;

  border: 1px solid var(--line);
  border-radius: 12px;
  box-sizing: border-box;
}

.roadmap-card--travel {
  background: var(--surface-subtle);
  border-color: var(--pastel-blue);
}

.roadmap-card--job {
  background: var(--kb-yellow-pale);
  border-color: var(--pastel-yellow);
}

.roadmap-card--car {
  background: var(--military-green-light);
  border-color: var(--pastel-green);
}

.roadmap-card--rent {
  background: #fff4f6;
  border-color: var(--pastel-pink);
}

.roadmap-card__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
}

.roadmap-card__content :deep(.base-tag) {
  margin-bottom: 3px;
  padding: 2px 6px;
  font-size: 9px;
}

.roadmap-card__title {
  display: -webkit-box;
  overflow: hidden;
  max-width: 100%;
  margin-top: 1px;

  color: var(--text-strong);
  font-size: 11px;
  font-weight: 600;
  line-height: 1.4;

  word-break: keep-all;
  overflow-wrap: break-word;

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.roadmap-card__amount {
  overflow: hidden;
  max-width: 100%;
  margin: 2px 0 0;
  color: var(--brand-gold);
  font-size: 10px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.roadmap-card__amount--pending {
  color: var(--text-hint);
  font-weight: 400;
}

.favorite-empty {
  display: flex;
  min-height: 68px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-hint);
  background: var(--surface-subtle);
  border-radius: 12px;
}

.favorite-empty__icon {
  font-size: 18px;
}

.favorite-empty p {
  margin: 0;
}

/* ── 다가오는 일정 ── */
.schedule-section {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 2px 0;
}

.schedule-section--single {
  grid-template-columns: 1fr;
}

.schedule-item {
  display: flex;
  min-width: 0;
  align-items: flex-start;
  gap: 7px;
  padding: 8px 4px;
  box-sizing: border-box;
}

.schedule-item__icon {
  display: block;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  object-fit: contain;
}

.schedule-item__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 1px;
}

.schedule-item__dday {
  flex-shrink: 0;
  color: var(--notification);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
}

.schedule-item__dday--travel {
  color: var(--info-blue);
}

.schedule-item__title {
  display: -webkit-box;
  overflow: hidden;
  max-width: 100%;

  color: var(--text-strong);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.35;

  word-break: keep-all;
  overflow-wrap: break-word;

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.schedule-empty {
  width: 100%;
  padding: 12px 0;
  text-align: center;
}

.schedule-empty p {
  margin: 0;
}

/* ── 후회소비·전우들 ── */

.summary-link-card {
  display: flex;
  min-height: 62px;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  box-sizing: border-box;
  cursor: pointer;
}

.summary-link-card__icon {
  display: flex;
  width: 28px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  line-height: 1;
}

.summary-link-card__message {
  min-width: 0;
  flex: 1;
  margin: 0;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
}
.summary-link-card__arrow {
  display: flex;
  width: 18px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 20px;
  line-height: 1;
}

/* ── 추천 상품 배너 ── */

.product-banner {
  position: relative;
  display: flex;
  min-height: 104px;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  padding: 18px 22px;
  cursor: pointer;
  background: linear-gradient(
    100deg,
    var(--kb-yellow) 0%,
    var(--kb-yellow-pale) 100%
  );
  border: 1px solid var(--kb-yellow-deep);
  border-radius: 16px;
  box-shadow: 0 5px 14px var(--focus-ring-yellow);
  box-sizing: border-box;
}

.product-banner__content {
  position: relative;
  z-index: 1;
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  padding-right: 12px;
}

.product-banner__content strong {
  overflow: hidden;
  max-width: 100%;
  color: var(--text-strong);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-banner__content > span {
  margin-top: 4px;
  color: var(--kb-gray);
  font-size: 11px;
}

.product-banner__image {
  position: relative;
  width: 62px;
  height: 54px;
  flex-shrink: 0;
}

.product-banner__card {
  position: absolute;
  top: 2px;
  left: 0;
  font-size: 40px;
  transform: rotate(-8deg);
}

.product-banner__coins {
  position: absolute;
  right: 0;
  bottom: 0;
  font-size: 24px;
}

.product-banner__dots {
  display: flex;
  gap: 5px;
  margin-top: 9px;
}

.product-banner__dots button {
  width: 6px;
  height: 6px;
  padding: 0;
  background: var(--kb-gray);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0.35;
}

.product-banner__dots .product-banner__dot--active {
  width: 16px;
  border-radius: 8px;
  opacity: 1;
}

/* 아주 좁은 모바일 화면 */

@media (max-width: 380px) {
  .home-page {
    padding-inline: 12px;
  }

  .discharge-card {
    grid-template-columns: 84px 1px minmax(0, 1fr);
    padding-right: 76px;
  }

  .discharge-card__dday strong {
    font-size: 27px;
  }

  .discharge-card__character {
    right: -6px;
    bottom: -10px;
    width: 86px;
  }

  .fund-summary-layout {
    grid-template-columns: 108px minmax(0, 1fr);
    gap: 6px;
  }

  .chart-area {
    width: 108px;
    height: 108px;
  }

  .chart-area :deep(svg) {
    width: 108px;
    height: 108px;
  }

  .chart-center__rate strong {
    font-size: 26px;
  }

  .chart-center__rate span {
    font-size: 14px;
  }

  .chart-center small {
    font-size: 7px;
  }

  .amount-summary__item {
    grid-template-columns: 28px minmax(0, 1fr);
    gap: 5px;
  }

  .amount-summary__icon {
    width: 28px;
    height: 28px;
    font-size: 13px;
  }

  .amount-summary__content p {
    font-size: 9px;
  }

  .amount-summary__content strong {
    font-size: 11px;
    letter-spacing: -1.3px;
  }

  .favorite-heading > p {
    display: none;
  }

  .schedule-section {
    gap: 7px;
  }

  .schedule-item {
    gap: 5px;
    padding-inline: 8px;
  }

  .schedule-item__icon {
    width: 28px;
    height: 28px;
    font-size: 15px;
  }

  .schedule-item__title,
  .schedule-item__dday {
    font-size: 11px;
  }
}
</style>
