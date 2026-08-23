<script setup>
// SCR-CAR-02 · step2) 자동차 로드맵 추천  (담당: 호빈)
// step2 - 예산·조건 기반 차량 추천 (경차/준중형/SUV 탭)
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BaseCard from '@/components/common/BaseCard.vue';
import TabBar from '@/components/common/TabBar.vue';
import RangeSlider from '@/components/common/RangeSlider.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import Chip from '@/components/common/Chip.vue';
import Badge from '@/components/common/Badge.vue';
import filterIcon from '@/assets/images/filter.png';
import leafIcon from '@/assets/images/leaf.png';
import checkGreenIcon from '@/assets/images/check-green.png';
import budgetTightIcon from '@/assets/images/budget-tight.png';
import budgetOverIcon from '@/assets/images/budget-over.png';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const CAR_TYPE_TABS = [
  { value: 1, label: '경차' },
  { value: 2, label: '준중형' },
  { value: 3, label: 'SUV' },
];

// 백엔드 DEFAULT_ASSUMED_AGE_YEARS/ANNUAL_MILEAGE_KM과 맞춘 슬라이더 초기값·범위
const CURRENT_YEAR = new Date().getFullYear();
const DEFAULT_AGE_YEARS = 3;
const ANNUAL_MILEAGE_KM = 12_000;
const MIN_YEAR = CURRENT_YEAR - 10;
const MAX_YEAR = CURRENT_YEAR;
const MAX_MILEAGE_KM = 200_000;

const goal = ref(null);
const recommendations = ref([]);
const activeTab = ref(1);
const selectedModelId = ref(null);
const loading = ref(true);
const loadError = ref('');
const submitError = ref('');
const submitting = ref(false);
const filterLoading = ref(false);

const selectedYear = ref(CURRENT_YEAR - DEFAULT_AGE_YEARS);
const selectedMileageKm = ref(DEFAULT_AGE_YEARS * ANNUAL_MILEAGE_KM);

// 조건 요약 바 접이식 (기본 접힘 → 요약만 표시)
const filterOpen = ref(false);

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

// 중고차 목표만 연식/키로수를 직접 골라 가격을 재계산할 수 있다.
const isUsedCarGoal = computed(() => goal.value?.isNew === false);

const buildFilterParams = () =>
  isUsedCarGoal.value
    ? { year: selectedYear.value, mileageKm: selectedMileageKm.value }
    : undefined;

const fetchRecommendations = async () => {
  const recommendResult = await carApi.findRecommendations(goalId, buildFilterParams());
  recommendations.value = unwrap(recommendResult) || [];
};

const loadRecommendations = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const goalResult = await carApi.findGoalDetail(goalId);
    goal.value = unwrap(goalResult);

    // 이미 차량을 골라둔 목표라면(추천 목록 다시 보기 등) 이전 선택을 그대로 복원한다.
    if (goal.value.selectedYear != null) selectedYear.value = goal.value.selectedYear;
    if (goal.value.selectedMileageKm != null) selectedMileageKm.value = goal.value.selectedMileageKm;

    await fetchRecommendations();

    if (goal.value.selectedModelId != null) {
      selectedModelId.value = goal.value.selectedModelId;
    }

    const preferredTab = goal.value.carTypeCode
      ?? CAR_TYPE_TABS.find((tab) =>
        recommendations.value.some((item) => item.carTypeCode === tab.value),
      )?.value;
    activeTab.value = preferredTab ?? 1;
  } catch (error) {
    loadError.value = readErrorMessage(
      error,
      '추천 차량 목록을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

onMounted(loadRecommendations);

let filterDebounceTimer = null;
watch([selectedYear, selectedMileageKm], () => {
  if (!isUsedCarGoal.value) return;

  clearTimeout(filterDebounceTimer);
  filterDebounceTimer = setTimeout(async () => {
    filterLoading.value = true;
    try {
      await fetchRecommendations();
    } catch (error) {
      submitError.value = readErrorMessage(error, '연식/키로수 필터를 적용하지 못했습니다.');
    } finally {
      filterLoading.value = false;
    }
  }, 300);
});

const itemsByTab = computed(() =>
  recommendations.value.filter((item) => item.carTypeCode === activeTab.value),
);

const selectModel = (modelId) => {
  selectedModelId.value = modelId;
};

const isFormValid = computed(() => selectedModelId.value !== null);

const formatKm = (km) => `${km.toLocaleString()}km`;

// 예산 대비 상태 뱃지 (딱 맞아요 / 빠듯해요 / 예산 초과)
const CAR_BUDGET_BADGE = {
  fit: { tone: 'green', icon: checkGreenIcon, text: '딱 맞아요', iconSize: 12 },
  tight: { tone: 'yellow', icon: budgetTightIcon, text: '빠듯해요', iconSize: 13 },
  over: { tone: 'red', icon: budgetOverIcon, text: '예산 초과', iconSize: 13 },
};
const budgetBadge = (item) => {
  if (!item.withinBudget) return CAR_BUDGET_BADGE.over;
  const budget = goal.value?.budget;
  // 예산이 있고 실제 총액이 90% 이상이면 '빠듯'
  if (budget && item.totalPrice >= budget * 0.9) return CAR_BUDGET_BADGE.tight;
  return CAR_BUDGET_BADGE.fit;
};

const handleConfirm = async () => {
  if (!isFormValid.value || submitting.value) return;

  submitting.value = true;
  submitError.value = '';

  try {
    const payload = { modelId: selectedModelId.value };
    if (isUsedCarGoal.value) {
      payload.selectedYear = selectedYear.value;
      payload.selectedMileageKm = selectedMileageKm.value;
    }
    await carApi.selectModel(goalId, payload);
    await router.push({ name: 'CarCost', params: { goalId } });
  } catch (error) {
    submitError.value = readErrorMessage(
      error,
      '차량 선택을 저장하지 못했습니다.',
    );
  } finally {
    submitting.value = false;
  }
};

const handlePrev = () => {
  router.back();
};
</script>

<template>
  <div class="car-recommend">
    <RoadmapCharacterSlider :step="2" label="자동차 로드맵" />

    <PageHeader
      title="어떤 자동차를 원하십니까?"
      description="전기차 선택 시, 보조금 지원까지 볼 수 있습니다."
    />
    <p v-if="goal" class="car-recommend__subtitle text-caption">
      {{ goal.budget != null ? `예산 ${formatManwonUnit(goal.budget)}` : '군적금 만기예상액 기준' }}
      · {{ goal.isNew ? '신차' : '중고' }} 기준
    </p>

    <a
      v-if="goal"
      class="kbcc-banner"
      href="https://www.kbchachacha.com/"
      target="_blank"
      rel="noopener noreferrer"
      aria-label="KB차차차에서 실제 매물 보러가기"
    >
      <span>KB차차차에서 {{ goal.isNew ? '신차' : '실제 매물' }} 확인해보기</span>
      <span aria-hidden="true">&#8250;</span>
    </a>

    <div v-if="isUsedCarGoal" class="filter-box" :class="{ 'is-open': filterOpen }">
      <button
        type="button"
        class="filter-summary"
        :aria-expanded="filterOpen"
        @click="filterOpen = !filterOpen"
      >
        <img class="filter-summary__icon" :src="filterIcon" alt="" />
        <span class="filter-summary__text">
          {{ selectedYear }}년식 이후 · {{ formatKm(selectedMileageKm) }} 이하
        </span>
        <svg
          class="filter-summary__caret"
          viewBox="0 0 24 24"
          width="16"
          height="16"
          aria-hidden="true"
        >
          <path d="M7 10l5 5 5-5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        </svg>
      </button>

      <div v-if="filterOpen" class="filter-panel">
        <RangeSlider
          v-model="selectedYear"
          :min="MIN_YEAR"
          :max="MAX_YEAR"
          :step="1"
          label="연식"
          unit="년식"
          :ticks="[`${MIN_YEAR}년`, `${MAX_YEAR}년`]"
        />
        <RangeSlider
          v-model="selectedMileageKm"
          :min="0"
          :max="MAX_MILEAGE_KM"
          :step="2000"
          label="주행거리"
          :display-value="formatKm(selectedMileageKm)"
          :ticks="['0km', formatKm(MAX_MILEAGE_KM)]"
        />
        <p v-if="filterLoading" class="filter-panel__status text-caption">가격 재계산 중...</p>
      </div>
    </div>

    <TabBar v-model="activeTab" :tabs="CAR_TYPE_TABS" />


    <p v-if="loading" class="car-recommend__status text-caption">불러오는 중...</p>
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>
    <EmptyState
      v-else-if="!itemsByTab.length"
      title="해당 차종에 추천 가능한 차량이 없어요"
      description="예산·조건을 다시 설정하면 다른 차량을 추천받을 수 있어요"
    >
      <template #action>
        <button type="button" class="empty-state__retry-btn" @click="handlePrev">
          조건 다시 설정하기
        </button>
      </template>
    </EmptyState>

    <div class="car-list">
      <BaseCard
        v-for="item in itemsByTab"
        :key="item.modelId"
        padding="16px"
        class="recommend-card"
        :class="{ 'recommend-card--active': selectedModelId === item.modelId }"
        @click="selectModel(item.modelId)"
      >
        <div class="recommend-card__body">
          <div class="recommend-card__info">
          <div class="recommend-card__name-row">
            <Chip
              :tone="item.fuelType === '전기' ? 'green' : 'gray'"
              :icon="item.fuelType === '전기' ? leafIcon : ''"
              :text="item.fuelType === '전기' ? 'EV' : item.fuelType"
            />
            <span class="recommend-card__name">{{ item.modelName }}</span>
            <span v-if="item.assumedYear" class="recommend-card__meta">
              {{ item.assumedYear }}년식<template v-if="isUsedCarGoal"> · {{ formatKm(selectedMileageKm) }}</template>
            </span>
          </div>
          <div class="recommend-card__price-row">
            <span class="recommend-card__price-label">
              {{ goal?.isNew ? '신차가' : '추정 시세' }} {{ formatManwonUnit(item.estimatedPrice) }}
            </span>
            <span class="recommend-card__total">{{ formatManwonUnit(item.totalPrice) }}</span>
          </div>
          </div>
          <Badge
            class="recommend-card__budget"
            :tone="budgetBadge(item).tone"
            :icon="budgetBadge(item).icon"
            :icon-size="budgetBadge(item).iconSize"
            :text="budgetBadge(item).text"
          />
        </div>
      </BaseCard>
    </div>

    <p v-if="submitError" class="form-error text-caption" role="alert">
      {{ submitError }}
    </p>

    <BottomButtonBar
      primary-label="선택완료"
      secondary-label="이전"
      :primary-disabled="!isFormValid || submitting"
      @primary-click="handleConfirm"
      @secondary-click="handlePrev"
    />
  </div>
</template>

<style scoped>
.car-recommend {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
/* 제목 아래 컨텐츠 간격 통일(20px): flex gap 16 + ph mb 2 + subtitle mt 2 */
.car-recommend :deep(.page-header) {
  margin-bottom: 2px;
}

.car-recommend :deep(.character-slider) {
  margin-bottom: 12px;
}

.car-recommend__subtitle {
  margin: 2px 0 0;
  color: var(--text-muted);
}

.kbcc-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--kb-yellow-pale);
  color: var(--text-body);
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
}

/* 조건 요약/조정 — 요약 + 연식·주행거리 슬라이더를 한 박스에 */
.filter-box {
  border: 1px solid var(--divider-thin);
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
}
.filter-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 14px;
  border: 0;
  background: transparent;
  font-family: inherit;
  cursor: pointer;
}
.filter-summary__icon {
  flex: none;
  width: 18px;
  height: 18px;
  object-fit: contain;
}
.filter-summary__text {
  flex: 1;
  text-align: left;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.filter-summary__caret {
  flex: none;
  color: var(--text-hint);
  transition: transform 0.2s ease;
}
.filter-box.is-open .filter-summary__caret {
  transform: rotate(180deg);
}

.filter-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 2px 14px 16px;
}
/* 필터 박스 안 슬라이더 값(2021년식·36,000km)만 살짝 작게 */
.filter-panel :deep(.range-slider__value) {
  font-size: 18px;
}

.filter-panel__status {
  margin: 0;
  color: var(--text-hint);
}

.car-recommend__status {
  color: var(--text-muted);
}

.empty-state__retry-btn {
  padding: 10px 18px;
  border: 1.5px solid var(--kb-yellow-deep);
  border-radius: 999px;
  background: var(--surface-default);
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.car-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.recommend-card--active {
  border-color: var(--kb-yellow-deep);
  box-shadow: 0 0 0 1px var(--kb-yellow-deep);
}

.recommend-card__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

/* 이름줄 + 추정시세·가격 = 한 묶음 (좁게), 예산 태그와는 body gap 유지 */
.recommend-card__info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.recommend-card__name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.recommend-card__name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

/* 자취 카드와 동일한 회색 chip 배지 (연료), 전기는 초록 */
.recommend-card__meta {
  font-size: 12px;
  color: var(--text-hint);
}

.recommend-card__price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.recommend-card__price-label {
  font-size: 12px;
  color: var(--text-muted);
}

.recommend-card__total {
  font-size: 16px;
  font-weight: 800;
  color: var(--text-strong);
}

/* 예산 뱃지(공통 Badge) 위치만 지정 */
.recommend-card__budget {
  align-self: flex-start;
}

.form-error {
  margin: 0;
  color: var(--danger);
}

.car-recommend :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.car-recommend :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}

.car-recommend :deep(.bottom-button-bar .bar-button.primary:disabled) {
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  cursor: not-allowed;
}
</style>
