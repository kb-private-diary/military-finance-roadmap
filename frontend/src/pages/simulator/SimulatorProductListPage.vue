<script setup>
// SCR-SIM-05 · 예적금/정책 상품 전체 목록  (담당: 석윤)
// SimulatorPage의 "더보기"로 진입 — 적금/예금/정책 탭으로 전체 상품을 보여준다.
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';

const TABS = [
  { value: 'savings', label: '적금' },
  { value: 'deposits', label: '예금' },
  { value: 'policy', label: '정책' },
];

const route = useRoute();
const router = useRouter();

const activeTab = ref(
  TABS.some((tab) => tab.value === route.query.tab)
    ? route.query.tab
    : 'savings',
);

const savingProducts = ref([]);
const depositProducts = ref([]);
const policyProducts = ref([]);
const isLoading = ref(true);
const loadError = ref('');

const displayedItems = computed(() => {
  if (activeTab.value === 'deposits') {
    return depositProducts.value;
  }
  if (activeTab.value === 'policy') {
    return policyProducts.value;
  }
  return savingProducts.value;
});

const fetchAll = async () => {
  isLoading.value = true;
  loadError.value = '';
  try {
    const [savings, deposits, policies] = await Promise.all([
      productApi.findSavingProductList('savings'),
      productApi.findSavingProductList('deposits'),
      productApi.findPolicyProductList(),
    ]);
    savingProducts.value = savings;
    depositProducts.value = deposits;
    policyProducts.value = policies;
  } catch (error) {
    console.error(error);
    loadError.value = '상품 정보를 불러오지 못했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const selectTab = (tab) => {
  activeTab.value = tab;
};

const goToDetail = (item) => {
  if (activeTab.value === 'policy') {
    router.push({
      name: 'PolicyProductDetail',
      params: { policyId: item.policyId },
    });
  } else {
    router.push({
      name: 'SavingProductDetail',
      params: { productId: item.productId },
    });
  }
};

onMounted(fetchAll);
</script>

<template>
  <div class="product-list container py-4">
    <p class="product-list__eyebrow">모으고 또 모으자</p>
    <h2 class="product-list__title">예적금 시뮬레이션</h2>

    <div class="product-list__tabs">
      <CategoryButton
        v-for="tab in TABS"
        :key="tab.value"
        variant="square-yellow"
        :active="activeTab === tab.value"
        :label="tab.label"
        @click="selectTab(tab.value)"
      />
    </div>

    <p v-if="loadError" class="product-list__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <EmptyState
      v-else-if="displayedItems.length === 0"
      title="추천 상품이 없어요"
      description="조건에 맞는 상품을 찾을 수 없어요"
    />

    <div v-else class="product-list__list">
      <BaseCard
        v-for="item in displayedItems"
        :key="activeTab === 'policy' ? item.policyId : item.productId"
        class="product-card"
        padding="14px 16px"
        @click="goToDetail(item)"
      >
        <template v-if="activeTab === 'policy'">
          <p class="product-card__title">{{ item.policyName }}</p>
          <p class="product-card__desc">최대 연 {{ item.maxRate }}% 금리</p>
        </template>
        <template v-else>
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
        </template>
      </BaseCard>
    </div>
  </div>
</template>

<style scoped>
.product-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 24px;
}

.product-list__eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-hint);
}

.product-list__title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}

.product-list__tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.product-list__error {
  color: var(--danger);
}

.product-list__list {
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
  color: var(--text-strong);
}

.product-card__desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
