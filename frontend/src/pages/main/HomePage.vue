<script setup>
// SCR-MAIN-01 · 메인 요약 화면 (담당: 지원)
// 복무 정보·관심 로드맵 자금 현황·다가오는 일정·후회소비·전우들·추천 상품 조회

import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import { useRouter } from 'vue-router';

import mainApi from '@/api/mainApi';
import regretApi from '@/api/regretApi';
import socialApi from '@/api/socialApi';

import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import LikeButton from '@/components/common/LikeButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import { formatWon } from '@/util/format';

import bibiChar from '@/assets/images/char-bibi.png';
import calendarIcon from '@/assets/images/calendar.png';
import commanderIcon from '@/assets/images/commander.png';

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
    icon: '✈️',
    cardClass: 'roadmap-card--travel',
    tagVariant: 'travel',
  },
  2: {
    code: 'JOB',
    label: '진로',
    icon: '💼',
    cardClass: 'roadmap-card--job',
    tagVariant: 'job',
  },
  3: {
    code: 'CAR',
    label: '자동차',
    icon: '🚗',
    cardClass: 'roadmap-card--car',
    tagVariant: 'car',
  },
  4: {
    code: 'RENT',
    label: '자취',
    icon: '🏠',
    cardClass: 'roadmap-card--rent',
    tagVariant: 'rent',
  },
};

// 관심 등록한 로드맵(등록 개수 제한 없음)
const favoriteRoadmaps = computed(() => summary.value?.favoriteRoadmaps ?? []);

// 캐러셀에서 현재 노출 중인 관심 로드맵 위치
const currentFavoriteIndex = ref(0);

// 이동 버튼·인디케이터는 관심 로드맵이 2개 이상일 때만 노출
const hasMultipleFavorites = computed(() => favoriteRoadmaps.value.length > 1);

// 현재 카드에 노출할 관심 로드맵
const currentFavorite = computed(
  () => favoriteRoadmaps.value[currentFavoriteIndex.value] ?? null,
);

// 이전 카드 (첫 카드에서 이전을 누르면 마지막 카드로 순환)
const goPrevFavorite = () => {
  const len = favoriteRoadmaps.value.length;
  if (len === 0) return;
  currentFavoriteIndex.value = (currentFavoriteIndex.value - 1 + len) % len;
};

// 다음 카드 (마지막 카드에서 다음을 누르면 첫 카드로 순환)
const goNextFavorite = () => {
  const len = favoriteRoadmaps.value.length;
  if (len === 0) return;
  currentFavoriteIndex.value = (currentFavoriteIndex.value + 1) % len;
};

// 인디케이터를 눌러 특정 카드로 이동
const selectFavorite = (index) => {
  currentFavoriteIndex.value = index;
};

// 스와이프로 인정할 최소 이동 거리(px)
const FAVORITE_SWIPE_THRESHOLD = 40;

let favoriteTouchStartX = 0;

// 스와이프 시작 지점 기록
const handleFavoriteTouchStart = (event) => {
  favoriteTouchStartX = event.changedTouches[0].clientX;
};

// 이동 거리와 방향으로 이전·다음 카드 판정
const handleFavoriteTouchEnd = (event) => {
  const movedDistance = event.changedTouches[0].clientX - favoriteTouchStartX;

  if (Math.abs(movedDistance) < FAVORITE_SWIPE_THRESHOLD) {
    return;
  }

  if (movedDistance < 0) {
    goNextFavorite();

    return;
  }

  goPrevFavorite();
};

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

  return Math.round((plannedAmount.value / maturityAmount.value) * 100);
});

// 관심 로드맵 예상 비용이 만기 예상금을 초과했는지 여부
const isOverBudget = computed(
  () => maturityAmount.value > 0 && plannedAmount.value > maturityAmount.value,
);

// 만기 예상금 대비 남거나 부족한 금액
const budgetDifference = computed(() =>
  Math.abs(maturityAmount.value - plannedAmount.value),
);

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

// 현재 추천 적금 상품이 선택된 상태로 예적금 상품 계산기 화면 이동
const goProductDetail = () => {
  if (!currentProduct.value?.productId) {
    return;
  }

  router.push({
    name: 'SimulatorProductList',
    query: {
      tab: 'savings',
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

// 일정 카테고리 코드(TRAVEL/JOB 등) → 라벨·뱃지색 (다가오는 일정 도메인 구분)
const scheduleCategoryLabel = (code) =>
  Object.values(CATEGORY_MAP).find((c) => c.code === code)?.label ?? '일정';
const scheduleCategoryVariant = (code) =>
  Object.values(CATEGORY_MAP).find((c) => c.code === code)?.tagVariant ?? 'gray';

// 일정 날짜 "M월 D일 (요일)" 포맷
const SCHEDULE_DOW = ['일', '월', '화', '수', '목', '금', '토'];
const formatScheduleDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(`${dateStr}T00:00:00`);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getMonth() + 1}월 ${d.getDate()}일 (${SCHEDULE_DOW[d.getDay()]})`;
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
// ── 후회소비·전우들 미리보기 (홈 요약 API엔 없어서 각 API 직접 조회) ──
const regretStats = ref(null);
const socialStats = ref(null);

// 이번 달 후회소비 요약
const loadRegretPreview = async () => {
  const ym = `${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}`;
  try {
    regretStats.value = await regretApi.getMonthlyStats(ym);
  } catch {
    regretStats.value = null;
  }
};

// 전우들 저축률 비교 요약
const loadSocialPreview = async () => {
  try {
    socialStats.value = await socialApi.findStats('ALL');
  } catch {
    socialStats.value = null;
  }
};

// 이번 달 후회소비 금액 (없으면 null → 기본 문구)
const regretPreviewAmount = computed(() =>
  regretStats.value?.regretAmount != null ? regretStats.value.regretAmount : null,
);

// 내 저축률 상위 백분율 "상위 X%" (모수 부족 시 null)
const socialPercentileLabel = computed(() => {
  const s = socialStats.value;
  if (!s) return null;
  const member = s.comparisonMemberCount;
  const higher = s.higherSavingsCount;
  if (!member || higher == null || member < 5) return null;
  const top = Math.ceil(((higher + 1) * 100) / member);
  return top <= 80 ? `상위 ${top}%` : null;
});

// 백분율이 없을 땐 내 저축률(%)로 대체
const socialRate = computed(() =>
  socialStats.value?.savingsRate != null ? socialStats.value.savingsRate : null,
);

const fetchSummary = async () => {
  isLoading.value = true;
  hasError.value = false;

  try {
    summary.value = await mainApi.findSummary();
    currentProductIndex.value = 0;
    currentFavoriteIndex.value = 0;

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
  // 미리보기는 보조 정보라 병렬로, 실패해도 화면엔 영향 없음
  loadRegretPreview();
  loadSocialPreview();
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
            <span>축하합니다</span>

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
        <img :src="bibiChar" alt="" class="discharge-card__character" />
      </section>

      <!-- 관심 로드맵 자금 현황 -->
      <div class="fund-section-heading">
        <div class="fund-section-heading__text">
          <p class="fund-section-heading__eyebrow">목표 자금, 어디까지 왔나?</p>
          <h2 class="text-title">관심 로드맵 자금 현황</h2>
        </div>

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
              :progress="Math.min(usageRate / 100, 1)"
              :gradient="['#FFE08A', '#F4B400']"
              :overlay="isOverBudget ? Math.min((usageRate - 100) / 100, 1) : null"
              :overlay-gradient="['#C98A3E', '#6B3A24']"
              track-color="var(--surface-muted)"
              :size="168"
              :thickness="14"
              chart-label="군적금 만기 예상금 대비 관심 로드맵 사용 예정 금액"
            />

            <div class="chart-center">
              <div
                class="chart-center__rate"
                :class="{ 'chart-center__rate--over': isOverBudget }"
              >
                <strong>{{ usageRate }}</strong>
                <span>%</span>
              </div>

              <small :class="{ 'chart-center__badge--over': isOverBudget }">
                예정 사용률
              </small>
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
              </div>
            </div>
          </div>
        </div>

        <!-- 만기 예상금 초과 안내 -->
        <div v-if="isOverBudget" class="fund-over-message">
          <div class="commander">
            <span class="commander__avatar">
              <img :src="commanderIcon" alt="사령관" />
            </span>
            <div class="commander__bubble">
              <strong>목표까지 실탄이 부족하다!</strong>
              <p>
                예상 만기금보다 약
                <span>{{ formatWon(budgetDifference) }}</span>
                더 확보하라
              </p>
            </div>
          </div>
        </div>

        <!-- 관심 등록 현황 -->
        <div class="favorite-section">
          <!-- 관심 등록한 로드맵이 있을 때 -->
          <div
            v-if="currentFavorite"
            class="favorite-carousel"
            @touchstart.passive="handleFavoriteTouchStart"
            @touchend.passive="handleFavoriteTouchEnd"
          >
            <button
              v-if="hasMultipleFavorites"
              type="button"
              class="favorite-nav favorite-nav--prev"
              aria-label="이전 관심 로드맵 보기"
              @click="goPrevFavorite"
            >
              ‹
            </button>

            <!-- 뒤에 쌓인 카드 (장식용, 2장 이상일 때만 노출) -->
            <span
              v-if="favoriteRoadmaps.length > 2"
              class="favorite-deck-layer favorite-deck-layer--back"
              aria-hidden="true"
            ></span>

            <span
              v-if="hasMultipleFavorites"
              class="favorite-deck-layer favorite-deck-layer--mid"
              aria-hidden="true"
            ></span>

            <article
              class="roadmap-card"
              :class="getCategoryInfo(currentFavorite.categoryId).cardClass"
              @click="goRoadmapDetail(currentFavorite)"
            >
              <!-- 관심 등록 상태 표시 전용 (해제는 로드맵 화면에서) -->
              <span class="roadmap-card__like" aria-hidden="true">
                <LikeButton :model-value="true" />
              </span>

              <div class="roadmap-card__head">
                <BaseTag
                  :label="getCategoryInfo(currentFavorite.categoryId).label"
                  :variant="getCategoryInfo(currentFavorite.categoryId).tagVariant"
                />
                <strong class="roadmap-card__title">
                  {{ currentFavorite.title }}
                </strong>
              </div>

              <p v-if="currentFavorite.detail" class="roadmap-card__detail">
                {{ currentFavorite.detail }}
              </p>

              <div class="roadmap-card__cost">
                <strong
                  v-if="currentFavorite.amount != null"
                  class="roadmap-card__amount"
                >
                  {{ formatWon(currentFavorite.amount) }}
                </strong>

                <strong
                  v-else
                  class="roadmap-card__amount roadmap-card__amount--pending"
                >
                  금액 계산 전
                </strong>
              </div>

              <span class="roadmap-card__icon" aria-hidden="true">
                {{ getCategoryInfo(currentFavorite.categoryId).icon }}
              </span>

              <div
                v-if="hasMultipleFavorites"
                class="favorite-dots"
                @click.stop
              >
                <button
                  v-for="(_, index) in favoriteRoadmaps"
                  :key="index"
                  type="button"
                  :class="{
                    'favorite-dot--active': currentFavoriteIndex === index,
                  }"
                  :aria-label="`${index + 1}번째 관심 로드맵 보기`"
                  @click="selectFavorite(index)"
                ></button>
              </div>
            </article>

            <button
              v-if="hasMultipleFavorites"
              type="button"
              class="favorite-nav favorite-nav--next"
              aria-label="다음 관심 로드맵 보기"
              @click="goNextFavorite"
            >
              ›
            </button>
          </div>

          <!-- 관심 등록한 로드맵이 없을 때 -->
          <EmptyState
            v-else
            class="favorite-empty"
            title="관심 등록한 로드맵이 없어요"
            description="로드맵에서 하트를 눌러 등록해보세요"
          >
            <template #icon>
              <LikeButton :model-value="false" />
            </template>
          </EmptyState>
        </div>

      </BaseCard>

      <!-- 추천 상품 배너 (광고) - 작전 대기 중 위로 -->
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

      <!-- 하단 브리핑 섹션 제목 (자금 현황 제목과 동일 패턴) -->
      <div class="fund-section-heading">
        <div class="fund-section-heading__text">
          <p class="fund-section-heading__eyebrow">전역까지, 오늘의 상황</p>
          <h2 class="text-title">병영 브리핑</h2>
        </div>
      </div>

      <!-- 다가오는 일정 (관심 로드맵과 별개 - 여행/진로 시험 등 도메인별 일정) -->
      <BaseCard
        v-if="upcomingSchedules.length > 0"
        class="schedule-card"
        padding="18px 16px 16px"
      >
        <p class="schedule-card__title">작전 대기 중</p>

        <section class="schedule-list">
          <article
            v-for="schedule in upcomingSchedules"
            :key="`${schedule.category}-${schedule.title}-${schedule.scheduleDate}`"
            class="schedule-item"
          >
            <div class="schedule-item__head">
              <img
                :src="calendarIcon"
                class="schedule-item__icon"
                alt=""
                aria-hidden="true"
              />
              <BaseTag
                :label="scheduleCategoryLabel(schedule.category)"
                :variant="scheduleCategoryVariant(schedule.category)"
              />
              <span
                class="schedule-item__dday"
                :class="{
                  'schedule-item__dday--travel': schedule.category === 'TRAVEL',
                  'schedule-item__dday--job': schedule.category === 'JOB',
                }"
              >
                D-{{ schedule.dday }}
              </span>
            </div>

            <strong class="schedule-item__title">
              {{ schedule.title }}
            </strong>

            <span class="schedule-item__date">
              {{ formatScheduleDate(schedule.scheduleDate) }}
            </span>
          </article>
        </section>
      </BaseCard>

      <!-- 후회소비 -->
      <button
        type="button"
        class="home-cta-banner home-cta-banner--regret"
        @click="router.push({ name: 'RegretDashboard' })"
      >
        <div class="home-cta-banner__text">
          <p class="home-cta-banner__eyebrow">이번 달 후회한 소비</p>
          <strong class="home-cta-banner__value">
            {{
              regretPreviewAmount != null
                ? formatWon(regretPreviewAmount)
                : '소비 돌아보기'
            }}
          </strong>
          <span class="home-cta-banner__cta">
            소비 점호 하러 가기
            <i class="home-cta-banner__chevron" aria-hidden="true">›</i>
          </span>
        </div>
        <span class="home-cta-banner__emoji" aria-hidden="true">💸</span>
      </button>

      <!-- 전우들 -->
      <button
        type="button"
        class="home-cta-banner home-cta-banner--social"
        @click="router.push({ name: 'Social' })"
      >
        <div class="home-cta-banner__text">
          <p class="home-cta-banner__eyebrow">전우들 사이 내 저축률</p>
          <strong class="home-cta-banner__value">
            {{
              socialPercentileLabel ??
              (socialRate != null ? `저축률 ${socialRate}%` : '전우들과 비교')
            }}
          </strong>
          <span class="home-cta-banner__cta">
            전우들과 비교하기
            <i class="home-cta-banner__chevron" aria-hidden="true">›</i>
          </span>
        </div>
        <span class="home-cta-banner__emoji" aria-hidden="true">👥</span>
      </button>
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
  /* 밀리터리 얼룩(카모) - 올리브 베이스 위에 카키·다크그린 얼룩 */
  background-color: #5c6739;
  background-image: radial-gradient(
      ellipse 42% 40% at 16% 26%,
      #414d2b 0%,
      #414d2b 32%,
      transparent 60%
    ),
    radial-gradient(
      ellipse 38% 44% at 73% 16%,
      #838a55 0%,
      #838a55 28%,
      transparent 56%
    ),
    radial-gradient(
      ellipse 50% 46% at 90% 70%,
      #37421f 0%,
      #37421f 34%,
      transparent 62%
    ),
    radial-gradient(
      ellipse 40% 50% at 40% 84%,
      #6d7642 0%,
      #6d7642 30%,
      transparent 58%
    );
  border-radius: 16px;
  box-shadow: 0 6px 18px rgb(95 107 57 / 22%);
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
  right: 6px;
  bottom: -6px;
  width: 74px;
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
  align-items: flex-end;
  gap: 7px;
  margin: 0;
}

.fund-section-heading__text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 소제목(eyebrow) 목돈작전 PageHeader 기준(13px/600/muted)으로 통일 */
.fund-section-heading__eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
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

/* 도넛(왼쪽) + 금액(바로 오른쪽)을 왼쪽에 붙여 배치 */
.fund-summary-layout {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 8px 0;
  box-sizing: border-box;
}

/* 도넛 차트 */

.chart-area {
  position: relative;
  display: flex;
  width: 168px;
  height: 168px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
}

.chart-area :deep(svg) {
  display: block;
  width: 168px;
  height: 168px;
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

.chart-center__badge--over {
  color: var(--surface-default);
  background: var(--kb-yellow);
}

/* ── 만기 예상금 초과 안내 ── */

.fund-over-message {
  padding: 2px 0 12px;
}

/* 사령관이 말하는 것처럼: 아바타 + 말풍선 */
.commander {
  display: flex;
  align-items: center;
  gap: 10px;
}

.commander__avatar {
  display: flex;
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #ffffff;
  border: 2px solid var(--camo-rust);
  border-radius: 50%;
  box-sizing: border-box;
}

.commander__avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  /* 원에 맞춰 두고 살짝만 아래로 */
  transform: translateY(3px);
}

.commander__bubble {
  position: relative;
  flex: 1;
  min-width: 0;
  padding: 11px 14px;
  background: var(--camo-rust-tint);
  border-radius: 16px;
  box-shadow: 0 2px 7px rgba(107, 58, 36, 0.13);
}

/* 말풍선 꼬리 (아바타 쪽을 가리키는 삼각형) */
.commander__bubble::before {
  content: '';
  position: absolute;
  top: 50%;
  left: -8px;
  transform: translateY(-50%);
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-right: 9px solid var(--camo-rust-tint);
}

.commander__bubble strong {
  display: block;
  color: var(--camo-rust);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
  word-break: keep-all;
}

.commander__bubble p {
  margin: 3px 0 0;
  color: var(--text-body);
  font-size: 10.5px;
  line-height: 1.4;
}

.commander__bubble p span {
  color: var(--camo-rust);
  font-weight: 700;
}

/* 금액 요약 */

.amount-summary {
  display: flex;
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
  font-size: 12px;
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
  color: #333333;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.5px;
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
  padding-top: 14px;
  border-top: 1px dashed var(--line-strong);
}

.favorite-carousel {
  position: relative;
  width: 100%;
  /* 좌우 여백으로 카드를 안쪽으로 줄여 화살표와 간격 확보 */
  padding: 0 18px 6px;
}
/*
  * 뒤에 카드가 더 있다는 걸 좌우로 삐져나온 색 레이어로 보여준다.
  * 실제 카드 내용은 렌더링하지 않는다.
 */
.favorite-deck-layer {
  position: absolute;
  z-index: 0;
  display: block;
  border-radius: 14px;
  background: var(--surface-default);
  border: 1px solid var(--line);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 뒤에 카드가 여러 장 쌓인 느낌 - 위·좌우로 살짝 삐져나온 흰 카드(계단식, 대칭) */
.favorite-deck-layer--mid {
  top: 6px;
  bottom: 6px;
  left: 7px;
  right: 7px;
}

.favorite-deck-layer--back {
  top: 2px;
  bottom: 10px;
  left: 14px;
  right: 14px;
}

.favorite-nav {
  position: absolute;
  z-index: 2;
  top: 50%;
  display: flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  padding: 0;

  color: var(--text-muted);
  font-family: inherit;
  font-size: 15px;
  line-height: 1;

  background: var(--surface-default);
  border: 1px solid var(--line);
  border-radius: 50%;
  box-shadow: 0 2px 6px var(--shadow-dropdown);
  box-sizing: border-box;
  cursor: pointer;
  transform: translateY(-50%);
}

.favorite-nav--prev {
  left: -7px;
}

.favorite-nav--next {
  right: -7px;
}

.roadmap-card {
  position: relative;
  display: flex;
  width: 100%;
  min-width: 0;
  min-height: 84px;
  flex-direction: column;
  align-items: flex-start;
  overflow: hidden;
  padding: 13px 80px 26px 16px;

  color: inherit;
  text-decoration: none;
  cursor: pointer;

  border: 1px solid var(--line);
  border-radius: 14px;
  box-sizing: border-box;
}

.roadmap-card--travel {
  background: #fbfdff;
  border-color: var(--pastel-blue);
}

.roadmap-card--job {
  background: #fffefa;
  border-color: var(--pastel-yellow);
}

.roadmap-card--car {
  background: #fbfefb;
  border-color: var(--pastel-green);
}

.roadmap-card--rent {
  background: #fffcfc;
  border-color: var(--pastel-pink);
}

.roadmap-card :deep(.base-tag) {
  padding: 3px 11px;
  font-size: 11px;
  font-weight: 700;
}

.roadmap-card__like {
  position: absolute;
  top: 9px;
  right: 16px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: var(--danger);
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
}

/* 뱃지 + 이름 한 줄 (like 버튼 우상단 공간 확보) */
.roadmap-card__head {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding-right: 30px;
}
.roadmap-card__title {
  overflow: hidden;
  min-width: 0;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.roadmap-card__detail {
  overflow: hidden;
  max-width: 100%;
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.roadmap-card__cost {
  display: flex;
  flex-direction: column;
  gap: 1px;
  margin-top: 6px;
}

.roadmap-card__amount {
  overflow: hidden;
  max-width: 100%;
  margin: 0;

  color: var(--brand-gold);
  font-size: 16px;
  font-weight: 800;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.roadmap-card__amount--pending {
  color: var(--text-hint);
  font-size: 11px;
  font-weight: 400;
}

.roadmap-card__icon {
  position: absolute;
  right: 18px;
  bottom: 14px;
  display: flex;
  width: 54px;
  height: 54px;
  align-items: center;
  justify-content: center;

  font-size: 26px;

  background: var(--surface-default);
  border-radius: 14px;
  box-shadow: 0 2px 8px var(--shadow-dropdown);
}

.favorite-dots {
  position: absolute;
  bottom: 11px;
  left: 16px;
  display: flex;
  gap: 5px;
}

.favorite-dots button {
  width: 6px;
  height: 6px;
  padding: 0;
  background: var(--kb-gray);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0.3;
}

.favorite-dots .favorite-dot--active {
  width: 16px;
  border-radius: 8px;
  opacity: 1;
}

/* 카드 안쪽에 들어가므로 기본 EmptyState보다 여백과 글자를 줄인다. */
.favorite-empty {
  display: flex;
  min-height: 100px;
  flex-direction: column;
  justify-content: center;
  padding: 12px 16px;
  box-sizing: border-box;
}

.favorite-empty :deep(.empty-state__icon) {
  width: 30px;
  height: 30px;
  margin-bottom: 7px;
}

.favorite-empty :deep(.empty-state__icon .like-button) {
  width: 18px;
  height: 18px;
  pointer-events: none;
}

.favorite-empty :deep(.empty-state__icon .like-button svg) {
  width: 16px;
  height: 16px;
}

.favorite-empty :deep(.empty-state__icon .like-button svg path) {
  stroke: var(--text-hint);
}

.favorite-empty :deep(.empty-state__title) {
  margin-bottom: 4px;
  font-size: 13px;
}

.favorite-empty :deep(.empty-state__description) {
  font-size: 12px;
}

/* ── 다가오는 일정 (관심 로드맵과 별개 카드) ── */
.schedule-card__title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.schedule-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.schedule-item {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

/* 도메인 뱃지 + 디데이 한 줄 */
.schedule-item__head {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.schedule-item__dday {
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
}
/* 디데이 색: 여행=파랑, 진로=골든 (도메인 구분) */
.schedule-item__dday--travel {
  color: var(--info-blue);
}
.schedule-item__dday--job {
  color: var(--theme-job);
}
/* 달력 아이콘 */
.schedule-item__icon {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
  object-fit: contain;
}
/* 뱃지 작게 (BaseTag 기본이 반응형이라 여기선 컴팩트하게) */
.schedule-item__head :deep(.base-tag) {
  padding: 2px 7px;
  font-size: 10px;
}

.schedule-item__title {
  overflow: hidden;
  max-width: 100%;
  margin-top: 1px;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 일정 날짜 (제목 아래 작은 캡션) */
.schedule-item__date {
  overflow: hidden;
  max-width: 100%;
  color: var(--text-muted);
  font-size: 11px;
  font-weight: 500;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── 후회소비·전우들 ── */

/* 홈 CTA 배너 (후회소비·전우들) - 목돈작전 calc-banner 톤 통일: 초록 그라데이션 + 제목 + CTA */
.home-cta-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  padding: 18px 20px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(105deg, #e5ecdd 0%, #d6e1c9 100%);
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.15s ease;
}

/* 후회소비: 소프트 연주황(살구/피치) (전우들 그린과 구분) */
.home-cta-banner--regret {
  background: linear-gradient(105deg, #fdf1e2 0%, #fae1c3 100%);
}
.home-cta-banner--regret .home-cta-banner__eyebrow {
  color: #b0783a;
}
.home-cta-banner--regret .home-cta-banner__value {
  color: #c26410;
}
.home-cta-banner--regret .home-cta-banner__cta {
  color: #b06a2a;
}

.home-cta-banner:active {
  transform: scale(0.99);
}
.home-cta-banner__text {
  display: flex;
  flex-direction: column;
  gap: 5px;
  z-index: 1;
}
/* 실데이터 미리보기: 소제목 + 큰 값 */
.home-cta-banner__eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--camo-green);
}
.home-cta-banner__value {
  font-size: 19px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -0.02em;
  color: var(--camo-forest);
}
.home-cta-banner__cta {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 3px;
  color: var(--military-green);
  font-size: 13px;
  font-weight: 700;
}
.home-cta-banner__chevron {
  font-style: normal;
  font-size: 16px;
  line-height: 1;
}
.home-cta-banner__emoji {
  flex: none;
  font-size: 44px;
  line-height: 1;
  z-index: 1;
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
  background: linear-gradient(100deg, #fce9a3 0%, #fef6d8 100%);
  border: 1px solid #efd98c;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(255, 188, 0, 0.13);
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
    right: 2px;
    bottom: -4px;
    width: 64px;
  }

  .fund-summary-layout {
    gap: 6px;
  }

  .chart-area {
    width: 150px;
    height: 150px;
  }

  .chart-area :deep(svg) {
    width: 150px;
    height: 150px;
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

<style>
/* 홈 화면 배경 - D-Day·목돈작전과 같은 은은한 세이지 그린 */
.app-content:has(.home-page) {
  background-color: rgba(120, 152, 130, 0.06);
}
</style>
