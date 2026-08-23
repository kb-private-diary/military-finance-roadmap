<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import saluteImage from '@/assets/images/salute.png';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const products = ref({
  cards: [],
  savings: [],
  insurances: [],
});
const loading = ref(false);
const saving = ref(false);
const loadError = ref('');
const saveError = ref('');
const isCompleteModalOpen = ref(false);
let scrollContainer = null;

const productGroups = computed(() => [
  {
    key: 'cards',
    title: '관련 카드상품',
    items: products.value.cards,
  },
  {
    key: 'savings',
    title: '관련 적금상품',
    items: products.value.savings,
  },
  {
    key: 'insurances',
    title: '관련 보험상품',
    items: products.value.insurances,
  },
]);

const hasProducts = computed(() =>
  productGroups.value.some((group) => group.items.length > 0),
);

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  fallback;

const loadProducts = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const response = await travelApi.findProducts(goalId);
    const data = response.data?.data ?? {};
    products.value = {
      cards: data.cards ?? [],
      savings: data.savings ?? [],
      insurances: data.insurances ?? [],
    };
  } catch (error) {
    loadError.value = readErrorMessage(
      error,
      '금융상품 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const goPrevious = () =>
  router.push({ name: 'TravelPackages', params: { goalId } });

const saveRoadmap = async () => {
  if (saving.value || loading.value) return;

  saving.value = true;
  saveError.value = '';
  try {
    await travelApi.confirmGoal(goalId);
    isCompleteModalOpen.value = true;
  } catch (error) {
    saveError.value = readErrorMessage(
      error,
      '여행 로드맵을 저장하지 못했습니다.',
    );
  } finally {
    saving.value = false;
  }
};

const goToDetail = () =>
  router.push({ name: 'TravelGoalDetail', params: { goalId } });

onMounted(() => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');
  loadProducts();
});

onBeforeUnmount(() => {
  scrollContainer?.classList.remove('travel-scrollbar-hidden');
});
</script>

<template>
  <div class="travel-products">
    <RoadmapCharacterSlider :step="4" label="여행 로드맵" />

    <PageHeader
      title="여행 떠나기 전, 아래 금융상품은 어떠십니까?"
      description="여행에서 활용하기 좋은 금융상품입니다. 알찬 여행이 되시길 바랍니다."
    />

    <div v-if="loading" class="status-box text-caption" role="status">
      금융상품 정보를 불러오고 있습니다.
    </div>

    <div
      v-else-if="loadError"
      class="status-box status-box--error text-caption"
      role="alert"
    >
      <p>{{ loadError }}</p>
      <button type="button" @click="loadProducts">다시 시도</button>
    </div>

    <EmptyState
      v-else-if="!hasProducts"
      title="조회 가능한 금융상품이 없습니다."
      description="상품 정보가 등록되면 이 화면에서 확인할 수 있어요."
    />

    <div v-else class="product-groups">
      <section
        v-for="group in productGroups"
        v-show="group.items.length > 0"
        :key="group.key"
        class="product-group"
      >
        <h2 class="product-group__title text-label">
          {{ group.title }}
        </h2>

        <ul class="product-list">
          <li
            v-for="product in group.items"
            :key="`${product.type}:${product.productId}`"
          >
            <BaseCard
              class="product-card"
              padding="18px"
            >
              <div class="product-card__content">
                <strong class="product-card__name text-label">
                  {{ product.name }}
                </strong>
                <p
                  v-if="product.description"
                  class="product-card__description text-caption"
                >
                  {{ product.description }}
                </p>
                <p
                  v-if="product.qualification"
                  class="product-card__qualification text-caption"
                >
                  {{ product.qualification }}
                </p>
              </div>

              <a
                v-if="product.url"
                class="product-card__external"
                :href="product.url"
                target="_blank"
                rel="noopener noreferrer"
                :aria-label="`${product.name} 외부 상품 페이지 열기`"
              >
                <svg
                  viewBox="0 0 24 24"
                  width="20"
                  height="20"
                  aria-hidden="true"
                >
                  <path
                    d="M14 4h6v6M20 4l-9 9M18 13v5a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h5"
                  />
                </svg>
              </a>
            </BaseCard>
          </li>
        </ul>
      </section>
    </div>

    <p v-if="saveError" class="save-error text-caption" role="alert">
      {{ saveError }}
    </p>

    <section
      v-if="!loading && !loadError"
      class="save-guide"
      aria-label="여행 로드맵 저장 안내"
    >
      <p class="text-label">
        지금까지 여행 로드맵이었습니다.<br />
        마음에 든다면 저장해주세요.
      </p>
      <img :src="saluteImage" alt="경례하는 캐릭터" />
    </section>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="saving ? '저장 중...' : '저장'"
      :primary-disabled="loading || saving"
      @secondary-click="goPrevious"
      @primary-click="saveRoadmap"
    />

    <BaseModal
      v-model="isCompleteModalOpen"
      title="알림"
      confirm-text="확인"
      @confirm="goToDetail"
      @cancel="goToDetail"
    >
      <p class="complete-modal__message">
        여행 로드맵이 저장되었습니다.
      </p>
    </BaseModal>
  </div>
</template>

<style scoped>
/* 제목 아래 컨텐츠 간격 통일(20px) */
.travel-products :deep(.page-header) {
  margin-bottom: 20px;
}
.travel-products {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.travel-products :deep(.character-slider) {
  margin-bottom: 28px;
}

.status-box {
  padding: 48px 12px;
  color: var(--text-muted);
  text-align: center;
}

.status-box p {
  margin: 0;
}

.status-box--error {
  color: var(--danger);
}

.status-box button {
  margin-top: 12px;
  padding: 8px 14px;
  border: 0;
  background: var(--travel-primary);
  color: var(--surface-default);
}

.product-groups {
  display: grid;
  gap: 26px;
}

.product-group__title {
  margin: 0 0 10px;
}

.product-list {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.product-card {
  position: relative;
  min-height: 104px;
}

.product-card__content {
  padding-right: 38px;
}

.product-card__name {
  display: block;
  line-height: 1.45;
}

.product-card__description,
.product-card__qualification {
  display: -webkit-box;
  margin: 7px 0 0;
  overflow: hidden;
  color: var(--text-muted);
  white-space: pre-line;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-card__qualification {
  color: var(--text-hint);
  -webkit-line-clamp: 1;
}

.product-card__external {
  position: absolute;
  top: 50%;
  right: 16px;
  display: grid;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: var(--text-strong);
  text-decoration: none;
  transform: translateY(-50%);
  place-items: center;
}

.product-card__external:hover,
.product-card__external:focus-visible {
  background: var(--surface-muted);
}

.product-card__external svg {
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.save-error {
  margin: 14px 0 0;
  color: var(--danger);
  text-align: center;
}

.complete-modal__message {
  margin: 0;
}

.save-guide {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-top: 46px;
  padding: 0 8px;
}

.save-guide p {
  margin: 0 0 8px;
  line-height: 1.6;
}

.save-guide img {
  width: 70px;
  height: auto;
}

:global(.app-content.travel-scrollbar-hidden) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

:global(.app-content.travel-scrollbar-hidden::-webkit-scrollbar) {
  display: none;
  width: 0;
  height: 0;
}
</style>
