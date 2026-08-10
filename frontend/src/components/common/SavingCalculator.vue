<script setup>
// SavingCalculator - 적금/예금 "금융 계산기" (개월수+금액 입력 → 원금/이자/세금/총수령금)
// SavingProductDetailPage와 SimulatorProductListPage(예적금 상품 계산기)에서 동일하게 쓰는 공통 컴포넌트.
// 계산 로직이 여기 한 곳에만 있어야 두 화면의 결과가 어긋나지 않는다.
import { computed, ref, watch } from 'vue';
import { formatWon } from '@/util/format';
import BaseInput from '@/components/common/BaseInput.vue';

// 일반과세 이자소득세율(14% + 지방소득세 1.4%). 비과세 상품은 isTaxExempt로 0 처리.
const TAX_RATE = 0.154;

const props = defineProps({
  product: { type: Object, required: true },
});

const selectedMonths = ref(props.product.minSaveTrm);
const inputAmount = ref('');

// 드롭다운으로 다른 상품을 선택해 product가 바뀌면, 이전 상품 기준 입력값이 남지 않도록 초기화한다.
watch(
  () => props.product.productId,
  () => {
    selectedMonths.value = props.product.minSaveTrm;
    inputAmount.value = '';
  },
);

// 예금(DEPOSIT)은 일시불 예치, 적금(SAVING)은 매월 정기 납입 — 계산 방식이 다르다.
const isDeposit = computed(() => props.product.productType === 'DEPOSIT');

// 가입기간(개월)마다 기본금리가 다르게 저장돼 있어, 드롭다운 선택지도 그 개월수 기준으로 만든다.
const monthOptions = computed(() =>
  [...props.product.saveTrmRates]
    .sort((a, b) => a.saveTrm - b.saveTrm)
    .map((rate) => ({ label: `${rate.saveTrm}`, value: rate.saveTrm })),
);

const selectedRate = computed(() => {
  if (selectedMonths.value == null) {
    return 0;
  }
  const matched = props.product.saveTrmRates.find(
    (rate) => rate.saveTrm === selectedMonths.value,
  );
  return matched ? Number(matched.basicRate) : 0;
});

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

const principal = computed(() => {
  const amount = Number(inputAmount.value) || 0;
  const months = Number(selectedMonths.value) || 0;
  // 예금은 입력 금액을 한 번만 예치하는 것이므로 개월수를 곱하지 않는다.
  return isDeposit.value ? amount : amount * months;
});

const interest = computed(() => {
  const amount = Number(inputAmount.value) || 0;
  const months = Number(selectedMonths.value) || 0;
  if (!amount || !months) {
    return 0;
  }
  // 예금: 예치금 전체가 가입기간 내내 단리로 굴러간다.
  if (isDeposit.value) {
    return Math.round(amount * (selectedRate.value / 100) * (months / 12));
  }
  // 적금: 회차별 납입액이 잔여 개월 수만큼 단리로 붙는다 (1회차는 개월수, 마지막 회차는 1개월치).
  const totalInvestedMonths = (months * (months + 1)) / 2;
  return Math.round(
    amount * (selectedRate.value / 100) * (totalInvestedMonths / 12),
  );
});

const tax = computed(() =>
  props.product.isTaxExempt ? 0 : Math.round(interest.value * TAX_RATE),
);

const totalReceipt = computed(
  () => principal.value + interest.value - tax.value,
);
</script>

<template>
  <div class="saving-calculator">
    <h3 class="saving-calculator__title">금융 계산기</h3>

    <div class="saving-calculator__inputs">
      <div class="saving-calculator__field saving-calculator__field--months">
        <BaseInput
          v-model="selectedMonths"
          type="select"
          :options="monthOptions"
        />
        <span class="saving-calculator__unit">개월간</span>
      </div>
      <div class="saving-calculator__field">
        <BaseInput
          v-model="inputAmount"
          type="amount"
          variant="underline"
          suffix="원"
          placeholder="0"
          :error="amountError"
        />
        <span class="saving-calculator__unit">{{
          isDeposit ? '예치시' : '납입시'
        }}</span>
      </div>
    </div>
    <p v-if="amountError" class="saving-calculator__error">
      {{ amountError }}
    </p>

    <div class="saving-calculator__result">
      <div class="saving-calculator__row">
        <span class="saving-calculator__label">원금</span>
        <span class="saving-calculator__value">{{ formatWon(principal) }}</span>
      </div>
      <div class="saving-calculator__row">
        <span class="saving-calculator__label"
          >예상 이자({{ selectedRate }}% 세전)</span
        >
        <span class="saving-calculator__value">{{ formatWon(interest) }}</span>
      </div>
      <div class="saving-calculator__row">
        <span class="saving-calculator__label">세금</span>
        <span class="saving-calculator__value">{{ formatWon(tax) }}</span>
      </div>
      <div class="saving-calculator__row saving-calculator__row--total">
        <span class="saving-calculator__label">총 수령금</span>
        <span
          class="saving-calculator__value saving-calculator__value--total"
          >{{ formatWon(totalReceipt) }}</span
        >
      </div>
    </div>
  </div>
</template>

<style scoped>
.saving-calculator__title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.saving-calculator__inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.saving-calculator__field {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 6px;
}

.saving-calculator__field--months {
  flex: 0 0 auto;
}

.saving-calculator__field--months :deep(.dropdown) {
  width: auto;
}

.saving-calculator__field--months :deep(.dropdown__button) {
  width: auto;
  justify-content: flex-start;
  padding: 8px 22px 8px 2px;
}

.saving-calculator__field--months :deep(.dropdown__text) {
  flex: none;
  text-align: left;
}

.saving-calculator__unit {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-body);
}

/* 인풋 자체 폭에 맞춰 줄바꿈되던 BaseInput 기본 에러 문구 대신, 아래에 전체 폭으로 따로 보여준다 */
.saving-calculator__field :deep(.base-input__footer) {
  display: none;
}

.saving-calculator__error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--danger);
}

.saving-calculator__result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}

.saving-calculator__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.saving-calculator__row + .saving-calculator__row {
  margin-top: 12px;
}

.saving-calculator__row--total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.saving-calculator__label {
  font-size: 14px;
  color: var(--text-body);
}

.saving-calculator__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
  text-align: right;
}

.saving-calculator__value--total {
  font-size: 17px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}
</style>
