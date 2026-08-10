<script setup>
// SCR-SIM-04 · 정책 상품 상세  (담당: 석윤)
// 정책 금융상품 상세 정보
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import PolicyCalculator from '@/components/common/PolicyCalculator.vue';

const route = useRoute();
const router = useRouter();
const policyId = route.params.policyId;

const product = ref(null);
const isLoading = ref(true);
const loadError = ref('');

const limitText = computed(() => {
  const min = `${formatWon(product.value.minLimit)} 이상`;
  const max =
    product.value.maxLimit == null
      ? '한도 없음'
      : `${formatWon(product.value.maxLimit)} 이하`;
  return `${min} ${max}`;
});

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

      <PolicyCalculator :product="product" />

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
  background: linear-gradient(
    180deg,
    var(--kb-yellow) 0%,
    var(--surface-default) 260px
  );
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
</style>
