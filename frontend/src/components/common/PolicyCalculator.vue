<script setup>
// PolicyCalculator - 정책 상품(예: 청년미래적금) "금융 계산기".
// 정부매칭분도 매달 같이 적립되는 것처럼 원금+매칭분 합산 잔액에 이자가 붙는 구조.
// 이자는 항상 minRate 고정, 소득유형(일반/우대형)은 매칭비율만 바꾼다.
// PolicyProductDetailPage와 SimulatorProductListPage(예적금 상품 계산기)에서 동일하게 쓰는 공통 컴포넌트.
// product.hasCalculator가 false면 아무것도 그리지 않으므로 호출부에서 별도 v-if가 필요 없다.
import { computed, ref, watch } from 'vue';
import { formatWon } from '@/util/format';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseRadioGroup from '@/components/common/BaseRadioGroup.vue';

const props = defineProps({
  product: { type: Object, required: true },
});

const inputAmount = ref('');
// 소득유형: NORMAL(일반) | PREFER(우대형) — 정부매칭지원금 비율만 바뀐다 (이자율은 항상 minRate 고정)
const incomeType = ref('NORMAL');

// 드롭다운으로 다른 상품을 선택해 product가 바뀌면, 이전 상품 기준 입력값이 남지 않도록 초기화한다.
watch(
  () => props.product.policyId,
  () => {
    inputAmount.value = '';
    incomeType.value = 'NORMAL';
  },
);

const incomeTypeOptions = computed(() => [
  { label: `일반(${props.product.normalMatchRate}%)`, value: 'NORMAL' },
  { label: `우대형(${props.product.preferMatchRate}%)`, value: 'PREFER' },
]);

const matchingRate = computed(() =>
  incomeType.value === 'PREFER'
    ? Number(props.product.preferMatchRate)
    : Number(props.product.normalMatchRate),
);

// 상품별 최소·최대 납입금(정보 카드의 "납입금" 항목과 동일한 한도)을 벗어나면 인라인으로 안내한다.
const amountError = computed(() => {
  if (!inputAmount.value) {
    return '';
  }
  const amount = Number(inputAmount.value);
  if (amount < props.product.minLimit) {
    return `최소 ${formatWon(props.product.minLimit)} 이상 입력해주세요`;
  }
  if (props.product.maxLimit != null && amount > props.product.maxLimit) {
    return `최대 ${formatWon(props.product.maxLimit)} 이하로 입력해주세요`;
  }
  return '';
});

// 정기적금식 단리: 매월 납입액이 계산 기간(calcPeriodMonths) 동안 매달 쌓인다.
const principal = computed(() => {
  const amount = Number(inputAmount.value) || 0;
  const months = props.product.calcPeriodMonths || 0;
  return amount * months;
});

// 정부매칭분도 매달 같이 적립되는 것처럼 원금+매칭분 합산 잔액에 이자가 붙는다.
const interest = computed(() => {
  const amount = Number(inputAmount.value) || 0;
  const months = props.product.calcPeriodMonths || 0;
  if (!amount || !months) {
    return 0;
  }
  const effectiveAmount = amount * (1 + matchingRate.value / 100);
  const totalInvestedMonths = (months * (months + 1)) / 2;
  return Math.round(
    effectiveAmount *
      (Number(props.product.minRate) / 100) *
      (totalInvestedMonths / 12),
  );
});

const matchingFund = computed(() =>
  Math.round(principal.value * (matchingRate.value / 100)),
);

const totalReceipt = computed(
  () => principal.value + interest.value + matchingFund.value,
);
</script>

<template>
  <div v-if="product.hasCalculator" class="policy-calculator">
    <h3 class="policy-calculator__title">금융 계산기</h3>

    <div class="policy-calculator__inputs">
      <div class="policy-calculator__field policy-calculator__field--months">
        <span class="policy-calculator__months">{{
          product.calcPeriodMonths
        }}</span>
        <span class="policy-calculator__unit">개월간</span>
      </div>
      <div class="policy-calculator__field">
        <BaseInput
          v-model="inputAmount"
          type="amount"
          variant="underline"
          suffix="원"
          placeholder="0"
          :error="amountError"
        />
        <span class="policy-calculator__unit">납입시</span>
      </div>
    </div>
    <p v-if="amountError" class="policy-calculator__error">
      {{ amountError }}
    </p>

    <div class="policy-calculator__income-type">
      <BaseRadioGroup
        v-model="incomeType"
        name="incomeType"
        label="소득유형"
        :options="incomeTypeOptions"
      />
    </div>

    <div class="policy-calculator__result">
      <div class="policy-calculator__row">
        <span class="policy-calculator__label">원금</span>
        <span class="policy-calculator__value">{{ formatWon(principal) }}</span>
      </div>
      <div class="policy-calculator__row">
        <span class="policy-calculator__label"
          >예상 이자({{ product.minRate }}% 세전)</span
        >
        <span class="policy-calculator__value">{{ formatWon(interest) }}</span>
      </div>
      <div class="policy-calculator__row">
        <span class="policy-calculator__label">정부 매칭지원금</span>
        <span class="policy-calculator__value">{{
          formatWon(matchingFund)
        }}</span>
      </div>
      <div class="policy-calculator__row policy-calculator__row--total">
        <span class="policy-calculator__label">총 수령금</span>
        <span
          class="policy-calculator__value policy-calculator__value--total"
          >{{ formatWon(totalReceipt) }}</span
        >
      </div>
    </div>
  </div>
</template>

<style scoped>
.policy-calculator__title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.policy-calculator__inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.policy-calculator__field {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 6px;
}

.policy-calculator__field--months {
  flex: 0 0 auto;
}

.policy-calculator__months {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}

.policy-calculator__unit {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-body);
}

/* 인풋 자체 폭에 맞춰 줄바꿈되던 BaseInput 기본 에러 문구 대신, 아래에 전체 폭으로 따로 보여준다 */
.policy-calculator__field :deep(.base-input__footer) {
  display: none;
}

.policy-calculator__error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--danger);
}

.policy-calculator__income-type {
  margin-top: 16px;
}

.policy-calculator__result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}

.policy-calculator__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.policy-calculator__row + .policy-calculator__row {
  margin-top: 12px;
}

.policy-calculator__row--total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.policy-calculator__label {
  font-size: 14px;
  color: var(--text-body);
}

.policy-calculator__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
  text-align: right;
}

.policy-calculator__value--total {
  font-size: 17px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}
</style>
