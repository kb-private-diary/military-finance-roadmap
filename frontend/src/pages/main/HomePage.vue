<script setup>
// SCR-MAIN-01 · 메인 요약 화면 (담당: 지원)
// 복무 정보·관심 로드맵 자금 현황·다가오는 일정·후회소비·전우들·추천 상품 조회

import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import mainApi from '@/api/mainApi';

import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import LikeButton from '@/components/common/LikeButton.vue';

import soldierCharacter from '@/assets/images/bear-salute.png';

// 메인 API 응답 데이터
const summary = ref(null);

// API 조회 상태
const isLoading = ref(false);
const hasError = ref(false);

// 추천 상품 배너 상태
const currentProductIndex = ref(0);
let productTimer = null;

// 로드맵 카테고리별 화면 표시 정보
const CATEGORY_MAP = {
  1: {
    label: '여행',
    emoji: '✈️',
    cardClass: 'roadmap-card--travel',
    tagVariant: 'blue',
  },
  2: {
    label: '진로',
    emoji: '💻',
    cardClass: 'roadmap-card--job',
    tagVariant: 'yellow',
  },
  3: {
    label: '자동차',
    emoji: '🚗',
    cardClass: 'roadmap-card--car',
    tagVariant: 'green-light',
  },
  4: {
    label: '자취',
    emoji: '🏠',
    cardClass: 'roadmap-card--rent',
    tagVariant: 'purple',
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

// 전역일까지 남은 일수
const dischargeDday = computed(() => {
  const dischargeDate = summary.value?.basicInfo?.dischargeDate;

  if (!dischargeDate) {
    return 0;
  }

  const today = new Date();
  const targetDate = new Date(`${dischargeDate}T00:00:00`);

  today.setHours(0, 0, 0, 0);

  const difference = targetDate.getTime() - today.getTime();

  return Math.max(Math.ceil(difference / (1000 * 60 * 60 * 24)), 0);
});

// 현재 배너에 노출할 적금 상품
const currentProduct = computed(() => {
  const products = summary.value?.recommendedProducts ?? [];

  if (products.length === 0) {
    return null;
  }

  return products[currentProductIndex.value];
});

// 전우들 상위 비율
// social 정보가 응답에 없으면 null 반환
const socialTopRate = computed(() => {
  const social = summary.value?.social;
  const rank = Number(social?.savingsRank ?? 0);
  const memberCount = Number(social?.comparisonMemberCount ?? 0);

  if (rank <= 0 || memberCount <= 0) {
    return null;
  }

  return Math.max(Math.round((rank / memberCount) * 100), 1);
});

// 카테고리별 화면 정보 반환
const getCategoryInfo = (categoryId) =>
  CATEGORY_MAP[categoryId] ?? {
    label: '로드맵',
    emoji: '🎯',
    cardClass: '',
    tagVariant: 'gray',
  };

// 일정 카테고리별 이모지 반환
const getScheduleEmoji = (category) => (category === 'TRAVEL' ? '🗓️' : '📅');

// 금액 표시
const formatAmount = (amount) => `${Number(amount ?? 0).toLocaleString()}원`;

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
          <span>전역까지</span>

          <strong> D-{{ dischargeDday }} </strong>
        </div>

        <div class="discharge-card__divider"></div>

        <div class="discharge-card__message">
          <p>오늘도 고생하셨습니다!</p>

          <strong>
            {{ summary.basicInfo?.rankName }}
            {{ summary.basicInfo?.name }}님
          </strong>
        </div>

        <!-- 군인 캐릭터 -->
        <img :src="soldierCharacter" alt="" class="discharge-card__character" />
      </section>

      <!-- 관심 로드맵 자금 현황 -->
      <BaseCard padding="20px 16px 16px" class="fund-card">
        <div class="section-heading">
          <h2 class="text-title">관심 로드맵 자금 현황</h2>

          <span
            class="info-icon"
            title="관심 목표 금액과 군적금 예상 만기 수령액을 비교합니다."
          >
            i
          </span>
        </div>

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
              <div class="chart-center__rate">
                <strong>{{ usageRate }}</strong>
                <span>%</span>
              </div>

              <small>목표 달성률</small>
            </div>

            <span class="chart-sparkle chart-sparkle--left" aria-hidden="true">
              ✦
            </span>

            <span class="chart-sparkle chart-sparkle--right" aria-hidden="true">
              ✦
            </span>
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
                  {{ formatAmount(maturityAmount) }}
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
                <p>사용 예정 금액</p>

                <strong>
                  {{ formatAmount(plannedAmount) }}
                </strong>
              </div>
            </div>
          </div>
        </div>

        <!-- 관심 등록 로드맵 -->
        <div class="favorite-section">
          <div class="favorite-heading">
            <div class="favorite-heading__title">
              <h3 class="text-label">관심 등록한 로드맵</h3>

              <span> {{ favoriteRoadmaps.length }}/4 </span>
            </div>

            <p>
              카테고리별 1개까지 등록 가능
              <span class="info-icon">i</span>
            </p>
          </div>

          <div v-if="favoriteRoadmaps.length > 0" class="roadmap-grid">
            <article
              v-for="roadmap in favoriteRoadmaps"
              :key="`${roadmap.categoryId}-${roadmap.goalId}`"
              class="roadmap-card"
              :class="getCategoryInfo(roadmap.categoryId).cardClass"
            >
              <span class="roadmap-card__icon" aria-hidden="true">
                {{ getCategoryInfo(roadmap.categoryId).emoji }}
              </span>

              <div class="roadmap-card__content">
                <BaseTag
                  :label="getCategoryInfo(roadmap.categoryId).label"
                  :variant="getCategoryInfo(roadmap.categoryId).tagVariant"
                />

                <strong class="roadmap-card__title">
                  {{ roadmap.title }}
                </strong>

                <p v-if="roadmap.amount != null" class="roadmap-card__amount">
                  {{ formatAmount(roadmap.amount) }}
                </p>

                <p
                  v-else
                  class="roadmap-card__amount roadmap-card__amount--pending"
                >
                  금액 계산 전
                </p>
              </div>

              <!-- 홈에서는 북마크 상태만 표시 -->
              <div class="roadmap-card__like">
                <LikeButton :model-value="true" />
              </div>
            </article>
          </div>

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
        <BaseCard
          v-for="schedule in upcomingSchedules"
          :key="`${schedule.category}-${schedule.title}-${schedule.scheduleDate}`"
          padding="0"
          class="schedule-card"
        >
          <article class="schedule-item">
            <span
              class="schedule-item__icon"
              :class="{
                'schedule-item__icon--travel': schedule.category === 'TRAVEL',
              }"
              aria-hidden="true"
            >
              {{ getScheduleEmoji(schedule.category) }}
            </span>

            <strong class="schedule-item__title">
              {{ schedule.title }}
            </strong>

            <span
              class="schedule-item__dday"
              :class="{
                'schedule-item__dday--travel': schedule.category === 'TRAVEL',
              }"
            >
              D-{{ schedule.dday }}
            </span>

            <span class="schedule-item__arrow" aria-hidden="true"> › </span>
          </article>
        </BaseCard>
      </section>

      <BaseCard v-else padding="22px" class="schedule-empty">
        <p class="text-caption">다가오는 일정이 없습니다.</p>
      </BaseCard>

      <!-- 후회소비 -->
      <RouterLink :to="{ name: 'RegretDashboard' }" class="summary-link">
        <BaseCard padding="0 18px" class="summary-link-card">
          <span class="summary-link-card__icon" aria-hidden="true"> 💸 </span>

          <strong class="text-label">후회 소비</strong>

          <span
            class="summary-link-card__value summary-link-card__value--danger"
          >
            {{ formatAmount(summary.regret?.regretAmount) }}
          </span>

          <span class="summary-link-card__arrow" aria-hidden="true"> › </span>
        </BaseCard>
      </RouterLink>

      <!-- 전우들 -->
      <RouterLink :to="{ name: 'Social' }" class="summary-link">
        <BaseCard padding="0 18px" class="summary-link-card">
          <span class="summary-link-card__icon" aria-hidden="true"> 👥 </span>

          <strong class="text-label">전우들</strong>

          <span class="summary-link-card__value">
            {{
              socialTopRate !== null ? `상위 ${socialTopRate}%` : '연동 예정'
            }}
          </span>

          <span class="summary-link-card__arrow" aria-hidden="true"> › </span>
        </BaseCard>
      </RouterLink>

      <!-- 추천 상품 배너 -->
      <section
        v-if="currentProduct"
        class="product-banner"
        @mouseenter="stopProductSlider"
        @mouseleave="startProductSlider"
      >
        <div class="product-banner__content">
          <p class="product-banner__title">KB 금융 적금 파이팅 상품</p>

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
  gap: 14px;
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

.section-heading {
  display: flex;
  align-items: center;
  gap: 7px;
}

.section-heading h2 {
  margin: 0;
}

.info-icon {
  display: inline-flex;
  width: 17px;
  height: 17px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
  font-size: 10px;
  font-weight: 700;
  border: 1px solid var(--line-strong);
  border-radius: 50%;
  box-sizing: border-box;
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
  padding: 22px 0;
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

.chart-sparkle {
  position: absolute;
  color: var(--kb-yellow-deep);
  font-size: 13px;
  line-height: 1;
}

.chart-sparkle--left {
  top: 1px;
  left: 3px;
}

.chart-sparkle--right {
  right: 2px;
  bottom: 3px;
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
  border-radius: 50%;
  box-sizing: border-box;
}

.amount-summary__icon--maturity {
  background: var(--kb-yellow-pale);
}

.amount-summary__icon--planned {
  background: var(--military-green-light);
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
  padding-top: 16px;
  border-top: 1px dashed var(--line-strong);
}

.favorite-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 13px;
}

.favorite-heading__title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 7px;
}

.favorite-heading__title h3 {
  margin: 0;
  font-size: 14px;
  white-space: nowrap;
}

.favorite-heading__title > span {
  padding: 3px 7px;
  color: var(--military-green);
  font-size: 11px;
  font-weight: 700;
  background: var(--military-green-light);
  border-radius: 8px;
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
  align-items: center;
  gap: 8px;
  overflow: hidden;
  padding: 11px 28px 11px 9px;
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

.roadmap-card__icon {
  display: flex;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  background: var(--surface-default);
  border-radius: 50%;
  box-shadow: 0 3px 10px var(--shadow-dropdown);
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
  overflow: hidden;
  max-width: 100%;
  color: var(--text-strong);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.roadmap-card__amount {
  overflow: hidden;
  max-width: 100%;
  margin: 3px 0 0;
  color: var(--brand-gold);
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.roadmap-card__amount--pending {
  color: var(--text-hint);
  font-weight: 400;
}

.roadmap-card__like {
  position: absolute;
  top: 7px;
  right: 6px;
  pointer-events: none;
}

.favorite-empty {
  display: flex;
  min-height: 88px;
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
  gap: 10px;
}

/* 일정이 1개면 가로 전체 너비 사용 */
.schedule-section--single {
  grid-template-columns: 1fr;
}

.schedule-card {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.schedule-item {
  display: grid;
  min-width: 0;
  min-height: 64px;
  grid-template-columns: 32px minmax(0, 1fr) auto 10px;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  box-sizing: border-box;
}

.schedule-item__icon {
  display: flex;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  background: #fff2f2;
  border-radius: 9px;
}

.schedule-item__icon--travel {
  background: #eef5ff;
}

.schedule-item__title {
  overflow: hidden;
  min-width: 0;
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-item__dday {
  color: var(--danger);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.schedule-item__dday--travel {
  color: #2479dd;
}

.schedule-item__arrow {
  color: var(--text-muted);
  font-size: 20px;
  line-height: 1;
}

.schedule-empty {
  width: 100%;
  text-align: center;
}

.schedule-empty p {
  margin: 0;
}

/* ── 후회소비·전우들 ── */

.summary-link {
  color: inherit;
  text-decoration: none;
}

.summary-link-card {
  display: grid;
  min-height: 62px;
  grid-template-columns: 30px minmax(0, 1fr) auto 12px;
  align-items: center;
  gap: 10px;
}

.summary-link-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
}

.summary-link-card__value {
  color: var(--success);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.summary-link-card__value--danger {
  color: var(--danger);
}

.summary-link-card__arrow {
  color: var(--text-muted);
  font-size: 22px;
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

.product-banner__title {
  margin: 0 0 5px;
  color: var(--kb-dark-gray);
  font-size: 15px;
  font-weight: 700;
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
    grid-template-columns: 28px minmax(0, 1fr) auto 8px;
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
