<script setup>
// SCR-SIM-04 · 정책 상품 상세  (담당: 석윤)
// 정책 금융상품 상세 정보
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseRadioGroup from '@/components/common/BaseRadioGroup.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const policyId = route.params.policyId;

const product = ref(null);
const isLoading = ref(true);
const loadError = ref('');

const inputAmount = ref('');
// 소득유형: NORMAL(일반) | PREFER(우대형) — 정부매칭지원금 비율만 바뀐다 (이자율은 항상 minRate 고정)
const incomeType = ref('NORMAL');

const incomeTypeOptions = computed(() => {
  if (!product.value) {
    return [];
  }
  return [
    { label: `일반(${product.value.normalMatchRate}%)`, value: 'NORMAL' },
    { label: `우대형(${product.value.preferMatchRate}%)`, value: 'PREFER' },
  ];
});

const matchingRate = computed(() => {
  if (!product.value) {
    return 0;
  }
  return incomeType.value === 'PREFER'
    ? Number(product.value.preferMatchRate)
    : Number(product.value.normalMatchRate);
});

const limitText = computed(() => {
  const min = `${formatWon(product.value.minLimit)} 이상`;
  const max =
    product.value.maxLimit == null
      ? '한도 없음'
      : `${formatWon(product.value.maxLimit)} 이하`;
  return `${min} ${max}`;
});

// 상품별 최소·최대 납입금(위 "납입금" 항목과 동일한 한도)을 벗어나면 인라인으로 안내한다.
const amountError = computed(() => {
  if (!product.value || !inputAmount.value) {
    return '';
  }
  const amount = Number(inputAmount.value);
  if (amount < product.value.minLimit) {
    return `최소 ${formatWon(product.value.minLimit)} 이상 입력해주세요`;
  }
  if (product.value.maxLimit != null && amount > product.value.maxLimit) {
    return `최대 ${formatWon(product.value.maxLimit)} 이하로 입력해주세요`;
  }
  return '';
});

// 정기적금식 단리: 매월 납입액이 계산 기간(calcPeriodMonths) 동안 매달 쌓인다.
const principal = computed(() => {
  const amount = Number(inputAmount.value) || 0;
  const months = product.value?.calcPeriodMonths || 0;
  return amount * months;
});

// 정부매칭분도 매달 같이 적립되는 것처럼 원금+매칭분 합산 잔액에 이자가 붙는다.
const interest = computed(() => {
  const amount = Number(inputAmount.value) || 0;
  const months = product.value?.calcPeriodMonths || 0;
  if (!amount || !months || !product.value) {
    return 0;
  }
  const effectiveAmount = amount * (1 + matchingRate.value / 100);
  const totalInvestedMonths = (months * (months + 1)) / 2;
  return Math.round(
    effectiveAmount *
      (Number(product.value.minRate) / 100) *
      (totalInvestedMonths / 12),
  );
});

const matchingFund = computed(() =>
  Math.round(principal.value * (matchingRate.value / 100)),
);

const totalReceipt = computed(
  () => principal.value + interest.value + matchingFund.value,
);

const fetchProduct = async () => {
  isLoading.value = true;
  loadError.value = '';
  try {
    product.value = await productApi.findPolicyProductDetail(policyId);
  } catch (error) {
    console.error(error);
    loadError.value = '상품 정보를 불러오지 못했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const goPrevious = () => router.back();
const goProductLink = () => {
  window.open(product.value.policyLink, '_blank', 'noopener,noreferrer');
};

onMounted(fetchProduct);
</script>

<template>
  <div class="policy-detail container py-4">
    <p v-if="loadError" class="policy-detail__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <template v-else-if="product">
      <p class="policy-detail__eyebrow">정책 상품</p>
      <h2 class="policy-detail__title">{{ product.policyName }}</h2>

      <BaseCard>
        <div class="policy-detail__row">
          <span class="policy-detail__label">연 금리</span>
          <span class="policy-detail__value">
            <span class="policy-detail__highlight">{{ product.minRate }}%</span>
            ~
            <span class="policy-detail__highlight">{{ product.maxRate }}%</span>
          </span>
        </div>
        <div class="policy-detail__row">
          <span class="policy-detail__label">가입기간</span>
          <span class="policy-detail__value">{{ product.saveTrmNote }}</span>
        </div>
        <div class="policy-detail__row">
          <span class="policy-detail__label">납입금</span>
          <span class="policy-detail__value">{{ limitText }}</span>
        </div>
      </BaseCard>

      <BaseCard v-if="product.hasCalculator">
        <h3 class="policy-detail__sim-title">만기예상액 시뮬레이션</h3>

        <div class="policy-detail__sim-inputs">
          <div
            class="policy-detail__sim-field policy-detail__sim-field--months"
          >
            <span class="policy-detail__sim-months">{{
              product.calcPeriodMonths
            }}</span>
            <span class="policy-detail__sim-unit">개월간</span>
          </div>
          <div class="policy-detail__sim-field">
            <BaseInput
              v-model="inputAmount"
              type="amount"
              variant="underline"
              suffix="원"
              placeholder="0"
              :error="amountError"
            />
            <span class="policy-detail__sim-unit">납입시</span>
          </div>
        </div>
        <p v-if="amountError" class="policy-detail__sim-error">
          {{ amountError }}
        </p>

        <div class="policy-detail__income-type">
          <BaseRadioGroup
            v-model="incomeType"
            name="incomeType"
            label="소득유형"
            :options="incomeTypeOptions"
          />
        </div>

        <div class="policy-detail__sim-result">
          <div class="policy-detail__row">
            <span class="policy-detail__label">원금</span>
            <span class="policy-detail__value">{{ formatWon(principal) }}</span>
          </div>
          <div class="policy-detail__row">
            <span class="policy-detail__label"
              >예상 이자({{ product.minRate }}% 세전)</span
            >
            <span class="policy-detail__value">{{ formatWon(interest) }}</span>
          </div>
          <div class="policy-detail__row">
            <span class="policy-detail__label">정부 매칭지원금</span>
            <span class="policy-detail__value">{{
              formatWon(matchingFund)
            }}</span>
          </div>
          <div class="policy-detail__row policy-detail__row--total">
            <span class="policy-detail__label">총 수령금</span>
            <span class="policy-detail__value policy-detail__value--total">{{
              formatWon(totalReceipt)
            }}</span>
          </div>
        </div>
      </BaseCard>

      <BottomButtonBar
        primary-label="상품 자세히보기"
        secondary-label="이전"
        @primary-click="goProductLink"
        @secondary-click="goPrevious"
      />
    </template>
  </div>
</template>

<style scoped>
.policy-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 88px;
}

.policy-detail__error {
  color: var(--danger);
}

.policy-detail__eyebrow {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}

.policy-detail__title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-strong);
}

.policy-detail__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.policy-detail__row + .policy-detail__row {
  margin-top: 12px;
}

.policy-detail__label {
  font-size: 14px;
  color: var(--text-body);
}

.policy-detail__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
  text-align: right;
}

.policy-detail__highlight {
  color: var(--danger);
  font-weight: 700;
}

.policy-detail__sim-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.policy-detail__sim-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.policy-detail__sim-field {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 6px;
}

.policy-detail__sim-field--months {
  flex: 0 0 auto;
}

.policy-detail__sim-months {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}

.policy-detail__sim-unit {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-body);
}

/* 인풋 자체 폭에 맞춰 줄바꿈되던 BaseInput 기본 에러 문구 대신, 아래에 카드 전체 폭으로 따로 보여준다 */
.policy-detail__sim-field :deep(.base-input__footer) {
  display: none;
}

.policy-detail__sim-error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--danger);
}

.policy-detail__income-type {
  margin-top: 16px;
}

.policy-detail__sim-result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}

.policy-detail__row--total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.policy-detail__value--total {
  font-size: 17px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}
</style>
