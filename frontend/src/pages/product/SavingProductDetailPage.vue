<script setup>
// SCR-SIM-03 · 적금 상품 상세  (담당: 석윤)
// KB 예적금 상품 상세 정보
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

// 일반과세 이자소득세율(14% + 지방소득세 1.4%). 비과세 상품은 isTaxExempt 로 0 처리.
const TAX_RATE = 0.154;

const route = useRoute();
const router = useRouter();
const productId = route.params.productId;

const product = ref(null);
const isLoading = ref(true);
const loadError = ref('');

const selectedMonths = ref(null);
const inputAmount = ref('');

const limitText = computed(() => {
  const min = `${formatWon(product.value.minLimit)} 이상`;
  const max =
    product.value.maxLimit == null
      ? '한도 없음'
      : `${formatWon(product.value.maxLimit)} 이하`;
  return `${min} ${max}`;
});

// 최소/최대가 같으면 범위(~) 대신 단일 값으로 표시
const saveTrmText = computed(() => {
  const { minSaveTrm, maxSaveTrm } = product.value;
  return minSaveTrm === maxSaveTrm
    ? `${minSaveTrm}개월`
    : `${minSaveTrm} ~ ${maxSaveTrm}개월`;
});

// 가입기간(개월)마다 기본금리가 다르게 저장돼 있어, 드롭다운 선택지도 그 개월수 기준으로 만든다.
const monthOptions = computed(() => {
  if (!product.value) {
    return [];
  }
  return [...product.value.saveTrmRates]
    .sort((a, b) => a.saveTrm - b.saveTrm)
    .map((rate) => ({ label: `${rate.saveTrm}개월`, value: rate.saveTrm }));
});

const selectedRate = computed(() => {
  if (!product.value || selectedMonths.value == null) {
    return 0;
  }
  const matched = product.value.saveTrmRates.find(
    (rate) => rate.saveTrm === selectedMonths.value,
  );
  return matched ? Number(matched.basicRate) : 0;
});

// 상품별 최소·최대 납입금(위 "납입금" 항목과 동일한 한도)을 벗어나면 인라인으로 안내한다.
const amountError = computed(() => {
  if (!product.value || inputAmount.value === '') {
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

// 예금(DEPOSIT)은 일시불 예치, 적금(SAVING)은 매월 정기 납입 — 계산 방식이 다르다.
const isDeposit = computed(() => product.value?.productType === 'DEPOSIT');

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
  product.value?.isTaxExempt ? 0 : Math.round(interest.value * TAX_RATE),
);

const totalReceipt = computed(
  () => principal.value + interest.value - tax.value,
);

const fetchProduct = async () => {
  isLoading.value = true;
  loadError.value = '';
  try {
    product.value = await productApi.findSavingProductDetail(productId);
    selectedMonths.value = product.value.minSaveTrm;
  } catch (error) {
    console.error(error);
    loadError.value = '상품 정보를 불러오지 못했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const eyebrow = computed(() =>
  product.value.productType === 'DEPOSIT' ? '예금 상품' : '적금 상품',
);

const goPrevious = () => router.back();
const goProductLink = () => {
  window.open(product.value.productLink, '_blank', 'noopener,noreferrer');
};

onMounted(fetchProduct);
</script>

<template>
  <div class="saving-detail container py-4">
    <p v-if="loadError" class="saving-detail__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <template v-else-if="product">
      <p class="saving-detail__eyebrow">{{ eyebrow }}</p>
      <h2 class="saving-detail__title">{{ product.productName }}</h2>

      <BaseCard>
        <div class="saving-detail__row">
          <span class="saving-detail__label">신청 대상</span>
          <span class="saving-detail__value">{{ product.joinMember }}</span>
        </div>
        <div class="saving-detail__row">
          <span class="saving-detail__label">연 금리</span>
          <span class="saving-detail__value">
            <span class="saving-detail__highlight">{{ product.minRate }}%</span>
            ~
            <span class="saving-detail__highlight">{{ product.maxRate }}%</span>
          </span>
        </div>
        <div class="saving-detail__row">
          <span class="saving-detail__label">가입기간</span>
          <span class="saving-detail__value">{{ saveTrmText }}</span>
        </div>
        <div class="saving-detail__row">
          <span class="saving-detail__label">납입금</span>
          <span class="saving-detail__value">{{ limitText }}</span>
        </div>
      </BaseCard>

      <BaseCard>
        <h3 class="saving-detail__sim-title">만기예상액 시뮬레이션</h3>

        <div class="saving-detail__sim-inputs">
          <div
            class="saving-detail__sim-field saving-detail__sim-field--months"
          >
            <BaseInput
              v-model="selectedMonths"
              type="select"
              :options="monthOptions"
            />
            <span class="saving-detail__sim-unit">개월간</span>
          </div>
          <div class="saving-detail__sim-field">
            <BaseInput
              v-model="inputAmount"
              type="number"
              variant="underline"
              suffix="원"
              placeholder="0"
              :error="amountError"
            />
            <span class="saving-detail__sim-unit">{{
              isDeposit ? '예치시' : '납입시'
            }}</span>
          </div>
        </div>
        <p v-if="amountError" class="saving-detail__sim-error">
          {{ amountError }}
        </p>

        <div class="saving-detail__sim-result">
          <div class="saving-detail__row">
            <span class="saving-detail__label">원금</span>
            <span class="saving-detail__value">{{ formatWon(principal) }}</span>
          </div>
          <div class="saving-detail__row">
            <span class="saving-detail__label"
              >예상 이자({{ selectedRate }}% 세전)</span
            >
            <span class="saving-detail__value">{{ formatWon(interest) }}</span>
          </div>
          <div class="saving-detail__row">
            <span class="saving-detail__label">세금</span>
            <span class="saving-detail__value">{{ formatWon(tax) }}</span>
          </div>
          <div class="saving-detail__row saving-detail__row--total">
            <span class="saving-detail__label">총 수령금</span>
            <span class="saving-detail__value saving-detail__value--total">{{
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
.saving-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 88px;
}

.saving-detail__error {
  color: var(--danger);
}

.saving-detail__eyebrow {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}

.saving-detail__title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-strong);
}

.saving-detail__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.saving-detail__row + .saving-detail__row {
  margin-top: 12px;
}

.saving-detail__label {
  font-size: 14px;
  color: var(--text-body);
}

.saving-detail__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
  text-align: right;
}

.saving-detail__highlight {
  color: var(--danger);
  font-weight: 700;
}

.saving-detail__sim-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.saving-detail__sim-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.saving-detail__sim-field {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 6px;
}

.saving-detail__sim-field--months {
  flex: 0 0 auto;
}

.saving-detail__sim-field--months :deep(.dropdown) {
  width: auto;
}

.saving-detail__sim-field--months :deep(.dropdown__button) {
  width: auto;
  justify-content: flex-start;
  padding: 8px 22px 8px 2px;
}

.saving-detail__sim-field--months :deep(.dropdown__text) {
  flex: none;
  text-align: left;
}

.saving-detail__sim-unit {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-body);
}

/* 인풋 자체 폭에 맞춰 줄바꿈되던 BaseInput 기본 에러 문구 대신, 아래에 카드 전체 폭으로 따로 보여준다 */
.saving-detail__sim-field :deep(.base-input__footer) {
  display: none;
}

.saving-detail__sim-error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--danger);
}

.saving-detail__sim-result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}

.saving-detail__row--total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.saving-detail__value--total {
  font-size: 17px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}
</style>
