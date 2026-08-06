<script setup>
// SCR-SIM-01 · 군적금 시뮬레이터  (담당: 석윤)
// 군적금 만기금 시뮬레이션 메인 + 모의 계산(바텀시트)
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import simulatorApi from '@/api/simulatorApi';
import { formatWon } from '@/util/format';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import SavingsBreakdown from '@/components/common/SavingsBreakdown.vue';

const route = useRoute();
const router = useRouter();

const details = ref(null);
const isLoading = ref(true);
const hasNoAccount = ref(false);

const savingLoss = ref(null);
const today = new Date();
const todayLabel = `${today.getMonth() + 1}월 ${today.getDate()}일`;

// 'real' = 실제 계좌 기준 상세내역 / 'simulated' = 모의 계산 결과로 대체된 상세내역
const viewMode = ref('real');
const simulatedResult = ref(null);
const simulateError = ref('');

const isCalcSheetOpen = ref(false);
// 'constant' = 동일 금액 매달 납입 / 'variable' = 구간별로 다른 금액 납입
const calcMode = ref('constant');
const monthlySave = ref('');
const saveMonths = ref('');
// 등록 완료된 구간 목록. 각 항목은 { startMonthOffset, endMonthOffset, amount }.
const periods = ref([]);
// 구간별 금액 모드에서 "등록" 누르기 전까지의 입력 중인 구간 — 시작월은 직전 구간
// 종료월+1로 자동 결정되므로(구간이 이어붙어야 함) 종료월만 입력받는다.
const periodDraft = ref({ endMonth: '', amount: '' });

// 기간 설정 드롭다운 선택지 (최대 가입기간 24개월, SIMUL_005와 동일한 한도)
const monthOptions = computed(() =>
  Array.from({ length: 24 }, (_, i) => ({
    value: i + 1,
    label: `${i + 1}개월`,
  })),
);

// 다음 구간의 시작월: 등록된 구간이 없으면 1개월, 있으면 마지막 구간 종료월+1.
const draftStartMonth = computed(() => {
  if (periods.value.length === 0) {
    return 1;
  }
  const lastEnd = Math.max(...periods.value.map((p) => p.endMonthOffset));
  return lastEnd + 1;
});

const canAddMorePeriods = computed(() => draftStartMonth.value <= 24);

const endMonthOptions = computed(() =>
  monthOptions.value.filter((opt) => opt.value >= draftStartMonth.value),
);

const isDraftValid = computed(() => {
  const end = periodDraft.value.endMonth;
  return (
    end !== '' &&
    Number(end) >= draftStartMonth.value &&
    Number(periodDraft.value.amount) > 0
  );
});

const isCalcFormValid = computed(() => {
  if (calcMode.value === 'constant') {
    return Number(monthlySave.value) > 0 && saveMonths.value !== '';
  }
  return periods.value.length > 0;
});

// 상세내역 카드에 실제로 보여줄 값 (실제 계좌 vs 모의 계산 결과)
const activeDetails = computed(() =>
  viewMode.value === 'simulated' ? simulatedResult.value : details.value,
);

const formatManwon = (amount) =>
  `${Math.round((amount ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

// 개월차 구간의 개월 수 (양 끝 포함)
const monthDiff = (startMonthOffset, endMonthOffset) =>
  endMonthOffset - startMonthOffset + 1;

const fetchSavingDetails = async () => {
  isLoading.value = true;
  hasNoAccount.value = false;
  viewMode.value = 'real';
  try {
    details.value = await simulatorApi.findSavingDetails();
  } catch (error) {
    // 군적금 미가입(SIMUL_002) 등은 빈 상태로 처리
    details.value = null;
    hasNoAccount.value = true;
  } finally {
    isLoading.value = false;
  }
};

const fetchSavingLoss = async () => {
  try {
    savingLoss.value = await simulatorApi.findSavingLoss();
  } catch (error) {
    const code = error.response?.data?.code;
    // 군적금 미가입(SIMUL_002)·이미 전역(SIMUL_008)은 카드 자체를 숨긴다
    if (code !== 'SIMUL_002' && code !== 'SIMUL_008') {
      console.error(error);
    }
    savingLoss.value = null;
  }
};

const openCalcSheet = () => {
  simulateError.value = '';
  isCalcSheetOpen.value = true;
};

const registerPeriod = () => {
  if (!isDraftValid.value) return;
  periods.value.push({
    startMonthOffset: draftStartMonth.value,
    endMonthOffset: Number(periodDraft.value.endMonth),
    amount: Number(periodDraft.value.amount),
  });
  periodDraft.value = { endMonth: '', amount: '' };
};

// 등록된 구간 전체 + 입력 중인 구간을 함께 초기화한다.
const resetPeriods = () => {
  periods.value = [];
  periodDraft.value = { endMonth: '', amount: '' };
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
        startMonthOffset: period.startMonthOffset,
        endMonthOffset: period.endMonthOffset,
        amount: period.amount,
      }));
      const apiResult = await simulatorApi.calculateVariable(payload);
      const totalMonths = periods.value.reduce(
        (sum, period) =>
          sum + monthDiff(period.startMonthOffset, period.endMonthOffset),
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
    console.error(error);
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
    console.error(error);
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
    console.error(error);
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
  fetchSavingLoss();
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

        <div v-if="periods.length" class="calc-sheet__period-list">
          <div
            v-for="(period, index) in periods"
            :key="index"
            class="calc-sheet__period-row"
          >
            <span class="calc-sheet__period-row-label">
              {{ index + 1 }}구간 ({{ period.startMonthOffset }}개월~{{
                period.endMonthOffset
              }}개월)
            </span>
            <span class="calc-sheet__period-row-amount">
              {{ formatWon(period.amount) }}
            </span>
          </div>
        </div>

        <p v-if="!canAddMorePeriods" class="calc-sheet__hint">
          24개월 전체 구간을 다 등록했어요.
        </p>

        <template v-else>
          <div class="calc-sheet__range-field">
            <span class="calc-sheet__range-label">기간 설정</span>
            <div class="calc-sheet__range-inputs">
              <span class="calc-sheet__range-start"
                >{{ draftStartMonth }}개월</span
              >
              <span class="base-input__range-sep">~</span>
              <BaseInput
                type="select"
                :options="endMonthOptions"
                :model-value="periodDraft.endMonth"
                @update:model-value="periodDraft.endMonth = $event"
              />
            </div>
          </div>

          <div class="calc-sheet__amount-row">
            <BaseInput
              type="amount"
              label="금액 설정"
              suffix="원"
              placeholder="최대 550,000"
              :model-value="periodDraft.amount"
              @update:model-value="periodDraft.amount = $event"
            />
            <div class="calc-sheet__amount-actions">
              <button
                type="button"
                class="calc-sheet__reset-btn"
                @click="resetPeriods"
              >
                초기화
              </button>
              <button
                type="button"
                class="calc-sheet__register-btn"
                :disabled="!isDraftValid"
                @click="registerPeriod"
              >
                등록
              </button>
            </div>
          </div>
        </template>
      </div>
    </BaseBottomSheet>

    <BaseCard v-if="savingLoss" class="loss-card">
      <p class="loss-card__eyebrow">
        {{ todayLabel }}, 만약
        <span class="loss-card__highlight">중도 해지</span>를 한다면?
      </p>
      <h3 class="loss-card__title">예상 수령액 및 손실금</h3>

      <div class="loss-card__stat">
        <span class="loss-card__stat-label">해지 시, 수령 액은?</span>
        <span class="loss-card__stat-value">{{
          formatWon(savingLoss.withdrawalAmount)
        }}</span>
      </div>
      <div class="loss-card__stat">
        <span class="loss-card__stat-label">해지 시, 손실 액은?</span>
        <span class="loss-card__stat-value loss-card__stat-value--danger"
          >-{{ formatWon(savingLoss.lossAmount) }}</span
        >
      </div>
    </BaseCard>

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

.calc-sheet__period-list {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 4px 16px;
}

.calc-sheet__period-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
}

.calc-sheet__period-row + .calc-sheet__period-row {
  border-top: 1px solid var(--line);
}

.calc-sheet__period-row-label {
  flex: 1;
  font-size: 13px;
  color: var(--text-body);
}

.calc-sheet__period-row-amount {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}

.calc-sheet__range-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.calc-sheet__range-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-body);
}

.calc-sheet__range-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.calc-sheet__range-start {
  flex-shrink: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-body);
}

.calc-sheet__amount-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.calc-sheet__amount-row :deep(.base-input) {
  flex: 1;
}

.calc-sheet__amount-actions {
  display: flex;
  gap: 6px;
  padding-bottom: 2px;
}

.calc-sheet__reset-btn,
.calc-sheet__register-btn {
  padding: 14px 14px;
  border: none;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.calc-sheet__reset-btn {
  background-color: var(--surface-muted);
  color: var(--text-body);
}

.calc-sheet__register-btn {
  background-color: var(--kb-yellow-deep);
  color: var(--text-strong);
}

.calc-sheet__register-btn:disabled {
  background-color: var(--surface-muted);
  color: var(--text-hint);
  cursor: not-allowed;
}

/* ── 중도해지 수령액·손실금 카드 ── */
.loss-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.loss-card__eyebrow {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}

.loss-card__highlight {
  color: var(--danger);
  font-weight: 700;
}

.loss-card__title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-strong);
}

.loss-card__stat {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background-color: var(--military-green-light);
  border-radius: 14px;
}

.loss-card__stat-label {
  font-size: 14px;
  color: var(--text-body);
}

.loss-card__stat-value {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-strong);
}

.loss-card__stat-value--danger {
  color: var(--danger);
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
