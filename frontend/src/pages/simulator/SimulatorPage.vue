<script setup>
// SCR-SIM-01 · 군적금 시뮬레이터  (담당: 석윤)
// 군적금 만기금 시뮬레이션 메인 + 모의 계산(바텀시트)
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import simulatorApi from '@/api/simulatorApi';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import SavingsBreakdown from '@/components/common/SavingsBreakdown.vue';

// TODO: JWT 연동 후 SecurityContext(authStore)에서 userId 추출
// 백엔드가 아직 @RequestParam Long userId 임시 방식이라 프론트도 임시 고정값 사용
const TEMP_USER_ID = 1;

const route = useRoute();
const router = useRouter();

const details = ref(null);
const isLoading = ref(true);
const hasNoAccount = ref(false);

// 'real' = 실제 계좌 기준 상세내역 / 'simulated' = 모의 계산 결과로 대체된 상세내역
const viewMode = ref('real');
const simulatedResult = ref(null);
const simulateError = ref('');

const isCalcSheetOpen = ref(false);
// 'constant' = 동일 금액 매달 납입 / 'variable' = 구간별로 다른 금액 납입
const calcMode = ref('constant');
const monthlySave = ref('');
const saveMonths = ref('');
const periods = ref([{ range: ['', ''], amount: '' }]);

const isCalcFormValid = computed(() => {
  if (calcMode.value === 'constant') {
    return Number(monthlySave.value) > 0 && saveMonths.value !== '';
  }
  return periods.value.every(
    (period) => period.range[0] && period.range[1] && Number(period.amount) > 0,
  );
});

// 상세내역 카드에 실제로 보여줄 값 (실제 계좌 vs 모의 계산 결과)
const activeDetails = computed(() =>
  viewMode.value === 'simulated' ? simulatedResult.value : details.value,
);

const formatManwon = (amount) =>
  `${Math.round((amount ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

// "yyyy-MM" ~ "yyyy-MM" 구간의 개월 수 (양 끝 포함)
const monthDiff = (startMonth, endMonth) => {
  const [startYear, startM] = startMonth.split('-').map(Number);
  const [endYear, endM] = endMonth.split('-').map(Number);
  return (endYear - startYear) * 12 + (endM - startM) + 1;
};

const fetchSavingDetails = async () => {
  isLoading.value = true;
  hasNoAccount.value = false;
  viewMode.value = 'real';
  try {
    details.value = await simulatorApi.findSavingDetails(TEMP_USER_ID);
  } catch (error) {
    // 군적금 미가입(SIMUL_002) 등은 빈 상태로 처리
    details.value = null;
    hasNoAccount.value = true;
  } finally {
    isLoading.value = false;
  }
};

const openCalcSheet = () => {
  simulateError.value = '';
  isCalcSheetOpen.value = true;
};

const addPeriod = () => {
  periods.value.push({ range: ['', ''], amount: '' });
};

const removePeriod = (index) => {
  periods.value.splice(index, 1);
};

const runCalculation = async () => {
  if (!isCalcFormValid.value) {
    simulateError.value = '입력값을 모두 채워주세요.';
    return;
  }

  try {
    if (calcMode.value === 'constant') {
      const apiResult = await simulatorApi.calculateConstant({
        monthlySave: Number(monthlySave.value),
        saveMonths: Number(saveMonths.value),
      });
      simulatedResult.value = {
        monthlySaveTotal: Number(monthlySave.value),
        joinableMonths: Number(saveMonths.value),
        expectedPrincipal: apiResult.totalPrincipal,
        expectedInterest: apiResult.totalInterest,
        expectedMatchingFund: apiResult.totalMatchingFund,
        totalReceiptAmount: apiResult.totalReceiptAmount,
      };
    } else {
      const payload = periods.value.map((period) => ({
        startMonth: period.range[0],
        endMonth: period.range[1],
        amount: Number(period.amount),
      }));
      const apiResult = await simulatorApi.calculateVariable(payload);
      const totalMonths = periods.value.reduce(
        (sum, period) => sum + monthDiff(period.range[0], period.range[1]),
        0,
      );
      simulatedResult.value = {
        monthlySaveTotal:
          totalMonths > 0
            ? Math.round(apiResult.totalPrincipal / totalMonths)
            : 0,
        joinableMonths: totalMonths,
        expectedPrincipal: apiResult.totalPrincipal,
        expectedInterest: apiResult.totalInterest,
        expectedMatchingFund: apiResult.totalMatchingFund,
        totalReceiptAmount: apiResult.totalReceiptAmount,
      };
    }
    viewMode.value = 'simulated';
    simulateError.value = '';
  } catch (error) {
    console.error(error)
    simulateError.value =
      error.response?.data?.message ?? '계산에 실패했습니다.';
  }
};

// '/simulator/calc'로 들어온 경우 모의 계산 바텀시트를 자동으로 연다.
// '/simulator'와 '/simulator/calc'는 같은 컴포넌트를 쓰므로(Vue Router가 인스턴스를
// 재사용) SPA 내부에서 두 경로를 오갈 때도 열리도록 watch로 감지한다.
watch(
  () => route.meta.openCalc,
  (openCalc) => {
    if (openCalc) {
      openCalcSheet();
    }
  },
  { immediate: true },
);

// ── 적금/정책 추천 목록 (전체 | KB | 정책 탭) ──
// 'all' = 적금+정책 상품 전체 / 'kb' = 적금 상품 중 KB / 'policy' = 정책 상품
const productTab = ref('all');
const savingProducts = ref([]);
const policyProducts = ref([]);
const isProductLoading = ref(true);
const productError = ref('');

const kbSavingProducts = computed(() =>
  // FSS API는 은행명을 "국민은행"처럼 한글로 내려주므로 "KB" 문자열로는 매칭되지 않는다.
  savingProducts.value.filter((product) =>
    product.korCoNm?.includes('국민은행'),
  ),
);

// 탭에 맞춰 적금/정책 상품을 하나의 목록으로 합쳐서 렌더링한다.
const displayedItems = computed(() => {
  const savingItems = (
    productTab.value === 'kb' ? kbSavingProducts.value : savingProducts.value
  ).map((item) => ({ type: 'saving', id: item.productId, data: item }));

  if (productTab.value === 'kb') {
    return savingItems;
  }

  const policyItems = policyProducts.value.map((item) => ({
    type: 'policy',
    id: item.policyId,
    data: item,
  }));

  if (productTab.value === 'policy') {
    return policyItems;
  }

  return [...savingItems, ...policyItems];
});

const fetchProducts = async () => {
  isProductLoading.value = true;
  productError.value = '';
  try {
    const [savings, policies] = await Promise.all([
      productApi.findSavingProductList('savings'),
      productApi.findPolicyProductList(),
    ]);
    savingProducts.value = savings;
    policyProducts.value = policies;
  } catch (error) {
    console.error(error)
    productError.value = '상품 정보를 불러오지 못했습니다.';
  } finally {
    isProductLoading.value = false;
  }
};

const selectProductTab = (tab) => {
  productTab.value = tab;
};

// ── 예금 추천 목록 (KB 예금 상품만 존재 — 구분 탭 불필요) ──
const depositProducts = ref([]);
const isDepositLoading = ref(true);
const depositError = ref('');

const fetchDepositProducts = async () => {
  isDepositLoading.value = true;
  depositError.value = '';
  try {
    depositProducts.value = await productApi.findSavingProductList('deposits');
  } catch (error) {
    console.error(error)
    depositError.value = '예금 상품을 불러오지 못했습니다.';
  } finally {
    isDepositLoading.value = false;
  }
};

const goToSavingProductDetail = (productId) => {
  router.push({ name: 'SavingProductDetail', params: { productId } });
};

const goToPolicyProductDetail = (policyId) => {
  router.push({ name: 'PolicyProductDetail', params: { policyId } });
};

const goToProductDetail = (item) => {
  if (item.type === 'policy') {
    goToPolicyProductDetail(item.id);
  } else {
    goToSavingProductDetail(item.id);
  }
};

onMounted(() => {
  fetchSavingDetails();
  fetchProducts();
  fetchDepositProducts();
});
</script>

<template>
  <div class="simulator-page container py-4">
    <div class="simulator-page__header">
      <div class="simulator-page__summary">
        <p class="simulator-page__summary-title">
          현재 납입액
          {{ details ? formatManwon(details.currentPaidAmount) : '-' }},
        </p>
        <p class="simulator-page__summary-sub">
          {{ details ? details.currentPaidMonths : 0 }}개월 납입 중
        </p>
      </div>
      <CategoryButton
        class="simulator-page__calc-btn"
        variant="oval-yellow"
        active
        label="모의 계산"
        @click="openCalcSheet"
      />
    </div>

    <p v-if="simulateError" class="simulator-page__error">
      {{ simulateError }}
    </p>

    <p v-if="isLoading" class="text-caption">불러오는 중...</p>

    <EmptyState
      v-else-if="hasNoAccount && viewMode === 'real'"
      title="아직 군적금 가입 내역이 없어요"
      description="군적금에 가입하면 예상 만기 수령액을 시뮬레이션할 수 있어요"
    />

    <BaseCard v-else-if="activeDetails" class="report-card" padding="16px">
      <div class="report-card__title-row">
        <p class="report-card__title">충성, 군장병적금 보고합니다.</p>
        <div v-if="viewMode === 'simulated'" class="report-card__title-actions">
          <BaseTag label="모의 결과" variant="yellow" />
          <button
            type="button"
            class="report-card__reset"
            aria-label="리셋"
            @click="fetchSavingDetails"
          >
            ↻
          </button>
        </div>
      </div>

      <div class="report-card__stats">
        <div class="report-card__stat-row">
          <span class="report-card__stat-label">월 납입액</span>
          <span class="report-card__stat-value">{{
            formatManwon(activeDetails.monthlySaveTotal)
          }}</span>
        </div>
        <div class="report-card__stat-row">
          <span class="report-card__stat-label">총 납입 개월 수</span>
          <span class="report-card__stat-value"
            >{{ activeDetails.joinableMonths }}개월</span
          >
        </div>
      </div>

      <SavingsBreakdown
        :principal="activeDetails.expectedPrincipal"
        :interest="activeDetails.expectedInterest"
        :matching-fund="activeDetails.expectedMatchingFund"
        :total="activeDetails.totalReceiptAmount"
      />
    </BaseCard>

    <BaseBottomSheet
      v-model="isCalcSheetOpen"
      title="군적금 모의 계산"
      confirm-text="실행"
      cancel-text="취소"
      @confirm="runCalculation"
    >
      <div class="calc-sheet__mode-tabs">
        <CategoryButton
          variant="square-yellow"
          :active="calcMode === 'constant'"
          label="고정 금액"
          @click="calcMode = 'constant'"
        />
        <CategoryButton
          variant="square-yellow"
          :active="calcMode === 'variable'"
          label="구간별 금액"
          @click="calcMode = 'variable'"
        />
      </div>

      <div v-if="calcMode === 'constant'" class="calc-sheet__form">
        <BaseInput
          v-model="monthlySave"
          type="amount"
          label="월 납입액"
          suffix="원"
          placeholder="최대 550,000"
        />
        <BaseInput
          v-model="saveMonths"
          type="number"
          label="납입 개월 수"
          suffix="개월"
          placeholder="최대 24"
        />
      </div>

      <div v-else class="calc-sheet__form">
        <p class="calc-sheet__hint">
          구간마다 다른 금액을 납입한다고 가정하고 계산해요.
        </p>

        <div
          v-for="(period, index) in periods"
          :key="index"
          class="calc-sheet__period"
        >
          <div class="calc-sheet__period-header">
            <span class="calc-sheet__period-label">구간 {{ index + 1 }}</span>
            <button
              v-if="periods.length > 1"
              type="button"
              class="calc-sheet__period-remove"
              aria-label="구간 삭제"
              @click="removePeriod(index)"
            >
              ×
            </button>
          </div>
          <BaseInput
            type="month-range"
            label="납입 기간"
            :model-value="period.range"
            @update:model-value="period.range = $event"
          />
          <BaseInput
            type="amount"
            label="월 납입액"
            suffix="원"
            placeholder="최대 550,000"
            :model-value="period.amount"
            @update:model-value="period.amount = $event"
          />
        </div>

        <button type="button" class="calc-sheet__add-period" @click="addPeriod">
          + 구간 추가
        </button>
      </div>
    </BaseBottomSheet>

    <div class="product-section">
      <p class="product-section__eyebrow">모으고 또 모으자</p>
      <h2 class="product-section__title">적금 시뮬레이션</h2>

      <div class="product-section__tabs">
        <CategoryButton
          variant="square-yellow"
          :active="productTab === 'all'"
          label="전체"
          @click="selectProductTab('all')"
        />
        <CategoryButton
          variant="square-yellow"
          :active="productTab === 'kb'"
          label="KB"
          @click="selectProductTab('kb')"
        />
        <CategoryButton
          variant="square-yellow"
          :active="productTab === 'policy'"
          label="정책"
          @click="selectProductTab('policy')"
        />
      </div>

      <p v-if="productError" class="simulator-page__error">
        {{ productError }}
      </p>
      <p v-else-if="isProductLoading" class="text-caption">불러오는 중...</p>

      <EmptyState
        v-else-if="displayedItems.length === 0"
        title="추천 상품이 없어요"
        description="조건에 맞는 상품을 찾을 수 없어요"
      />

      <div v-else class="product-section__list">
        <BaseCard
          v-for="item in displayedItems"
          :key="`${item.type}-${item.id}`"
          class="product-card"
          padding="14px 16px"
          @click="goToProductDetail(item)"
        >
          <template v-if="item.type === 'policy'">
            <p class="product-card__title">{{ item.data.policyName }}</p>
            <p class="product-card__desc">
              최대 연 {{ item.data.maxRate }}% 금리
            </p>
          </template>
          <template v-else>
            <p class="product-card__title">
              {{ item.data.korCoNm }} {{ item.data.productName }}
            </p>
            <BaseTag
              v-if="item.data.isTaxExempt"
              label="비과세"
              variant="green-light"
            />
            <p class="product-card__desc">
              {{ item.data.saveTrm }}개월 기준 최대 연 {{ item.data.maxRate }}%
              금리
            </p>
          </template>
        </BaseCard>
      </div>
    </div>

    <div class="product-section">
      <h2 class="product-section__title">예금 시뮬레이션</h2>

      <p v-if="depositError" class="simulator-page__error">
        {{ depositError }}
      </p>
      <p v-else-if="isDepositLoading" class="text-caption">불러오는 중...</p>

      <EmptyState
        v-else-if="depositProducts.length === 0"
        title="추천 예금 상품이 없어요"
        description="조건에 맞는 예금 상품을 찾을 수 없어요"
      />

      <div v-else class="product-section__list">
        <BaseCard
          v-for="item in depositProducts"
          :key="item.productId"
          class="product-card"
          padding="14px 16px"
          @click="goToSavingProductDetail(item.productId)"
        >
          <p class="product-card__title">
            {{ item.korCoNm }} {{ item.productName }}
          </p>
          <BaseTag
            v-if="item.isTaxExempt"
            label="비과세"
            variant="green-light"
          />
          <p class="product-card__desc">
            {{ item.saveTrm }}개월 기준 최대 연 {{ item.maxRate }}% 금리
          </p>
        </BaseCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.simulator-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.simulator-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

:deep(.simulator-page__calc-btn) {
  flex: none;
  width: auto;
}

.simulator-page__summary-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.simulator-page__summary-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.simulator-page__error {
  margin: 0;
  font-size: 13px;
  color: var(--danger, #fa6e6e);
}

/* ── 군적금 리포트 카드 ── */
.report-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.report-card__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.report-card__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.report-card__title-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.report-card__reset {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  padding: 0;
  border: none;
  background: none;
  font-size: 15px;
  color: var(--text-hint, #999999);
  cursor: pointer;
}

.report-card__stats {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--line, #e0e0e0);
  border-radius: 12px;
  padding: 4px 16px;
}

.report-card__stat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 0;
}

.report-card__stat-row + .report-card__stat-row {
  border-top: 1px solid var(--line, #e0e0e0);
}

.report-card__stat-label {
  font-size: 13px;
  color: var(--text-hint, #999999);
}

.report-card__stat-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

/* ── 모의 계산 바텀시트 ── */
.calc-sheet__mode-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.calc-sheet__form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.calc-sheet__hint {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint, #999999);
}

.calc-sheet__period {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line, #e0e0e0);
}

.calc-sheet__period:last-of-type {
  padding-bottom: 0;
  border-bottom: none;
}

.calc-sheet__period-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.calc-sheet__period-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body, #545045);
}

.calc-sheet__period-remove {
  width: 22px;
  height: 22px;
  border: none;
  background: none;
  color: var(--text-hint, #999999);
  font-size: 16px;
  cursor: pointer;
}

.calc-sheet__add-period {
  padding: 10px;
  border: 1px dashed var(--line-strong, #d0d0d0);
  border-radius: 12px;
  background: none;
  color: var(--text-body, #545045);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

/* ── 적금/정책 추천 목록 ── */
.product-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--line, #e0e0e0);
}

.product-section__eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-hint, #999999);
}

.product-section__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.product-section__tabs {
  display: flex;
  gap: 8px;
}

.product-section__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
}

.product-card__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.product-card__desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint, #999999);
}
</style>
