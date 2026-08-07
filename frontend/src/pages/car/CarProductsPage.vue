<script setup>
// SCR-CAR-04 · step4) 자동차 금융상품 추천  (담당: 호빈)
// step4 - 보험상품 추천(+전기차 보조금) → 완료
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { formatManwonUnit } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = computed(() => Number(route.params.goalId));

const currentStep = 4;
const progress = computed(() => (currentStep / 4) * 100);

const goal = ref(null);
const maintenanceCost = ref(null);
const evSubsidy = ref(null);
const budgetStatus = ref(null);
const loading = ref(true);
const loadError = ref('');
const completing = ref(false);
const completeError = ref('');
const isCompleteModalOpen = ref(false);

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const isElectric = computed(() => maintenanceCost.value?.fuelType === '전기');
const showLoanProduct = computed(() => budgetStatus.value?.withinBudget === false);
const overBudgetAmount = computed(() => {
  if (!budgetStatus.value) return 0;
  return budgetStatus.value.purchaseTotal - budgetStatus.value.effectiveBudget;
});

const loadProductInfo = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const [goalResult, maintenanceResult, budgetResult] = await Promise.all([
      carApi.findGoalDetail(goalId.value),
      carApi.findMaintenanceCost(goalId.value),
      carApi.findBudgetStatus(goalId.value),
    ]);

    goal.value = unwrap(goalResult);
    maintenanceCost.value = unwrap(maintenanceResult);
    budgetStatus.value = unwrap(budgetResult);

    if (maintenanceCost.value?.fuelType === '전기') {
      const evResult = await carApi.findEvSubsidy(goalId.value);
      evSubsidy.value = unwrap(evResult);
    }
  } catch (error) {
    loadError.value = readErrorMessage(
      error,
      '금융상품 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

onMounted(loadProductInfo);

const handleComplete = async () => {
  if (completing.value) return;
  completing.value = true;
  completeError.value = '';
  try {
    await carApi.confirmGoal(goalId.value);
    isCompleteModalOpen.value = true;
  } catch (error) {
    completeError.value = readErrorMessage(error, '저장하지 못했습니다.');
  } finally {
    completing.value = false;
  }
};

const goToDetail = () => {
  router.push({ name: 'CarGoalDetail', params: { goalId: goalId.value } });
};

const handlePrev = () => {
  router.back();
};
</script>

<template>
  <div class="car-products">
    <RoadmapCharacterSlider :progress="progress" label="자동차 로드맵" />

    <h2 class="car-products__title text-title">금융상품 추천</h2>

    <div v-if="loading" class="car-products__status text-caption">불러오는 중...</div>
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>

    <template v-else>
      <section class="car-products__section">
        <h3 class="car-products__section-title text-label">관련 보험상품</h3>

        <BaseCard padding="18px" class="product-card">
          <div class="product-card__icon" aria-hidden="true">&#128737;</div>
          <div class="product-card__content">
            <strong class="product-card__name">KB 손해보험 다이렉트 자동차보험</strong>
            <p class="product-card__desc">
              {{ goal?.experienceYears }}년 운전경력 · {{ goal?.selectedModelName }} 기준 추천
            </p>
            <p class="product-card__amount">
              예상 연 보험료 {{ formatManwonUnit(maintenanceCost?.insurancePremiumMin) }}~{{
                formatManwonUnit(maintenanceCost?.insurancePremiumMax)
              }}
            </p>
          </div>
          <a
            class="product-card__external"
            href="https://direct.kbinsure.co.kr/"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="KB 손해보험 다이렉트 자동차보험 홈페이지 열기"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <path d="M14 4h6v6M20 4l-9 9M18 13v5a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h5" />
            </svg>
          </a>
        </BaseCard>
      </section>

      <section v-if="isElectric" class="car-products__section">
        <h3 class="car-products__section-title text-label">전기차 보조금</h3>

        <BaseCard padding="18px" class="product-card">
          <div class="product-card__icon" aria-hidden="true">&#9889;</div>
          <div class="product-card__content">
            <strong class="product-card__name">{{ evSubsidy?.region }} 전기차 보조금</strong>
            <p class="product-card__desc">
              국비 {{ formatManwonUnit(evSubsidy?.nationalSubsidy) }} + 지방비 {{
                formatManwonUnit(evSubsidy?.localSubsidy)
              }}
            </p>
            <p class="product-card__amount">
              보조금 적용가 {{ formatManwonUnit(evSubsidy?.finalPrice) }}
              <span class="product-card__amount-sub">
                (기준가 {{ formatManwonUnit(evSubsidy?.basePrice) }} - {{ formatManwonUnit(evSubsidy?.totalSubsidy) }})
              </span>
            </p>
          </div>
        </BaseCard>
      </section>

      <section v-if="showLoanProduct" class="car-products__section">
        <h3 class="car-products__section-title text-label">관련 대출상품</h3>

        <BaseCard padding="18px" class="product-card">
          <div class="product-card__icon" aria-hidden="true">&#128176;</div>
          <div class="product-card__content">
            <strong class="product-card__name">
              KB 매직카대출({{ goal?.isNew ? '신차' : '중고차' }})
            </strong>
            <p class="product-card__desc">
              선택한 차량이 준비 가능한 금액보다 {{ formatManwonUnit(overBudgetAmount) }} 더 필요해요.
              최고 {{ goal?.isNew ? '6,000' : '4,000' }}만원 · 연 5.67~7.17% (신용도별 차등)
            </p>
            <p class="product-card__notice">
              신청 자격: 만 19세 이상, 근로소득자 재직 6개월 이상(사업소득자 12개월 이상) 등 소득증빙 필요 —
              전역 직후라면 재직 기간 요건을 못 채울 수 있어요. 자세한 조건은 KB국민은행에서 꼭 확인해주세요.
            </p>
          </div>
        </BaseCard>
      </section>

      <div class="car-products__guide">
        <p class="car-products__guide-title text-label">지금까지 자동차 로드맵이었습니다.</p>
        <p class="car-products__guide-description">마음에 든다면 완료해주세요.</p>
      </div>

      <p v-if="completeError" class="form-error text-caption" role="alert">
        {{ completeError }}
      </p>
    </template>

    <BottomButtonBar
      :primary-label="completing ? '저장 중...' : '완료'"
      secondary-label="이전"
      :primary-disabled="loading || !!loadError || completing"
      @primary-click="handleComplete"
      @secondary-click="handlePrev"
    />

    <BaseModal
      v-model="isCompleteModalOpen"
      title="알림"
      confirm-text="확인"
      @confirm="goToDetail"
    >
      <p class="car-products__modal-message">
        선택한 자동차 목표가 저장되었습니다.
      </p>
    </BaseModal>
  </div>
</template>

<style scoped>
.car-products {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px 0 96px;
  color: var(--text-strong);
}

.car-products__title {
  margin: 0;
}

.car-products__status {
  padding: 40px 0;
  text-align: center;
  color: var(--text-muted);
}

.form-error {
  margin: 0;
  color: var(--danger);
}

.car-products__section-title {
  margin: 0 0 10px;
  color: var(--text-muted);
}

.product-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.product-card__external {
  position: absolute;
  top: 50%;
  right: 16px;
  display: grid;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: var(--text-strong);
  text-decoration: none;
  transform: translateY(-50%);
  place-items: center;
}

.product-card__external:hover,
.product-card__external:focus-visible {
  background: var(--surface-muted);
}

.product-card__external svg {
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
}

.product-card__icon {
  display: flex;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--kb-yellow-pale);
  font-size: 22px;
}

.product-card__content {
  min-width: 0;
  padding-right: 38px;
}

.product-card__name {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-body);
}

.product-card__desc {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.product-card__amount {
  margin: 8px 0 0;
  font-size: 15px;
  font-weight: 800;
  color: var(--text-strong);
}

.product-card__amount-sub {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-hint);
}

.product-card__notice {
  margin: 10px 0 0;
  padding-top: 10px;
  border-top: 1px solid var(--line);
  color: var(--text-hint);
  font-size: 11px;
  line-height: 1.6;
}

.car-products__guide {
  margin-top: 16px;
  padding: 18px;
  border-radius: 16px;
  background: var(--surface-subtle);
}

.car-products__guide-title {
  margin: 0;
  color: var(--text-body);
}

.car-products__guide-description {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.car-products__modal-message {
  margin: 0;
}

.car-products :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.car-products :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}

.car-products :deep(.bottom-button-bar .bar-button.primary:disabled) {
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  cursor: not-allowed;
}
</style>
