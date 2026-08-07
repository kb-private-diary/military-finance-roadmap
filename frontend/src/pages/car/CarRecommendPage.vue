<script setup>
// SCR-CAR-02 · step2) 자동차 로드맵 추천  (담당: 호빈)
// step2 - 예산·조건 기반 차량 추천 (경차/준중형/SUV 탭)
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BaseCard from '@/components/common/BaseCard.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const CAR_TYPE_TABS = [
  { value: 1, label: '경차' },
  { value: 2, label: '준중형' },
  { value: 3, label: 'SUV' },
];

const goal = ref(null);
const recommendations = ref([]);
const activeTab = ref(1);
const selectedModelId = ref(null);
const loading = ref(true);
const loadError = ref('');
const submitError = ref('');
const submitting = ref(false);

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const loadRecommendations = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const [goalResult, recommendResult] = await Promise.all([
      carApi.findGoalDetail(goalId),
      carApi.findRecommendations(goalId),
    ]);

    goal.value = unwrap(goalResult);
    recommendations.value = unwrap(recommendResult) || [];

    const firstTabWithItems = CAR_TYPE_TABS.find((tab) =>
      recommendations.value.some((item) => item.carTypeCode === tab.value),
    );
    activeTab.value = firstTabWithItems?.value ?? 1;
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

const itemsByTab = computed(() =>
  recommendations.value.filter((item) => item.carTypeCode === activeTab.value),
);

const selectTab = (value) => {
  activeTab.value = value;
};

const selectModel = (modelId) => {
  selectedModelId.value = modelId;
};

const isFormValid = computed(() => selectedModelId.value !== null);

const handleConfirm = async () => {
  if (!isFormValid.value || submitting.value) return;

  submitting.value = true;
  submitError.value = '';

  try {
    await carApi.selectModel(goalId, { modelId: selectedModelId.value });
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

    <h2 class="car-recommend__title text-title">차량을 추천해드려요</h2>
    <p v-if="goal" class="car-recommend__subtitle text-caption">
      {{ goal.budget != null ? `예산 ${formatManwonUnit(goal.budget)}` : '군적금 만기예상액 기준' }}
      · {{ goal.isNew ? '신차' : '중고' }} 기준
    </p>

    <div class="tab-row">
      <CategoryButton
        v-for="tab in CAR_TYPE_TABS"
        :key="tab.value"
        variant="square-yellow"
        :label="tab.label"
        :active="activeTab === tab.value"
        @click="selectTab(tab.value)"
      />
    </div>

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
        <div class="recommend-card__check">
          <span v-if="selectedModelId === item.modelId">&#10003;</span>
        </div>

        <div class="recommend-card__body">
          <div class="recommend-card__name-row">
            <span class="recommend-card__name">{{ item.modelName }}</span>
            <span v-if="item.fuelType === '전기'" class="recommend-card__ev-badge">EV</span>
            <span class="recommend-card__fuel">{{ item.fuelType }}</span>
            <span v-if="item.assumedYear" class="recommend-card__year">{{ item.assumedYear }}년식</span>
          </div>
          <div class="recommend-card__price-row">
            <span class="recommend-card__price-label">
              {{ goal?.isNew ? '신차가' : '추정 시세' }} {{ formatManwonUnit(item.estimatedPrice) }}
            </span>
            <span class="recommend-card__total">{{ formatManwonUnit(item.totalPrice) }}</span>
          </div>
          <span
            class="recommend-card__budget-tag"
            :class="item.withinBudget ? 'is-fit' : 'is-over'"
          >
            {{ item.withinBudget ? '예산 내' : '예산 초과' }}
          </span>
        </div>
      </BaseCard>
    </div>

    <p v-if="submitError" class="form-error text-caption" role="alert">
      {{ submitError }}
    </p>

    <BottomButtonBar
      primary-label="선택 완료"
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

.car-recommend :deep(.character-slider) {
  margin-bottom: 12px;
}

.car-recommend__title {
  margin: 0;
}

.car-recommend__subtitle {
  margin: -8px 0 0;
  color: var(--text-muted);
}

.tab-row {
  display: flex;
  gap: 8px;
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
  display: flex;
  align-items: flex-start;
  gap: 12px;
  transition: all 0.2s ease;
}

.recommend-card--active {
  border-color: var(--kb-yellow-deep);
  box-shadow: 0 0 0 1px var(--kb-yellow-deep);
}

.recommend-card__check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 1.5px solid var(--line-strong);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--surface-default);
  flex-shrink: 0;
  margin-top: 2px;
}

.recommend-card--active .recommend-card__check {
  background-color: var(--kb-yellow-deep);
  border-color: var(--kb-yellow-deep);
  font-weight: 700;
}

.recommend-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.recommend-card__name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.recommend-card__name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}

.recommend-card__ev-badge {
  padding: 2px 6px;
  border-radius: 6px;
  background: var(--military-green);
  color: var(--surface-default);
  font-size: 11px;
  font-weight: 700;
}

.recommend-card__fuel {
  font-size: 12px;
  color: var(--text-muted);
}

.recommend-card__year {
  font-size: 12px;
  color: var(--text-hint);
}

.recommend-card__price-row {
  display: flex;
  align-items: baseline;
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

.recommend-card__budget-tag {
  align-self: flex-start;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.recommend-card__budget-tag.is-fit {
  background: var(--kb-yellow-pale);
  color: var(--kb-gold);
}

.recommend-card__budget-tag.is-over {
  background: var(--surface-muted);
  color: var(--text-hint);
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
