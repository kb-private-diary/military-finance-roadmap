<script setup>
// SCR-SIM-01 · 군적금 시뮬레이터  (담당: 석윤)
// 군적금 만기금 시뮬레이션 메인
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import simulatorApi from '@/api/simulatorApi';
import BaseCard from '@/components/common/BaseCard.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';

// TODO: JWT 연동 후 SecurityContext(authStore)에서 userId 추출
// 백엔드가 아직 @RequestParam Long userId 임시 방식이라 프론트도 임시 고정값 사용
const TEMP_USER_ID = 1;

const router = useRouter();

const details = ref(null);
const isLoading = ref(true);
const hasNoAccount = ref(false);

const formatManwon = (amount) =>
  `${Math.round((amount ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

const formatWon = (amount) => `${(amount ?? 0).toLocaleString('ko-KR')}원`;

const fetchSavingDetails = async () => {
  isLoading.value = true;
  hasNoAccount.value = false;
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

const goToCalc = () => {
  router.push({ name: 'SimulatorCalc' });
};

onMounted(fetchSavingDetails);
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
          <button
            type="button"
            class="simulator-page__refresh"
            aria-label="새로고침"
            @click="fetchSavingDetails"
          >
            ↻
          </button>
        </p>
      </div>
      <CategoryButton
        variant="oval-yellow"
        active
        label="직접 계산"
        @click="goToCalc"
      />
    </div>

    <p v-if="isLoading" class="text-caption">불러오는 중...</p>

    <EmptyState
      v-else-if="hasNoAccount"
      title="아직 군적금 가입 내역이 없어요"
      description="군적금에 가입하면 예상 만기 수령액을 시뮬레이션할 수 있어요"
    />

    <BaseCard v-else-if="details" class="report-card">
      <p class="report-card__title">충성, 군장병적금 보고합니다.</p>

      <div class="report-card__stats">
        <div class="report-card__stat-row">
          <span class="report-card__stat-label">월 납입액</span>
          <span class="report-card__stat-value">{{
            formatManwon(details.monthlySaveTotal)
          }}</span>
        </div>
        <div class="report-card__stat-row">
          <span class="report-card__stat-label">총 납입 개월 수</span>
          <span class="report-card__stat-value"
            >{{ details.joinableMonths }}개월</span
          >
        </div>
      </div>

      <div class="receipt-bar">
        <div
          class="receipt-bar__segment receipt-bar__segment--principal"
          :style="{ flexGrow: details.expectedPrincipal }"
        />
        <div
          class="receipt-bar__segment receipt-bar__segment--interest"
          :style="{ flexGrow: details.expectedInterest }"
        />
        <div
          class="receipt-bar__segment receipt-bar__segment--matching"
          :style="{ flexGrow: details.expectedMatchingFund }"
        />
      </div>

      <ul class="receipt-legend">
        <li class="receipt-legend__row">
          <span class="receipt-legend__dot receipt-legend__dot--principal" />
          <span class="receipt-legend__label">납입 원금</span>
          <span class="receipt-legend__value">{{
            formatWon(details.expectedPrincipal)
          }}</span>
        </li>
        <li class="receipt-legend__row">
          <span class="receipt-legend__dot receipt-legend__dot--interest" />
          <span class="receipt-legend__label">이자 (연 5.0%)</span>
          <span class="receipt-legend__value">{{
            formatWon(details.expectedInterest)
          }}</span>
        </li>
        <li class="receipt-legend__row">
          <span class="receipt-legend__dot receipt-legend__dot--matching" />
          <span class="receipt-legend__label">정부 매칭지원금</span>
          <span class="receipt-legend__value">{{
            formatWon(details.expectedMatchingFund)
          }}</span>
        </li>
      </ul>

      <div class="report-card__total">
        <span class="report-card__total-label">총 수령액</span>
        <span class="report-card__total-value">{{
          formatWon(details.totalReceiptAmount)
        }}</span>
      </div>
    </BaseCard>
  </div>
</template>

<style scoped>
.simulator-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.simulator-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
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

.simulator-page__refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  background: none;
  font-size: 16px;
  color: var(--text-hint, #999999);
  cursor: pointer;
}

/* ── 군적금 리포트 카드 ── */
.report-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-card__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong, #000000);
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
  padding: 10px 0;
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

/* ── 수령액 구성비 바 ── */
.receipt-bar {
  display: flex;
  height: 12px;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--kb-gray-pale, #e8e8e8);
}

.receipt-bar__segment {
  flex-shrink: 0;
  flex-basis: 0;
}

.receipt-bar__segment--principal {
  background-color: #f2a56d;
}

.receipt-bar__segment--interest {
  background-color: #bfe3a0;
}

.receipt-bar__segment--matching {
  background-color: #aedff5;
}

.receipt-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.receipt-legend__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.receipt-legend__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.receipt-legend__dot--principal {
  background-color: #f2a56d;
}

.receipt-legend__dot--interest {
  background-color: #bfe3a0;
}

.receipt-legend__dot--matching {
  background-color: #aedff5;
}

.receipt-legend__label {
  flex: 1;
  font-size: 13px;
  color: var(--text-body, #545045);
}

.receipt-legend__value {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.report-card__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid var(--line, #e0e0e0);
}

.report-card__total-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.report-card__total-value {
  font-size: 18px;
  font-weight: 800;
  color: #a9895a;
}
</style>
