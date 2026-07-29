<script setup>
// SCR-SIM-03 · 적금 상품 상세  (담당: 석윤)
// KB 예적금 상품 상세 정보
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const productId = route.params.productId;

const product = ref(null);
const isLoading = ref(true);
const loadError = ref('');

const formatAmount = (won) =>
  won >= 10000
    ? `${Math.round(won / 10000)}만원`
    : `${won.toLocaleString('ko-KR')}원`;

const limitText = () => {
  const min = `${formatAmount(product.value.minLimit)} 이상`;
  const max =
    product.value.maxLimit == null
      ? '한도 없음'
      : `${formatAmount(product.value.maxLimit)} 이하`;
  return `${min} ${max}`;
};

const fetchProduct = async () => {
  isLoading.value = true;
  loadError.value = '';
  try {
    product.value = await productApi.findSavingProductDetail(productId);
  } catch (error) {
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

      <div class="saving-detail__card">
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
          <span class="saving-detail__value">
            {{ product.minSaveTrm }} ~ {{ product.maxSaveTrm }}개월
          </span>
        </div>
        <div class="saving-detail__row">
          <span class="saving-detail__label">납입금</span>
          <span class="saving-detail__value">{{ limitText() }}</span>
        </div>
      </div>

      <div class="saving-detail__divider" />

      <!-- TODO: 만기 시뮬레이션 UI 예정 -->

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

.saving-detail__card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
}

.saving-detail__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.saving-detail__divider {
  height: 1px;
  margin-top: 8px;
  background-color: var(--line);
}
</style>
