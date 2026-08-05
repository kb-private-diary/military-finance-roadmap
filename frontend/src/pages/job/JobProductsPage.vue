<script setup>
// SCR-JOB-04 · step4) 진로 금융상품 추천  (담당: 지원)
// step4 - 청년 지원 정책·KB 예적금 추천 → 저장
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import jobApi from '@/api/jobApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import roadmapRabbit from '@/assets/images/roadmap/rabbit.png';
import BaseModal from '@/components/common/BaseModal.vue';
import { useToast } from '@/composables/useToast';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const currentStep = 4;
const progress = computed(() => (currentStep / 4) * 100);

const goalId = computed(() => Number(route.params.goalId));

const loading = ref(false);
const saving = ref(false);
const isCompleteModalOpen = ref(false);
const policies = ref([]);
const financialProducts = ref([]);

const isValidGoalId = computed(
  () => Number.isInteger(goalId.value) && goalId.value > 0,
);

const hasPolicies = computed(() => policies.value.length > 0);
const hasFinancialProducts = computed(() => financialProducts.value.length > 0);

const loadRecommendedProducts = async () => {
  if (!isValidGoalId.value) {
    show('올바른 진로 목표 정보가 아닙니다.', 'error');
    return;
  }

  try {
    loading.value = true;

    const result = await jobApi.findServiceRecommend(goalId.value);

    policies.value = result?.policies ?? [];
    financialProducts.value = result?.financialProducts ?? [];
  } catch (error) {
    console.error('정책·금융상품 추천 조회 실패:', error);
    show('추천 정보를 불러오지 못했습니다.', 'error');
  } finally {
    loading.value = false;
  }
};

const getBadgeText = (product) => {
  return (product?.badgeCode ?? '').replace(' 가능', '');
};

const getBadgeVariant = (product) => {
  const badgeText = product?.badgeCode ?? '';

  if (badgeText.includes('복무 중')) {
    return 'yellow';
  }

  if (badgeText.includes('전역 후')) {
    return 'green';
  }

  if (badgeText.includes('편입 후')) {
    return 'gray';
  }

  return 'gray';
};

const openProductLink = (linkUrl) => {
  if (!linkUrl) {
    show('등록된 상세 페이지가 없습니다.', 'info');
    return;
  }

  window.open(linkUrl, '_blank', 'noopener,noreferrer');
};

const goPrevious = () => {
  router.back();
};

const handleSave = async () => {
  if (saving.value) {
    return;
  }

  /*
   * TODO: 목표 최종 저장 API가 확정되면 연결
   *
   * saving.value = true;
   *
   * try {
   *   await jobApi.confirmJobGoal(goalId.value);
   *   isCompleteModalOpen.value = true;
   * } catch (error) {
   *   show('진로 로드맵을 저장하지 못했습니다.', 'error');
   * } finally {
   *   saving.value = false;
   * }
   */

  // 임시 확인용
  isCompleteModalOpen.value = true;
};

const goJobGoalDetail = () => {
  router.push({
    name: 'JobGoalDetail',
    params: {
      goalId: goalId.value,
    },
  });
};

onMounted(() => {
  loadRecommendedProducts();
});
</script>

<template>
  <div class="job-products">
    <RoadmapCharacterSlider :progress="progress" label="진로 로드맵" />

    <h2 class="job-products__title text-title">금융상품 추천</h2>

    <div v-if="loading" class="job-products__loading job-products__body">
      추천 정보를 불러오는 중입니다.
    </div>

    <template v-else>
      <section class="job-products__section">
        <h3 class="job-products__section-title text-label">관련 정책</h3>

        <div v-if="hasPolicies" class="job-products__list">
          <BaseCard
            v-for="policy in policies"
            :key="`${policy.productType}-${policy.productId}`"
            padding="16px"
            class="product-card"
          >
            <div class="product-card__header">
              <div class="product-card__content">
                <BaseTag
                  v-if="policy.badgeCode"
                  class="product-card__tag"
                  :label="getBadgeText(policy)"
                  :variant="getBadgeVariant(policy)"
                />

                <strong class="product-card__name text-label">
                  {{ policy.productName }}
                </strong>

                <p class="product-card__description job-products__body">
                  {{ policy.productDesc }}
                </p>
              </div>

              <button
                v-if="policy.linkUrl"
                type="button"
                class="product-card__link"
                :aria-label="`${policy.productName} 상세 페이지 열기`"
                @click="openProductLink(policy.linkUrl)"
              >
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  class="external-link-icon"
                >
                  <path
                    d="M14 5h5v5M19 5l-8 8M19 13v5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5"
                  />
                </svg>
              </button>
            </div>
          </BaseCard>
        </div>

        <div v-else class="job-products__empty job-products__body">
          추천된 정책이 없습니다.
        </div>
      </section>

      <section class="job-products__section">
        <h3 class="job-products__section-title text-label">관련 금융상품</h3>

        <div v-if="hasFinancialProducts" class="job-products__list">
          <BaseCard
            v-for="product in financialProducts"
            :key="`${product.productType}-${product.productId}`"
            padding="16px"
            class="product-card"
          >
            <div class="product-card__header">
              <div class="product-card__content">
                <BaseTag
                  v-if="product.badgeCode"
                  class="product-card__tag"
                  :label="getBadgeText(product)"
                  :variant="getBadgeVariant(product)"
                />

                <strong class="product-card__name text-label">
                  {{ product.productName }}
                </strong>

                <p class="product-card__description job-products__body">
                  {{ product.productDesc }}
                </p>
              </div>

              <button
                v-if="product.linkUrl"
                type="button"
                class="product-card__link"
                :aria-label="`${product.productName} 상세 페이지 열기`"
                @click="openProductLink(product.linkUrl)"
              >
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  class="external-link-icon"
                >
                  <path
                    d="M14 5h5v5M19 5l-8 8M19 13v5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5"
                  />
                </svg>
              </button>
            </div>
          </BaseCard>
        </div>

        <div v-else class="job-products__empty">
          추천된 금융상품이 없습니다.
        </div>
      </section>

      <div class="job-products__guide">
        <div class="job-products__guide-content">
          <p class="job-products__guide-title text-label">
            지금까지 진로 로드맵이었습니다.
          </p>
          <p class="job-products__guide-description job-products__body">
            마음에 든다면 저장해주세요.
          </p>
        </div>

        <img
          :src="roadmapRabbit"
          alt="로드맵 토끼"
          class="job-products__guide-image"
        />
      </div>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="저장"
      :primary-disabled="loading || saving"
      @secondary-click="goPrevious"
      @primary-click="handleSave"
    />

    <BaseModal
      v-model="isCompleteModalOpen"
      title="알림"
      confirm-text="확인"
      @confirm="goJobGoalDetail"
    >
      <p class="job-products__modal-message">
        선택한 진로 목표가 저장되었습니다.
      </p>
    </BaseModal>
  </div>
</template>

<style scoped>
/*
 * TODO:
 * 공통 본문 Typography(text-body)가 추가되면
 * job-products__body를 제거하고 공통 클래스로 교체
 */
.job-products__body {
  font-family: 'Escoredream', sans-serif;
  font-size: 14px;
  font-weight: 400;
  color: var(--text-body);
  line-height: 1.5;
}

.job-products {
  padding: 0 20px 96px;
}

.job-products__title {
  margin: 28px 0 24px;
}

.job-products__loading,
.job-products__empty {
  padding: 32px 0;
  text-align: center;
}

.job-products__section + .job-products__section {
  margin-top: 16px;
}

.job-products__section-title {
  margin: 0 0 12px;
  color: var(--kb-dark-gray);
}

.job-products__list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.product-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.product-card__content {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  align-items: flex-start;
}

:deep(.product-card__tag.base-tag) {
  margin: 0 0 12px;
  padding: 2px 8px;
  font-size: 10px;
  line-height: 1.2;
}

.product-card__link {
  display: inline-flex;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--text-body);
  cursor: pointer;
}

.product-card__link:hover {
  background: var(--gray-pale-bg);
}

.external-link-icon {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.product-card__name {
  display: block;
  margin: 0;
  line-height: 1.4;
}

.product-card__description {
  display: -webkit-box;
  margin: 6px 0 0;
  overflow: hidden;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.5;
  word-break: keep-all;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.job-products__guide {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 32px;
}

.job-products__guide-content {
  flex: 1;
}

.job-products__guide-title {
  display: block;
  line-height: 1.4;
}

.job-products__guide-description {
  margin: 2px 0 0;
}

.job-products__guide-image {
  width: 54px;
  height: auto;
  flex-shrink: 0;
}

/*
 * TODO: 공통 본문 Typography(text-body)가 추가되면
 * 공통 클래스로 교체
 */
.job-products__modal-message {
  margin: 0;
  font-family: 'Escoredream', sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  color: var(--text-body);
}
</style>
