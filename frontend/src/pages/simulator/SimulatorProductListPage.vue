<script setup>
// SCR-SIM-05 · 예적금 상품 계산기  (담당: 석윤)
// SimulatorPage의 "+ 상품 계산해보기"로 진입 — 카테고리(적금/예금/정책)를 고르고,
// 드롭다운으로 상품 하나를 선택하면 그 상품의 계산기+정보를 이 화면에서 바로 보여준다.
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import { formatKoreanWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import SavingCalculator from '@/components/common/SavingCalculator.vue';
import PolicyCalculator from '@/components/common/PolicyCalculator.vue';

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

const listItems = computed(() => {
  if (activeTab.value === 'deposits') {
    return depositProducts.value;
  }
  if (activeTab.value === 'policy') {
    return policyProducts.value;
  }
  return savingProducts.value;
});

const getItemId = (item) =>
  activeTab.value === 'policy' ? item.policyId : item.productId;

const productOptions = computed(() =>
  listItems.value.map((item) => ({
    value: getItemId(item),
    label: activeTab.value === 'policy' ? item.policyName : item.productName,
  })),
);

// 카테고리 탭을 바꾸면(=목록이 바뀌면) 그 목록의 첫 상품을 자동 선택한다.
// 단, SimulatorPage 미리보기 카드처럼 특정 상품을 지정해 들어온 경우(query.productId/policyId)엔
// 처음 한 번만 그 상품을 우선 선택한다.
const deepLinkedId = route.query.productId ?? route.query.policyId ?? null;
let didApplyDeepLink = false;

const selectedId = ref(null);
watch(
  listItems,
  (items) => {
    // 목록이 아직 로딩 전(빈 배열)이면 딥링크 매칭을 시도조차 하지 않는다 — 여기서 플래그를
    // 소진해버리면, 실제 데이터가 도착했을 때는 이미 "적용 완료"로 취급돼 매칭을 건너뛴다.
    if (items.length === 0) {
      selectedId.value = null;
      return;
    }
    if (!didApplyDeepLink) {
      didApplyDeepLink = true;
      const matched = items.find(
        (item) => String(getItemId(item)) === String(deepLinkedId),
      );
      if (deepLinkedId != null && matched) {
        selectedId.value = getItemId(matched);
        return;
      }
    }
    selectedId.value = items.length > 0 ? getItemId(items[0]) : null;
  },
  { immediate: true },
);

const selectedProduct = ref(null);
const detailLoading = ref(false);
const detailError = ref('');

const fetchSelectedDetail = async () => {
  if (selectedId.value == null) {
    selectedProduct.value = null;
    return;
  }
  detailLoading.value = true;
  detailError.value = '';
  try {
    selectedProduct.value =
      activeTab.value === 'policy'
        ? await productApi.findPolicyProductDetail(selectedId.value)
        : await productApi.findSavingProductDetail(selectedId.value);
  } catch (error) {
    console.error(error);
    detailError.value = '상품 정보를 불러오지 못했습니다.';
  } finally {
    detailLoading.value = false;
  }
};

watch(selectedId, fetchSelectedDetail);

const selectTab = (tab) => {
  activeTab.value = tab;
};

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

onMounted(fetchAll);

const limitText = computed(() => {
  if (!selectedProduct.value) {
    return '';
  }
  const min = `${formatKoreanWon(selectedProduct.value.minLimit)} 이상`;
  const max =
    selectedProduct.value.maxLimit == null
      ? '한도 없음'
      : `${formatKoreanWon(selectedProduct.value.maxLimit)} 이하`;
  return `${min} ${max}`;
});

// 최소/최대가 같으면 범위(~) 대신 단일 값으로 표시 (적금/예금 전용)
const saveTrmText = computed(() => {
  if (!selectedProduct.value || activeTab.value === 'policy') {
    return '';
  }
  const { minSaveTrm, maxSaveTrm } = selectedProduct.value;
  return minSaveTrm === maxSaveTrm
    ? `${minSaveTrm}개월`
    : `${minSaveTrm}개월 ~ ${maxSaveTrm}개월`;
});

const goPrevious = () => router.back();
const goProductLink = () => {
  const link =
    activeTab.value === 'policy'
      ? selectedProduct.value.policyLink
      : selectedProduct.value.productLink;
  window.open(link, '_blank', 'noopener,noreferrer');
};
</script>

<template>
  <div class="product-calc container py-4">
    <p class="product-calc__eyebrow">계산해보고 모으자</p>
    <h2 class="product-calc__title">예적금 상품 계산기</h2>

    <div class="product-calc__tabs">
      <CategoryButton
        v-for="tab in TABS"
        :key="tab.value"
        variant="square-yellow"
        :active="activeTab === tab.value"
        :label="tab.label"
        @click="selectTab(tab.value)"
      />
    </div>

    <p v-if="loadError" class="product-calc__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <EmptyState
      v-else-if="listItems.length === 0"
      title="상품이 없어요"
      description="조건에 맞는 상품을 찾을 수 없어요"
    />

    <template v-else>
      <BaseInput type="select" v-model="selectedId" :options="productOptions" />

      <p v-if="detailError" class="product-calc__error">{{ detailError }}</p>
      <p v-else-if="detailLoading" class="text-caption">불러오는 중...</p>

      <template v-else-if="selectedProduct">
        <BaseCard>
          <template v-if="activeTab === 'policy'">
            <div class="product-calc__row">
              <span class="product-calc__label">연 금리</span>
              <span class="product-calc__value">
                <span class="product-calc__highlight"
                  >{{ selectedProduct.minRate }}%</span
                >
                ~
                <span class="product-calc__highlight"
                  >{{ selectedProduct.maxRate }}%</span
                >
              </span>
            </div>
            <div class="product-calc__row">
              <span class="product-calc__label">가입기간</span>
              <span class="product-calc__value">{{
                selectedProduct.saveTrmNote
              }}</span>
            </div>
            <div class="product-calc__row">
              <span class="product-calc__label">납입금</span>
              <span class="product-calc__value">{{ limitText }}</span>
            </div>
          </template>
          <template v-else>
            <div class="product-calc__row">
              <span class="product-calc__label">신청 대상</span>
              <span class="product-calc__value">{{
                selectedProduct.joinMember
              }}</span>
            </div>
            <div class="product-calc__row">
              <span class="product-calc__label">연 금리</span>
              <span class="product-calc__value">
                <span class="product-calc__highlight"
                  >{{ selectedProduct.minRate }}%</span
                >
                ~
                <span class="product-calc__highlight"
                  >{{ selectedProduct.maxRate }}%</span
                >
              </span>
            </div>
            <div class="product-calc__row">
              <span class="product-calc__label">가입기간</span>
              <span class="product-calc__value">{{ saveTrmText }}</span>
            </div>
            <div class="product-calc__row">
              <span class="product-calc__label">납입금</span>
              <span class="product-calc__value">{{ limitText }}</span>
            </div>
          </template>
        </BaseCard>

        <!-- 정책 상품 중 hasCalculator=false인 상품은 계산기 자체가 없으므로 빈 카드를 띄우지 않는다. -->
        <BaseCard
          v-if="activeTab !== 'policy' || selectedProduct.hasCalculator"
        >
          <SavingCalculator
            v-if="activeTab !== 'policy'"
            :product="selectedProduct"
          />
          <PolicyCalculator v-else :product="selectedProduct" />
        </BaseCard>

        <BottomButtonBar
          primary-label="상품 자세히보기"
          secondary-label="이전"
          @primary-click="goProductLink"
          @secondary-click="goPrevious"
        />
      </template>
    </template>
  </div>
</template>

<style scoped>
.product-calc {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 88px;
}

.product-calc__eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-hint);
}

.product-calc__title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}

.product-calc__tabs {
  display: flex;
  gap: 8px;
}

.product-calc__error {
  color: var(--danger);
}

.product-calc__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-calc__row + .product-calc__row {
  margin-top: 12px;
}

.product-calc__label {
  font-size: 14px;
  color: var(--text-body);
}

.product-calc__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
  text-align: right;
}

.product-calc__highlight {
  color: var(--danger);
  font-weight: 700;
}
</style>
