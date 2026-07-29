<script setup>
// SCR-SIM-04 · 정책 상품 상세  (담당: 석윤)
// 정책 금융상품 상세 정보
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const policyId = route.params.policyId;

const product = ref(null);
const isLoading = ref(true);
const loadError = ref('');

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

// benefits/joinMember 는 "1. ...\n2. ..." 형태로 저장돼 있어, 줄바꿈만 나눠서 그대로 보여준다.
const benefitLines = computed(() => product.value.benefits.split('\n'));
const joinMemberLines = computed(() => product.value.joinMember.split('\n'));

const limitText = computed(() => {
  const min = formatWon(product.value.minLimit);
  const max =
    product.value.maxLimit == null
      ? '한도 없음'
      : formatWon(product.value.maxLimit);
  return `${min} ~ ${max} / ${product.value.saveTrmNote}`;
});

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
        <section class="policy-detail__section">
          <h3 class="policy-detail__section-title">주요혜택</h3>
          <ul class="policy-detail__list">
            <li v-for="(line, idx) in benefitLines" :key="idx">{{ line }}</li>
          </ul>
        </section>

        <section class="policy-detail__section">
          <h3 class="policy-detail__section-title">가입 대상</h3>
          <ul class="policy-detail__list">
            <li v-for="(line, idx) in joinMemberLines" :key="idx">
              {{ line }}
            </li>
          </ul>
        </section>

        <section class="policy-detail__section">
          <h3 class="policy-detail__section-title">월 납입금 / 가입기간</h3>
          <p class="policy-detail__value">{{ limitText }}</p>
        </section>
      </BaseCard>

      <!-- hasCalculator=true 상품만 만기 시뮬레이션 진입 공간을 예약해둔다. -->
      <!-- TODO: 만기 시뮬레이션 UI 예정 -->
      <div
        v-if="product.hasCalculator"
        class="policy-detail__calculator-slot"
      />

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

.policy-detail__section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.policy-detail__section + .policy-detail__section {
  margin-top: 16px;
}

.policy-detail__section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.policy-detail__list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.policy-detail__list li {
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-body);
}

.policy-detail__value {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
}

.policy-detail__calculator-slot {
  min-height: 0;
}
</style>
